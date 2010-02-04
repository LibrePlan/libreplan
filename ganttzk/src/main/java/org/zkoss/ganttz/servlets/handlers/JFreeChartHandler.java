/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
