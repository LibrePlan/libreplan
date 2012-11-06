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
    public void doAction(IContext<TaskElement> context) {
        List<TaskElement> taskElements = planningState.getRootTask()
                .getAllChildren();
        for (TaskElement taskElement : taskElements) {
            OrderElement orderElement = taskElement.getOrderElement();
            taskElement.setUpdatedFromTimesheets(orderElement
                    .hasTimesheetsReportingHours());

            if (taskElement.isUpdatedFromTimesheets()) {
                setStartDateAndConstraint(taskElement, orderElement
                        .getSumChargedEffort().getFirstTimesheetDate());
                Date lastTimesheetDate = orderElement.getSumChargedEffort()
                        .getLastTimesheetDate();
                setEndDateIfNeeded(taskElement, lastTimesheetDate);

                if (orderElement.isFinishedTimesheets()) {
                    setEndDate(taskElement, lastTimesheetDate);
                    addTimesheetsProgress(orderElement, lastTimesheetDate);
                } else {
                    removeTimesheetsProgressIfAny(orderElement);
                }

                updateTask(context, taskElement);
            }
        }
        context.reloadCharts();
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
        TaskComponent taskComponent = planner.getTaskComponentRelatedTo(context
                .getMapper().findAssociatedBean(taskElement));
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