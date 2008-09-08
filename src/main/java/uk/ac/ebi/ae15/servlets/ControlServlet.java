package uk.ac.ebi.ae15.servlets;

import uk.ac.ebi.ae15.app.ApplicationServlet;
import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.components.JobsController;
import uk.ac.ebi.ae15.components.Users;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlServlet extends ApplicationServlet
{
    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(request);

        String command = "";
        String params = "";

        Pattern p = Pattern.compile("servlets/control/([^/]+)/?(.*)");
        Matcher m = p.matcher(request.getRequestURL());
        if (m.find()) {
            command = m.group(1);
            if (0 < m.group(2).length())
                params = m.group(2);
        }

        if (command.equals("reload-xml")) {
            if (0 < params.length()) {
                ((Experiments) getComponent("Experiments")).setDataSource(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob("reload-xml");
        } else if (command.equals("rescan-files")) {
            if (0 < params.length()) {
                ((DownloadableFilesRegistry) getComponent("DownloadableFilesRegistry")).setRootFolder(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob("rescan-files");
        } else if (command.equals("verify-login")) {
            response.setContentType("text/plain; charset=ISO-8859-1");
            // Disable cache no matter what (or we're fucked on IE side)
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "must-revalidate");
            response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

            // Output goes to the response PrintWriter.
            PrintWriter out = response.getWriter();
            out.print(((Users) getComponent("Users")).hashLogin(request.getParameter("u"), request.getParameter("p")));
            out.close();
        }
    }
}
