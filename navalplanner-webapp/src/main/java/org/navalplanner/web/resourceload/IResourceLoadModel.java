/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resourceload;

import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

public interface IResourceLoadModel {

    void initGlobalView(boolean filterByResources);

    void initGlobalView(Order filterBy, boolean filterByResources);

    List<LoadTimeLine> getLoadTimeLines();

    Interval getViewInterval();

    ZoomLevel calculateInitialZoomLevel();

    Order getOrderByTask(TaskElement task);

    boolean userCanRead(Order order, String loginName);

    void setResourcesToShow(List<Resource> resourcesList);

    void clearResourcesToShow();

    void setCriteriaToShow(List<Criterion> criteriaList);

    void clearCriteriaToShow();

    void setInitDateFilter(LocalDate value);

    void setEndDateFilter(LocalDate value);

    LocalDate getInitDateFilter();

    LocalDate getEndDateFilter();

    List<DayAssignment> getDayAssignments();

    List<Resource> getResources();

    boolean isExpandResourceLoadViewCharts();

    List<Resource> getAllResourcesList();

    List<Criterion> getAllCriteriaList();

    int getPageSize();

    int getPageFilterPosition();

    void setPageFilterPosition(int pageFilterPosition);

}
