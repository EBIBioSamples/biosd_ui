package uk.ac.ebi.arrayexpress.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LookupServlet extends ApplicationServlet
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
    {
        return (requestType == RequestType.GET || requestType == RequestType.POST);
    }

    // Respond to HTTP requests from browsers.
    protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType ) throws ServletException, IOException
    {
        logRequest(logger, request, requestType);

        String[] requestArgs = new RegExpHelper("servlets/lookup/([^/]+)", "i")
                .match(request.getRequestURL().toString());

        String type = "";
        String query = "";

        if (null != request.getParameter("q")) {
            query = request.getParameter("q");
        }

        if (null != requestArgs) {
            if (!requestArgs[0].equals("")) {
                type = requestArgs[0];
            }
        }

        // Set content type for HTML/XML/plain
        response.setContentType("text/plain; charset=ISO-8859-1");

        // Disable cache no matter what (or we're fucked on IE side)
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

        // Output goes to the response PrintWriter.
        PrintWriter out = response.getWriter();
        Experiments experiments = (Experiments)getComponent("Experiments");
        if (type.equals("arrays")) {
            out.print(experiments.getArrays());
        } else if (type.equals("species")) {
            out.print(experiments.getSpecies());
        } else if (type.equals("expdesign")) {
            out.print(experiments.getAssaysByMolecule(query));
        } else if (type.equals("exptech")) {
            out.print(experiments.getAssaysByInstrument(query));
        }
    }
}
