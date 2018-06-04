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

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.components.finders.FilterPair;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.Sessions;

/**
 * Manages operations to read and write filter parameters from the session.
 * <br />
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class FilterUtils {

    /** Company view and Project list session variables */

    public static Date readProjectsStartDate() {
        return (Date) Sessions.getCurrent().getAttribute("companyFilterStartDate");
    }

    public static Date readProjectsEndDate() {
        return (Date) Sessions.getCurrent().getAttribute("companyFilterEndDate");
    }

    public static String readProjectsName() {
        return (String) Sessions.getCurrent().getAttribute("companyFilterOrderName");
    }

    public static Boolean readExcludeFinishedProjects() {
        Boolean res = (Boolean) Sessions.getCurrent().getAttribute("companyFilterFinished");
        return res;
    }

    public static List<FilterPair> readProjectsParameters() {
        return (List<FilterPair>) Sessions.getCurrent().getAttribute("companyFilterLabel");
    }

    public static void writeProjectsStartDate(Date date) {
        Sessions.getCurrent().setAttribute("companyFilterStartDate", date);
        Sessions.getCurrent().setAttribute("companyFilterStartDateChanged", true);
    }

    public static boolean hasProjectsStartDateChanged() {
        return Sessions.getCurrent().hasAttribute("companyFilterStartDateChanged");
    }

    public static void writeProjectsEndDate(Date date) {
        Sessions.getCurrent().setAttribute("companyFilterEndDate", date);
        Sessions.getCurrent().setAttribute("companyFilterEndDateChanged", true);
    }

    public static boolean hasProjectsEndDateChanged() {
        return Sessions.getCurrent().hasAttribute("companyFilterEndDateChanged");
    }

    public static void writeProjectsName(String name) {
        Sessions.getCurrent().setAttribute("companyFilterOrderName", name);
    }

    public static void writeExcludeFinishedProjects(Boolean excludeFinishedProject) {
        Sessions.getCurrent().setAttribute("companyFilterFinished", excludeFinishedProject);
        Sessions.getCurrent().setAttribute("companyFilterFinishedChanged", true);
    }

    public static boolean hasExcludeFinishedProjects() {
        return Sessions.getCurrent().hasAttribute("companyFilterFinishedChanged");
    }

    public static void writeProjectsParameters(List<FilterPair> parameters) {
        Sessions.getCurrent().setAttribute("companyFilterLabel", parameters);
    }

    public static void writeProjectsFilter(Date startDate,
                                           Date endDate,
                                           List<FilterPair> parameters,
                                           String projectName,
                                           Boolean excludeFinishedProject) {

        writeProjectsStartDate(startDate);
        writeProjectsEndDate(endDate);
        writeProjectsParameters(parameters);
        writeProjectsName(projectName);
        writeExcludeFinishedProjects(excludeFinishedProject);
    }

    public static void writeProjectFilterChanged(boolean changed) {
        Sessions.getCurrent().setAttribute("companyFilterChanged", changed);
    }

    public static boolean hasProjectFilterChanged() {
        return (Sessions.getCurrent().getAttribute("companyFilterChanged") != null) &&
                ((Boolean) Sessions.getCurrent().getAttribute("companyFilterChanged"));
    }

    public static void writeProjectPlanningFilterChanged(boolean changed) {
        Sessions.getCurrent().setAttribute("companyFilterPlanningChanged", changed);
    }

    public static boolean hasProjectPlanningFilterChanged() {
        return (Sessions.getCurrent().getAttribute("companyFilterPlanningChanged") != null) &&
                ((Boolean) Sessions.getCurrent().getAttribute("companyFilterPlanningChanged"));
    }

    /** Resources load filter */

    public static LocalDate readResourceLoadsStartDate() {
        return (LocalDate) Sessions.getCurrent().getAttribute("resourceLoadStartDate");
    }

    public static LocalDate readResourceLoadsEndDate() {
        return (LocalDate) Sessions.getCurrent().getAttribute("resourceLoadEndDate");
    }

    public static List<FilterPair> readResourceLoadsBandbox() {
        return (List<FilterPair>) Sessions.getCurrent().getAttribute("resourceLoadFilterWorkerOrCriterion");
    }

    public static void writeResourceLoadsStartDate(LocalDate date) {
        Sessions.getCurrent().setAttribute("resourceLoadStartDate", date);
        Sessions.getCurrent().setAttribute("resourceLoadStartDateChanged", true);
    }

    public static boolean hasResourceLoadsStartDateChanged() {
        return Sessions.getCurrent().hasAttribute("resourceLoadStartDateChanged");
    }

    public static void writeResourceLoadsEndDate(LocalDate date) {
        Sessions.getCurrent().setAttribute("resourceLoadEndDate", date);
        Sessions.getCurrent().setAttribute("resourceLoadEndDateChanged", true);
    }

    public static boolean hasResourceLoadsEndDateChanged() {
        return Sessions.getCurrent().hasAttribute("resourceLoadEndDateChanged");
    }

    public static void writeResourceLoadsParameters(List<Object> parameters) {
        Sessions.getCurrent().setAttribute("resourceLoadFilterWorkerOrCriterion", parameters);
    }

    /** Project gantt and WBS filter parameters */

    public static Date readOrderStartDate(Order order) {
        return (Date) Sessions.getCurrent().getAttribute(order.getCode() + "-startDateFilter");
    }

    public static Date readOrderEndDate(Order order) {
        return (Date) Sessions.getCurrent().getAttribute(order.getCode() + "-endDateFilter");
    }

    public static String readOrderTaskName(Order order) {
        return (String) Sessions.getCurrent().getAttribute(order.getCode() + "-tasknameFilter");
    }

    public static String readOrderStatus(Order order) {
        return (String) Sessions.getCurrent().getAttribute(order.getCode() + "-orderStatus");
    }

    public static List<FilterPair> readOrderParameters(Order order) {
        return (List<FilterPair>) Sessions.getCurrent().getAttribute(order.getCode() + "-labelsandcriteriaFilter");
    }

    public static Boolean readOrderInheritance(Order order) {
        return (Boolean) Sessions.getCurrent().getAttribute(order.getCode() + "-inheritanceFilter");
    }


    public static void writeOrderStartDate(Order order, Date date) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-startDateFilter", date);
    }

    public static void writeOrderEndDate(Order order, Date date) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-endDateFilter", date);
    }

    public static void writeOrderTaskName(Order order, String name) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-tasknameFilter", name);
    }

    public static void writeOrderParameters(Order order, List<FilterPair> parameters) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-labelsandcriteriaFilter", parameters);
    }

    public static void writeOrderInheritance(Order order, boolean value) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-inheritanceFilter", value);
    }

    public static void clearBandboxes() {
        writeProjectsParameters(null);
        writeResourceLoadsParameters(null);
    }

    public static void clearSessionDates() {
        writeProjectsStartDate(null);
        Sessions.getCurrent().setAttribute("companyFilterStartDateChanged", null);
        writeProjectsEndDate(null);
        Sessions.getCurrent().setAttribute("companyFilterEndDateChanged", null);
        writeResourceLoadsStartDate(null);
        Sessions.getCurrent().setAttribute("resourceLoadStartDateChanged", null);
        writeResourceLoadsEndDate(null);
        Sessions.getCurrent().setAttribute("resourceLoadEndDateChanged", null);
        writeProjectsName(null);
    }

    public static ZoomLevel readZoomLevelCompanyView() {
        return (ZoomLevel) Sessions.getCurrent().getAttribute("zoomLevelCompanyView");
    }

    public static void writeZoomLevelCompanyView(ZoomLevel zoomLevel) {
        Sessions.getCurrent().setAttribute("zoomLevelCompanyView", zoomLevel);
    }

    public static ZoomLevel readZoomLevelResourcesLoad() {
        return (ZoomLevel) Sessions.getCurrent().getAttribute("zoomLevelResourcesLoad");
    }

    public static void writeZoomLevelResourcesLoad(ZoomLevel zoomLevel) {
        Sessions.getCurrent().setAttribute("zoomLevelResourcesLoad", zoomLevel);
    }

    public static ZoomLevel readZoomLevel(Order order) {
        return (ZoomLevel) Sessions.getCurrent().getAttribute(order.getCode() + "-zoomLevel");
    }

    public static void writeZoomLevel(Order order, ZoomLevel zoomLevel) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-zoomLevel", zoomLevel);
    }

    public static boolean sessionExists() {
        return Sessions.getCurrent() != null;
    }

    public static boolean hasOrderWBSFiltersChanged(Order order) {
        return sessionExists() &&
                (Sessions.getCurrent().getAttribute(order.getCode() + "-orderWBSFilterChanged") != null) &&
                ((Boolean) Sessions.getCurrent().getAttribute(order.getCode() + "-orderWBSFilterChanged"));
    }

    public static void writeOrderWBSFiltersChanged(Order order, boolean changed) {
        Sessions.getCurrent().setAttribute(order.getCode() + "-orderWBSFilterChanged", changed);
    }

}
