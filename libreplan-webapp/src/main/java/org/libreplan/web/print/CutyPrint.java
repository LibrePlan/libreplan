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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

    private static final String CUTYCAPT_COMMAND = "cutycapt";

    private static final String INDEX_ZUL = "/planner/index.zul";

    private static final String PX_IMPORTANT = "px !important; } \n";

    /**  Estimated maximum execution time (ms) */
    private static final int CAPTURE_DELAY = 10000;

    /**
     * Default width in pixels of the task name text field for depth level 1.
     * Got from .listdetails .depth_1 input.task_title { width: 121px; } at src/main/webapp/planner/css/ganttzk.css
     */
    private static final int BASE_TASK_NAME_PIXELS = 121;

    private static int TASK_HEIGHT = 25;

    private static class CutyCaptParameters {

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

        public CutyCaptParameters(final String forwardURL,
                                  final Map<String, String> entryPointsMap,
                                  Map<String, String> printParameters,
                                  Planner planner) {

            this.forwardURL = forwardURL;
            this.entryPointsMap = (entryPointsMap != null) ? entryPointsMap : Collections.emptyMap();

            this.printParameters = (printParameters != null) ? printParameters : Collections.emptyMap();

            this.planner = planner;

            containersExpandedByDefault = Planner.guessContainersExpandedByDefaultGivenPrintParameters(printParameters);
            minWidthForTaskNameColumn = planner.calculateMinimumWidthForTaskNameColumn(containersExpandedByDefault);
            generatedSnapshotServerPath = buildCaptureDestination(printParameters.get("extension"));
        }

        String getGeneratedSnapshotServerPath() {
            return generatedSnapshotServerPath;
        }

        private String buildCaptureDestination(String extension) {
            String newExtension = extension;

            if ( StringUtils.isEmpty(newExtension) ) {
                newExtension = ".pdf";
            }

            return String.format("/print/%tY%<tm%<td%<tH%<tM%<tS-%s%s", new Date(), recentUniqueToken, newExtension);
        }

        /**
         * An unique recent display number for Xvfb.
         * It's not truly unique across all the life of a LibrePlan application, but it's in the last period of time.
         *
         * @return the display number to use by Xvfb
         */
        public int getXvfbDisplayNumber() {
            // avoid display 0
            return recentUniqueToken + 1;
        }

        void fillParameters(ProcessBuilder c) {
            Map<String, String> parameters = buildParameters();
            for (Entry<String, String> each : parameters.entrySet()) {
                c.command().add(String.format("--%s=%s", each.getKey(), each.getValue()));
            }
        }

        private Map<String, String> buildParameters() {
            Map<String, String> result = new HashMap<>();

            result.put("url", buildSnapshotURLParam());

            int width = buildMinWidthParam();
            result.put("min-width", Integer.toString(width));

            result.put("min-height", Integer.toString(buildMinHeightParam()));
            result.put("delay", Integer.toString(CAPTURE_DELAY));
            result.put("user-style-path", buildCustomCSSParam(width));
            result.put("out", buildPathToOutputFileParam());
            result.put("header", String.format("Accept-Language:%s", Locales.getCurrent().getLanguage()));

            return result;
        }

        private String buildSnapshotURLParam() {
            IServletRequestHandler snapshotRequestHandler = executeOnOriginalContext(new IServletRequestHandler() {

                @Override
                public void handle(
                        HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

                    EntryPointsHandler.setupEntryPointsForThisRequest(request, entryPointsMap);

                    // Pending to forward and process additional parameters as show labels, resources, zoom or expand all
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
            // Calculate application path and destination file relative route
            String absolutePath = context.getRealPath("/");
            cssLinesToAppend(plannerWidth);

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
            heightCSS += " body div#scroll_container { height: " + height + PX_IMPORTANT; /* 1110 */
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

        CutyCaptParameters params = new CutyCaptParameters(forwardURL, entryPointsMap, parameters, planner);
        String generatedSnapshotServerPath = takeSnapshot(params);

        openInAnotherTab(generatedSnapshotServerPath);
    }

    private static void openInAnotherTab(String producedPrintFilePath) {
        Executions.getCurrent().sendRedirect(producedPrintFilePath, "_blank");
    }

    /**
     * It blocks until the snapshot is ready.
     * It invokes cutycapt program in order to take a snapshot from a specified url.
     *
     * @return the path in the web application to access via a HTTP GET to the
     *         generated snapshot.
     */
    private static String takeSnapshot(CutyCaptParameters params) {

        ProcessBuilder capture = new ProcessBuilder(CUTYCAPT_COMMAND);
        params.fillParameters(capture);
        String generatedSnapshotServerPath = params.getGeneratedSnapshotServerPath();

        Process printProcess = null;
        Process serverProcess = null;
        try {
            LOG.info("calling printing: " + capture.command());

            // If there is a not real X server environment then use Xvfb
            if ( StringUtils.isEmpty(System.getenv("DISPLAY")) ) {
                ProcessBuilder s = new ProcessBuilder("Xvfb", ":" + params.getXvfbDisplayNumber());
                serverProcess = s.start();
                capture.environment().put("DISPLAY", ":" + params.getXvfbDisplayNumber() + ".0");
            }

            printProcess = capture.start();
            printProcess.waitFor();

            // Once the printProcess finishes, the print snapshot is available
            return generatedSnapshotServerPath;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOG.error("error invoking command", e);
            throw new RuntimeException(e);
        } finally {
            if ( printProcess != null ) {
                destroy(printProcess);
            }
            if ( serverProcess != null ) {
                destroy(serverProcess);
            }
        }
    }

    private static void destroy(Process process) {
        try {
            process.destroy();
        } catch (Exception e) {
            LOG.error("error stoping process " + process, e);
        }
    }

    }
