package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CombinedWorkHours;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Represents the relation between {@link Task} and a generic {@link Resource}.
 * @author Diego Pino García <dpino@igalia.com>
 */
public class GenericResourceAllocation extends ResourceAllocation {

    private Set<Criterion> criterions;

    private Set<GenericDayAssigment> genericDayAssigments = new HashSet<GenericDayAssigment>();

    private Map<Resource, List<GenericDayAssigment>> orderedDayAssignmentsByResource = null;

    public static GenericResourceAllocation create() {
        return (GenericResourceAllocation) create(new GenericResourceAllocation());
    }

    public static GenericResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return (GenericResourceAllocation) create(new GenericResourceAllocation(
                resourcesPerDay, task));
    }

    private GenericResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        super(resourcesPerDay, task);
    }

    public GenericResourceAllocation() {

    }

    public static GenericResourceAllocation create(Task task) {
        return (GenericResourceAllocation) create(new GenericResourceAllocation(
                task));
    }

    private GenericResourceAllocation(Task task) {
        super(task);
        this.criterions = task.getCriterions();
    }

    public Set<GenericDayAssigment> getGenericDayAssigments() {
        return Collections.unmodifiableSet(genericDayAssigments);
    }

    public List<GenericDayAssigment> getOrderedAssigmentsFor(Resource resource) {
        List<GenericDayAssigment> list = getOrderedAssignmentsFor().get(
                resource);
        if (list == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    private Map<Resource, List<GenericDayAssigment>> getOrderedAssignmentsFor() {
        if (orderedDayAssignmentsByResource == null) {
            orderedDayAssignmentsByResource = DayAssigment
                    .byResourceAndOrdered(genericDayAssigments);
        }
        return orderedDayAssignmentsByResource;
    }

    private void clearFieldsCalculatedFromAssignments() {
        this.orderedDayAssignmentsByResource = null;
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }

    private class Allocation implements IAllocatable {

        private final List<Resource> resources;

        private final List<IWorkHours> workHours;

        public Allocation(List<Resource> resources) {
            this.resources = resources;
            this.workHours = new ArrayList<IWorkHours>();
            for (Resource resource : resources) {
                this.workHours.add(generateWorkHoursFor(resource));
            }
        }

        private IWorkHours generateWorkHoursFor(Resource resource) {
            List<BaseCalendar> calendars = new ArrayList<BaseCalendar>();
            if (resource.getCalendar() != null) {
                calendars.add(resource.getCalendar());
            }
            if (getTaskCalendar() != null) {
                calendars.add(getTaskCalendar());
            }
            if (!calendars.isEmpty()) {
                return CombinedWorkHours.minOf(calendars
                        .toArray(new IWorkHours[0]));
            } else {
                return SameWorkHoursEveryDay.getDefaultWorkingDay();
            }
        }

        @Override
        public void allocate(ResourcesPerDay resourcesPerDay) {
            Task task = getTask();
            LocalDate startInclusive = new LocalDate(task.getStartDate());
            List<GenericDayAssigment> assigmentsCreated = new ArrayList<GenericDayAssigment>();
            for (int i = 0; i < getDaysElapsedAt(task); i++) {
                LocalDate day = startInclusive.plusDays(i);
                int totalForDay = calculateTotalToDistribute(day,
                        resourcesPerDay);
                assigmentsCreated.addAll(distributeForDay(day, totalForDay));
            }
            setResourcesPerDay(resourcesPerDay);
            setAssigments(assigmentsCreated);
        }

        private int calculateTotalToDistribute(LocalDate day,
                ResourcesPerDay resourcesPerDay) {
            Integer workableHours = getWorkableHoursAt(day);
            return resourcesPerDay.asHoursGivenResourceWorkingDayOf(workableHours);
        }

        private Integer getWorkableHoursAt(LocalDate day) {
            if (getTaskCalendar() == null) {
                return SameWorkHoursEveryDay.getDefaultWorkingDay()
                        .getWorkableHours(day);
            } else {
                return getTaskCalendar().getWorkableHours(day);
            }
        }

        private BaseCalendar getTaskCalendar() {
            return getTask().getCalendar();
        }

        private int getDaysElapsedAt(Task task) {
            LocalDate endExclusive = new LocalDate(task.getEndDate());
            Days daysBetween = Days.daysBetween(new LocalDate(task
                    .getStartDate()), endExclusive);
            return daysBetween.getDays();
        }

        private List<GenericDayAssigment> distributeForDay(LocalDate day,
                int totalHours) {
            List<GenericDayAssigment> result = new ArrayList<GenericDayAssigment>();
            List<Share> shares = currentSharesFor(day);
            ShareDivision currentDivision = ShareDivision.create(shares);
            ShareDivision newDivison = currentDivision.plus(totalHours);
            int[] differences = currentDivision.to(newDivison);
            for (int i = 0; i < differences.length; i++) {
                assert differences[i] >= 0;
                GenericDayAssigment dayAssigment = GenericDayAssigment.create(
                        day, differences[i], resources
                        .get(i));
                result.add(dayAssigment);
            }
            return result;
        }

        private List<Share> currentSharesFor(LocalDate day) {
            List<Share> shares = new ArrayList<Share>();
            for (int i = 0; i < resources.size(); i++) {
                Resource resource = resources.get(i);
                IWorkHours workHoursForResource = workHours.get(i);
                int alreadyAssignedHours = resource.getAssignedHours(day);
                Integer workableHours = workHoursForResource
                        .getWorkableHours(day);
                // a resource would have a zero share if all it's hours for a
                // given day are filled
                shares.add(new Share(alreadyAssignedHours - workableHours));
            }
            return shares;
        }

    }

    public IAllocatable forResources(Collection<? extends Resource> resources) {
        return new Allocation(new ArrayList<Resource>(resources));
    }

    private void setAssigments(List<GenericDayAssigment> assignmentsCreated) {
        this.genericDayAssigments = new HashSet<GenericDayAssigment>(
                assignmentsCreated);
        clearFieldsCalculatedFromAssignments();
    }

    @Override
    protected List<? extends DayAssigment> getAssignments() {
        return DayAssigment.orderedByDay(genericDayAssigments);
    }

}
