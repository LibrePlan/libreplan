package org.navalplanner.web.print;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;
import org.zkoss.zk.ui.Executions;

public class CutyPrint {

    private static final Log LOG = LogFactory.getLog(CutyPrint.class);

    private static final String CUTYCAPT_COMMAND = "/usr/bin/CutyCapt ";

    // Fixed taskdetails width padding left
    private static int TASKDETAILS_WIDTH = 310;

    public static void print(Order order) {
        print("/planner/index.zul", entryPointForShowingOrder(order),
                Collections.<String, String> emptyMap());
    }

    public static void print(Order order, Map<String, String> parameters) {
        print("/planner/index.zul", entryPointForShowingOrder(order),
                parameters);
    }

    public static void print(Order order, HashMap<String, String> parameters,
            Planner planner) {
        print("/planner/index.zul", entryPointForShowingOrder(order),
                parameters, planner);
    }

    public static void print() {
        print("/planner/index.zul", Collections.<String, String> emptyMap(),
                Collections.<String, String> emptyMap());
    }

    public static void print(Map<String, String> parameters) {
        print("/planner/index.zul", Collections.<String, String> emptyMap(),
                parameters);
    }

    public static void print(HashMap<String, String> parameters, Planner planner) {
        print("/planner/index.zul", Collections.<String, String> emptyMap(),
                parameters, planner);
    }

    private static Map<String, String> entryPointForShowingOrder(Order order) {
        final Map<String, String> result = new HashMap<String, String>();
        result.put("order", order.getId() + "");
        return result;
    }

    public static void print(final String forwardURL,
            final Map<String, String> entryPointsMap,
            Map<String, String> parameters) {
        print(forwardURL, entryPointsMap, parameters, null);
    }

    public static void print(final String forwardURL,
            final Map<String, String> entryPointsMap,
            Map<String, String> parameters, Planner planner) {

        HttpServletRequest request = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();

        String extension = ".pdf";
        if (((parameters.get("extension") != null) && !(parameters
                .get("extension").equals("")))) {
            extension = parameters.get("extension");
        }

        // Calculate application path and destination file relative route
        String absolutePath = request.getSession().getServletContext()
                .getRealPath("/");

        String filename = "/print/"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + extension;

        int plannerWidth = 0;
        if ((planner != null) && (planner.getTimeTracker() != null)) {
            plannerWidth = planner.getTimeTracker().getHorizontalSize()
                    + TASKDETAILS_WIDTH;
        }

        // Generate capture string
        String captureString = CUTYCAPT_COMMAND;

        String url = CallbackServlet.registerAndCreateURLFor(request,
                new IServletRequestHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response)
                            throws ServletException, IOException {

                        URLHandler.setupEntryPointsForThisRequest(request,
                                entryPointsMap);
                        // Pending to forward and process additional parameters
                        // as show labels, resources, zoom or expand all
                        request.getRequestDispatcher(forwardURL).forward(
                                request, response);
                    }
                });

        // Add capture destination callback URL
        captureString += " --url=http://" + request.getLocalName() + ":"
                + request.getLocalPort() + url;

        if (parameters != null) {
            captureString += "?";
            for (String key : parameters.keySet()) {
                captureString += key + "=" + parameters.get(key) + "&";
            }
            captureString = captureString.substring(0,
                    (captureString.length() - 1));
        }

        captureString += " --min-width=" + plannerWidth;

        // Static width and time delay parameters (FIX)
        captureString += " --delay=1000 ";

        String generatedCSSFile = createCSSFile(absolutePath
                + "/planner/css/printlabels.css", plannerWidth);

        // Relative user styles
        captureString += "--user-styles=" + generatedCSSFile;

        // Destination complete absolute path
        captureString += " --out=" + absolutePath + filename;

        try {

            // CutyCapt command execution
            LOG.debug(captureString);

            Process print;
            Process server = null;

            // Ensure cleanup of unfinished CutyCapt processes and CSS
            Runtime.getRuntime().exec("killall CutyCapt");

            // If there is a not real X server environment then use Xvfb
            if ((System.getenv("DISPLAY") == null)
                    || (System.getenv("DISPLAY").equals(""))) {
                String[] serverEnvironment = { "PATH=$PATH" };
                server = Runtime.getRuntime().exec("env - Xvfb :99",
                        serverEnvironment);
                String[] environment = { "DISPLAY=:99.0" };
                print = Runtime.getRuntime().exec(captureString, environment);
            } else {
                print = Runtime.getRuntime().exec(captureString);
            }
            try {
                print.waitFor();
                print.destroy();
                if ((System.getenv("DISPLAY") == null)
                        || (System.getenv("DISPLAY").equals(""))) {
                    server.destroy();
                }
                Executions.getCurrent().sendRedirect(filename, "_blank");
            } catch (Exception e) {
                LOG.error(_("Could open generated PDF"), e);
            }

        } catch (IOException e) {
            LOG.error(_("Could not execute print command"), e);
        }
    }

    private static String createCSSFile(String srFile, int width) {
        File generatedCSS = null;
        try {
            generatedCSS = File.createTempFile("print", ".css");

            File f1 = new File(srFile);
            InputStream in = new FileInputStream(f1);

            // For Overwrite the file.
            OutputStream out = new FileOutputStream(generatedCSS);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            String body = "body { width: " + width + "px; } \n";
            out.write(body.getBytes());
            in.close();
            out.close();
            LOG.debug(_("Generated CSS:") + generatedCSS.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage() + _(" in the specified directory."));
            System.exit(0);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        if (generatedCSS != null) {
            return generatedCSS.getAbsolutePath();
        } else
            return srFile;
    }
}
