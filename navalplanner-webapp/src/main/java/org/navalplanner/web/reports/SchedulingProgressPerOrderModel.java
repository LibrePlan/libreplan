/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.joda.time.LocalDate;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.reports.dtos.SchedulingProgressPerOrderDTO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SchedulingProgressPerOrderModel implements ISchedulingProgressPerOrderModel {

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    IOrderElementDAO taskDAO;

    @Autowired
    IAdvanceTypeDAO advanceTypeDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private List<Order> selectedOrders = new ArrayList<Order>();

    private List<Order> allOrders = new ArrayList<Order>();

    @Override
    @Transactional(readOnly = true)
    public void init() {
        selectedOrders.clear();
        allOrders.clear();
        loadAllOrders();
    }

    private void loadAllOrders() {
        allOrders = orderDAO.getOrdersByScenario(scenarioManager
                .getCurrent());

        for (Order each : allOrders) {
            each.useSchedulingDataFor(scenarioManager.getCurrent());
            initializeTasks(each.getTaskElements());
            initializeOrderElements(each.getAllOrderElements());
            initializaReportGlobalAdvanceAssignment(each
                    .getReportGlobalAdvanceAssignment());
            initializeDirectAdvanceAssignments(each.getDirectAdvanceAssignments());
            initializeIndirectAdvanceAssignments(each.getIndirectAdvanceAssignments());
        }
    }

    private void initializeOrderElements(List<OrderElement> orderElements) {
        for(OrderElement each: orderElements) {
            each.getCode();
            initializeDirectAdvanceAssignments(each.getDirectAdvanceAssignments());
            initializeTasks(each.getTaskElements());
        }
    }

    private void initializeTasks(Set<TaskElement> tasks) {
        for(TaskElement each: tasks) {
            each.getName();
            initializeTaskSource(each.getTaskSource());
            initializeResourceAllocations(each.getSatisfiedResourceAllocations());
        }
    }

    private void initializeResourceAllocations(Set<ResourceAllocation<?>> resourceAllocations) {
        for (ResourceAllocation<?> each: resourceAllocations) {
            each.getAssignedHours();
        }
    }

    private void initializeTaskSource(TaskSource taskSource) {
        taskSource.getTotalHours();
        initializeHoursGroups(taskSource.getHoursGroups());
    }

    private void initializeHoursGroups(Set<HoursGroup> hoursGroups) {
        for (HoursGroup each: hoursGroups) {
            each.getPercentage();
        }
    }

    private void initializeDirectAdvanceAssignments(Set<DirectAdvanceAssignment> directAdvanceAssingments) {
        for (DirectAdvanceAssignment each: directAdvanceAssingments) {
            each.getMaxValue();
            initializaAdvanceType(each.getAdvanceType());
            initializaAdvanceMeasurements(each.getAdvanceMeasurements());
        }
    }

    private void initializeIndirectAdvanceAssignments(Set<IndirectAdvanceAssignment> indirectAdvanceAssingments) {
        for (IndirectAdvanceAssignment each: indirectAdvanceAssingments) {
            each.getReportGlobalAdvance();
            initializaAdvanceType(each.getAdvanceType());
        }
    }

    private void initializaReportGlobalAdvanceAssignment(
            DirectAdvanceAssignment directAdvance) {
        if (directAdvance != null) {
            directAdvance.getAdvancePercentage();
            initializaAdvanceMeasurements(directAdvance
                    .getAdvanceMeasurements());
        }
    }

    private void initializaAdvanceType(AdvanceType advanceType) {
        advanceType.getUnitName();
    }

    private void initializaAdvanceMeasurements(Set<AdvanceMeasurement> advanceMeasurements) {
        for (AdvanceMeasurement each: advanceMeasurements) {
            each.getDate();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getSchedulingProgressPerOrderReport(List<Order> orders,
            AdvanceType advanceType, Date startingDate, Date endingDate,
            LocalDate referenceDate) {

        if (orders == null || orders.isEmpty()) {
            return new JREmptyDataSource();
        }

        // Create DTOs for orders
        final List<SchedulingProgressPerOrderDTO> schedulingProgressPerOrderList =
            new ArrayList<SchedulingProgressPerOrderDTO>();
        for (Order each: orders) {
            // Filter by date
            if ((startingDate != null) && (each.getInitDate() != null)
                    && startingDate.compareTo(each.getInitDate()) > 0) {
                continue;
            }
            if ((endingDate != null) && (each.getDeadline() != null)
                    && endingDate.compareTo(each.getDeadline()) < 0) {
                continue;
            }
            // Add to list
            final List<Task> tasks = getTasks(each);
            schedulingProgressPerOrderList
                    .add(new SchedulingProgressPerOrderDTO(each, tasks,
                            advanceType, referenceDate));
        }
        if (schedulingProgressPerOrderList.isEmpty()) {
            return new JREmptyDataSource();
        }
        return new JRBeanCollectionDataSource(schedulingProgressPerOrderList);

    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceType> getAdvanceTypes() {
        List<AdvanceType> result = new ArrayList<AdvanceType>();
        result.addAll(advanceTypeDAO.getAll());
        return result;
    }

    private List<Task> getTasks(Order order) {
        List<Task> result = new ArrayList<Task>();
        final List<TaskElement> taskElements = order
                .getAllChildrenAssociatedTaskElements();
        for (TaskElement each : taskElements) {
            if (each instanceof Task) {
                result.add((Task) each);
            }
        }
        return result;
    }

    @Override
    public List<Order> getOrders() {
        return allOrders;
    }

    @Override
    public void removeSelectedOrder(Order order) {
        this.selectedOrders.remove(order);
    }

    @Override
    public boolean addSelectedOrder(Order order) {
        if (this.selectedOrders.contains(order)) {
            return false;
        }
        this.selectedOrders.add(order);
        return true;
    }

    @Override
    public List<Order> getSelectedOrders() {
        return selectedOrders;
    }

}
