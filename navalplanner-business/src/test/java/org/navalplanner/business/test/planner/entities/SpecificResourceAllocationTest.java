package org.navalplanner.business.test.planner.entities;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.test.planner.entities.DayAssigmentMatchers.consecutiveDays;
import static org.navalplanner.business.test.planner.entities.DayAssigmentMatchers.from;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Worker;

public class SpecificResourceAllocationTest {

    private BaseCalendar baseCalendar;

    private Task task;

    private SpecificResourceAllocation specificResourceAllocation;

    private Worker worker;

    private ResourceCalendar calendar;

    private int assignedHours = 0;

    private void givenAssignedHours(int assignedHours){
        this.assignedHours = assignedHours;
    }

    private void givenWorker(){
        this.worker = createNiceMock(Worker.class);
        expect(this.worker.getCalendar()).andReturn(calendar).anyTimes();
        expect(this.worker.getAssignedHours(isA(LocalDate.class))).andReturn(assignedHours).anyTimes();
        replay(this.worker);
    }

    private void givenTask(LocalDate start, LocalDate end) {
        task = createNiceMock(Task.class);
        expect(task.getCalendar()).andReturn(baseCalendar).anyTimes();
        expect(task.getStartDate()).andReturn(
                start.toDateTimeAtStartOfDay().toDate()).anyTimes();
        expect(task.getEndDate()).andReturn(
                end.toDateTimeAtStartOfDay().toDate()).anyTimes();
        replay(task);
    }

    private void givenSpecificResourceAllocation(LocalDate start, LocalDate end) {
        givenWorker();
        givenTask(start, end);
        specificResourceAllocation = SpecificResourceAllocation.create(task);
        specificResourceAllocation.setWorker(worker);
    }

    private void givenSpecificResourceAllocation(LocalDate start, int days) {
        givenSpecificResourceAllocation(start, start.plusDays(days));
    }

    @Test
    public void theAllocationsDoneAreOrderedByDay() {
        givenSpecificResourceAllocation(new LocalDate(2000, 2, 4), 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                consecutiveDays(2));
    }

    @Test
    public void theAllocationStartsAtTheStartDate() {
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(), from(start));
    }

    @Test
    public void theAllocationIsDoneEvenIfThereisOvertime() {
        givenAssignedHours(4);
        LocalDate start = new LocalDate(2000, 2, 4);
        givenSpecificResourceAllocation(start, 2);
        specificResourceAllocation.allocate(ResourcesPerDay.amount(1));
        assertThat(specificResourceAllocation.getAssignments(),
                DayAssigmentMatchers.haveHours(8, 8));
    }

}
