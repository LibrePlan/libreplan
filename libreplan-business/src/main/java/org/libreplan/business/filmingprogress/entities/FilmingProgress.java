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

import java.math.BigDecimal;
import java.util.SortedMap;

import org.apache.commons.lang.Validate;
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

    private SortedMap<LocalDate, BigDecimal> initialProgressForecast;

    private SortedMap<LocalDate, BigDecimal> progressForecast;

    private SortedMap<LocalDate, BigDecimal> realProgress;

    private Order order;

    private FilmingProgressTypeEnum type;

    protected FilmingProgress() {

    }

    private FilmingProgress(Order order, FilmingProgressTypeEnum type) {
        this.setType(type);
        this.setOrder(order);
        resetDatesByOrder();
    }

    public static FilmingProgress create(Order order,
            FilmingProgressTypeEnum type) {
        Validate.notNull(order);
        Validate.notNull(order.getInitDate());
        Validate.notNull(order.getDeadline());
        FilmingProgress filmingProgress = create(new FilmingProgress(order,
                type));
        return filmingProgress;
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

    public void setInitialProgressForecast(
            SortedMap<LocalDate, BigDecimal> initialProgressForecast) {
        this.initialProgressForecast = initialProgressForecast;
    }

    public SortedMap<LocalDate, BigDecimal> getInitialProgressForecast() {
        return initialProgressForecast;
    }

    public void setProgressForecast(
            SortedMap<LocalDate, BigDecimal> progressForecast) {
        this.progressForecast = progressForecast;
    }

    public SortedMap<LocalDate, BigDecimal> getProgressForecast() {
        return progressForecast;
    }

    public void setRealProgress(SortedMap<LocalDate, BigDecimal> realProgress) {
        this.realProgress = realProgress;
    }

    public SortedMap<LocalDate, BigDecimal> getRealProgress() {
        return realProgress;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setType(FilmingProgressTypeEnum type) {
        this.type = type;
    }

    public FilmingProgressTypeEnum getType() {
        return type;
    }
}
