/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.print;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Executions;

public class CutyPrint {

    private static final Log LOG = LogFactory.getLog(CutyPrint.class);

    private static final String WKHTMLTOPDF_COMMAND = "wkhtmltopdf";

    private static final String INDEX_ZUL = "/planner/index.zul";

    private static final String PX_IMPORTANT = "px !important; } \n";

    /** Estimated maximum JS execution time (ms) */
    private static final int CAPTURE_DELAY = 10000;

    /**
     * Default width in pixels of the task name text field for depth level 1.
     * Got from .listdetails .depth_1 input.task_title { width: 121px; } at src/main/webapp/planner/css/ganttzk.css
     */
    private static final int BASE_TASK_NAME_PIXELS = 121;

    private static int TASK_HEIGHT = 25;

    private static class WkhtmltopdfParameters {

        private static final AtomicLong counter = new AtomicLong();

        private final HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();

        private final ServletContext context = request.getSession().getServletContext();

        private final String forwardURL;

        private final Map<String, String> entryPointsMap;

        private final Map<String, String> printParameters;

        private final Planner planner;

        private final boolean containersExpandedByDefault;

        private final int minWidthForTaskNameColumn;

        private final String generatedSnapshotServerPath;

        private final int recentUniqueToken = (int) (counter.getAndIncrement() % 1000);

        public WkhtmltopdfParameters(final String forwardURL,
                                     final Map<String, String> entryPointsMap,
                                     Map<String, String> printParameters,
                                     Planner planner) {

            this.forwardURL = forwardURL;
            this.entryPointsMap = (entryPointsMap != null) ? entryPointsMap : Collections.emptyMap();
            this.printParameters = (printParameters != null) ? printParameters : Collections.emptyMap();
            this.planner = planner;

            containersExpandedByDefault = Planner.guessContainersExpandedByDefaultGivenPrintParameters(printParameters);
            minWidthForTaskNameColumn = planner.calculateMinimumWidthForTaskNameColumn(containersExpandedByDefault);
            generatedSnapshotServerPath = buildCaptureDestination();
        }

        String getGeneratedSnapshotServerPath() {
            return generatedSnapshotServerPath;
        }

        private String buildCaptureDestination() {
            return String.format("/print/%tY%<tm%<td%<tH%<tM%<tS-%s.pdf", new Date(), recentUniqueToken);
        }

        /**
         * Builds the full wkhtmltopdf command as a list of arguments.
         * Syntax: wkhtmltopdf [options] <url> <output-file>
         */
        List<String> buildCommand() {
            List<String> cmd = new ArrayList<>();
            cmd.add(WKHTMLTOPDF_COMMAND);

            // Page dimensions derived from planner state
            int widthPx = buildMinWidthParam();
            int heightPx = buildMinHeightParam();
            cmd.add("--page-width");
            cmd.add(widthPx + "px");
            cmd.add("--page-height");
            cmd.add(heightPx + "px");

            cmd.add("--javascript-delay");
            cmd.add(Integer.toString(CAPTURE_DELAY));

            String cssPath = buildCustomCSSParam(widthPx);
            cmd.add("--user-style-sheet");
            cmd.add(cssPath);

            cmd.add("--custom-header");
            cmd.add("Accept-Language");
            cmd.add(Locales.getCurrent().getLanguage());

            // Disable smart shrinking so pixel dimensions are respected
            cmd.add("--disable-smart-shrinking");

            // Allow scripts and local file access needed for ZK rendering
            cmd.add("--no-stop-slow-scripts");
            cmd.add("--enable-local-file-access");

            // Positional args: URL then output file
            cmd.add(buildSnapshotURLParam());
            cmd.add(buildPathToOutputFileParam());

            return cmd;
        }

        private String buildSnapshotURLParam() {
            IServletRequestHandler snapshotRequestHandler = executeOnOriginalContext(new IServletRequestHandler() {
                @Override
                public void handle(
                        HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

                    EntryPointsHandler.setupEntryPointsForThisRequest(request, entryPointsMap);
                    request.getRequestDispatcher(forwardURL).forward(request, response);
                }
            });

            String pageToSnapshot = CallbackServlet.registerAndCreateURLFor(request, snapshotRequestHandler);
            return createCaptureURL(pageToSnapshot);
        }

        private String createCaptureURL(String capturePath) {
            String hostName = resolveLocalHost();
            String uri = String.format("%s://%s:%s", request.getScheme(), hostName, request.getLocalPort());
            UriBuilder result = UriBuilder.fromUri(uri).path(capturePath);

            for (Entry<String, String> entry : printParameters.entrySet()) {
                result = result.queryParam(entry.getKey(), entry.getValue());
            }

            return result.build().toASCIIString();
        }

        private String resolveLocalHost() {
            try {
                return InetAddress.getByName(request.getLocalName()).getHostName();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        private int buildMinWidthParam() {
            return planner != null && planner.getTimeTracker() != null
                    ? planner.getTimeTracker().getHorizontalSize() + calculateTaskDetailsWidth()
                    : 0;
        }

        private int calculateTaskDetailsWidth() {
            int TASKDETAILS_BASE_WIDTH = 310;
            return TASKDETAILS_BASE_WIDTH + Math.max(0, minWidthForTaskNameColumn - BASE_TASK_NAME_PIXELS);
        }

        private int buildMinHeightParam() {
            int PRINT_VERTICAL_SPACING = 160;
            return (containersExpandedByDefault
                    ? planner.getAllTasksNumber()
                    : planner.getTaskNumber())
                    * TASK_HEIGHT + PRINT_VERTICAL_SPACING;
        }

        private String buildCustomCSSParam(int plannerWidth) {
            String absolutePath = context.getRealPath("/");
            return createCSSFile(absolutePath + "/planner/css/print.css", cssLinesToAppend(plannerWidth));
        }

        private static String createCSSFile(String sourceFile, String cssLinesToAppend) {
            File destination;
            try {
                destination = File.createTempFile("print", ".css");
                FileUtils.copyFile(new File(sourceFile), destination);
            } catch (IOException e) {
                LOG.error("Can't create a temporal file for storing the CSS files", e);
                return sourceFile;
            }

            FileWriter appendToFile = null;
            try {
                appendToFile = new FileWriter(destination, true);
                appendToFile.write(cssLinesToAppend);
                appendToFile.flush();
            } catch (IOException e) {
                LOG.error("Can't append to the created file " + destination, e);
            } finally {
                try {
                    if ( appendToFile != null ) {
                        appendToFile.close();
                    }
                } catch (IOException e) {
                    LOG.warn("error closing fileWriter", e);
                }
            }

            return destination.getAbsolutePath();
        }

        private String cssLinesToAppend(int width) {
            String includeCSSLines = " body { width: " + width + "px; } \n";
            if ( "all".equals(printParameters.get("labels")) ) {
                includeCSSLines += " .task-labels { display: inline !important;} \n ";
            }

            if ( "all".equals(printParameters.get("resources")) ) {
                includeCSSLines += " .task-resources { display: inline !important;} \n";
            }

            includeCSSLines += heightCSS();
            includeCSSLines += widthForTaskNamesColumnCSS();

            return includeCSSLines;
        }

        private String heightCSS() {
            int tasksNumber = containersExpandedByDefault ? planner.getAllTasksNumber() : planner.getTaskNumber();
            int PRINT_VERTICAL_PADDING = 50;
            int height = (tasksNumber * TASK_HEIGHT) + PRINT_VERTICAL_PADDING;
            String heightCSS = "";
            heightCSS += " body div#scroll_container { height: " + height + PX_IMPORTANT;
            heightCSS += " body div#timetracker { height: " + (height + 20) + PX_IMPORTANT;
            heightCSS += " body div.plannerlayout { height: " + (height + 80) + PX_IMPORTANT;
            heightCSS += " body div.main-layout { height: " + (height + 90) + PX_IMPORTANT;

            return heightCSS;
        }

        private String widthForTaskNamesColumnCSS() {
            String css = "/* ------ Make the area for task names wider ------ */\n";
            css += "th.z-tree-col {width: 76px !important;}\n";
            css += "th.tree-text {width: " + (34 + minWidthForTaskNameColumn) + "px !important;}\n";
            css += ".taskdetailsContainer, .z-west-body, .z-tree-header, .z-tree-body {";
            css += "width: " + (176 + minWidthForTaskNameColumn) + "px !important;}\n";

            return css;
        }

        private String buildPathToOutputFileParam() {
            return context.getRealPath(generatedSnapshotServerPath);
        }

        private static IServletRequestHandler executeOnOriginalContext(final IServletRequestHandler original) {
            final SecurityContext originalContext = SecurityContextHolder.getContext();
            final Locale current = Locales.getCurrent();

            return new IServletRequestHandler() {
                @Override
                public void handle(
                        HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

                    Locales.setThreadLocal(current);
                    SecurityContextHolder.setContext(originalContext);
                    original.handle(request, response);
                }
            };
        }
    }

    public static void print(Order order) {
        print(INDEX_ZUL, entryPointForShowingOrder(order), Collections.emptyMap());
    }

    public static void print(Order order, Map<String, String> parameters) {
        print(INDEX_ZUL, entryPointForShowingOrder(order), parameters);
    }

    public static void print(Order order, Map<String, String> parameters, Planner planner) {
        print(INDEX_ZUL, entryPointForShowingOrder(order), parameters, planner);
    }

    public static void print() {
        print(INDEX_ZUL, Collections.emptyMap(), Collections.emptyMap());
    }

    public static void print(Map<String, String> parameters) {
        print(INDEX_ZUL, Collections.emptyMap(), parameters);
    }

    public static void print(Map<String, String> parameters, Planner planner) {
        print(INDEX_ZUL, Collections.emptyMap(), parameters, planner);
    }

    private static Map<String, String> entryPointForShowingOrder(Order order) {
        final Map<String, String> result = new HashMap<>();
        result.put("order", order.getCode() + "");
        return result;
    }

    public static void print(final String forwardURL, final Map<String, String> entryPointsMap, Map<String, String> parameters) {
        print(forwardURL, entryPointsMap, parameters, null);
    }

    public static void print(final String forwardURL,
                             final Map<String, String> entryPointsMap,
                             Map<String, String> parameters,
                             Planner planner) {

        WkhtmltopdfParameters params = new WkhtmltopdfParameters(forwardURL, entryPointsMap, parameters, planner);
        String generatedSnapshotServerPath = takeSnapshot(params);

        openInAnotherTab(generatedSnapshotServerPath);
    }

    private static void openInAnotherTab(String producedPrintFilePath) {
        Executions.getCurrent().sendRedirect(producedPrintFilePath, "_blank");
    }

    /**
     * Blocks until the PDF is ready.
     * Invokes wkhtmltopdf to render the planner page and save a PDF to the print directory.
     *
     * @return the web-application-relative path to the generated PDF.
     */
    private static String takeSnapshot(WkhtmltopdfParameters params) {
        List<String> command = params.buildCommand();
        String generatedSnapshotServerPath = params.getGeneratedSnapshotServerPath();

        ProcessBuilder capture = new ProcessBuilder(command);
        capture.redirectErrorStream(true);

        Process printProcess = null;
        try {
            LOG.info("calling printing: " + capture.command());
            printProcess = capture.start();
            printProcess.waitFor();

            return generatedSnapshotServerPath;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOG.error("error invoking wkhtmltopdf command", e);
            throw new RuntimeException(e);
        } finally {
            if ( printProcess != null ) {
                destroy(printProcess);
            }
        }
    }

    private static void destroy(Process process) {
        try {
            process.destroy();
        } catch (Exception e) {
            LOG.error("error stopping process " + process, e);
        }
    }

}
