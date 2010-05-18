/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;

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
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.StartConstraintType;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskStartConstraint;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.DateConstraint;

/**
 * Responsible of adaptating a {@link TaskElement} into a
 * {@link ITaskFundamentalProperties} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TaskElementAdapter implements ITaskElementAdapter {

    private static final Log LOG = LogFactory.getLog(TaskElementAdapter.class);

    public static List<Constraint<Date>> getStartConstraintsFor(
            TaskElement taskElement) {
        if (taskElement instanceof Task) {
            Task task = (Task) taskElement;
            TaskStartConstraint startConstraint = task.getStartConstraint();
            final StartConstraintType constraintType = startConstraint
                    .getStartConstraintType();
            switch (constraintType) {
            case AS_SOON_AS_POSSIBLE:
                return Collections.emptyList();
            case START_IN_FIXED_DATE:
                return Collections.singletonList(DateConstraint
                        .equalTo(startConstraint.getConstraintDate()));
            case START_NOT_EARLIER_THAN:
                return Collections
                        .singletonList(DateConstraint
                                .biggerOrEqualThan(startConstraint
                                        .getConstraintDate()));
            default:
                throw new RuntimeException("can't handle " + constraintType);
            }
        } else if (taskElement.isMilestone()) {
            return Collections.singletonList(DateConstraint
                    .biggerOrEqualThan(taskElement.getStartDate()));
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
    private IResourceDAO resourceDAO;

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

    private class TaskElementWrapper implements ITaskFundamentalProperties {

        private final TaskElement taskElement;
        private final Scenario currentScenario;

        protected TaskElementWrapper(Scenario currentScenario,
                TaskElement taskElement) {
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
        public Date getBeginDate() {
            return taskElement.getStartDate();
        }

        @Override
        public long getLengthMilliseconds() {
            return taskElement.getLengthMilliseconds();
        }

        @Override
        public long setBeginDate(final Date beginDate) {
            Long runOnReadOnlyTransaction = transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Long>() {
                        @Override
                        public Long execute() {
                            stepsBeforePossibleReallocation();
                            Long result = setBeginDateInsideTransaction(beginDate);
                            return result;
                        }
                    });
            return runOnReadOnlyTransaction;
        }

        private void reattachAllResourcesForTask() {
            Set<Resource> resources = resourcesForTask();
            for (Resource each : resources) {
                resourceDAO.reattach(each);
            }
            for (Machine machine : Resource.machines(resources)) {
                Set<MachineWorkersConfigurationUnit> configurationUnits = machine
                        .getConfigurationUnits();
                for (MachineWorkersConfigurationUnit eachUnit : configurationUnits) {
                    Hibernate.initialize(eachUnit);
                }
            }
        }

        private Set<Resource> resourcesForTask() {
            Set<ResourceAllocation<?>> resourceAllocations = taskElement.getSatisfiedResourceAllocations();
            Set<Resource> resources = new HashSet<Resource>();
            for (ResourceAllocation<?> each : resourceAllocations) {
                resources.addAll(each.getAssociatedResources());
                for (DerivedAllocation derivedAllocation : each
                        .getDerivedAllocations()) {
                    resources
                            .addAll(derivedAllocation.getResources());
                }
            }
            return resources;
        }

        private Long setBeginDateInsideTransaction(final Date beginDate) {
            taskElement.moveTo(currentScenario, beginDate);
            return getLengthMilliseconds();
        }

        @Override
        public void setLengthMilliseconds(final long lengthMilliseconds) {
            transactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Void>() {
                        @Override
                        public Void execute() {
                            stepsBeforePossibleReallocation();
                            updateEndDate(lengthMilliseconds);
                            return null;
                        }
                    });
        }

        private void updateEndDate(long lengthMilliseconds) {
            Date endDate = new Date(getBeginDate().getTime()
                    + lengthMilliseconds);
            taskElement.resizeTo(currentScenario, endDate);
        }

        @Override
        public Date getHoursAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();
            Integer assignedHours = orderElementDAO
                    .getAssignedHours(orderElement);

            LocalDate date = calculateLimitDate(assignedHours);
            if (date == null) {
                Integer hours = 0;
                if (orderElement != null) {
                    hours = orderElement.getWorkHours();
                }

                if (hours == 0) {
                    return getBeginDate();
                } else {
                    BigDecimal percentage = new BigDecimal(assignedHours)
                            .setScale(2).divide(new BigDecimal(hours),
                                    RoundingMode.DOWN);
                    date = calculateLimitDate(percentage);
                }
            }

            return date.toDateTimeAtStartOfDay().toDate();
        }

        @Override
        public BigDecimal getHoursAdvancePercentage() {
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement != null) {
                return orderElementDAO.getHoursAdvancePercentage(orderElement);
            } else {
                return new BigDecimal(0);
            }
        }

        @Override
        public Date getAdvanceEndDate() {
            OrderElement orderElement = taskElement.getOrderElement();

            BigDecimal advancePercentage;
            Integer hours;
            if (orderElement != null) {
                advancePercentage = orderElement
                        .getAdvancePercentage();
                hours = taskElement.getTotalHoursAssigned();
            } else {
                advancePercentage = new BigDecimal(0);
                hours = Integer.valueOf(0);
            }

            Integer advanceHours = advancePercentage.multiply(
                    new BigDecimal(hours)).intValue();

            LocalDate date = calculateLimitDate(advanceHours);
            if (date == null) {
                date = calculateLimitDate(advancePercentage);
            }

            return date.toDateTimeAtStartOfDay().toDate();
        }

        private LocalDate calculateLimitDate(BigDecimal advancePercentage) {
            Long totalMillis = getLengthMilliseconds();
            Long advanceMillis = advancePercentage.multiply(
                    new BigDecimal(totalMillis)).longValue();
            return new LocalDate(getBeginDate().getTime() + advanceMillis);
        }

        @Override
        public BigDecimal getAdvancePercentage() {
            if (taskElement.getOrderElement() != null) {
                return taskElement.getOrderElement().getAdvancePercentage();
            }
            return new BigDecimal(0);
        }

        private LocalDate calculateLimitDate(Integer hours) {
            if (hours == null || hours == 0) {
                return null;
            }
            boolean limitReached = false;

            Integer count = 0;
            LocalDate lastDay = null;
            Integer hoursLastDay = 0;

            Map<LocalDate, Integer> daysMap = taskElement
                    .getHoursAssignedByDay();
            if (daysMap.isEmpty()) {
                return null;
            }
            for (Entry<LocalDate, Integer> entry : daysMap.entrySet()) {
                lastDay = entry.getKey();
                hoursLastDay = entry.getValue();
                count += hoursLastDay;
                if (count >= hours) {
                    limitReached = true;
                    break;
                }
            }

            if (!limitReached) {
                while (count < hours) {
                    count += hoursLastDay;
                    lastDay = lastDay.plusDays(1);
                }
            }

            return lastDay.plusDays(1);
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
            if (taskElement.getOrderElement() == null) {
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
            StringBuilder result = new StringBuilder();

            if (taskElement.getOrderElement() != null) {
                Set<Label> labels = getLabelsFromElementAndPredecesors(taskElement
                        .getOrderElement());

                if (!labels.isEmpty()) {
                    for (Label label : labels) {
                        result.append(label.getName()).append(",");
                    }
                    result.delete(result.length() - 1, result
                            .length());
                }
            }

            return result.toString();
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

        private String buildTooltipText() {
            StringBuilder result = new StringBuilder();
            result.append(_("Name: {0}", getName()) + "<br/>");
            result.append(_("Advance") + ": ").append(
                    getAdvancePercentage().multiply(new BigDecimal(100)))
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
        public List<Constraint<Date>> getStartConstraints() {
            return getStartConstraintsFor(this.taskElement);
        }

        @Override
        public void moveTo(Date date) {
            setBeginDate(date);
            if (taskElement instanceof Task) {
                Task task = (Task) taskElement;
                task.explicityMoved(date);
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
            reattachAllResourcesForTask();
        }

        @Override
        public boolean canBeExplicitlyResized() {
            return taskElement.canBeExplicitlyResized();
        }

        @Override
        public String getAssignedStatus() {
            return taskElement.getAssignedStatus();
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
