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

package org.navalplanner.web.planner;

import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.seconds;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.equalTo;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.lessOrEqualThan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.Fraction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Seconds;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ITaskPositionConstrained;
import org.navalplanner.business.planner.entities.PositionConstraintType;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation.Direction;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskElement.IDatesHandler;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskPositionConstraint;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourcesSearcher;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.IEffortFrom;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDate.Cases;
import org.zkoss.ganttz.data.GanttDate.CustomDate;
import org.zkoss.ganttz.data.GanttDate.LocalDateBased;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementAdapter {

    private static final Log LOG = LogFactory.getLog(TaskElementAdapter.class);

    public static List<Constraint<GanttDate>> getStartConstraintsFor(
            TaskElement taskElement, LocalDate orderInitDate) {
        if (taskElement instanceof ITaskPositionConstrained) {
            ITaskPositionConstrained task = (ITaskPositionConstrained) taskElement;
            TaskPositionConstraint startConstraint = task
                    .getPositionConstraint();
            final PositionConstraintType constraintType = startConstraint
                    .getConstraintType();
            switch (constraintType) {
            case AS_SOON_AS_POSSIBLE:
                if (orderInitDate != null) {
                    return Collections
                            .singletonList(biggerOrEqualThan(toGantt(orderInitDate)));
                }
                return Collections.emptyList();
            case START_IN_FIXED_DATE:
                return Collections
                        .singletonList(equalTo(toGantt(startConstraint
                                .getConstraintDate())));
            case START_NOT_EARLIER_THAN:
                return Collections
                        .singletonList(biggerOrEqualThan(toGantt(startConstraint
                                .getConstraintDate())));
            }
        }
        return Collections.emptyList();
    }

    public static List<Constraint<GanttDate>> getEndConstraintsFor(
            TaskElement taskElement, LocalDate deadline) {
        if (taskElement instanceof ITaskPositionConstrained) {
            ITaskPositionConstrained task = (ITaskPositionConstrained) taskElement;
            TaskPositionConstraint endConstraint = task.getPositionConstraint();
            PositionConstraintType type = endConstraint.getConstraintType();
            switch (type) {
            case AS_LATE_AS_POSSIBLE:
                if (deadline != null) {
                    return Collections
                            .singletonList(lessOrEqualThan(toGantt(deadline)));
                }
            case FINISH_NOT_LATER_THAN:
                GanttDate date = toGantt(endConstraint.getConstraintDate());
                return Collections.singletonList(lessOrEqualThan(date));
            }
        }
        return Collections.emptyList();
    }

    public static GanttDate toGantt(IntraDayDate date) {
        return toGantt(date, null);
    }

    public static GanttDate toGantt(IntraDayDate date,
            EffortDuration dayCapacity) {
        if (date == null) {
            return null;
        }
        if (dayCapacity == null) {
            // a sensible default
            dayCapacity = EffortDuration.hours(8);
        }
        return new GanttDateAdapter(date, dayCapacity);
    }

    public static GanttDate toGantt(LocalDate date) {
        if (date == null) {
            return null;
        }
        return GanttDate.createFrom(date);
    }

    public static IntraDayDate toIntraDay(GanttDate date) {
        if (date == null) {
            return null;
        }
        return date.byCases(new Cases<GanttDateAdapter, IntraDayDate>(
                GanttDateAdapter.class) {

            @Override
            public IntraDayDate on(LocalDateBased localDate) {
                return IntraDayDate.startOfDay(localDate.getLocalDate());
            }

            @Override
            protected IntraDayDate onCustom(GanttDateAdapter customType) {
                return customType.date;
            }
        });
    }

    public IAdapterToTaskFundamentalProperties<TaskElement> createForCompany(
            Scenario currentScenario) {
        Adapter result = new Adapter();
        result.useScenario(currentScenario);
        result.setPreventCalculateResourcesText(true);
        return result;
    }

    public IAdapterToTaskFundamentalProperties<TaskElement> createForOrder(
            Scenario currentScenario, Order order) {
        Adapter result = new Adapter();
        result.useScenario(currentScenario);
        result.setInitDate(asLocalDate(order.getInitDate()));
        result.setDeadline(asLocalDate(order.getDeadline()));
        return result;
    }

    private LocalDate asLocalDate(Date date) {
        return date != null ? LocalDate.fromDateFields(date) : null;
    }

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IResourcesSearcher searcher;

    @Autowired
    private IConfigurationDAO configurationDAO;

    static class GanttDateAdapter extends CustomDate {

        private static final int DAY_MILLISECONDS = (int) Days.days(1)
                .toStandardDuration().getMillis();

        private final IntraDayDate date;
        private final Duration workingDayDuration;

        GanttDateAdapter(IntraDayDate date, EffortDuration capacityForDay) {
            this.date = date;
            this.workingDayDuration = toMilliseconds(capacityForDay);
        }

        protected int compareToCustom(CustomDate customType) {
            if (customType instanceof GanttDateAdapter) {
                GanttDateAdapter other = (GanttDateAdapter) customType;
                return this.date.compareTo(other.date);
            }
            throw new RuntimeException("incompatible type: " + customType);
        }

        protected int compareToLocalDate(LocalDate localDate) {
            return this.date.compareTo(localDate);
        }

        public IntraDayDate getDate() {
            return date;
        }

        @Override
        public Date toDayRoundedDate() {
            return date.toDateTimeAtStartOfDay().toDate();
        }

        @Override
        public LocalDate toLocalDate() {
            return date.getDate();
        }

        @Override
        public LocalDate asExclusiveEnd() {
            return date.asExclusiveEnd();
        }

        @Override
        protected boolean isEqualsToCustom(CustomDate customType) {
            if (customType instanceof GanttDateAdapter) {
                GanttDateAdapter other = (GanttDateAdapter) customType;
                return this.date.equals(other.date);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return date.hashCode();
        }

        @Override
        public int toPixels(IDatesMapper datesMapper) {
            int pixesUntilDate = datesMapper.toPixels(this.date.getDate());
            EffortDuration effortDuration = date.getEffortDuration();
            Duration durationInDay = calculateDurationInDayFor(effortDuration);
            int pixelsInsideDay = datesMapper.toPixels(durationInDay);
            return pixesUntilDate + pixelsInsideDay;
        }

        private Duration calculateDurationInDayFor(EffortDuration effortDuration) {
            if (workingDayDuration.getStandardSeconds() == 0) {
                return Duration.ZERO;
            }
            Fraction fraction = fractionOfWorkingDayFor(effortDuration);
            try {
                return new Duration(fraction.multiplyBy(
                        Fraction.getFraction(DAY_MILLISECONDS, 1)).intValue());
            } catch (ArithmeticException e) {
                // if fraction overflows use floating point arithmetic
                return new Duration(
                        (int) (fraction.doubleValue() * DAY_MILLISECONDS));
            }
        }

        @SuppressWarnings("unchecked")
        private Fraction fractionOfWorkingDayFor(EffortDuration effortDuration) {
            Duration durationInDay = toMilliseconds(effortDuration);
            // cast to int is safe because there are not enough seconds in
            // day
            // to overflow
            Fraction fraction = Fraction.getFraction(
                    (int) durationInDay.getStandardSeconds(),
                    (int) workingDayDuration.getStandardSeconds());
            return (Fraction) Collections.min(Arrays.asList(fraction,
                    Fraction.ONE));
        }

        private static Duration toMilliseconds(EffortDuration duration) {
            return Seconds.seconds(duration.getSeconds()).toStandardDuration();
        }
    }

    /**
     * Responsible of adaptating a {@link TaskElement} into a
     * {@link ITaskFundamentalProperties} <br />
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     */
    public class Adapter implements
            IAdapterToTaskFundamentalProperties<TaskElement> {

        private Scenario scenario;

        private LocalDate initDate;

        private LocalDate deadline;

        private boolean preventCalculateResourcesText = false;

        private void useScenario(Scenario scenario) {
            this.scenario = scenario;
        }

        private void setInitDate(LocalDate initDate) {
            this.initDate = initDate;
        }

        private void setDeadline(LocalDate deadline) {
            this.deadline = deadline;
        }

        public boolean isPreventCalculateResourcesText() {
            return preventCalculateResourcesText;
        }

        public void setPreventCalculateResourcesText(
                boolean preventCalculateResourcesText) {
            this.preventCalculateResourcesText = preventCalculateResourcesText;
        }

        public Adapter() {
        }


        private class TaskElementWrapper implements ITaskFundamentalProperties {

            private final TaskElement taskElement;

            private final Scenario currentScenario;

            protected TaskElementWrapper(Scenario currentScenario,
                    TaskElement taskElement) {
                Validate.notNull(currentScenario);
                this.currentScenario = currentScenario;
                this.taskElement = taskElement;
            }

            @Override
            public void setName(String name) {
                taskElement.setName(name);
            }

            @Override
            public void setNotes(String notes) {
                taskElement.setNotes(notes);
            }

            @Override
            public String getName() {
                return taskElement.getName();
            }

            @Override
            public String getNotes() {
                return taskElement.getNotes();
            }

            @Override
            public GanttDate getBeginDate() {
                IntraDayDate start = taskElement.getIntraDayStartDate();
                return toGantt(start);
            }

            private GanttDate toGantt(IntraDayDate date) {
                BaseCalendar calendar = taskElement.getCalendar();
                if (calendar == null) {
                    return TaskElementAdapter.toGantt(date);
                }
                return TaskElementAdapter.toGantt(date, calendar
                        .getCapacityOn(PartialDay.wholeDay(date.getDate())));
            }

            @Override
            public void setBeginDate(final GanttDate beginDate) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                stepsBeforePossibleReallocation();
                                getDatesHandler(taskElement).moveTo(
                                        toIntraDay(beginDate));
                                return null;
                            }
                        });
            }

            @Override
            public GanttDate getEndDate() {
                return toGantt(taskElement.getIntraDayEndDate());
            }

            @Override
            public void setEndDate(final GanttDate endDate) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                stepsBeforePossibleReallocation();
                                getDatesHandler(taskElement).moveEndTo(
                                        toIntraDay(endDate));
                                return null;
                            }
                        });
            }

            @Override
            public void resizeTo(final GanttDate endDate) {
                transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                            @Override
                            public Void execute() {
                                stepsBeforePossibleReallocation();
                                updateTaskPositionConstraint(endDate);
                                getDatesHandler(taskElement).resizeTo(
                                        toIntraDay(endDate));
                                return null;
                            }
                        });
            }

            IDatesHandler getDatesHandler(TaskElement taskElement) {
                return taskElement.getDatesHandler(currentScenario, searcher);
            }

            private void updateTaskPositionConstraint(GanttDate endDate) {
                if (taskElement instanceof ITaskPositionConstrained) {
                    ITaskPositionConstrained task = (ITaskPositionConstrained) taskElement;
                    PositionConstraintType constraintType = task
                            .getPositionConstraint().getConstraintType();
                    if (constraintType
                            .compareTo(PositionConstraintType.FINISH_NOT_LATER_THAN) == 0
                            || constraintType
                                    .compareTo(PositionConstraintType.AS_LATE_AS_POSSIBLE) == 0) {
                        task.explicityMoved(toIntraDay(endDate));
                    }
                }
            }

            @Override
            public GanttDate getHoursAdvanceEndDate() {
                OrderElement orderElement = taskElement.getOrderElement();

                EffortDuration assignedEffort = EffortDuration.zero();
                if (orderElement.getSumChargedEffort() != null) {
                    assignedEffort = orderElement.getSumChargedEffort()
                            .getTotalChargedEffort();
                }

                GanttDate result = null;
                if (!(taskElement instanceof TaskGroup)) {
                    result = calculateLimitDateByHours(assignedEffort
                            .toHoursAsDecimalWithScale(2).intValue());
                }

                if (result == null) {
                    Integer hours = taskElement.getSumOfHoursAllocated();

                    if (hours == 0) {
                        hours = orderElement.getWorkHours();
                        if (hours == 0) {
                            return getBeginDate();
                        }
                    }
                    BigDecimal percentage = assignedEffort
                            .toHoursAsDecimalWithScale(2).divide(
                                    new BigDecimal(hours), RoundingMode.DOWN);
                    result = calculateLimitDateByPercentage(percentage);

                }

                return result;
            }

            @Override
            public BigDecimal getHoursAdvancePercentage() {
                OrderElement orderElement = taskElement.getOrderElement();
                if (orderElement == null) {
                    return BigDecimal.ZERO;
                }

                EffortDuration totalChargedEffort = orderElement
                        .getSumChargedEffort() != null ? orderElement
                        .getSumChargedEffort().getTotalChargedEffort()
                        : EffortDuration.zero();
                BigDecimal assignedHours = totalChargedEffort
                        .toHoursAsDecimalWithScale(2);

                BigDecimal estimatedHours = new BigDecimal(
                        taskElement.getSumOfHoursAllocated()).setScale(2);

                if (estimatedHours.compareTo(BigDecimal.ZERO) <= 0) {
                    estimatedHours = new BigDecimal(orderElement.getWorkHours())
                            .setScale(2);
                    if (estimatedHours.compareTo(BigDecimal.ZERO) <= 0) {
                        return BigDecimal.ZERO;
                    }
                }
                return assignedHours.divide(estimatedHours, RoundingMode.DOWN);
            }

            @Override
            public GanttDate getAdvanceEndDate(String progressType) {
                return getAdvanceEndDate(ProgressType.asEnum(progressType));
            }

            private GanttDate getAdvanceEndDate(ProgressType progressType) {
                BigDecimal advancePercentage = BigDecimal.ZERO;
                if (taskElement.getOrderElement() != null) {
                    advancePercentage = taskElement
                            .getAdvancePercentage(progressType);
                }
                return getAdvanceEndDate(advancePercentage);
            }

            @Override
            public GanttDate getAdvanceEndDate() {
                BigDecimal advancePercentage = BigDecimal.ZERO;

                if (taskElement.getOrderElement() != null) {
                    if (isTaskRoot(taskElement)) {
                        ProgressType progressType = getProgressTypeFromConfiguration();
                        advancePercentage = taskElement
                                .getAdvancePercentage(progressType);

                    } else {
                        advancePercentage = taskElement.getAdvancePercentage();
                    }
                }
                return getAdvanceEndDate(advancePercentage);
            }

            private boolean isTaskRoot(TaskElement taskElement) {
                return taskElement instanceof TaskGroup
                        && taskElement.getParent() == null;
            }

            private ProgressType getProgressTypeFromConfiguration() {
                return transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<ProgressType>() {
                            @Override
                            public ProgressType execute() {
                                return configurationDAO.getConfiguration()
                                        .getProgressType();
                            }
                        });
            }

            private GanttDate getAdvanceEndDate(BigDecimal advancePercentage) {
                Integer hours = Integer.valueOf(0);
                if (taskElement.getOrderElement() != null) {
                    hours = taskElement.getSumOfHoursAllocated();
                }

                if (taskElement instanceof TaskGroup) {
                    return calculateLimitDateByPercentage(advancePercentage);
                }

                // Calculate date according to advanceHours or advancePercentage
                final Integer advanceHours = advancePercentage.multiply(
                        new BigDecimal(hours)).intValue();
                GanttDate result = calculateLimitDateByHours(advanceHours);
                if (result == null) {
                    result = calculateLimitDateByPercentage(advancePercentage);
                }
                return result;
            }

            private GanttDate calculateLimitDateByPercentage(BigDecimal advancePercentage) {
                BaseCalendar calendar = taskElement.getCalendar();
                if (advancePercentage.compareTo(BigDecimal.ZERO) == 0
                        || calendar == null) {
                    return getBeginDate();
                }
                IntraDayDate start = taskElement.getIntraDayStartDate();
                IntraDayDate end = taskElement.getIntraDayEndDate();
                int daysBetween = start.numberOfDaysUntil(end);
                if (daysBetween == 0) {
                    return calculateLimitDateWhenDaysBetweenAreZero(advancePercentage);
                }
                int daysAdvance = advancePercentage.multiply(
                        new BigDecimal(daysBetween)).intValue();
                return GanttDate.createFrom(taskElement.getIntraDayStartDate()
                        .getDate().plusDays(daysAdvance));
            }

            private GanttDate calculateLimitDateWhenDaysBetweenAreZero(
                    BigDecimal advancePercentage) {
                IntraDayDate start = taskElement.getIntraDayStartDate();
                IntraDayDate end = taskElement.getIntraDayEndDate();
                final BaseCalendar calendar = taskElement.getCalendar();
                Iterable<PartialDay> daysUntil = start.daysUntil(end);

                EffortDuration total = EffortDuration.sum(daysUntil,
                        new IEffortFrom<PartialDay>() {
                            @Override
                            public EffortDuration from(PartialDay each) {
                                return calendar.getCapacityOn(each);
                            }
                        });

                BigDecimal totalAsSeconds = new BigDecimal(total.getSeconds());
                EffortDuration advanceLeft = seconds(advancePercentage
                        .multiply(totalAsSeconds).intValue());
                for (PartialDay each : daysUntil) {
                    if (advanceLeft.compareTo(calendar.getCapacityOn(each)) <= 0) {
                        LocalDate dayDate = each.getStart().getDate();
                        if (dayDate.equals(start.getDate())) {
                            return toGantt(IntraDayDate
                                    .create(dayDate, advanceLeft.plus(start
                                            .getEffortDuration())));
                        }
                        return toGantt(IntraDayDate
                                .create(dayDate, advanceLeft));
                    }
                    advanceLeft = advanceLeft.minus(calendar
                            .getCapacityOn(each));
                }
                return toGantt(end);
            }

            private GanttDate calculateLimitDateByHours(Integer hours) {
                if (hours == null || hours == 0) {
                    return null;
                }
                EffortDuration hoursLeft = hours(hours);
                IntraDayDate result = null;
                EffortDuration effortLastDayNotZero = zero();

                Map<LocalDate, EffortDuration> daysMap = taskElement
                        .getDurationsAssignedByDay();
                if (daysMap.isEmpty()) {
                    return null;
                }
                for (Entry<LocalDate, EffortDuration> entry : daysMap
                        .entrySet()) {
                    if (!entry.getValue().isZero()) {
                        effortLastDayNotZero = entry.getValue();
                    }
                    EffortDuration decrement = min(entry.getValue(), hoursLeft);
                    hoursLeft = hoursLeft.minus(decrement);
                    if (hoursLeft.isZero()) {
                        if (decrement.equals(entry.getValue())) {
                            result = IntraDayDate.startOfDay(entry.getKey()
                                    .plusDays(1));
                        } else {
                            result = IntraDayDate.create(entry.getKey(),
                                    decrement);
                        }
                        break;
                    } else {
                        result = IntraDayDate.startOfDay(entry.getKey()
                                .plusDays(1));
                    }
                }
                if (!hoursLeft.isZero() && effortLastDayNotZero.isZero()) {
                    LOG.warn("limit not reached and no day with effort not zero");
                }
                if (!hoursLeft.isZero() && !effortLastDayNotZero.isZero()) {
                    while (!hoursLeft.isZero()) {
                        hoursLeft = hoursLeft.minus(min(effortLastDayNotZero,
                                hoursLeft));
                        result = result.nextDayAtStart();
                    }
                }
                if (result == null) {
                    return null;
                }
                return toGantt(result);
            }

            @Override
            public String getTooltipText() {
                if (taskElement.isMilestone()) {
                    return "";
                }
                return transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<String>() {

                            @Override
                            public String execute() {
                                orderElementDAO.reattach(taskElement
                                        .getOrderElement());
                                return buildTooltipText();
                            }
                        });
            }

            @Override
            public String getLabelsText() {
                if (taskElement.isMilestone()) {
                    return "";
                }
                return transactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<String>() {

                            @Override
                            public String execute() {
                                orderElementDAO.reattach(taskElement
                                        .getOrderElement());
                                return buildLabelsText();
                            }
                        });
            }

            @Override
            public String getResourcesText() {
                if (isPreventCalculateResourcesText()
                        || taskElement.getOrderElement() == null) {
                    return "";
                }
                try {
                    return transactionService
                            .runOnAnotherReadOnlyTransaction(new IOnTransaction<String>() {

                                @Override
                                public String execute() {
                                    orderElementDAO.reattach(taskElement
                                            .getOrderElement());
                                    return buildResourcesText();
                                }
                            });
                } catch (Exception e) {
                    LOG.error("error calculating resources text", e);
                    return "";
                }
            }

            private Set<Label> getLabelsFromElementAndPredecesors(
                    OrderElement order) {
                if (order != null) {
                    if (order.getParent() == null) {
                        return order.getLabels();
                    } else {
                        HashSet<Label> labels = new HashSet<Label>(
                                order.getLabels());
                        labels.addAll(getLabelsFromElementAndPredecesors(order
                                .getParent()));
                        return labels;
                    }
                }
                return new HashSet<Label>();
            }

            private String buildLabelsText() {
                List<String> result = new ArrayList<String>();

                if (taskElement.getOrderElement() != null) {
                    Set<Label> labels = getLabelsFromElementAndPredecesors(taskElement
                            .getOrderElement());

                    for (Label label : labels) {
                        String representation = label.getName();
                        if (!result.contains(representation)) {
                            result.add(representation);
                        }
                    }
                }

                Collections.sort(result);
                return StringUtils.join(result, ", ");
            }

            private String buildResourcesText() {
                List<String> result = new ArrayList<String>();
                for (ResourceAllocation<?> each : taskElement
                        .getSatisfiedResourceAllocations()) {
                    if (each instanceof SpecificResourceAllocation) {
                        for (Resource r : each.getAssociatedResources()) {
                            String representation = r.getName();
                            if (!result.contains(representation)) {
                                result.add(representation);
                            }
                        }
                    } else {
                        String representation = extractRepresentationForGeneric((GenericResourceAllocation) each);
                        if (!result.contains(representation)) {
                            result.add(representation);
                        }
                    }
                }
                Collections.sort(result);
                return StringUtils.join(result, ", ");
            }

            private String extractRepresentationForGeneric(
                    GenericResourceAllocation generic) {
                if (!generic.isNewObject()) {
                    resourceAllocationDAO.reattach(generic);
                }
                Set<Criterion> criterions = generic.getCriterions();
                List<String> forCriterionRepresentations = new ArrayList<String>();
                if (!criterions.isEmpty()) {
                    for (Criterion c : criterions) {
                        criterionDAO.reattachUnmodifiedEntity(c);
                        forCriterionRepresentations.add(c.getName());
                    }
                } else {
                    forCriterionRepresentations.add((_("All workers")));
                }
                return "["
                        + StringUtils.join(forCriterionRepresentations, ", ")
                        + "]";
            }

            @Override
            public String updateTooltipText() {
                return buildTooltipText();
            }

            @Override
            public String updateTooltipText(String progressType) {
                return buildTooltipText(ProgressType.asEnum(progressType));
            }

            @Override
            public BigDecimal getAdvancePercentage() {
                if (taskElement != null) {
                    return taskElement.getAdvancePercentage();
                }
                return new BigDecimal(0);
            }

            private String buildTooltipText() {
                return buildTooltipText(asPercentage(taskElement
                        .getAdvancePercentage()));
            }

            private BigDecimal asPercentage(BigDecimal value) {
                return value.multiply(BigDecimal.valueOf(100)).setScale(2,
                        RoundingMode.DOWN);
            }

            private String buildTooltipText(BigDecimal progressPercentage) {
                StringBuilder result = new StringBuilder();
                result.append(_("Name: {0}", getName()) + "<br/>");
                result.append(_("Progress") + ": ").append(progressPercentage)
                        .append("% , ");

                result.append(_("Hours invested") + ": ")
                        .append(getHoursAdvancePercentage().multiply(
                                new BigDecimal(100))).append("% <br/>");
                if (taskElement.getOrderElement() instanceof Order) {
                    result.append(_("State") + ": ").append(getOrderState());
                }
                String labels = buildLabelsText();
                if (!labels.equals("")) {
                    result.append("<div class='tooltip-labels'>" + _("Labels")
                            + ": " + labels + "</div>");
                }
                return result.toString();
            }

            private String buildTooltipText(ProgressType progressType) {
                return buildTooltipText(asPercentage(taskElement
                        .getAdvancePercentage(progressType)));
            }

            private String getOrderState() {
                String cssClass;
                OrderStatusEnum state = taskElement.getOrderElement()
                        .getOrder().getState();

                if (Arrays.asList(OrderStatusEnum.ACCEPTED,
                        OrderStatusEnum.OFFERED, OrderStatusEnum.STARTED,
                        OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER).contains(
                        state)) {
                    if (taskElement.getAssignedStatus() == "assigned") {
                        cssClass = "order-open-assigned";
                    } else {
                        cssClass = "order-open-unassigned";
                    }
                } else {
                    cssClass = "order-closed";
                }
                return "<font class='" + cssClass + "'>" + state.toString()
                        + "</font>";
            }

            @Override
            public List<Constraint<GanttDate>> getStartConstraints() {
                return getStartConstraintsFor(this.taskElement, initDate);
            }

            @Override
            public List<Constraint<GanttDate>> getEndConstraints() {
                return getEndConstraintsFor(this.taskElement, deadline);
            }

            @Override
            public List<Constraint<GanttDate>> getCurrentLengthConstraint() {
                if (taskElement instanceof Task) {
                    Task task = (Task) taskElement;
                    if (task.getAllocationDirection() == Direction.FORWARD) {
                        return Collections
                                .singletonList(biggerOrEqualThan(getEndDate()));
                    }
                }
                return Collections.emptyList();
            }

            @Override
            public void moveTo(GanttDate newStart) {
                if (taskElement instanceof ITaskPositionConstrained) {
                    ITaskPositionConstrained task = (ITaskPositionConstrained) taskElement;
                    if (task.getPositionConstraint()
                            .isConstraintAppliedToStart()) {
                        setBeginDate(newStart);
                        task.explicityMoved(toIntraDay(newStart));
                    } else {
                        GanttDate newEnd = inferEndFrom(newStart);
                        setEndDate(newEnd);
                        task.explicityMoved(toIntraDay(newEnd));
                    }
                }
            }

            private GanttDate inferEndFrom(GanttDate newStart) {
                if (taskElement instanceof Task) {
                    Task task = (Task) taskElement;
                    return toGantt(task
                            .calculateEndKeepingLength(toIntraDay(newStart)));
                }
                return newStart;
            }

            @Override
            public Date getDeadline() {
                LocalDate deadline = taskElement.getDeadline();
                if (deadline == null) {
                    return null;
                }
                return deadline.toDateTimeAtStartOfDay().toDate();
            }

            @Override
            public void setDeadline(Date date) {
                if (date != null) {
                    taskElement.setDeadline(LocalDate.fromDateFields(date));
                } else {
                    taskElement.setDeadline(null);
                }
            }

            @Override
            public GanttDate getConsolidatedline() {
                if (!taskElement.isLeaf() || !taskElement.hasConsolidations()) {
                    return null;
                }
                LocalDate consolidatedline = ((Task) taskElement)
                        .getFirstDayNotConsolidated().getDate();
                return TaskElementAdapter.toGantt(consolidatedline);
            }

            @Override
            public boolean isSubcontracted() {
                return taskElement.isSubcontracted();
            }

            @Override
            public boolean isLimiting() {
                return taskElement.isLimiting();
            }

            @Override
            public boolean isLimitingAndHasDayAssignments() {
                return taskElement.isLimitingAndHasDayAssignments();
            }

            public boolean hasConsolidations() {
                return taskElement.hasConsolidations();
            }

            private void stepsBeforePossibleReallocation() {
                taskDAO.reattach(taskElement);
            }

            @Override
            public boolean canBeExplicitlyResized() {
                return taskElement.canBeExplicitlyResized();
            }

            @Override
            public String getAssignedStatus() {
                return taskElement.getAssignedStatus();
            }

            @Override
            public boolean isFixed() {
                return taskElement.isLimitingAndHasDayAssignments()
                        || taskElement.hasConsolidations();
            }

        }

        @Override
        public ITaskFundamentalProperties adapt(final TaskElement taskElement) {
            return new TaskElementWrapper(scenario, taskElement);
        }

        @Override
        public List<DomainDependency<TaskElement>> getIncomingDependencies(
                TaskElement taskElement) {
            return toDomainDependencies(taskElement
                    .getDependenciesWithThisDestination());
        }

        @Override
        public List<DomainDependency<TaskElement>> getOutcomingDependencies(
                TaskElement taskElement) {
            return toDomainDependencies(taskElement
                    .getDependenciesWithThisOrigin());
        }

        private List<DomainDependency<TaskElement>> toDomainDependencies(
                Collection<? extends Dependency> dependencies) {
            List<DomainDependency<TaskElement>> result = new ArrayList<DomainDependency<TaskElement>>();
            for (Dependency dependency : dependencies) {
                result.add(DomainDependency.createDependency(
                        dependency.getOrigin(), dependency.getDestination(),
                        toGanntType(dependency.getType())));
            }
            return result;
        }

        private DependencyType toGanntType(Type type) {
            switch (type) {
            case END_START:
                return DependencyType.END_START;
            case START_END:
                return DependencyType.START_END;
            case START_START:
                return DependencyType.START_START;
            case END_END:
                return DependencyType.END_END;
            default:
                throw new RuntimeException(_("{0} not supported yet", type));
            }
        }

        private Type toDomainType(DependencyType type) {
            switch (type) {
            case END_START:
                return Type.END_START;
            case START_END:
                return Type.START_END;
            case START_START:
                return Type.START_START;
            case END_END:
                return Type.END_END;
            default:
                throw new RuntimeException(_("{0} not supported yet", type));
            }
        }

        @Override
        public void addDependency(DomainDependency<TaskElement> dependency) {
            TaskElement source = dependency.getSource();
            TaskElement destination = dependency.getDestination();
            Type domainType = toDomainType(dependency.getType());
            Dependency.create(source, destination, domainType);
        }

        @Override
        public boolean canAddDependency(DomainDependency<TaskElement> dependency) {
            return true;
        }

        @Override
        public void removeDependency(DomainDependency<TaskElement> dependency) {
            TaskElement source = dependency.getSource();
            Type type = toDomainType(dependency.getType());
            source.removeDependencyWithDestination(dependency.getDestination(),
                    type);
        }

        @Override
        public void doRemovalOf(TaskElement taskElement) {
            taskElement.detach();
            TaskGroup parent = taskElement.getParent();
            if (parent != null) {
                parent.remove(taskElement);
            }
        }

    }
}
