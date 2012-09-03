/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.orders.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.cashflow.entities.CashflowType;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.externalcompanies.entities.DeadlineCommunication;
import org.libreplan.business.externalcompanies.entities.DeliverDateComparator;
import org.libreplan.business.externalcompanies.entities.EndDateCommunication;
import org.libreplan.business.externalcompanies.entities.EndDateCommunicationComparator;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.templates.entities.Budget;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.util.deepcopy.DeepCopy;

/**
 * It represents an {@link Order} with its related information. <br />
 *
 * Every project in the application is an {@link Order}.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Order extends OrderLineGroup implements Comparable {

    public static Order create() {
        Order order = new Order();
        order.setNewObject(true);
        return order;
    }

    public static Order createUnvalidated(String code) {
        Order order = create(new Order(), code);
        return order;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Order() {

    }

    private String responsible;

    private Boolean dependenciesConstraintsHavePriority;

    private BaseCalendar calendar;

    private Integer lastOrderElementSequenceCode = 0;

    private BigDecimal workBudget = BigDecimal.ZERO.setScale(2);

    private BigDecimal materialsBudget = BigDecimal.ZERO.setScale(2);

    private Integer totalHours = 0;

    private OrderStatusEnum state = OrderStatusEnum.getDefault();

    private ExternalCompany customer;

    private String customerReference;

    private Map<Scenario, OrderVersion> scenarios = new HashMap<Scenario, OrderVersion>();

    private Set<OrderAuthorization> orderAuthorizations = new HashSet<OrderAuthorization>();

    private CurrentVersionInfo currentVersionInfo;

    private Set<FilmingProgress> filmingProgressSet = new HashSet<FilmingProgress>();

    private Set<CustomerCommunication> customerCommunications = new HashSet<CustomerCommunication>();

    private Budget associatedBudgetObject;

    /**
     * If <code>null</code> the project won't manage cashflow. If a
     * {@link CashflowType} is set, this will be the default type for all the
     * project tasks.
     */
    private CashflowType cashflowType;

    private Integer cashflowDelayDays = 0;

    @Valid
    private SortedSet<DeadlineCommunication> deliveringDates = new TreeSet<DeadlineCommunication>(
            new DeliverDateComparator());

    @Valid
    private SortedSet<EndDateCommunication> endDateCommunicationToCustomer = new TreeSet<EndDateCommunication>(
            new EndDateCommunicationComparator());

    public enum SchedulingMode {
        FORWARD, BACKWARDS;
    }

    private SchedulingMode schedulingMode = SchedulingMode.FORWARD;

    private boolean neededToRecalculateSumChargedEfforts = false;

    private boolean neededToRecalculateSumExpenses = false;

    public static class CurrentVersionInfo {

        private final OrderVersion orderVersion;

        private final boolean modifyingTheOwnerScenario;

        static CurrentVersionInfo create(Scenario scenario,
                OrderVersion orderVersion) {
            return new CurrentVersionInfo(scenario, orderVersion);
        }

        private CurrentVersionInfo(Scenario scenario, OrderVersion orderVersion) {
            Validate.notNull(scenario);
            Validate.notNull(orderVersion);
            this.orderVersion = orderVersion;
            this.modifyingTheOwnerScenario = orderVersion.isOwnedBy(scenario);
        }

        public boolean isUsingTheOwnerScenario() {
            return modifyingTheOwnerScenario;
        }

        public OrderVersion getOrderVersion() {
            return orderVersion;
        }
    }

    public CurrentVersionInfo getCurrentVersionInfo() {
        if (currentVersionInfo == null) {
            throw new IllegalStateException(
                    "Order#useSchedulingDataFor(Scenario scenario)"
                            + " must have been called first in order to use"
                            + " this method");
        }
        return currentVersionInfo;
    }

    public void addOrderAuthorization(OrderAuthorization orderAuthorization) {
        orderAuthorization.setOrder(this);
        orderAuthorizations.add(orderAuthorization);
    }

    public Map<Scenario, OrderVersion> getScenarios() {
        return Collections.unmodifiableMap(scenarios);
    }

    public void useSchedulingDataFor(Scenario scenario) {
        useSchedulingDataFor(scenario, true);
    }

    public void useSchedulingDataFor(Scenario scenario, boolean recursive) {
        OrderVersion orderVersion = scenarios.get(scenario);
        currentVersionInfo = CurrentVersionInfo.create(scenario, orderVersion);
        useSchedulingDataFor(orderVersion, recursive);
    }

    @Override
    public void writeSchedulingDataChanges() {
        super.writeSchedulingDataChanges();
    }

    public void writeSchedulingDataChangesTo(Scenario currentScenario,
            OrderVersion newOrderVersion) {
        setVersionForScenario(currentScenario, newOrderVersion);
        writeSchedulingDataChangesTo(
                deepCopyWithNeededReplaces(newOrderVersion),
                newOrderVersion);
        useSchedulingDataFor(currentScenario);
        removeSpuriousDayAssignments(currentScenario);
    }

    private DeepCopy deepCopyWithNeededReplaces(
            OrderVersion newOrderVersion) {
        DeepCopy result = new DeepCopy();
        addNeededReplaces(result, newOrderVersion);
        return result;
    }

    public boolean isUsingTheOwnerScenario() {
        return getCurrentVersionInfo().isUsingTheOwnerScenario();
    }

    public BigDecimal getWorkBudget() {
        if (workBudget == null) {
            return BigDecimal.ZERO;
        }
        return workBudget;
    }

    public void setWorkBudget(BigDecimal workBudget) {
        if (workBudget == null) {
            workBudget = BigDecimal.ZERO.setScale(2);
        }
        this.workBudget = workBudget;
    }

    public BigDecimal getMaterialsBudget() {
        if (materialsBudget == null) {
            return BigDecimal.ZERO;
        }
        return materialsBudget;
    }

    public void setMaterialsBudget(BigDecimal materialsBudget) {
        if (materialsBudget == null) {
            materialsBudget = BigDecimal.ZERO.setScale(2);
        }
        this.materialsBudget = materialsBudget;
    }

    public BigDecimal getTotalBudget() {
        return getWorkBudget().add(getMaterialsBudget());
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public OrderStatusEnum getState() {
        return state;
    }

    public void setState(OrderStatusEnum state) {
        this.state = state;
    }

    public String getCustomerReference() {
        return this.customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public ExternalCompany getCustomer() {
        return this.customer;
    }

    public void setCustomer(ExternalCompany customer) {
        this.customer = customer;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    @NotNull
    public SchedulingMode getSchedulingMode() {
        return schedulingMode;
    }

    public boolean isScheduleBackwards() {
        return getSchedulingMode() == SchedulingMode.BACKWARDS;
    }

    public void setSchedulingMode(SchedulingMode schedulingMode) {
        Validate.notNull(schedulingMode);
        this.schedulingMode = schedulingMode;
    }

    public boolean isDeadlineBeforeStart() {
        return getDeadline() != null && getDeadline().before(getInitDate());
    }

    public List<OrderElement> getOrderElements() {
        return new ArrayList<OrderElement>(getChildren());
    }

    public TaskGroup getAssociatedTaskElement() {
        return (TaskGroup) super.getAssociatedTaskElement();
    }

    public List<TaskElement> getAllChildrenAssociatedTaskElements() {
        List<TaskElement> result = new ArrayList<TaskElement>();

        for (OrderElement orderElement : getAllChildren()) {
            TaskElement taskElement = orderElement.getAssociatedTaskElement();
            if (taskElement != null) {
                result.add(taskElement);
            }
        }
        Validate.noNullElements(result);

        return result;
    }

    public List<TaskElement> getAssociatedTasks() {
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        TaskGroup taskGroup = getAssociatedTaskElement();
        if (taskGroup != null) {
            result.addAll(taskGroup.getChildren());
        }
        return result;
    }

    public boolean isSomeTaskElementScheduled() {
        return isScheduled();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the project must have a start date")
    private boolean ifSchedulingModeIsForwardOrderMustHaveStartDate() {
        return getSchedulingMode() != SchedulingMode.FORWARD
                || getInitDate() != null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the project must have a deadline")
    private boolean ifSchedulingModeIsBackwardsOrderMustHaveDeadline() {
        return getSchedulingMode() != SchedulingMode.BACKWARDS
                || getDeadline() != null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "deadline must be after start date")
    private boolean checkConstraintDeadlineMustBeAfterStart() {
        return getInitDate() == null || !this.isDeadlineBeforeStart();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "At least one hours group is needed for each task")
    private boolean checkConstraintAtLeastOneHoursGroupForEachOrderElement() {
        for (OrderElement orderElement : this.getOrderElements()) {
            if (!orderElement.checkAtLeastOneHoursGroup()) {
                return false;
            }
        }
        return true;
    }

    public List<DayAssignment> getDayAssignments(FilterType filter) {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
        for (OrderElement orderElement : getAllOrderElements()) {
            Set<TaskElement> taskElements = orderElement.getTaskElements();
            for (TaskElement taskElement : taskElements) {
                if (taskElement instanceof Task) {
                    dayAssignments
                            .addAll(taskElement.getDayAssignments(filter));
                }
            }
        }
        return DayAssignment.filter(dayAssignments, filter);
    }

    public Set<Resource> getResources(FilterType filter) {
        Set<Resource> resources = new HashSet<Resource>();
        for (DayAssignment dayAssignment : getDayAssignments(filter)) {
            resources.add(dayAssignment.getResource());
        }
        return resources;
    }

    @Override
    protected boolean applyConstraintBasedOnInitOrEndDate(Task task,
            boolean scheduleBackwards) {
        // the initDate or the deadline of a order doesn't imply a start
        // constraint at a task
        return false;
    }

    public boolean getDependenciesConstraintsHavePriority() {
        return dependenciesConstraintsHavePriority != null
                && dependenciesConstraintsHavePriority;
    }

    public void setDependenciesConstraintsHavePriority(
            Boolean dependenciesConstraintsHavePriority) {
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
    }

    public void setCalendar(BaseCalendar calendar) {
        this.calendar = calendar;
    }

    @NotNull(message = "project calendar not specified")
    public BaseCalendar getCalendar() {
        return calendar;
    }

    public void incrementLastOrderElementSequenceCode() {
        if (this.lastOrderElementSequenceCode == null) {
            this.lastOrderElementSequenceCode = 0;
        }
        this.lastOrderElementSequenceCode++;
    }

    @NotNull(message = "last task sequence code not specified")
    public Integer getLastOrderElementSequenceCode() {
        return lastOrderElementSequenceCode;
    }

    @Override
    public Order getOrder() {
        return this;
    }

    @Override
    public OrderTemplate createTemplate() {
        return OrderTemplate.create(this);
    }

    public void generateOrderElementCodes(int numberOfDigits) {
        if (isCodeAutogenerated()) {
            for (OrderElement orderElement : this.getAllOrderElements()) {
                if ((orderElement.getCode() == null)
                        || (orderElement.getCode().isEmpty())
                        || (!orderElement.getCode().startsWith(this.getCode()))) {
                    // LibrePlan Audiovisual
                    // Use the code already set to the order element (this is
                    // set from the budget) and add the prefix with the order
                    // code
                    String orderElementCode = orderElement.getCode();
                    if (StringUtils.isEmpty(orderElementCode)) {
                        this.incrementLastOrderElementSequenceCode();
                        orderElementCode = EntitySequence.formatValue(
                                numberOfDigits,
                                this.getLastOrderElementSequenceCode());
                    }
                    orderElement.setCode(this.getCode()
                            + EntitySequence.CODE_SEPARATOR_CHILDREN
                            + orderElementCode);
                }

                if (orderElement instanceof OrderLine) {
                    for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
                        if ((hoursGroup.getCode() == null)
                                || (hoursGroup.getCode().isEmpty())
                                || (!hoursGroup.getCode().startsWith(
                                        orderElement.getCode()))) {
                            ((OrderLine) orderElement)
                                    .incrementLastHoursGroupSequenceCode();
                            String hoursGroupCode = EntitySequence.formatValue(
                                    numberOfDigits, ((OrderLine) orderElement)
                                            .getLastHoursGroupSequenceCode());
                            hoursGroup.setCode(orderElement.getCode()
                                    + EntitySequence.CODE_SEPARATOR_CHILDREN
                                    + hoursGroupCode);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((getId() == null || isNewObject()) ? super.hashCode()
                        : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || isNewObject()) {
            return false;
        }
        if (!(obj instanceof Order)) {
            return false;
        }
        Order other = (Order) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    public void setVersionForScenario(Scenario currentScenario,
            OrderVersion orderVersion) {
        scenarios.put(currentScenario, orderVersion);
    }

    /**
     * Disassociates this order and its children from the scenario
     * @param scenario
     * @return <code>null</code> if there is no order version for the scenario;
     *         the order version associated to the supplied scenario
     */
    public OrderVersion disassociateFrom(Scenario scenario) {
        OrderVersion existentVersion = scenarios.remove(scenario);
        if (existentVersion != null && !isVersionUsed(existentVersion)) {
            removeVersion(existentVersion);
        }
        return existentVersion;
    }

    public OrderVersion getOrderVersionFor(Scenario current) {
        return scenarios.get(current);
    }

    public void setOrderVersion(Scenario scenario, OrderVersion newOrderVersion) {
        scenarios.put(scenario, newOrderVersion);
    }

    public boolean hasNoVersions() {
        return scenarios.isEmpty();
    }

    public boolean isVersionUsed(OrderVersion orderVersion) {
        for (OrderVersion each : getScenarios().values()) {
            if (each.getId().equals(orderVersion.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public OrderLine toLeaf() {
        throw new UnsupportedOperationException(
                "Order can not be converted to leaf");
    }

    public DirectAdvanceAssignment getDirectAdvanceAssignmentOfTypeSubcontractor() {
        if (StringUtils.isBlank(getExternalCode())) {
            return null;
        }

        AdvanceType advanceType = PredefinedAdvancedTypes.SUBCONTRACTOR
                .getType();

        return getAdvanceAssignmentByType(advanceType);
    }

    @AssertTrue(message = "project name is already being used")
    public boolean checkConstraintProjectUniqueName() {

        IOrderDAO orderDAO = Registry.getOrderDAO();

        if (isNewObject()) {
            return !orderDAO.existsByNameAnotherTransaction(getName());
        } else {
            try {
                Order o = orderDAO.findByNameAnotherTransaction(getName());
                return o.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }

        }

    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareToIgnoreCase(((Order) o).getName());
    }

    public void setCustomerCommunications(Set<CustomerCommunication> customerCommunications) {
        this.customerCommunications = customerCommunications;
    }

    public Set<CustomerCommunication> getCustomerCommunications() {
        return customerCommunications;
    }

    public void setDeliveringDates(SortedSet<DeadlineCommunication> deliveringDates) {
        this.deliveringDates = deliveringDates;
    }

    public SortedSet<DeadlineCommunication> getDeliveringDates() {
        return deliveringDates;
    }

    public void setEndDateCommunicationToCustomer(
            SortedSet<EndDateCommunication> endDateCommunicationToCustomer) {
        this.endDateCommunicationToCustomer.clear();
        this.endDateCommunicationToCustomer.addAll(endDateCommunicationToCustomer);
    }

    public SortedSet<EndDateCommunication> getEndDateCommunicationToCustomer() {
        return Collections.unmodifiableSortedSet(this.endDateCommunicationToCustomer);
    }


    public void updateFirstAskedEndDate(Date communicationDate) {
        if (this.endDateCommunicationToCustomer != null && !this.endDateCommunicationToCustomer.isEmpty()) {
            this.endDateCommunicationToCustomer.first().setCommunicationDate(communicationDate);
        }
    }

    public Date getLastAskedEndDate() {
        if (this.endDateCommunicationToCustomer != null
                && !this.endDateCommunicationToCustomer.isEmpty()) {
            return this.endDateCommunicationToCustomer.first().getEndDate();
        }
        return null;
    }

    public EndDateCommunication getLastEndDateCommunicationToCustomer() {
        if (this.endDateCommunicationToCustomer != null
                && !this.endDateCommunicationToCustomer.isEmpty()) {
            return this.endDateCommunicationToCustomer.first();
        }
        return null;
    }

    public void removeAskedEndDate(EndDateCommunication endDate) {
        this.endDateCommunicationToCustomer.remove(endDate);
    }

    public void addAskedEndDate(EndDateCommunication endDate) {
        this.endDateCommunicationToCustomer.add(endDate);
    }

    public void markAsNeededToRecalculateSumChargedEfforts() {
        neededToRecalculateSumChargedEfforts = true;
    }

    public boolean isNeededToRecalculateSumChargedEfforts() {
        return neededToRecalculateSumChargedEfforts;
    }

    public void setFilmingProgressSet(Set<FilmingProgress> filmingProgressSet) {
        this.filmingProgressSet = filmingProgressSet;
    }

    public Set<FilmingProgress> getFilmingProgressSet() {
        return filmingProgressSet;
    }

    public void markAsNeededToRecalculateSumExpenses() {
        neededToRecalculateSumExpenses = true;
    }

    public boolean isNeededToRecalculateSumExpenses() {
        return neededToRecalculateSumExpenses;
    }

    public Budget getAssociatedBudgetObject() {
        return associatedBudgetObject;
    }

    public void setAssociatedBudgetObject(Budget associatedBudgetObject) {
        this.associatedBudgetObject = associatedBudgetObject;
    }

    @NotNull(message = "project start date cannot be empty")
    public Date getInitDate() {
        return super.getInitDate();
    }

    @NotNull(message = "project deadline cannot be empty")
    public Date getDeadline() {
        return super.getDeadline();
    }

    public CashflowType getCashflowType() {
        return cashflowType;
    }

    public void setCashflowType(CashflowType cashflowType) {
        this.cashflowType = cashflowType;
    }

    public Integer getCashflowDelayDays() {
        return cashflowDelayDays == null ? 0 : cashflowDelayDays;
    }

    public void setCashflowDelayDays(Integer cashflowDelayDays) {
        this.cashflowDelayDays = cashflowDelayDays;
    }

}
