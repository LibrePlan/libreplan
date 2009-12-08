package org.zkoss.ganttz.servlets.handlers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class JFreeChartHandler implements IServletRequestHandler {

    private JFreeChart chart;

    private static final int WIDTH = 800;

    private static final int HEIGHT = 600;

    public JFreeChartHandler(JFreeChart chart) {
        this.chart = chart;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         toPNG(response, this.chart);
    }

    private void toPNG(HttpServletResponse response, JFreeChart chart) throws IOException {
        response.setContentType("image/png");
        ServletOutputStream writer = response.getOutputStream();
        writer.write(encodeAsPNG(chart));
        writer.close();
    }

    private static byte[] encodeAsPNG(JFreeChart chart) throws IOException {
        final BufferedImage chartImage = chart.createBufferedImage(WIDTH, HEIGHT);
        return ChartUtilities.encodeAsPNG(chartImage);
    }

}
