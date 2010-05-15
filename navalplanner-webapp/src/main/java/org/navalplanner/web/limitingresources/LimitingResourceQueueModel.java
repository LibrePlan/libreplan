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

package org.navalplanner.web.limitingresources;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.IDependencyDAO;
import org.navalplanner.business.planner.daos.ILimitingResourceQueueDAO;
import org.navalplanner.business.planner.daos.ILimitingResourceQueueDependencyDAO;
import org.navalplanner.business.planner.daos.ILimitingResourceQueueElementDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.DateAndHour;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElementGap;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourceQueueModel implements ILimitingResourceQueueModel {

    private final ResourcesPerDay ONE_RESOURCE_PER_DAY = ResourcesPerDay
            .amount(new BigDecimal(1));

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private ILimitingResourceQueueElementDAO limitingResourceQueueElementDAO;

    @Autowired
    private ILimitingResourceQueueDAO limitingResourceQueueDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private ILimitingResourceQueueDependencyDAO limitingResourceQueueDependencyDAO;

    private Interval viewInterval;

    private List<LimitingResourceQueue> limitingResourceQueues = new ArrayList<LimitingResourceQueue>();

    private List<LimitingResourceQueueElement> unassignedLimitingResourceQueueElements = new ArrayList<LimitingResourceQueueElement>();

    private Set<LimitingResourceQueueElement> toBeRemoved = new HashSet<LimitingResourceQueueElement>();

    private Set<LimitingResourceQueueElement> toBeSaved = new HashSet<LimitingResourceQueueElement>();

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(boolean filterByResources) {
        doGlobalView();
    }

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(Order filterBy, boolean filterByResources) {
        doGlobalView();
    }

    private void doGlobalView() {
        loadUnassignedLimitingResourceQueueElements();
        loadLimitingResourceQueues();
        final Date startingDate = getEarliestDate();
        viewInterval = new Interval(startingDate, plusFiveYears(startingDate));
    }

    private Date getEarliestDate() {
        final LimitingResourceQueueElement element = getEarliestQueueElement();
        return (element != null) ? element.getStartDate()
                .toDateTimeAtCurrentTime().toDate() : new Date();
    }

    private LimitingResourceQueueElement getEarliestQueueElement() {
        LimitingResourceQueueElement earliestQueueElement = null;

        if (!limitingResourceQueues.isEmpty()) {
            for (LimitingResourceQueue each : limitingResourceQueues) {
                LimitingResourceQueueElement element = getFirstLimitingResourceQueueElement(each);
                if (element == null) {
                    continue;
                }
                if (earliestQueueElement == null
                        || isEarlier(element, earliestQueueElement)) {
                    earliestQueueElement = element;
                }
            }
        }
        return earliestQueueElement;
    }

    private boolean isEarlier(LimitingResourceQueueElement arg1,
            LimitingResourceQueueElement arg2) {
        return (arg1.getStartDate().isBefore(arg2.getStartDate()));
    }

    private LimitingResourceQueueElement getFirstLimitingResourceQueueElement(
            LimitingResourceQueue queue) {
        return getFirstChild(queue.getLimitingResourceQueueElements());
    }

    private LimitingResourceQueueElement getFirstChild(
            SortedSet<LimitingResourceQueueElement> elements) {
        return (elements.isEmpty()) ? null : elements.iterator().next();
    }

    private Date plusFiveYears(Date date) {
        return (new LocalDate(date)).plusYears(5).toDateTimeAtCurrentTime()
                .toDate();
    }

    /**
     * Loads unassigned {@link LimitingResourceQueueElement} from DB
     *
     * @return
     */
    private void loadUnassignedLimitingResourceQueueElements() {
        unassignedLimitingResourceQueueElements.clear();
        unassignedLimitingResourceQueueElements
                .addAll(initializeLimitingResourceQueueElements(limitingResourceQueueElementDAO
                        .getUnassigned()));
    }

    private List<LimitingResourceQueueElement> initializeLimitingResourceQueueElements(
            List<LimitingResourceQueueElement> elements) {
        for (LimitingResourceQueueElement each : elements) {
            initializeLimitingResourceQueueElement(each);
        }
        return elements;
    }

    private void initializeLimitingResourceQueueElement(
            LimitingResourceQueueElement element) {
        ResourceAllocation<?> resourceAllocation = element
                .getResourceAllocation();
        resourceAllocation = initializeResourceAllocationIfNecessary(resourceAllocation);
        element.setResourceAllocation(resourceAllocation);
        initializeTask(resourceAllocation.getTask());
        initializeCalendarIfAny(element.getResource());
    }

    private void initializeTask(Task task) {
        Hibernate.initialize(task);
        for (ResourceAllocation<?> each: task.getAllResourceAllocations()) {
            Hibernate.initialize(each);
        }
        for (Dependency each: task.getDependenciesWithThisOrigin()) {
            Hibernate.initialize(each);
        }
        for (Dependency each: task.getDependenciesWithThisDestination()) {
            Hibernate.initialize(each);
        }
    }

    private void initializeCalendarIfAny(Resource resource) {
        if (resource != null) {
            resource.getName();
            initializeCalendarIfAny(resource.getCalendar());
        }
    }

    private void initializeCalendarIfAny(BaseCalendar calendar) {
        if (calendar != null) {
            Hibernate.initialize(calendar);
            initializeCalendarAvailabilities(calendar);
            initializeCalendarExceptions(calendar);
            initializeCalendarDataVersions(calendar);
        }
    }

    private void initializeCalendarAvailabilities(BaseCalendar calendar) {
        for (CalendarAvailability each : calendar.getCalendarAvailabilities()) {
            Hibernate.initialize(each);
        }
    }

    private void initializeCalendarExceptions(BaseCalendar calendar) {
        for (CalendarException each : calendar.getExceptions()) {
            Hibernate.initialize(each);
            Hibernate.initialize(each.getType());
        }
    }

    private void initializeCalendarDataVersions(BaseCalendar calendar) {
        for (CalendarData each : calendar.getCalendarDataVersions()) {
            Hibernate.initialize(each);
            Hibernate.initialize(each.getHoursPerDay());
            initializeCalendarIfAny(each.getParent());
        }
    }

    private ResourceAllocation<?> initializeResourceAllocationIfNecessary(
            ResourceAllocation<?> resourceAllocation) {
        if (resourceAllocation instanceof HibernateProxy) {
            resourceAllocation = (ResourceAllocation<?>) ((HibernateProxy) resourceAllocation)
                    .getHibernateLazyInitializer().getImplementation();
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                SpecificResourceAllocation specific = (SpecificResourceAllocation) resourceAllocation;
                Hibernate.initialize(specific.getAssignments());
            }
            Hibernate.initialize(resourceAllocation.getLimitingResourceQueueElement());
        }
        return resourceAllocation;
    }

    private void loadLimitingResourceQueues() {
        limitingResourceQueues.clear();
        limitingResourceQueues
                .addAll(initializeLimitingResourceQueues(limitingResourceQueueDAO
                        .getAll()));
    }

    private List<LimitingResourceQueue> initializeLimitingResourceQueues(
            List<LimitingResourceQueue> queues) {
        for (LimitingResourceQueue each : queues) {
            initializeLimitingResourceQueue(each);
        }
        return queues;
    }

    private void initializeLimitingResourceQueue(LimitingResourceQueue queue) {
        Hibernate.initialize(queue.getResource());
        for (LimitingResourceQueueElement each : queue
                .getLimitingResourceQueueElements()) {
            initializeLimitingResourceQueueElement(each);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByTask(TaskElement task) {
        return orderElementDAO
                .loadOrderAvoidingProxyFor(task.getOrderElement());
    }

    @Override
    public Interval getViewInterval() {
        return viewInterval;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userCanRead(Order order, String loginName) {
        if (SecurityUtils.isUserInRole(UserRole.ROLE_READ_ALL_ORDERS)
                || SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return true;
        }
        try {
            User user = userDAO.findByLoginName(loginName);
            for (OrderAuthorization authorization : orderAuthorizationDAO
                    .listByOrderUserAndItsProfiles(order, user)) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.READ_AUTHORIZATION
                        || authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    return true;
                }
            }
        } catch (InstanceNotFoundException e) {
            // this case shouldn't happen, because it would mean that there
            // isn't a logged user
            // anyway, if it happenned we don't allow the user to pass
        }
        return false;
    }

    @Override
    public List<LimitingResourceQueue> getLimitingResourceQueues() {
        return Collections.unmodifiableList(limitingResourceQueues);
    }

    public List<LimitingResourceQueueElement> getUnassignedLimitingResourceQueueElements() {
        return Collections
                .unmodifiableList(unassignedLimitingResourceQueueElements);
    }

    public ZoomLevel calculateInitialZoomLevel() {
        Interval interval = getViewInterval();
        return ZoomLevel.getDefaultZoomByDates(new LocalDate(interval
                .getStart()), new LocalDate(interval.getFinish()));
    }

    @Override
    public void assignLimitingResourceQueueElement(
            final LimitingResourceQueueElement element) {

        LimitingResourceQueueElement queueElement = retrieveQueueElementFromModel(element);
        LimitingResourceQueue queue = retrieveQueueByResourceFromModel(queueElement
                .getResource());

        DateAndHour startTime = findStartTimeInQueueForQueueElement(queue, queueElement);
        DateAndHour[] startAndEndTime = allocateDayAssignments(queueElement
                .getResourceAllocation(), startTime);
        updateStartAndEndTimes(queueElement, startAndEndTime);
        addLimitingResourceQueueElement(queue, queueElement);
        markAsModified(queueElement);
    }

    private void markAsModified(LimitingResourceQueueElement element) {
        if (!toBeSaved.contains(element)) {
            toBeSaved.add(element);
        }
    }

    private DateAndHour findStartTimeInQueueForQueueElement(
            LimitingResourceQueue queue, LimitingResourceQueueElement candidate) {

        final SortedSet<LimitingResourceQueueElement> elements = queue.getLimitingResourceQueueElements();
        if (!elements.isEmpty()) {
            final List<LimitingResourceQueueElementGap> gapList = buildGapList(candidate, elements);
            final DateAndHour startTime = findStartTimeInGapList(candidate, gapList);
            return (startTime != null) ? startTime : afterLastElement(candidate, elements);
        }
        return getStartTimeBecauseOfGantt(candidate);
    }

    private DateAndHour afterLastElement(LimitingResourceQueueElement candidate,
            SortedSet<LimitingResourceQueueElement> elements) {
        final DateAndHour lastElementEndTime = elements.last().getEndTime();
        final DateAndHour candidateStartTime = getStartTimeBecauseOfGantt(candidate);
        return DateAndHour.Max(lastElementEndTime, candidateStartTime);
    }

    private DateAndHour getStartTimeBecauseOfGantt(LimitingResourceQueueElement element) {
        return new DateAndHour(new LocalDate(element.getEarlierStartDateBecauseOfGantt()), 0);
    }

    private DateAndHour findStartTimeInGapList(LimitingResourceQueueElement candidate,
            List<LimitingResourceQueueElementGap> gapList) {
        for (LimitingResourceQueueElementGap each : gapList) {
            if (each.canFit(candidate)) {
                return each.getStartTime();
            }
        }
        return null;
    }

    private List<LimitingResourceQueueElementGap> buildGapList(LimitingResourceQueueElement candidate,
            final SortedSet<LimitingResourceQueueElement> elements) {
        List<LimitingResourceQueueElementGap> result = new ArrayList<LimitingResourceQueueElementGap>();

        // If start time of candidate element to fit in queue is before first
        // element, create a gap between candidate and the first element of the
        // queue
        DateAndHour startTimeBecauseOfGantt = getStartTimeBecauseOfGantt(candidate);
        final LimitingResourceQueueElement firstElement = elements.first();
        if (startTimeBecauseOfGantt.compareTo(firstElement.getStartTime()) < 0) {
            result.add(createGap(firstElement.getResource(), startTimeBecauseOfGantt,
                    firstElement.getStartTime()));
        }

        LimitingResourceQueueElement current, next;
        // Only include gaps from candidate start time on
        for (Iterator<LimitingResourceQueueElement> i = elements.iterator(); i
                .hasNext();) {

            current = i.next();
            if (i.hasNext()) {
                next = i.next();
                DateAndHour startTime = current.getEndTime();
                final DateAndHour endTime = next.getStartTime();

                if (startTime.compareTo(startTimeBecauseOfGantt) <= 0) {
                    if (endTime.compareTo(startTimeBecauseOfGantt) <= 0) {
                        // Start and end of the gap are before earlierStartDateBecauseOfGanttTime
                        continue;
                    }
                    // earliestStartDateBecauseOfGantt is in between
                    startTime = startTimeBecauseOfGantt;
                }
                result.add(createGap(current.getResource(), startTime, endTime));
            }
        }
        return result;
    }

    public LimitingResourceQueueElementGap createGap(Resource resource, DateAndHour startTime,
            DateAndHour endTime) {
        return LimitingResourceQueueElementGap.create(resource, startTime, endTime);
    }

    private void updateStartAndEndTimes(LimitingResourceQueueElement element,
            DateAndHour[] startAndEndTime) {

        final DateAndHour startTime = startAndEndTime[0];
        final DateAndHour endTime = startAndEndTime[1];

        element.setStartDate(startTime.getDate());
        element.setStartHour(startTime.getHour());
        element.setEndDate(endTime.getDate());
        element.setEndHour(endTime.getHour());

        // Update starting and ending dates for associated Task
        Task task = element.getResourceAllocation().getTask();
        updateStartingAndEndingDate(task, startTime.getDate(), endTime
                .getDate());
    }

    private void updateStartingAndEndingDate(Task task, LocalDate startDate,
            LocalDate endDate) {
        task.setStartDate(toDate(startDate));
        task.setEndDate(endDate.toDateTimeAtStartOfDay().toDate());
        task.explicityMoved(toDate(startDate));
    }

    private Date toDate(LocalDate date) {
        return date.toDateTimeAtStartOfDay().toDate();
    }

    private void addLimitingResourceQueueElement(LimitingResourceQueue queue,
            LimitingResourceQueueElement element) {
        queue.addLimitingResourceQueueElement(element);
        unassignedLimitingResourceQueueElements.remove(element);
    }

    private LimitingResourceQueue retrieveQueueByResourceFromModel(Resource resource) {
        return findQueueByResource(limitingResourceQueues, resource);
    }

    private LimitingResourceQueue findQueueByResource(
            List<LimitingResourceQueue> queues, Resource resource) {
        for (LimitingResourceQueue each : queues) {
            if (each.getResource().getId().equals(resource.getId())) {
                return each;
            }
        }
        return null;
    }

    private LimitingResourceQueue retrieveQueueFromModel(LimitingResourceQueue queue) {
        return findQueue(limitingResourceQueues, queue);
    }

    private LimitingResourceQueue findQueue(List<LimitingResourceQueue> queues,
            LimitingResourceQueue queue) {
        for (LimitingResourceQueue each : limitingResourceQueues) {
            if (each.getId().equals(queue.getId())) {
                return each;
            }
        }
        return null;
    }

    private LimitingResourceQueueElement retrieveQueueElementFromModel(
            LimitingResourceQueueElement element) {
        final LimitingResourceQueue queue = element.getLimitingResourceQueue();
        if (queue != null) {
            return findQueueElement(queue.getLimitingResourceQueueElements(),
                    element);
        } else {
            return findQueueElement(unassignedLimitingResourceQueueElements,
                    element);
        }
    }

    private LimitingResourceQueueElement findQueueElement(
            Collection<LimitingResourceQueueElement> elements,
            LimitingResourceQueueElement element) {
        for (LimitingResourceQueueElement each : elements) {
            if (each.getId().equals(element.getId())) {
                return each;
            }
        }
        return null;
    }

    private DateAndHour[] allocateDayAssignments(
            ResourceAllocation<?> resourceAllocation, DateAndHour startingTime) {
        if (resourceAllocation instanceof SpecificResourceAllocation) {
            return allocateDayAssignments(
                    (SpecificResourceAllocation) resourceAllocation,
                    startingTime);
        }
        if (resourceAllocation instanceof GenericResourceAllocation) {
            // TODO: Generate day assignments for generic resource allocation
        }
        return null;
    }

    private DateAndHour[] allocateDayAssignments(
            SpecificResourceAllocation resourceAllocation,
            DateAndHour startTime) {

        List<SpecificDayAssignment> assignments = new ArrayList<SpecificDayAssignment>();

        DateAndHour newStartTime = startTime;

        LocalDate date = startTime.getDate();
        int totalHours = resourceAllocation.getIntendedTotalHours();

        // Generate first day assignment
        int hoursCanAllocate = hoursCanWorkOnDay(resourceAllocation, date, startTime.getHour());
        if (hoursCanAllocate > 0) {
            int hoursToAllocate = Math.min(totalHours, hoursCanAllocate);
            SpecificDayAssignment dayAssignment = SpecificDayAssignment.create(
                    date, hoursToAllocate, resourceAllocation.getResource());
            totalHours -= addDayAssignment(assignments, dayAssignment);
        } else {
            newStartTime = new DateAndHour(date.plusDays(1), 0);
        }

        // Generate rest of day assignments
        for (date = date.plusDays(1); totalHours > 0; date = date.plusDays(1)) {
            totalHours -= addDayAssignment(assignments, generateDayAssignment(
                    resourceAllocation, date, totalHours));
        }
        resourceAllocation.allocateLimitingDayAssignments(assignments);

        DateAndHour newEndTime = new DateAndHour(date, getEndingTime(assignments));
        DateAndHour[] startAndEndTime = {newStartTime, newEndTime};
        return startAndEndTime;
    }

    private DayAssignment getLastDayAssignment(List<SpecificDayAssignment> dayAssignments) {
        return dayAssignments.get(dayAssignments.size() - 1);
    }

    private int getEndingTime(List<SpecificDayAssignment> dayAssignments) {
        return (dayAssignments.isEmpty()) ? 0 : getLastDayAssignment(dayAssignments).getHours();
    }

    private int addDayAssignment(List<SpecificDayAssignment> list, SpecificDayAssignment dayAssignment) {
        if (dayAssignment != null) {
            list.add(dayAssignment);
            return dayAssignment.getHours();
        }
        return 0;
    }

    private int hoursCanWorkOnDay(final SpecificResourceAllocation resourceAllocation,
            final LocalDate date, int alreadyWorked) {
        final ResourceCalendar calendar = resourceAllocation.getResource()
                .getCalendar();
        int hoursCanAllocate = calendar.toHours(date, ONE_RESOURCE_PER_DAY);
        return hoursCanAllocate - alreadyWorked;
    }

    private SpecificDayAssignment generateDayAssignment(
            final SpecificResourceAllocation resourceAllocation,
            final LocalDate date, int intentedHours) {

        final ResourceCalendar calendar = resourceAllocation.getResource()
                .getCalendar();

        int hoursCanAllocate = calendar.toHours(date, ONE_RESOURCE_PER_DAY);
        if (hoursCanAllocate > 0) {
            int hoursToAllocate = Math.min(intentedHours, hoursCanAllocate);
            return SpecificDayAssignment.create(date, hoursToAllocate,
                    resourceAllocation.getResource());
        }
        return null;
    }

    @Override
    @Transactional
    public void confirm() {
        applyChanges();
    }

    private void applyChanges() {
        removeQueueElements();
        saveQueueElements();
    }

    private void saveQueueElements() {
        for (LimitingResourceQueueElement each: toBeSaved) {
            if (each != null) {
                saveQueueElement(each);
            }
        }
        toBeSaved.clear();
    }

    private void saveQueueElement(LimitingResourceQueueElement element) {
        limitingResourceQueueElementDAO.save(element);
        taskDAO.save(getAssociatedTask(element));
    }

    private Task getAssociatedTask(LimitingResourceQueueElement element) {
        return element.getResourceAllocation().getTask();
    }

    private void removeQueueElements() {
        for (LimitingResourceQueueElement each: toBeRemoved) {
            removeQueueElement(each);
        }
        toBeRemoved.clear();
    }

    private void removeQueueElement(LimitingResourceQueueElement element) {
        Task task = getAssociatedTask(element);
        removeQueueDependenciesIfAny(task);
        removeQueueElementById(element.getId());
    }

    private void removeQueueElementById(Long id) {
        try {
            limitingResourceQueueElementDAO.remove(id);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Trying to remove non-existing entity");
        }
    }

    private void removeQueueDependenciesIfAny(Task task) {
        for (Dependency each: task.getDependenciesWithThisOrigin()) {
            removeQueueDependencyIfAny(each);
        }
        for (Dependency each: task.getDependenciesWithThisDestination()) {
            removeQueueDependencyIfAny(each);
        }
    }

    private void removeQueueDependencyIfAny(Dependency dependency) {
        LimitingResourceQueueDependency queueDependency = dependency.getQueueDependency();
        if (queueDependency != null) {
            queueDependency.getHasAsOrigin().remove(queueDependency);
            queueDependency.getHasAsDestiny().remove(queueDependency);
            dependency.setQueueDependency(null);
            dependencyDAO.save(dependency);
            removeQueueDependencyById(queueDependency.getId());
        }
    }

    @Autowired
    private IDependencyDAO dependencyDAO;

    private void removeQueueDependencyById(Long id) {
        try {
            limitingResourceQueueDependencyDAO.remove(id);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Trying to remove non-existing entity");
        }
    }

    /**
     * Unschedules an element from the list of queue elements. The element is
     * later added to the list of unassigned elements
     */
    @Override
    public void unschedule(LimitingResourceQueueElement element) {
        LimitingResourceQueueElement queueElement = retrieveQueueElementFromModel(element);
        LimitingResourceQueue queue = retrieveQueueFromModel(element.getLimitingResourceQueue());

        queue.removeLimitingResourceQueueElement(queueElement);

        // Set as unassigned element
        queueElement.setLimitingResourceQueue(null);
        queueElement.setStartDate(null);
        queueElement.setStartHour(0);
        queueElement.setEndDate(null);
        queueElement.setEndHour(0);

        queueElement.getResourceAllocation().removeLimitingDayAssignments();
        unassignedLimitingResourceQueueElements.add(queueElement);
        markAsModified(queueElement);
    }

    /**
     * Removes an {@link LimitingResourceQueueElement} from the list of
     * unassigned elements
     *
     */
    @Override
    public void removeUnassignedLimitingResourceQueueElement(
            LimitingResourceQueueElement element) {
        LimitingResourceQueueElement queueElement = retrieveQueueElementFromModel(element);

        queueElement.getResourceAllocation().setLimitingResourceQueueElement(null);
        queueElement.getResourceAllocation().getTask()
                .removeAllResourceAllocations();
        unassignedLimitingResourceQueueElements.remove(queueElement);
        markAsRemoved(queueElement);
    }

    private void markAsRemoved(LimitingResourceQueueElement element) {
        if (toBeSaved.contains(element)) {
            toBeSaved.remove(element);
        }
        if (!toBeRemoved.contains(element)) {
            toBeRemoved.add(element);
        }
    }

}
