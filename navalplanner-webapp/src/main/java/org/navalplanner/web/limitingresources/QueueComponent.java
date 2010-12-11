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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.limiting.entities.DateAndHour;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.zkoss.ganttz.DatesMapperOnInterval;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;


/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class QueueComponent extends XulElement implements
        AfterCompose {

    private static final int DEADLINE_MARK_HALF_WIDTH = 5;

    public static QueueComponent create(
            QueueListComponent queueListComponent,
            TimeTracker timeTracker,
            LimitingResourceQueue limitingResourceQueue) {

        return new QueueComponent(queueListComponent, timeTracker,
                limitingResourceQueue);
    }

    private final QueueListComponent queueListComponent;

    private final TimeTracker timeTracker;

    private transient IZoomLevelChangedListener zoomChangedListener;

    private LimitingResourceQueue limitingResourceQueue;

    private List<QueueTask> queueTasks = new ArrayList<QueueTask>();

    public List<QueueTask> getQueueTasks() {
        return queueTasks;
    }

    public void setLimitingResourceQueue(LimitingResourceQueue limitingResourceQueue) {
        this.limitingResourceQueue = limitingResourceQueue;
    }

    private QueueComponent(
            final QueueListComponent queueListComponent,
            final TimeTracker timeTracker,
            final LimitingResourceQueue limitingResourceQueue) {

        this.queueListComponent = queueListComponent;
        this.limitingResourceQueue = limitingResourceQueue;
        this.timeTracker = timeTracker;

        createChildren(limitingResourceQueue, timeTracker.getMapper());
        zoomChangedListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                getChildren().clear();
                createChildren(limitingResourceQueue, timeTracker.getMapper());
                // invalidate();
            }
        };
        this.timeTracker.addZoomListener(zoomChangedListener);
    }

    private void createChildren(LimitingResourceQueue limitingResourceQueue,
            IDatesMapper mapper) {
        List<QueueTask> queueTasks = createQueueTasks(mapper,
                limitingResourceQueue.getLimitingResourceQueueElements());
        appendQueueTasks(queueTasks);
    }

    public QueueListComponent getQueueListComponent() {
        return queueListComponent;
    }

    public LimitingResourcesPanel getLimitingResourcesPanel() {
        return queueListComponent.getLimitingResourcePanel();
    }

    public void invalidate() {
        removeChildren();
        appendQueueElements(limitingResourceQueue.getLimitingResourceQueueElements());
    }

    private void removeChildren() {
        for (QueueTask each: queueTasks) {
            removeChild(each);
        }
        queueTasks.clear();
    }

    private void appendQueueTasks(List<QueueTask> queueTasks) {
        for (QueueTask each: queueTasks) {
            appendQueueTask(each);
        }
    }

    private void appendQueueTask(QueueTask queueTask) {
        queueTasks.add(queueTask);
        appendChild(queueTask);
    }

    private void removeQueueTask(QueueTask queueTask) {
        queueTasks.remove(queueTask);
        removeChild(queueTask);
    }

    private List<QueueTask> createQueueTasks(IDatesMapper datesMapper,
            Set<LimitingResourceQueueElement> list) {

        List<QueueTask> result = new ArrayList<QueueTask>();

        org.zkoss.ganttz.util.Interval interval = null;
        if (timeTracker.getFilter() != null) {
            timeTracker.getFilter().resetInterval();
            interval = timeTracker.getFilter().getCurrentPaginationInterval();
        }
        for (LimitingResourceQueueElement each : list) {
            if (interval != null) {
                if (each.getEndDate().toDateMidnight()
                        .isAfter(interval.getStart().toDateMidnight())
                        && each.getStartDate().toDateMidnight()
                                .isBefore(interval.getFinish().toDateMidnight())) {
                    result.add(createQueueTask(datesMapper, each));
                }
            } else {
                result.add(createQueueTask(datesMapper, each));
            }
        }
        return result;
    }

    private static QueueTask createQueueTask(IDatesMapper datesMapper, LimitingResourceQueueElement element) {
        validateQueueElement(element);
        return createDivForElement(datesMapper, element);
    }

    private static OrderElement getRootOrder(Task task) {
        OrderElement order = task.getOrderElement();
        while (order.getParent() != null) {
            order = order.getParent();
        }
        return order;
    }

    private static String createTooltiptext(LimitingResourceQueueElement element) {
        final Task task = element.getResourceAllocation().getTask();
        final OrderElement order = getRootOrder(task);

        StringBuilder result = new StringBuilder();
        result.append(_("Order: {0} ", order.getName()));
        result.append(_("Task: {0} ", task.getName()));
        result.append(_("Completed: {0}% ", element.getAdvancePercentage().multiply(new BigDecimal(100))));

        final ResourceAllocation<?> resourceAllocation = element.getResourceAllocation();
        if (resourceAllocation instanceof SpecificResourceAllocation) {
            final SpecificResourceAllocation specific = (SpecificResourceAllocation) resourceAllocation;
            result.append(_("Resource: {0} ", specific.getResource().getName()));
        } else if (resourceAllocation instanceof GenericResourceAllocation) {
            final GenericResourceAllocation generic = (GenericResourceAllocation) resourceAllocation;
            result.append(_("Criteria: {0} ", Criterion.getCaptionFor(generic.getCriterions())));
        }
        result.append(_("Allocation: [{0},{1}]", element.getStartDate()
                .toString(), element.getEndDate()));

        return result.toString();
    }

    /**
     * Returns end date considering % of task completion
     *
     * @param element
     * @return
     */
    private static DateAndHour getAdvanceEndDate(LimitingResourceQueueElement element) {
        int hoursWorked = 0;
        final List<? extends DayAssignment> dayAssignments = element.getDayAssignments();
        if (element.hasDayAssignments()) {
            final int estimatedWorkedHours = estimatedWorkedHours(element.getIntentedTotalHours(), element.getAdvancePercentage());

            for (DayAssignment each: dayAssignments) {
                hoursWorked += each.getHours();
                if (hoursWorked >= estimatedWorkedHours) {
                    int hourSlot = each.getHours() - (hoursWorked - estimatedWorkedHours);
                    return new DateAndHour(each.getDay(), hourSlot);
                }
            }
        }
        if (hoursWorked != 0) {
            DayAssignment lastDayAssignment = dayAssignments.get(dayAssignments.size() - 1);
            return new DateAndHour(lastDayAssignment.getDay(), lastDayAssignment.getHours());
        }
        return null;
    }

    private static int estimatedWorkedHours(Integer totalHours, BigDecimal percentageWorked) {
        return (totalHours != null && percentageWorked != null) ? percentageWorked.multiply(
                new BigDecimal(totalHours)).intValue() : 0;
    }

    private static QueueTask createDivForElement(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {

        final Task task = queueElement.getResourceAllocation().getTask();
        final OrderElement order = getRootOrder(task);

        QueueTask result = new QueueTask(queueElement);
        String cssClass = "queue-element";
        result.setTooltiptext(createTooltiptext(queueElement));

        int startPixels = getStartPixels(datesMapper, queueElement);
        result.setLeft(forCSS(startPixels));
        if (startPixels < 0) {
            cssClass += " truncated-start ";
        }

        int taskWidth = getWidthPixels(datesMapper, queueElement);
        if ((startPixels + taskWidth) > datesMapper.getHorizontalSize()) {
            taskWidth = datesMapper.getHorizontalSize() - startPixels;
            cssClass += " truncated-end ";
        } else {
            result.appendChild(generateNonWorkableShade(datesMapper,
                    queueElement));
        }
        result.setWidth(forCSS(taskWidth));

        LocalDate deadlineDate = task.getDeadline();
        boolean isOrderDeadline = false;
        if (deadlineDate == null) {
            Date orderDate = order.getDeadline();
            if (orderDate != null) {
                deadlineDate = LocalDate.fromDateFields(orderDate);
                isOrderDeadline = true;
            }
        }
        if (deadlineDate != null) {
            int deadlinePosition = getDeadlinePixels(datesMapper, deadlineDate);
            if (deadlinePosition < datesMapper.getHorizontalSize()) {
                Div deadline = new Div();
                deadline.setSclass(isOrderDeadline ? "deadline order-deadline"
                        : "deadline");
                deadline
                        .setLeft((deadlinePosition - startPixels - DEADLINE_MARK_HALF_WIDTH)
                                + "px");
                result.appendChild(deadline);
                result.appendChild(generateNonWorkableShade(datesMapper,
                        queueElement));
            }
            if (deadlineDate.isBefore(queueElement.getEndDate())) {
                cssClass += " unmet-deadline ";
            }
        }

        result.setClass(cssClass);
        result.appendChild(generateCompletionShade(datesMapper, queueElement));
        Component progressBar = generateProgressBar(datesMapper, queueElement,
                task, startPixels);
        if (progressBar != null) {
            result.appendChild(progressBar);
        }
        return result;
    }

    private static Component generateProgressBar(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement, Task task,
            int startPixels) {
        DateAndHour advancementEndDate = getAdvanceEndDate(queueElement);
        if (advancementEndDate == null) {
            return null;
        }
        Duration durationBetween = new Duration(queueElement.getStartTime()
                .toDateTime().getMillis(), advancementEndDate.toDateTime().getMillis());
        Div progressBar = new Div();
        if (!queueElement.getStartDate().isEqual(advancementEndDate.getDate())) {
            progressBar.setWidth(datesMapper.toPixels(durationBetween) + "px");
            progressBar.setSclass("queue-progress-bar");
        }
        return progressBar;
    }

    private static Div generateNonWorkableShade(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {

        int workableHours = queueElement.getLimitingResourceQueue()
                .getResource().getCalendar()
                .getCapacityOn(PartialDay.wholeDay(queueElement.getEndDate()))
                .roundToHours();

        int shadeWidth = new Long((24 - workableHours)
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue();

        int shadeLeft = new Long((workableHours - queueElement.getEndHour())
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue()
                + shadeWidth;
        ;

        Div notWorkableHoursShade = new Div();
        notWorkableHoursShade
                .setTooltiptext(_("Workable capacity for this period ")
                        + workableHours + _(" hours"));

        notWorkableHoursShade.setContext("");
        notWorkableHoursShade.setSclass("not-workable-hours");

        notWorkableHoursShade.setStyle("left: " + shadeLeft + "px; width: "
                + shadeWidth + "px;");
        return notWorkableHoursShade;
    }

    private static Div generateCompletionShade(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {

        int workableHours = queueElement.getLimitingResourceQueue()
                .getResource().getCalendar()
                .getCapacityOn(PartialDay.wholeDay(queueElement.getEndDate()))
                .roundToHours();

        int shadeWidth = new Long((24 - workableHours)
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue();

        int shadeLeft = new Long((workableHours - queueElement.getEndHour())
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR
                / datesMapper.getMilisecondsPerPixel()).intValue()
                + shadeWidth;

        Div notWorkableHoursShade = new Div();
        notWorkableHoursShade.setContext("");
        notWorkableHoursShade.setSclass("limiting-completion");

        notWorkableHoursShade.setStyle("left: " + shadeLeft + "px; width: "
                + shadeWidth + "px;");
        return notWorkableHoursShade;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        return datesMapper.toPixels(queueElement.getLengthBetween());
    }

    private static int getDeadlinePixels(IDatesMapper datesMapper,
            LocalDate deadlineDate) {
        // Deadline date is considered inclusively
        return datesMapper.toPixelsAbsolute(deadlineDate.plusDays(1)
                .toDateMidnight().getMillis());
    }

    private static String forCSS(int pixels) {
        return String.format("%dpx", pixels);
    }

    private static int getStartPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        return datesMapper.toPixelsAbsolute((queueElement.getStartDate()
                .toDateMidnight().getMillis() + queueElement.getStartHour()
                * DatesMapperOnInterval.MILISECONDS_PER_HOUR));
    }

    public void appendQueueElements(SortedSet<LimitingResourceQueueElement> elements) {
        for (LimitingResourceQueueElement each : elements) {
            appendQueueElement(each);
        }
    }

    public void appendQueueElement(LimitingResourceQueueElement element) {
        QueueTask queueTask = createQueueTask(element);
        appendQueueTask(queueTask);
        appendMenu(queueTask);
        addDependenciesInPanel(element);
    }

    public void removeQueueElement(LimitingResourceQueueElement element) {
        QueueTask queueTask = findQueueTaskByElement(element);
        if (queueTask != null) {
            removeQueueTask(queueTask);
        }
    }

    private QueueTask findQueueTaskByElement(LimitingResourceQueueElement element) {
        for (QueueTask each: queueTasks) {
            if (each.getLimitingResourceQueueElement().getId().equals(element.getId())) {
                return each;
            }
        }
        return null;
    }

    private QueueTask createQueueTask(LimitingResourceQueueElement element) {
        validateQueueElement(element);
        return createDivForElement(timeTracker.getMapper(), element);
    }

    private void addDependenciesInPanel(LimitingResourceQueueElement element) {
        final LimitingResourcesPanel panel = getLimitingResourcesPanel();
        for (LimitingResourceQueueDependency each : element
                .getDependenciesAsDestiny()) {
            panel.addDependencyComponent(each);
        }
        for (LimitingResourceQueueDependency each : element
                .getDependenciesAsOrigin()) {
            panel.addDependencyComponent(each);
        }
    }

    public String getResourceName() {
        return limitingResourceQueue.getResource().getName();
    }

    private static void validateQueueElement(
            LimitingResourceQueueElement queueElement) {
        if ((queueElement.getStartDate() == null)
                || (queueElement.getEndDate() == null)) {
            throw new ValidationException(_("Invalid queue element"));
        }
    }

    private void appendMenu(QueueTask divElement) {
        if (divElement.getPage() != null) {
            MenuBuilder<QueueTask> menuBuilder = MenuBuilder.on(divElement
                    .getPage(), divElement);

            menuBuilder.item(_("Edit"), "/common/img/ico_editar.png",
                    new ItemAction<QueueTask>() {
                        @Override
                        public void onEvent(QueueTask choosen, Event event) {
                            editResourceAllocation(choosen);
                        }
                    });
            menuBuilder.item(_("Unassign"), "/common/img/ico_borrar.png",
                    new ItemAction<QueueTask>() {
                        @Override
                        public void onEvent(QueueTask choosen, Event event) {
                            unnasign(choosen);
                        }
                    });
            menuBuilder.item(_("Move"), "",
                    new ItemAction<QueueTask>() {
                        @Override
                        public void onEvent(QueueTask choosen, Event event) {
                            moveQueueTask(choosen);
                        }
                    });
            divElement.setContext(menuBuilder.createWithoutSettingContext());
        }
    }

    private void editResourceAllocation(QueueTask queueTask) {
        getLimitingResourcesPanel().editResourceAllocation(queueTask);
    }

    private void moveQueueTask(QueueTask queueTask) {
        getLimitingResourcesPanel().moveQueueTask(queueTask);
    }

    private void unnasign(QueueTask choosen) {
        getLimitingResourcesPanel().unschedule(choosen);
    }

    private void appendContextMenus() {
        for (QueueTask each : queueTasks) {
            appendMenu(each);
        }
    }

    @Override
    public void afterCompose() {
        appendContextMenus();
    }

}
