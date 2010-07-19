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

package org.navalplanner.web.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskStatusEnum;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.reports.dtos.WorkingArrangementPerOrderDTO;
import org.navalplanner.business.reports.dtos.WorkingArrangementPerOrderDTO.DependencyWorkingArrangementDTO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkingArrangementsPerOrderModel implements
        IWorkingArrangementsPerOrderModel {

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    ITaskElementDAO taskDAO;

    @Autowired
    IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        Scenario currentScenario = scenarioManager.getCurrent();
        final List<Order> orders = orderDAO
                .getOrdersByScenario(currentScenario);
        for (Order each: orders) {
            initializeOrderElements(each.getOrderElements());
            each.useSchedulingDataFor(currentScenario);
        }
        return orders;
    }

    private void initializeOrderElements(List<OrderElement> orderElements) {
        for (OrderElement orderElement: orderElements) {
            orderElement.getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getWorkingArrangementsPerOrderReportReport(
            Order order, TaskStatusEnum taskStatus, boolean showDependencies) {
        if (order == null) {
            return new JREmptyDataSource();
        }

        orderDAO.reattach(order);

        List<WorkingArrangementPerOrderDTO> workingArrangementPerOrderList =
            new ArrayList<WorkingArrangementPerOrderDTO>();

        final List<Task> tasks = filterOnlyTasks(order
                .getAllChildrenAssociatedTaskElements());
        final List<Task> sortTasks = sortTasks(order, tasks);
        final Date deadLineOrder = order.getDeadline();
        for (Task each : sortTasks) {
            final Task task = (Task) each;
            // If taskStatus is ALL, add task and calculate its real status
            if (TaskStatusEnum.ALL.equals(taskStatus)) {
                workingArrangementPerOrderList
                        .addAll(createWorkingArrangementPerOrderDTOs(
                                deadLineOrder, task,
                                calculateTaskStatus(task), showDependencies));
                continue;
            }

            // Only add task if matches selected taskStatus
            if (matchTaskStatus(task, taskStatus)) {
                workingArrangementPerOrderList
                        .addAll(createWorkingArrangementPerOrderDTOs(
                                deadLineOrder, task, taskStatus,
                                showDependencies));
            }
        }
        return new JRBeanCollectionDataSource(workingArrangementPerOrderList);
    }

    private List<Task> filterOnlyTasks(List<TaskElement> taskElements) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement taskElement : taskElements) {
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private List<Task> sortTasks(Order order, List<Task> tasks) {
        List<Task> sortTasks = new ArrayList<Task>();
        final List<OrderElement> orderElements = order.getAllChildren();
        for (OrderElement orderElement : orderElements) {
            Task task = findOrderElementInTasks(orderElement, tasks);
            if (task != null) {
                sortTasks.add(task);
            }
        }
        return sortTasks;
    }

    private Task findOrderElementInTasks(OrderElement orderElement,
            List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getOrderElement().getId().equals(orderElement.getId())) {
                return task;
            }
        }
        return null;
    }

    /**
     * Create a collection of {@link WorkingArrangementPerOrderDTO}
     *
     * It's necessary to create one dto for every {@link Dependency} in {@link Task}
     *
     * @param task
     * @param taskStatus
     * @return
     */
    @Transactional(readOnly = true)
    private List<WorkingArrangementPerOrderDTO> createWorkingArrangementPerOrderDTOs(
            Date deadLineOrder,
            Task task, TaskStatusEnum taskStatus, boolean showDependencies) {

        List<WorkingArrangementPerOrderDTO> result = new ArrayList<WorkingArrangementPerOrderDTO>();

        // Add current task
        final Set<Dependency> dependencies = task
                .getDependenciesWithThisDestination();
        result.add(new WorkingArrangementPerOrderDTO(deadLineOrder, task,
                taskStatus, showDependencies && !dependencies.isEmpty()));

        // Add dependencies
        if (showDependencies) {
            taskDAO.reattach(task);
            for (Dependency each : dependencies) {
                final OrderElement orderElement = each.getOrigin()
                        .getOrderElement();
                DependencyWorkingArrangementDTO dependencyDTO = new DependencyWorkingArrangementDTO(
                        orderElement.getName(), orderElement.getCode(), each
                                .getType().toString(), orderElement
                                .getAdvancePercentage());
                result.add(new WorkingArrangementPerOrderDTO(task, taskStatus,
                        dependencyDTO));
            }
        }
        return result;
    }

    private boolean matchTaskStatus(Task task, TaskStatusEnum taskStatus) {
        final TaskStatusEnum _taskStatus = calculateTaskStatus(task);
        return _taskStatus != null && _taskStatus.equals(taskStatus);
    }

    private TaskStatusEnum calculateTaskStatus(Task task) {

        if (matchTaskStatusFinished(task)) {
            return TaskStatusEnum.FINISHED;
        }
        if (matchTaskStatusInProgress(task)) {
            return TaskStatusEnum.IN_PROGRESS;
        }
        if (matchTaskStatusPending(task)) {
            return TaskStatusEnum.PENDING;
        }
        if (matchTaskStatusBlocked(task)) {
            return TaskStatusEnum.BLOCKED;
        }

        return null;
    }

    private boolean matchTaskStatusFinished(Task task) {
        final OrderElement order = task.getOrderElement();
        BigDecimal measuredProgress = order.getAdvancePercentage();
        return isFinished(measuredProgress);
    }

    private boolean isFinished(BigDecimal measuredProgress) {
        measuredProgress = (measuredProgress.multiply(new BigDecimal(100)))
                .setScale(0, BigDecimal.ROUND_UP);
        return measuredProgress.equals(new BigDecimal(100));
    }

    private boolean matchTaskStatusInProgress(Task task) {
        final OrderElement order = task.getOrderElement();
        final BigDecimal measuredProgress = order.getAdvancePercentage();
        return isInProgress(measuredProgress)
                || (hasNotYetStarted(measuredProgress) && hasAtLeastOneWorkReportLine(order));
    }

    private boolean isInProgress(BigDecimal measuredProgress) {
        return ((measuredProgress.compareTo(new BigDecimal(1)) < 0) && (measuredProgress
                .compareTo(new BigDecimal(0)) > 0));
    }

    private boolean hasAtLeastOneWorkReportLine(OrderElement order) {
        return !getWorkReportLines(order).isEmpty();
    }

    @Transactional(readOnly = true)
    private List<WorkReportLine> getWorkReportLines(OrderElement order) {
        return workReportLineDAO.findByOrderElement(order);
    }

    private boolean matchTaskStatusPending(Task task) {
        final OrderElement order = task.getOrderElement();
        final BigDecimal measuredProgress = order.getAdvancePercentage();

        return hasNotYetStarted(measuredProgress)
                && hasNotWorkReports(order)
                && (!isBlockedByDepedendantTasks(task));
    }

    private boolean hasNotWorkReports(OrderElement order) {
        return !hasAtLeastOneWorkReportLine(order);
    }

    private boolean hasNotYetStarted(BigDecimal measuredProgress) {
        return measuredProgress.setScale(2).equals(
                new BigDecimal(0).setScale(2));
    }

    private boolean matchTaskStatusBlocked(Task task) {
        final OrderElement order = task.getOrderElement();
        final BigDecimal measuredProgress = order.getAdvancePercentage();

        return hasNotYetStarted(measuredProgress)
                && hasNotWorkReports(order)
                && isBlockedByDepedendantTasks(task);
    }

    private boolean isBlockedByDepedendantTasks(Task task) {
        taskDAO.reattach(task);
        final Set<Dependency> dependencies = task
                .getDependenciesWithThisDestination();
        if (dependencies.isEmpty()) {
            return false;
        }

        boolean result = true;
        for (Dependency each: dependencies) {
            final TaskElement taskElement = each.getOrigin();
            final BigDecimal measuredProgress = taskElement.getOrderElement()
                .getAdvancePercentage();

            final Type dependencyType = each.getType();
            if (Type.END_START.equals(dependencyType)) {
                result &= (!isFinished(measuredProgress));
            }
            if (Type.START_START.equals(dependencyType)) {
                result &= hasNotYetStarted(measuredProgress);
            }
        }
        return result;
    }

}
