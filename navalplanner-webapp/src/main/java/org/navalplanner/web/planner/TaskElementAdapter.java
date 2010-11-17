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

package org.navalplanner.web.planner;

import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.seconds;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.equalTo;

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
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ITaskLeafConstraint;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation.Direction;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.StartConstraintType;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskStartConstraint;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDate.Cases;
import org.zkoss.ganttz.data.GanttDate.CustomDate;
import org.zkoss.ganttz.data.GanttDate.LocalDateBased;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.constraint.Constraint;
/**
 * Responsible of adaptating a {@link TaskElement} into a
 * {@link ITaskFundamentalProperties} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TaskElementAdapter implements ITaskElementAdapter {

    private static final Log LOG = LogFactory.getLog(TaskElementAdapter.class);

    private boolean preventCalculateResourcesText = false;

    public boolean isPreventCalculateResourcesText() {
        return preventCalculateResourcesText;
    }

    public void setPreventCalculateResourcesText(
            boolean preventCalculateResourcesText) {
        this.preventCalculateResourcesText = preventCalculateResourcesText;
    }

    public static List<Constraint<GanttDate>> getStartConstraintsFor(
            TaskElement taskElement) {
        if (taskElement instanceof ITaskLeafConstraint) {
            ITaskLeafConstraint task = (ITaskLeafConstraint) taskElement;
            TaskStartConstraint startConstraint = task.getStartConstraint();
            final StartConstraintType constraintType = startConstraint
                    .getStartConstraintType();
            switch (constraintType) {
            case AS_SOON_AS_POSSIBLE:
                return Collections.emptyList();
            case START_IN_FIXED_DATE:
                return Collections
                        .singletonList(equalTo(toGantt(startConstraint
                        .getConstraintDate())));
            case START_NOT_EARLIER_THAN:
                return Collections
                        .singletonList(biggerOrEqualThan(toGantt(startConstraint
                                .getConstraintDate())));
            default:
                throw new RuntimeException("can't handle " + constraintType);
            }
        } else {
            return Collections.emptyList();
        }
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

    private Scenario scenario;


    @Override
    public void useScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public TaskElementAdapter() {
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

    public static LocalDate toLocalDate(GanttDate date) {
        if (date == null) {
            return null;
        }
        return toIntraDay(date).getDate();
    }

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
            return new Duration(fraction.multiplyBy(
                    Fraction.getFraction(DAY_MILLISECONDS, 1)).intValue());
        }

        @SuppressWarnings("unchecked")
        private Fraction fractionOfWorkingDayFor(EffortDuration effortDuration) {
            Duration durationInDay = toMilliseconds(effortDuration);
            // cast to int is safe because there are not enough seconds in day
            // to overflow
            Fraction fraction = Fraction.getFraction(
                    (int) durationInDay.getStandardSeconds(),
                    (int) workingDayDuration.getStandardSeconds());
            return (Fraction) Collections.min(Arrays
                    .asList(fraction, Fraction.ONE));
        }

        private static Duration toMilliseconds(EffortDuration duration) {
            return Seconds.seconds(duration.getSeconds()).toStandardDuration();
        }
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
            return TaskElementAdapter
                    .toGantt(date, calendar.getCapacityOn(PartialDay
                            .wholeDay(date.getDate())));
        }

        @Override
        public void setBeginDate(final GanttDate beginDate) {
            transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                        @Override
                        public Void execute() {
                            stepsBeforePossibleReallocation();
                            taskElement.moveTo(currentScenario,
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
                            taskElement.moveEndTo(currentScenario,
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
                            taskElement.resizeTo(currentScenario,
                                    toIntraDay(endDate));
                            return null;
                        }
                    });
        }

        @Override
        public GanttDate getHoursAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();

            Integer assignedHours = 0;
            if (orderElement.getSumChargedHours() != null) {
                assignedHours = orderElement.getSumChargedHours()
                        .getTotalChargedHours();
            }

            GanttDate result = null;
            if(!(taskElement instanceof TaskGroup)) {
                result = calculateLimitDate(assignedHours);
            }
            if (result == null) {
                Integer hours = taskElement.getSumOfHoursAllocated();

                if (hours == 0) {
                    hours = orderElement.getWorkHours();
                    if (hours == 0) {
                        return getBeginDate();
                    }
                }
                BigDecimal percentage = new BigDecimal(assignedHours)
                        .setScale(2).divide(new BigDecimal(hours),
                                RoundingMode.DOWN);
                result = calculateLimitDate(percentage);

            }

            return result;
        }

        @Override
        public BigDecimal getHoursAdvancePercentage() {
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement == null) {
                return BigDecimal.ZERO;
            }

            Integer totalChargedHours = orderElement.getSumChargedHours() != null ? orderElement
                    .getSumChargedHours().getTotalChargedHours() : new Integer(0);
            BigDecimal assignedHours = new BigDecimal(totalChargedHours).setScale(2);

            BigDecimal estimatedHours = new BigDecimal(taskElement.getSumOfHoursAllocated())
                    .setScale(2);

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
        public GanttDate getAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();

            BigDecimal advancePercentage;
            Integer hours;
            if (orderElement != null) {
                advancePercentage = taskElement.getAdvancePercentage();
                hours = taskElement.getSumOfHoursAllocated();
            } else {
                advancePercentage = new BigDecimal(0);
                hours = Integer.valueOf(0);
            }

            Integer advanceHours = advancePercentage.multiply(
                    new BigDecimal(hours)).intValue();

            GanttDate result;
            if(taskElement instanceof TaskGroup) {
                result = calculateLimitDate(advancePercentage);
            }
            else {
                result = calculateLimitDate(advanceHours);
                if (result == null) {
                    result = calculateLimitDate(advancePercentage);
                }
            }

            return result;
        }

        private GanttDate calculateLimitDate(BigDecimal advancePercentage) {
            BaseCalendar calendar = taskElement.getCalendar();
            if (advancePercentage.compareTo(BigDecimal.ZERO) == 0 || calendar==null) {
                return getBeginDate();
            }
            IntraDayDate start = taskElement.getIntraDayStartDate();
            IntraDayDate end = taskElement.getIntraDayEndDate();
            int daysBetween = start.numberOfDaysUntil(end);
            if (daysBetween == 0) {
                return calculateLimitDateWhenDaysBetweenAreZero(advancePercentage);
            }
            int daysAdvance = advancePercentage.multiply(
                    new BigDecimal(daysBetween))
                    .intValue();
            return GanttDate.createFrom(taskElement.getIntraDayStartDate()
                    .getDate().plusDays(daysAdvance));
        }

        private GanttDate calculateLimitDateWhenDaysBetweenAreZero(
                BigDecimal advancePercentage) {
            IntraDayDate start = taskElement.getIntraDayStartDate();
            IntraDayDate end = taskElement.getIntraDayEndDate();
            BaseCalendar calendar = taskElement.getCalendar();
            Iterable<PartialDay> daysUntil = start.daysUntil(end);
            EffortDuration total = zero();
            for (PartialDay each : daysUntil) {
                total = total.plus(calendar.getCapacityOn(each));
            }
            BigDecimal totalAsSeconds = new BigDecimal(total
                    .getSeconds());
            EffortDuration advanceLeft = seconds(advancePercentage.multiply(
                    totalAsSeconds).intValue());
            for (PartialDay each : daysUntil) {
                if (advanceLeft.compareTo(calendar.getCapacityOn(each)) <= 0) {
                    LocalDate dayDate = each.getStart().getDate();
                    if (dayDate.equals(start.getDate())) {
                        return toGantt(IntraDayDate.create(dayDate,
                                advanceLeft.plus(start.getEffortDuration())));
                    }
                    return toGantt(IntraDayDate.create(dayDate, advanceLeft));
                }
                advanceLeft = advanceLeft.minus(calendar.getCapacityOn(each));
            }
            return toGantt(end);
        }

        @Override
        public BigDecimal getAdvancePercentage() {
            if (taskElement != null) {
                return taskElement.getAdvancePercentage();
            }
            return new BigDecimal(0);
        }

        private GanttDate calculateLimitDate(Integer hours) {
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
            for (Entry<LocalDate, EffortDuration> entry : daysMap.entrySet()) {
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
                        result = IntraDayDate.create(entry.getKey(), decrement);
                    }
                    break;
                } else {
                    result = IntraDayDate
                            .startOfDay(entry.getKey().plusDays(1));
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
            if (isPreventCalculateResourcesText() ||
                    taskElement.getOrderElement() == null) {
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
                    HashSet<Label> labels = new HashSet<Label>(order
                            .getLabels());
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
                    String representation =
                        extractRepresentationForGeneric((GenericResourceAllocation) each);
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
                    + StringUtils.join(forCriterionRepresentations,
                            ", ") + "]";
        }

        @Override
        public String updateTooltipText() {
            return buildTooltipText();
        }

        private String buildTooltipText() {
            StringBuilder result = new StringBuilder();
            result.append(_("Name: {0}", getName()) + "<br/>");
            result.append(_("Advance") + ": ").append(
                    (getAdvancePercentage().multiply(new BigDecimal(100)))
                            .setScale(2, RoundingMode.DOWN))
                    .append("% , ");

            result.append(_("Hours invested") + ": ").append(
                    getHoursAdvancePercentage().multiply(new BigDecimal(100)))
                    .append("% <br/>");
            result.append(_("State")  +": ").append(getOrderState());
            String labels = buildLabelsText();
            if (!labels.equals("")) {
                result.append("<div class='tooltip-labels'>" + _("Labels")
                        + ": " + labels + "</div>");
            }
            return result.toString();
        }

        private String getOrderState() {
            String cssClass;
            OrderStatusEnum state = taskElement.getOrderElement().getOrder().getState();

            if(Arrays.asList(OrderStatusEnum.ACCEPTED,
                    OrderStatusEnum.OFFERED,OrderStatusEnum.STARTED,
                    OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER)
                    .contains(state)) {
                if(taskElement.getAssignedStatus() == "assigned") {
                    cssClass="order-open-assigned";
                }
                else {
                    cssClass="order-open-unassigned";
                }
            }
            else {
                cssClass="order-closed";
            }
            return "<font class='" + cssClass + "'>"
                + state.toString()
                + "</font>";
        }

        @Override
        public List<Constraint<GanttDate>> getStartConstraints() {
            return getStartConstraintsFor(this.taskElement);
        }

        @Override
        public List<Constraint<GanttDate>> getCurrentLengthConstraint() {
            if (taskElement instanceof Task) {
                Task task = (Task) taskElement;
                if (task.getLastAllocationDirection() == Direction.FORWARD) {
                    return Collections
                            .singletonList(biggerOrEqualThan(getEndDate()));
                }
            }
            return Collections.emptyList();
        }

        @Override
        public void moveTo(GanttDate date) {
            setBeginDate(date);
            if (taskElement instanceof ITaskLeafConstraint) {
                ITaskLeafConstraint task = (ITaskLeafConstraint) taskElement;
                task.explicityMoved(toLocalDate(date));
            }
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
                    dependency.getOrigin(),
                    dependency.getDestination(), toGanntType(dependency
                            .getType())));
        }
        return result;
    }

    private DependencyType toGanntType(Type type) {
        switch (type) {
        case END_START:
            return DependencyType.END_START;
        case START_START:
            return DependencyType.START_START;
        case END_END:
            return DependencyType.END_END;
        case START_END:
        default:
            throw new RuntimeException(_("{0} not supported yet", type));
        }
    }

    private Type toDomainType(DependencyType type) {
        switch (type) {
        case END_START:
            return Type.END_START;
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

}
