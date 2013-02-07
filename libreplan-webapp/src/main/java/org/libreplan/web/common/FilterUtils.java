/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

package org.libreplan.web.common;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.components.finders.FilterPair;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.Sessions;

/**
 * Manages operations to read and write filter parameters from the session <br />
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class FilterUtils {

    // Company view and Project list session variables

    public static Date readProjectsStartDate() {
        return (Date) Sessions.getCurrent().getAttribute(
                "companyFilterStartDate");
    }

    public static Date readProjectsEndDate() {
        return (Date) Sessions.getCurrent()
                .getAttribute("companyFilterEndDate");
    }

    public static List<Object> readProjectsParameters() {
        return (List<Object>) Sessions.getCurrent().getAttribute(
                "companyFilterLabel");
    }

    public static void writeProjectsStartDate(Date date) {
        Sessions.getCurrent().setAttribute("companyFilterStartDate", date);
    }

    public static void writeProjectsEndDate(Date date) {
        Sessions.getCurrent().setAttribute("companyFilterEndDate", date);
    }

    public static void writeProjectsParameters(List<Object> parameters) {
        Sessions.getCurrent().setAttribute("companyFilterLabel", parameters);
    }

    public static void writeProjectsFilter(Date startDate, Date endDate,
            List<Object> parameters) {
        writeProjectsStartDate(startDate);
        writeProjectsEndDate(endDate);
        writeProjectsParameters(parameters);
    }

    // Resources load filter

    public static LocalDate readResourceLoadsStartDate() {
        return (LocalDate) Sessions.getCurrent().getAttribute(
                "resourceLoadStartDate");
    }

    public static LocalDate readResourceLoadsEndDate() {
        return (LocalDate) Sessions.getCurrent().getAttribute(
                "resourceLoadEndDate");
    }

    public static List<FilterPair> readResourceLoadsBandbox() {
        return (List<FilterPair>) Sessions.getCurrent().getAttribute(
                "resourceLoadFilterWorkerOrCriterion");
    }

    public static void writeResourceLoadsStartDate(LocalDate date) {
        Sessions.getCurrent().setAttribute("resourceLoadStartDate", date);
    }

    public static void writeResourceLoadsEndDate(LocalDate date) {
        Sessions.getCurrent().setAttribute("resourceLoadEndDate", date);
    }

    public static void writeResourceLoadsParameters(List<Object> parameters) {
        Sessions.getCurrent().setAttribute(
                "resourceLoadFilterWorkerOrCriterion", parameters);
    }

    // Project gantt and WBS filter parameters

    public static Date readOrderStartDate(String orderCode) {
        return (Date) Sessions.getCurrent().getAttribute(
                orderCode + "-startDateFilter");
    }

    public static Date readOrderEndDate(String orderCode) {
        return (Date) Sessions.getCurrent().getAttribute(
                orderCode + "-endDateFilter");
    }

    public static String readOrderTaskName(String orderCode) {
        return (String) Sessions.getCurrent().getAttribute(
                orderCode + "-tasknameFilter");
    }

    public static List<Object> readOrderParameters(String orderCode) {
        return (List<Object>) Sessions.getCurrent().getAttribute(
                orderCode + "-labelsandcriteriaFilter");
    }

    public static Boolean readOrderInheritance(String orderCode) {
        return (Boolean) Sessions.getCurrent().getAttribute(
                orderCode + "-inheritanceFilter");
    }

    public static void writeOrderStartDate(String orderCode, Date date) {
        Sessions.getCurrent()
                .setAttribute(orderCode + "-startDateFilter", date);
    }
    public static void writeOrderEndDate(String orderCode, Date date) {
        Sessions.getCurrent().setAttribute(orderCode + "-endDateFilter", date);
    }
    public static void writeOrderTaskName(String orderCode, String name) {
        Sessions.getCurrent().setAttribute(orderCode + "-tasknameFilter", name);
    }

    public static void clearBandboxes() {
        writeProjectsParameters(null);
        writeResourceLoadsParameters(null);
        // Locate all order-specific bandboxes
        for (String key : (Set <String>) Sessions.getCurrent().getAttributes().keySet() ) {
            if (key.contains("-tasknameFilter")) {
                Sessions.getCurrent().setAttribute(key, null);
            }
        }
    }

    public static ZoomLevel readZoomLevel() {
        return (ZoomLevel) Sessions.getCurrent().getAttribute("zoomLevel");
    }

    public static void writeZoomLevel(ZoomLevel zoomLevel) {
        Sessions.getCurrent().setAttribute("zoomLevel", zoomLevel);
    }

    public static ZoomLevel readZoomLevel(Order order) {
        return (ZoomLevel) Sessions.getCurrent().getAttribute(
                order.getCode() + "-zoomLevel");
    }

    public static void writeZoomLevel(Order order, ZoomLevel zoomLevel) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-zoomLevel",
                zoomLevel);
    }

}
