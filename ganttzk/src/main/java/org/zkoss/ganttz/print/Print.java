package org.zkoss.ganttz.print;

import gantt.builder.ChartBuilder;
import gantt.builder.DatasetBuilder;
import gantt.data.ExtendedGanttCategoryDataset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeries;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.servlets.CallbackServlet;
import org.zkoss.ganttz.servlets.handlers.GeneratePrintPageHandler;
import org.zkoss.ganttz.servlets.handlers.JFreeChartHandler;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.Executions;

public class Print {

    private static final int INTERVAL_LENGTH_IN_MINUTES = 300 * 24 * 30;

    private static final String DEFAULT_SERIES_NAME = "Scheduled";

    private static final long serialVersionUID = 1L;

    public static void print(GanttDiagramGraph diagramGraph) {
        try {
            printGanttHorizontalPagingDemo(diagramGraph);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void printGanttHorizontalPagingDemo(GanttDiagramGraph diagramGraph)
            throws Exception {
        DatasetBuilder datasetBuilder = new DatasetBuilder();

        final List<Task> tasks = diagramGraph.getTasks();
        final Date begin = getSmallestBeginDate(tasks);
        final Date end = getBiggestFinishDate(tasks);

        // Create series
        List<TaskSeries> taskSeriesList = new ArrayList<TaskSeries>();
        final TaskSeries taskSeries = datasetBuilder.createTaskSeries(diagramGraph,
                DEFAULT_SERIES_NAME);
        taskSeriesList.add(taskSeries);

        // Create dataset and split it
        final ExtendedGanttCategoryDataset dataset = datasetBuilder
                .createDataset(taskSeriesList);

        List<ExtendedGanttCategoryDataset> subdatasets = datasetBuilder
                .splitDatasetInIntervals(dataset, begin, end,
                        INTERVAL_LENGTH_IN_MINUTES);

        // Show resulting printing page
        final List<String> URIs = convertDatasetsToURIs(subdatasets);
        final String id = GanttDiagramURIStore.storeURIs(URIs);
        final String URL = generatePrintPage(id);
        showPage(URL);
    }

    private static void showPage(String URL) {
        Executions.getCurrent().sendRedirect(URL, "_blank");
    }

    private static String generatePrintPage(String id) {
        final GeneratePrintPageHandler handler = new GeneratePrintPageHandler(id);
        String URL = CallbackServlet.registerAndCreateURLFor(getCurrentRequest(), handler);
        URL = URL.replace("/navalplanner-webapp", "");
        return URL;
    }

    private static HttpServletRequest getCurrentRequest() {
        return (HttpServletRequest) Executions.getCurrent().getNativeRequest();
    }

    private static List<String> convertDatasetsToURIs(List<ExtendedGanttCategoryDataset> datasets) {
        List<String> result = new ArrayList<String>();

        for (ExtendedGanttCategoryDataset each : datasets) {
            final JFreeChartHandler handler = new JFreeChartHandler(
                    createChart(each));
            final String uri = CallbackServlet.registerAndCreateURLFor(getCurrentRequest(),
                    handler);
            result.add(uri);
        }
        return result;
    }

    private static JFreeChart createChart(ExtendedGanttCategoryDataset dataset) {
        final ChartBuilder chartBuilder = new ChartBuilder();
        return chartBuilder.createChart("Gantt Diagram",
                "Tasks", "Date", dataset, true, true, false, ZoomLevel.DETAIL_TWO);
    }

    private static Date getSmallestBeginDate(
            List<org.zkoss.ganttz.data.Task> tasks) {
        if (tasks.isEmpty())
            return Calendar.getInstance().getTime();
        return getSmallest(getStartDates(tasks));
    }

    private static Date getBiggestFinishDate(
            List<org.zkoss.ganttz.data.Task> tasks) {
        if (tasks.isEmpty())
            return Calendar.getInstance().getTime();
        return getBiggest(getEndDates(tasks));
    }

    private static List<Date> getStartDates(
            List<org.zkoss.ganttz.data.Task> tasks) {
        ArrayList<Date> result = new ArrayList<Date>();
        for (org.zkoss.ganttz.data.Task t : tasks) {
            result.add(t.getBeginDate());
        }
        return result;
    }

    private static List<Date> getEndDates(List<org.zkoss.ganttz.data.Task> tasks) {
        ArrayList<Date> result = new ArrayList<Date>();
        for (org.zkoss.ganttz.data.Task t : tasks) {
            result.add(t.getEndDate());
        }
        return result;
    }

    private static <T extends Comparable<? super T>> T getSmallest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    private static <T extends Comparable<? super T>> T getBiggest(
            Collection<T> elements) {
        return getSmallest(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o2.compareTo(o1);
            }
        });
    }

    private static <T> T getSmallest(Collection<T> elements,
            Comparator<T> comparator) {
        List<T> withoutNulls = removeNulls(elements);
        if (withoutNulls.isEmpty())
            throw new IllegalArgumentException("at least one required");
        T result = null;
        for (T element : withoutNulls) {
            result = result == null ? element : (comparator.compare(result,
                    element) < 0 ? result : element);
        }
        return result;
    }

    private static <T> List<T> removeNulls(Collection<T> elements) {
        ArrayList<T> result = new ArrayList<T>();
        for (T e : elements) {
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }

}
