package uk.ac.ebi.arrayexpress.components;

/*
 * Copyright 2009-2013 European Molecular Biology Laboratory
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.LRUMap;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
import uk.ac.ebi.arrayexpress.utils.saxon.SaxonException;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.FormatFileSizeFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.HTMLDocumentFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.HTTPStatusException;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.HTTPStatusFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.NamespaceConstant;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.SerializeXMLFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.TrimTrailingDotFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.saxon.ParseHTMLFunction;

public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TransformerFactoryImpl trFactory;
    private XPathEvaluator xPathEvaluator;
    private Map<String, Templates> templatesCache = new Hashtable<String, Templates>();
    private Map<String, IDocumentSource> documentSources = new Hashtable<String, IDocumentSource>();
    private Map<String, XPathExpression> xPathExpMap = Collections.synchronizedMap(new LRUMap<String, XPathExpression>(100));

    private DocumentInfo appDocument;

    private static final String XML_STRING_ENCODING = "UTF-8";

    public SaxonEngine()
    {
    }

    @Override
    public void initialize() throws Exception
    {
        // This is so we make sure we use Saxon and not anything else
        trFactory = (TransformerFactoryImpl) TransformerFactoryImpl.newInstance();
        trFactory.setErrorListener(this);
        trFactory.setURIResolver(this);

        // TODO: study the impact of this change later
        //trFactory.getConfiguration().setTreeModel(Builder.TINY_TREE_CONDENSED);

        // create application document
        appDocument = buildDocument(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application name=\""
                        + getApplication().getName()
                        + "\"/>"
        );

        
        registerExtensionFunction(new ParseHTMLFunction());
        registerExtensionFunction(new SerializeXMLFunction());
        //registerExtensionFunction(new TabularDocumentFunction());
        //registerExtensionFunction(new GetMappedValueFunction());
        registerExtensionFunction(new FormatFileSizeFunction());
        registerExtensionFunction(new TrimTrailingDotFunction());
        registerExtensionFunction(new HTMLDocumentFunction());
        registerExtensionFunction(new HTTPStatusFunction());

        xPathEvaluator = new XPathEvaluator(trFactory.getConfiguration());
        IndependentContext namespaces = new IndependentContext(trFactory.getConfiguration());
        namespaces.declareNamespace("ae", NamespaceConstant.AE_EXT);
        xPathEvaluator.setNamespaceResolver(namespaces);
    }

    @Override
    public void terminate() throws Exception
    {
    }

    public void registerDocumentSource( IDocumentSource documentSource )
    {
        logger.debug("Registering source [{}]", documentSource.getDocumentURI());
        this.documentSources.put(documentSource.getDocumentURI(), documentSource);
    }

    public void unregisterDocumentSource( IDocumentSource documentSource )
    {
        logger.debug("Removing source [{}]", documentSource.getDocumentURI());
        this.documentSources.remove(documentSource.getDocumentURI());
    }

    public DocumentInfo getRegisteredDocument( String documentURI ) throws Exception
    {
        if (this.documentSources.containsKey(documentURI)) {
            return this.documentSources.get(documentURI).getDocument();
        } else {
            return null;
        }
    }

    // implements URIResolver.resolve
    @Override
    public Source resolve( String href, String base ) throws TransformerException
    {
        Source src;
        try {
            // try document sources first
            if (documentSources.containsKey(href)) {
                return documentSources.get(href).getDocument();
            } else {
                if (null != href && !href.startsWith("/")) {
                    href = "/WEB-INF/server-assets/stylesheets/" + href;
                }
                URL resource = Application.getInstance().getResource(href);
                if (null == resource) {
                    throw new TransformerException("Unable to locate resource [" + href + "]");
                }
                InputStream input = resource.openStream();
                if (null == input) {
                    throw new TransformerException("Unable to open stream for resource [" + resource.toString() + "]");
                }
                src = new StreamSource(input);
            }
        } catch (TransformerException x) {
            throw x;
        } catch (Exception x) {
            logger.error("Caught an exception:", x);
            throw new TransformerException(x.getMessage());
        }

        return src;
    }

    // implements ErrorListener.error
    @Override
    public void error( TransformerException x ) throws TransformerException
    {
        logger.error("Caught XSLT transformation error:", x);
        getApplication().sendExceptionReport("[PROBLEM] XSLT transformation error occurred", x);
        throw x;
    }

    // implements ErrorListener.fatalError
    @Override
    public void fatalError( TransformerException x ) throws TransformerException
    {
        if (!(x instanceof HTTPStatusException)) {
            logger.error("Caught XSLT fatal transformation error:", x);
            getApplication().sendExceptionReport("[SEVERE] XSLT fatal transformation error occurred", x);
        }
        throw x;
    }

    // implements ErrorListener.warning
    @Override
    public void warning( TransformerException x )
    {
        logger.warn(x.getLocalizedMessage());
    }

    public DocumentInfo getAppDocument()
    {
        return appDocument;
    }

    public void registerExtensionFunction( ExtensionFunctionDefinition f )
    {
        trFactory.getConfiguration().registerExtensionFunction(f);
    }

    public String serializeDocument(DocumentInfo document) throws Exception {
		Transformer transformer = trFactory.newTransformer();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
//		transformer.setOutputProperty(SaxonOutputKeys.CHARACTER_REPRESENTATION,
//				"entity;decimal");

		transformer.transform(document, new StreamResult(outStream));
		return outStream.toString(XML_STRING_ENCODING);
	}

    
    
    
    public DocumentInfo buildDocument( String xml ) throws XPathException
    {
        StringReader reader = new StringReader(xml);
        Configuration config = trFactory.getConfiguration();
        return config.buildDocument(new StreamSource(reader));
    }

    public DocumentInfo buildDocument( InputStream stream ) throws XPathException
    {
        Configuration config = trFactory.getConfiguration();
        return config.buildDocument(new StreamSource(stream));
    }


    private XPathExpression getXPathExpression( String xpath ) throws XPathException
    {
        if (xPathExpMap.containsKey(xpath)) {
            return xPathExpMap.get(xpath);
        } else {
            XPathExpression xpe = xPathEvaluator.createExpression(xpath);
            xPathExpMap.put(xpath, xpe);
            return xpe;
        }
    }

    public List<Object> evaluateXPath( NodeInfo node, String xpath ) throws XPathException
    {
        XPathExpression xpe = getXPathExpression(xpath);

        return xpe.evaluate(node);
    }

    public Object evaluateXPathSingle( NodeInfo node, String xpath ) throws XPathException
    {
        XPathExpression xpe = getXPathExpression(xpath);

        return xpe.evaluateSingle(node);
    }

    public String evaluateXPathSingleAsString(NodeInfo node, String xpath) throws XPathException
    {
        Object e = evaluateXPathSingle(node, xpath);
        if (null == e) {
            return null;
        } else if (e instanceof Item) {
            return ((Item)e).getStringValue();
        } else if (e instanceof String ) {
            return (String)e;
        } else {
            throw new XPathException("Conversion from [" + e.getClass() + "] to String was not implemented");
        }
    }

    public boolean transformToWriter( Source srcDocument, String stylesheet, Map<String, String[]> params, Writer dstWriter ) throws SaxonException
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstWriter));
    }

    @SuppressWarnings("unused")
    public boolean transformToFile( DocumentInfo srcDocument, String stylesheet, Map<String, String[]> params, File dstFile ) throws SaxonException
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstFile));
    }

    public String transformToString(URL src, String stylesheet,
			Map<String, String[]> params) throws Exception {
		String str;
		InputStream inStream = null;
		ByteArrayOutputStream outStream = null;
		try {
			inStream = src.openStream();
			outStream = new ByteArrayOutputStream();
			if (transform(new StreamSource(inStream), stylesheet, params,
					new StreamResult(outStream))) {
				str = outStream.toString(XML_STRING_ENCODING);
				outStream.close();
				return str;
			} else {
				return null;
			}
		} finally {
			if (null != inStream)
				inStream.close();
			if (null != outStream)
				outStream.close();
		}
	}

	public String transformToString(DocumentInfo srcDocument,
			String stylesheet, Map<String, String[]> params) throws Exception {
		String str;
		ByteArrayOutputStream outStream = null;
		try {
			outStream = new ByteArrayOutputStream();

			if (transform(srcDocument, stylesheet, params, new StreamResult(
					outStream))) {
				str = outStream.toString(XML_STRING_ENCODING);
				return str;
			} else {
				return null;
			}
		} finally {
			if (null != outStream)
				outStream.close();
		}
	}

    public DocumentInfo transform( String srcXmlString, String stylesheet, Map<String, String[]> params ) throws SaxonException
    {
        Source src = new StreamSource(new StringReader(srcXmlString));
        TinyBuilder dstDocument = new TinyBuilder(trFactory.getConfiguration().makePipelineConfiguration());
        if (transform(src, stylesheet, params, dstDocument)) {
            return (DocumentInfo) dstDocument.getCurrentRoot();
        }
        return null;
    }

    public DocumentInfo transform( Source source, String stylesheet, Map<String, String[]> params ) throws SaxonException
    {
        TinyBuilder dstDocument = new TinyBuilder(trFactory.getConfiguration().makePipelineConfiguration());
        if (transform(source, stylesheet, params, dstDocument)) {
            return (DocumentInfo) dstDocument.getCurrentRoot();
        }
        return null;
    }

    private boolean transform( Source src, String stylesheet, Map<String, String[]> params, Result dst ) throws SaxonException
    {
        boolean result = false;
        try {
            Templates templates;
            if (!templatesCache.containsKey(stylesheet)) {
                logger.debug("Caching prepared stylesheet [{}]", stylesheet);
                // Open the stylesheet
                Source xslSource = resolve(stylesheet, null);
                if (xslSource == null) {
                	throw new RuntimeException("Unable to resolve stylesheet "+stylesheet);
                }
                
                if (trFactory == null) {
                	
                	throw new RuntimeException("SaxonEngine not initialized?");
                }

                templates = trFactory.newTemplates(xslSource);
                templatesCache.put(stylesheet, templates);
            } else {
                logger.debug("Getting prepared stylesheet [{}] from cache", stylesheet);
                templates = templatesCache.get(stylesheet);
            }
            Transformer xslt = templates.newTransformer();

            // redirect all messages to logger
            ((Controller) xslt).setMessageEmitter(new LoggerWriter(logger));

            // assign the parameters (if not null)
            if (null != params) {
                for (Map.Entry<String, String[]> param : params.entrySet()) {
                    xslt.setParameter(param.getKey(), StringTools.arrayToString(param.getValue(), " "));
                }
            }

            // Perform the transformation, sending the output to the response.
            logger.debug("Performing transformation, stylesheet [{}]", stylesheet);
            xslt.transform(src, dst);
            logger.debug("Transformation completed");

            result = true;
        } catch (TerminationException x) {
            logger.error("Transformation has been terminated by XSL instruction, please inspect log for details");
        } catch (TransformerException x) {
            throw new SaxonException(x);
        }
        return result;
    }

    class LoggerWriter extends SequenceWriter
    {
        private Logger logger;

        protected LoggerWriter( Logger logger )
        {
            super(null);
            this.logger = logger;
        }

        public void write( Item item )
        {
            logger.debug("[xsl:message] {}", item.getStringValue());
        }
    }
}
