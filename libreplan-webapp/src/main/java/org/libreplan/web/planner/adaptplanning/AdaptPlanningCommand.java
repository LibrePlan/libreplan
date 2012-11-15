/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.web.planner.adaptplanning;

import static org.libreplan.web.I18nHelper._;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.PositionConstraintType;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;

/**
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdaptPlanningCommand implements IAdaptPlanningCommand {

    private PlanningState planningState;

    @Override
    public String getName() {
        return _("Adapt planning acording to timesheets");
    }

    @Override
    public void doAction(final IContext<TaskElement> context) {
        LongOperationFeedback.execute(context.getRelativeTo(),
                new ILongOperation() {

                    @Override
                    public String getName() {
                        return _("Adapting planning according to timesheets");
                    }

                    @Override
                    public void doAction() throws Exception {
                        adaptPlanning(context);
                    }
                });
    }

    private void adaptPlanning(IContext<TaskElement> context) {
        List<TaskElement> taskElements = planningState.getRootTask()
                .getAllChildren();
        for (TaskElement taskElement : taskElements) {
            // Only adapt task leafs
            if (!taskElement.isLeaf()) {
                continue;
            }

            OrderElement orderElement = taskElement.getOrderElement();
            // Reset status to allow move the task if needed while adapting the
            // planning
            taskElement.setUpdatedFromTimesheets(false);

            if (orderElement.hasTimesheetsReportingHours()) {
                setStartDateAndConstraint(taskElement,
                        orderElement.getFirstTimesheetDate());
                Date lastTimesheetDate = orderElement.getLastTimesheetDate();
                setEndDateIfNeeded(taskElement, lastTimesheetDate);

                if (orderElement.isFinishedTimesheets()) {
                    setEndDate(taskElement, lastTimesheetDate);
                    addTimesheetsProgress(orderElement, lastTimesheetDate);
                    removeResourceAllocationsBeyondEndDate(taskElement);
                } else {
                    removeTimesheetsProgressIfAny(orderElement);
                }

                taskElement.setUpdatedFromTimesheets(true);
            }
        }
        for (TaskElement taskElement : taskElements) {
            if (taskElement.isUpdatedFromTimesheets()) {
                updateTask(context, taskElement);
            }
        }

        ((Planner) context.getRelativeTo()).invalidate();
        context.reloadCharts();
    }

    private void removeResourceAllocationsBeyondEndDate(TaskElement taskElement) {
        LocalDate endDate = taskElement.getEndAsLocalDate();

        for (ResourceAllocation<?> resourceAllocation : taskElement
                .getAllResourceAllocations()) {
            resourceAllocation.removeDayAssignmentsBeyondDate(endDate);
        }
    }

    private void setStartDateAndConstraint(TaskElement taskElement,
            Date startDate) {
        taskElement.setStartDate(startDate);
        setStartInFixedDateConstarint(taskElement, startDate);
    }

    private void setStartInFixedDateConstarint(TaskElement taskElement,
            Date startDate) {
        if (taskElement.isTask()) {
            Task task = (Task) taskElement;
            task.getPositionConstraint()
                    .update(PositionConstraintType.START_IN_FIXED_DATE,
                            IntraDayDate.startOfDay(LocalDate
                                    .fromDateFields(startDate)));
        }
    }

    private void setEndDateIfNeeded(TaskElement taskElement, Date endDate) {
        if (taskElement.getEndDate().compareTo(endDate) <= 0) {
            setEndDate(taskElement, endDate);
        }
    }

    private void setEndDate(TaskElement taskElement, Date endDate) {
        taskElement.setEndDate(LocalDate.fromDateFields(endDate).plusDays(1)
                .toDateTimeAtStartOfDay().toDate());
    }

    private void addTimesheetsProgress(OrderElement orderElement,
            Date progressDate) {
        AdvanceType timesheetsAdvanceType = getTimesheetsAdvanceType();

        DirectAdvanceAssignment timesheetsAdvanceAssignment = orderElement
                .getDirectAdvanceAssignmentByType(timesheetsAdvanceType);

        if (timesheetsAdvanceAssignment == null) {
            timesheetsAdvanceAssignment = DirectAdvanceAssignment.create(false,
                    timesheetsAdvanceType.getDefaultMaxValue());
            timesheetsAdvanceAssignment.setAdvanceType(timesheetsAdvanceType);
            try {
                orderElement.addAdvanceAssignment(timesheetsAdvanceAssignment);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                // This shouldn't happen as the new advanceAssignment is not
                // marked as spread yet
                throw new RuntimeException(e);
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                // If the same type already exists in other element we don't do
                // anything
                return;
            }
        }

        DirectAdvanceAssignment spreadAdvanceAssignment = orderElement
                .getReportGlobalAdvanceAssignment();
        if (spreadAdvanceAssignment != null) {
            spreadAdvanceAssignment.setReportGlobalAdvance(false);
        }

        timesheetsAdvanceAssignment.setReportGlobalAdvance(true);
        timesheetsAdvanceAssignment.resetAdvanceMeasurements(AdvanceMeasurement
                .create(LocalDate.fromDateFields(progressDate),
                        timesheetsAdvanceType.getDefaultMaxValue()));
    }

    private AdvanceType getTimesheetsAdvanceType() {
        return PredefinedAdvancedTypes.TIMESHEETS.getType();
    }

    private void removeTimesheetsProgressIfAny(OrderElement orderElement) {
        DirectAdvanceAssignment timesheetsAdvanceAssignment = orderElement
                .getDirectAdvanceAssignmentByType(getTimesheetsAdvanceType());
        if (timesheetsAdvanceAssignment != null) {
            orderElement.removeAdvanceAssignment(timesheetsAdvanceAssignment);
        }
    }

    private void updateTask(IContext<TaskElement> context,
            TaskElement taskElement) {
        taskElement.updateAdvancePercentageFromOrderElement();

        Planner planner = (Planner) context.getRelativeTo();
        org.zkoss.ganttz.data.Task task = context
                .getMapper().findAssociatedBean(taskElement);
        task.firePropertyChangeForTaskDates();
        TaskComponent taskComponent = planner.getTaskComponentRelatedTo(task);
        if (taskComponent != null) {
            taskComponent.updateTooltipText();
            taskComponent.updateProperties();
            taskComponent.invalidate();
        }

        context.recalculatePosition(taskElement);
    }

    @Override
    public String getImage() {
        return "/common/img/ico_adapt_planning.png";
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    public boolean isPlannerCommand() {
        return true;
    }

}