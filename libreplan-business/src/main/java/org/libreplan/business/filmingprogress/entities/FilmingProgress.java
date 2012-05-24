/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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
package org.libreplan.business.filmingprogress.entities;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.Order;

/**
 * Represents the progress associated to a filming.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class FilmingProgress extends BaseEntity {

    private LocalDate startDate;

    private LocalDate endDate;

    private ProgressGranularityType progressGranularity = ProgressGranularityType.getDefault();

    private SortedMap<LocalDate, Integer> initialProgressForecast = new TreeMap<LocalDate, Integer>();

    private SortedMap<LocalDate, Integer> progressForecast = new TreeMap<LocalDate, Integer>();

    private SortedMap<LocalDate, Integer> realProgress = new TreeMap<LocalDate, Integer>();

    private Order order;

    protected FilmingProgress() {

    }

    private FilmingProgress(Order order) {
        this.setOrder(order);
        resetDatesByOrder();
    }

    public static FilmingProgress create(Order order) {
        return create(new FilmingProgress(order));
    }

    public static FilmingProgress create() {
        return create(new FilmingProgress());
    }

    private void resetDatesByOrder() {
        LocalDate endDate = null;
        LocalDate startDate = null;
        if (order.getDeadline() != null) {
            endDate = new LocalDate(order.getDeadline());
        }
        if (order.getInitDate() != null) {
            startDate = new LocalDate(order.getInitDate());
        }
        this.setEndDate(endDate);
        this.setStartDate(startDate);
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setProgressGranularity(ProgressGranularityType progressGranularity) {
        this.progressGranularity = progressGranularity;
    }

    public ProgressGranularityType getProgressGranularity() {
        return progressGranularity;
    }

    public void setInitialProgressForecast(SortedMap<LocalDate, Integer> initialProgressForecast) {
        this.initialProgressForecast = initialProgressForecast;
    }

    public Map<LocalDate, Integer> getInitialProgressForecast() {
        return initialProgressForecast;
    }

    public void setProgressForecast(SortedMap<LocalDate, Integer> progressForecast) {
        this.progressForecast = progressForecast;
    }

    public Map<LocalDate, Integer> getProgressForecast() {
        return progressForecast;
    }

    public void setRealProgress(SortedMap<LocalDate, Integer> realProgress) {
        this.realProgress = realProgress;
    }

    public Map<LocalDate, Integer> getRealProgress() {
        return realProgress;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
