package uk.ac.ebi.arrayexpress.servlets;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.ResourceSet;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;
import uk.ac.ebi.arrayexpress.utils.StringTools;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

//rpe: for now I do not need to have an authentication system
public class QueryServlet extends AuthAwareApplicationServlet
{
    private static final long serialVersionUID = 6806580383145704364L;

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    
    private int pageSizeLimit;

 
    @Override
    public void init(ServletConfig config) throws ServletException
    {
               super.init(config);
               pageSizeLimit=Application
       				.getInstance().getPreferences().getInteger("bs.pageSizeLimit");
            
    }

    
    protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
    {
        return (requestType == RequestType.GET || requestType == RequestType.POST);
    }
    
    
    protected void doAuthenticatedRequest(
            HttpServletRequest request
            , HttpServletResponse response
            , RequestType requestType
            , String authUserName
    ) throws ServletException, IOException
    {
    		
    	RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper("/([^/]+)/([^/]+)/([^/]+)$", "i");

        logRequest(logger, request, requestType);

        String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request.getRequestURL().toString());

        if (null == requestArgs || requestArgs.length != 3
                || "".equals(requestArgs[0]) || "".equals(requestArgs[1]) || "".equals(requestArgs[2])) {
            throw new ServletException("Bad arguments passed via request URL [" + request.getRequestURL().toString() + "]");
        }

        if (request.getParameter("pagesize")!=null && Integer.parseInt(request.getParameter("pagesize"))>pageSizeLimit){
        	
        	throw new ServletException("Bad arguments passed via request URL [" + request.getRequestURL().toString() + "]! There's a limit to the page size that cannot be exceeded!!");
        	
        	
        }
        	
        	
        String index = requestArgs[0];
        String stylesheet = requestArgs[1];
        String outputType = requestArgs[2];

    	long time = System.nanoTime();
		
        if (outputType.equals("xls")) {
            // special case for Excel docs
            // we actually send tab-delimited file but mimick it as XLS doc
            String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
            response.setContentType("application/vnd.ms-excel; charset=ISO-8859-1");
            response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".xls\"");
            outputType = "tab";
        } else if (outputType.equals("tab")) {
            // special case for tab-delimited files
            // we send tab-delimited file as an attachment
            String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
            response.setContentType("text/plain; charset=ISO-8859-1");
            response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".txt\"");
            outputType = "tab";
        } else if (outputType.equals("json")) {
            response.setContentType("application/json; charset=UTF-8");
        } else if (outputType.equals("html")) {
            response.setContentType("text/html; charset=UTF-8");
        } else {
            response.setContentType("text/" + outputType + "; charset=UTF-8");
        }

        // tell client to not cache the page unless we want to
        if (!"true".equalsIgnoreCase(request.getParameter("cache"))) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "must-revalidate");
            response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past
        }

        // flushing buffer to output headers; should only be used for looooong operations to mitigate proxy timeouts
        if (null != request.getParameter("flusheaders")) {
            response.flushBuffer();
        }

        // Output goes to the response PrintWriter.
        PrintWriter out = response.getWriter();
        
        //TODO: [PT:45669791] I must remove this afterwards. I need to have this here because I can't put thi on the XSL stylesheet (problems with enconding)
//        if (outputType.equals("html")) {
//            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 4.01 Transitional//EN\">");
//        }
        
        
        try {
            String stylesheetName = new StringBuilder(stylesheet)
                    .append('-').append(outputType).append(".xsl").toString();

            HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);

            // adding "host" request header so we can dynamically create FQDN URLs
            params.put("host", request.getHeader("host"));
            params.put("basepath", request.getContextPath());

            // to make sure nobody sneaks in the other value w/o proper authentication
           // params.put("userid", StringTools.listToString(authUserIDs, " OR "));

            // setting "preferred" parameter to true allows only preferred experiments to be displayed, but if
            // any of source control parameters are present in the query, it will not be added
            String[] keywords = params.get("keywords");

            if (!(params.containsKey("migrated")
                || params.containsKey("source")
                || params.containsKey("visible")
                || ( null != keywords && keywords[0].matches(".*\\bmigrated:.*"))
                || ( null != keywords && keywords[0].matches(".*\\bsource:.*"))
                || ( null != keywords && keywords[0].matches(".*\\bvisible:.*"))
                )) {

                params.put("visible", "true");
            }

            
            //TODO: rpe
            //EFO expansion (this is now done in a different way by Nikolay
            params.put("expandefo", "true");
 
            // to make sure nobody sneaks in the other value w/o proper authentication
            //remove to add security stuff
            //params.put("userid", StringTools.listToString(getUserIds(authUserName), " OR "));
            params.put("username", authUserName);

            
            try {
                SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
                SaxonEngine saxonEngine = (SaxonEngine) getComponent("SaxonEngine");
                DocumentInfo source = saxonEngine.getAppDocument();
                if (search.getController().hasIndexDefined(index)) { // only do query if index id is defined
//                    source = saxonEngine.getRegisteredDocument(index + ".xml");
                    Integer queryId = search.getController().addQuery(index, params, request.getQueryString());
                    params.put("queryid", String.valueOf(queryId));
                    params.put("querystring", search.getController().getQueryString(queryId));
                    
                    
                    //all the queries are now executes in this Servlet and not in the XSLT
                    String xml = search.getController().queryPaged(queryId,params);
                    //xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xml;
    				StringReader reader = new StringReader(xml);
    				long xmlRead = System.currentTimeMillis();
    				
//    				System.out.println("xml->" + xml);
    				
                    //System.out.println("xml2->" + saxonEngine.transformToString(source, stylesheetName, params));

    				//    				System.out.println("xml size->" + xml.length());
    				Configuration config = ((SaxonEngine) Application
    						.getAppComponent("SaxonEngine")).trFactory
    						.getConfiguration();
    				source = config.buildDocument(new StreamSource(
    						reader));
    				
    			
                   // System.out.println("fdfdfd->" +saxonEngine.transformToString(saxonEngine.transform(xml, stylesheetName, params), stylesheetName, params));
                }
                //saxonEngine.transformToFile(source, stylesheetName, params, new File("/Users/rpslpereira/Desktop/testeEnc.xml"));
                logger.info("Transformation initial");
                if (!saxonEngine.transformToWriter(
                        source
                        , stylesheetName
                        , params
                        , out
                    )) {                     // where to dump resulting text
                    throw new Exception("Transformation returned an error");
                }
            } catch (ParseException x) {
                logger.error("Caught lucene parse exception:", x);
                reportQueryError(out, "query-syntax-error.txt", request.getParameter("keywords"));
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
        double ms = (System.nanoTime() - time) / 1000000d;
        out.close();
    }

    private void reportQueryError( PrintWriter out, String templateName, String query )
    {
        try {
            URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/templates/" + templateName);
            String template = StringTools.streamToString(resource.openStream(), "ISO-8859-1");
            Map<String, String> params = new HashMap<String, String>();
            params.put("variable.query", query);
            StrSubstitutor sub = new StrSubstitutor(params);
            out.print(sub.replace(template));
        } catch (Exception x) {
            logger.error("Caught an exception:", x);
        }
    }

	
}