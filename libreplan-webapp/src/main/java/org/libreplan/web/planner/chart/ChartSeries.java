/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.web.planner.chart;

import java.math.BigDecimal;
import java.util.SortedMap;

import org.joda.time.LocalDate;

/**
 * One data series of a {@link Chart}, rendered by the client-side Chart.js wrapper
 * (common/js/libreplan-chart.js). Replaces the old {@code org.zkforge.timeplot.Plotinfo}.
 */
public class ChartSeries {

    private final SortedMap<LocalDate, BigDecimal> points;

    private String label;

    private String lineColor;

    private String fillColor;

    private int lineWidth = 1;

    public ChartSeries(SortedMap<LocalDate, BigDecimal> points) {
        this.points = points;
    }

    public SortedMap<LocalDate, BigDecimal> getPoints() {
        return points;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

}
