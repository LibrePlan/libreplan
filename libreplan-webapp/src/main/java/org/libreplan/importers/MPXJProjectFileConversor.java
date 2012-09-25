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

package org.libreplan.importers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.mpxj.DateRange;
import net.sf.mpxj.DayType;
import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarDateRanges;
import net.sf.mpxj.ProjectCalendarException;
import net.sf.mpxj.ProjectCalendarWeek;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectHeader;
import net.sf.mpxj.Relation;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.libreplan.importers.CalendarDayHoursDTO.CalendarDayDTO;
import org.libreplan.importers.CalendarDayHoursDTO.CalendarTypeDayDTO;
import org.libreplan.importers.DependencyDTO.TypeOfDependencyDTO;

/**
 * Class that is a conversor from the MPXJ format File to {@link OrderDTO}.
 *
 * At these moment it only converts the tasks and its subtasks with the dates.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 * @todo It last relationships. resources, calendars, hours, etc.
 */
public class MPXJProjectFileConversor {

    private static ProjectHeader header;

    /**
     * Map between the MPXJ Task and the OrderElemenDTO or MilestoneDTO that represent it.
     */
    private static Map<Task, IHasTaskAssociated> mapTask;

    /**
     * Converts a ProjectFile into a {@link OrderDTO}.
     *
     * This method contains a switch that is going to select the method to call
     * for each format. At this time it only differences between planner and
     * project.
     *
     * @param file
     *            ProjectFile to extract data from.
     * @return ImportData with the data that we want to import.
     */
    public static OrderDTO convert(ProjectFile file, String filename) {

        OrderDTO importData;

        switch (file.getMppFileType()) {

        case 0:
            importData = getImportDataFromPlanner(file, filename);
            break;
        default:
            importData = getImportDataFromMPP(file, filename);
            break;

        }

        return importData;
    }

    /**
     * Get a list of {@link CalendarDTO} from a ProjectFile
     *
     * @param file
     *            ProjectFile to extract data from.
     * @return List<CalendarDTO> List with the calendars that we want to import.
     */
    public static List<CalendarDTO> convertCalendars(ProjectFile file) {

        List<CalendarDTO> calendarDTOs = new ArrayList<CalendarDTO>();

        for (ProjectCalendar projectCalendar : file.getBaseCalendars()) {
            if (StringUtils.isBlank(projectCalendar.getName())) {
                String name = "calendar-" + UUID.randomUUID();
                projectCalendar.setName(name);
            }
            calendarDTOs.add(toCalendarDTO(projectCalendar));
            calendarDTOs.addAll(getDerivedCalendars(projectCalendar
                    .getDerivedCalendars()));
        }

        return calendarDTOs;
    }

    /**
     * Get a list of {@link CalendarDTO} from a ProjectFile
     *
     * @param file
     *            ProjectFile to extract data from.
     * @return List<CalendarDTO> List with the calendars that we want to import.
     */
    public static List<CalendarDTO> getDerivedCalendars(
            List<ProjectCalendar> derivedProjectCalendars) {

        List<CalendarDTO> calendarDTOs = new ArrayList<CalendarDTO>();

        for (ProjectCalendar projectCalendar : derivedProjectCalendars) {

            if (projectCalendar.getResource() == null) {

                if (projectCalendar.getName() != null
                        && projectCalendar.getName().length() != 0) {
                calendarDTOs.add(toCalendarDTO(projectCalendar));
                calendarDTOs.addAll(getDerivedCalendars(projectCalendar
                        .getDerivedCalendars()));
                }
            }
        }

        return calendarDTOs;
    }

    /**
     * Private Method
     *
     * Get {@link CalendarDTO} from a ProjectCalendar
     *
     * @param projectCalendar
     *            ProjectCalendat to extract data from.
     * @return List<CalendarDTO> List with the calendars that we want to import.
     */
    private static CalendarDTO toCalendarDTO(ProjectCalendar projectCalendar) {

        CalendarDTO calendarDTO = new CalendarDTO();

        calendarDTO.name = projectCalendar.getName();

        if (projectCalendar.getParent() != null) {
            calendarDTO.parent = projectCalendar.getParent().getName();
        }

        calendarDTO.calendarExceptions = getCalendarExceptionDTOs(projectCalendar
                .getCalendarExceptions());

        List<ProjectCalendarWeek> workWeeks = projectCalendar.getWorkWeeks();

        Collections.sort(workWeeks, new CompareMPXJProjectCalendarWeeks());

        calendarDTO.calendarWeeks = getCalendarWeekDTOs(projectCalendar,
                workWeeks);

        return calendarDTO;
    }

    /**
     * Private Method
     *
     * Get a list of {@link CalendarExceptionDTO} from a list of CalendarExceptions
     *
     * @param calendarExceptions
     *            List of CalendarException to extract data from.
     * @return List<CalendarExceptionDTO> List with the CalendarExcepitions that we want to import.
     */
    private static List<CalendarExceptionDTO> getCalendarExceptionDTOs(
            List<ProjectCalendarException> calendarExceptions) {

        List<CalendarExceptionDTO> calendarExceptionDTOs = new ArrayList<CalendarExceptionDTO>();

        for (ProjectCalendarException projectCalendarException : calendarExceptions) {

            calendarExceptionDTOs
                    .addAll(toCalendarExceptionDTOs(projectCalendarException));
        }

        return calendarExceptionDTOs;
    }

    /**
     * Private Method
     *
     * Get {@link CalendarExceptionDTO} from a ProjectCalendarException
     *
     * @param projectCalendar
     *            ProjectCalendarException to extract data from.
     * @return List<CalendarExceptionDTO>  with the calendar exceptions that we want to import.
     */
    private static List<CalendarExceptionDTO> toCalendarExceptionDTOs(
            ProjectCalendarException projectCalendarException) {

        List<CalendarExceptionDTO> calendarExceptionDTOs = new ArrayList<CalendarExceptionDTO>();

        Date fromDate = projectCalendarException.getFromDate();

        Date toDate  = projectCalendarException.getToDate();

        Period period = new Period(new DateTime(fromDate), new DateTime(toDate));

        boolean working = projectCalendarException.getWorking();

        int day =  period.getDays();

        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);

        List<Integer> duration = toHours(projectCalendarException);

        int hours;

        int minutes;

        if (duration != null) {
            hours = duration.get(0);

            minutes = duration.get(1);
        } else {

            if (working) {

                hours = 8;

            } else {

                hours = 0;

            }

            minutes = 0;
        }

        while (day > -1){
            if (day==0){

                calendarExceptionDTOs.add(toCalendarExceptionDTO(cal.getTime(),
                        hours, minutes, working));

            } else {

                calendarExceptionDTOs.add(toCalendarExceptionDTO(cal.getTime(),
                        hours, minutes, working));

                cal.add(Calendar.DAY_OF_MONTH, +1);

            }

            day--;

        }

        return calendarExceptionDTOs;
    }

    /**
     * Private Method
     *
     * Get {@link CalendarExceptionDTO} from a ProjectCalendarException
     *
     * @param fromDate
     *            Date with the day of the exception.
     * @param hours
     *            int with the hours.
     * @param minutes
     *            int with the minutes.
     * @param working
     *            boolean to express it is a working exception or not
     * @return CalendarExceptionDTO  with the calendar exceptions that we want to import.
     */
    private static CalendarExceptionDTO toCalendarExceptionDTO(Date fromDate,
            int hours,
            int minutes, boolean working) {

        CalendarExceptionDTO calendarExceptionDTO = new CalendarExceptionDTO();

        calendarExceptionDTO.date = fromDate;

        calendarExceptionDTO.hours = hours;

        calendarExceptionDTO.minutes = minutes;

        calendarExceptionDTO.working = working;

        return calendarExceptionDTO;

    }

    /**
     * Private Method
     *
     * Get a list of {@link CalendarWeekDTO} from a list of ProjectCalendarWeek.
     *
     * @param projectCalendar
     *            ProjectCalendarWeek with the default data
     * @param workWeeks
     *            List of ProjectCalendarWeek to extract data from.Assume that is ordered
     *            for its DataRange start date.
     * @return List<CalendarDataDTO> List with the CalendarDatas that we want to import.
     */
    private static List<CalendarWeekDTO> getCalendarWeekDTOs(
            ProjectCalendar projectCalendar, List<ProjectCalendarWeek> workWeeks) {

        List<CalendarWeekDTO> calendarDataDTOs = new ArrayList<CalendarWeekDTO>();

        Date startCalendarDate;
        Date endCalendarDate;

        if (projectCalendar.getDateRange() == null) {
            startCalendarDate = projectCalendar.getParentFile()
                    .getProjectHeader().getStartDate();
            endCalendarDate = projectCalendar.getParentFile()
                    .getProjectHeader().getFinishDate();

        } else {
            startCalendarDate = projectCalendar.getDateRange().getStart();
            endCalendarDate = projectCalendar.getDateRange().getEnd();

        }

        if (workWeeks.size() == 0) {
            calendarDataDTOs.add(toCalendarWeekDTO(startCalendarDate,
                    endCalendarDate, projectCalendar));
        } else {

            // TODO This utility is not currently implemented in MPXJ
            // This one is going to represent all the work weeks. Including the
            // ones
            // with the default value that are in the middle of two.

            Date firsWorkWeekCalendarDate = workWeeks.get(0).getDateRange()
                    .getStart();
            Calendar calendar1 = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();

            // If the star of the first work week is after the start of the
            // default
            // we have to fill the hole
            if (startCalendarDate.compareTo(firsWorkWeekCalendarDate) < 0) {
                calendar1.setTime(firsWorkWeekCalendarDate);
                calendar1.set(Calendar.DAY_OF_MONTH, -1);
                calendarDataDTOs.add(toCalendarWeekDTO(startCalendarDate,
                        calendar1.getTime(), projectCalendar));
            }

            Date startDate;
            Date endDate;
            Date nextStartDate;

            int j;
            for (int i = 0; i < workWeeks.size(); i++) {
                startDate = workWeeks.get(i).getDateRange().getStart();
                endDate = workWeeks.get(i).getDateRange().getEnd();
                calendarDataDTOs.add(toCalendarWeekDTO(startDate, endDate,
                        workWeeks.get(i)));

                j = i + 1;
                // If is not the last one
                if (j < workWeeks.size()) {
                    nextStartDate = workWeeks.get(i + 1).getDateRange()
                            .getStart();
                    calendar1.setTime(endDate);
                    calendar1.set(Calendar.DAY_OF_MONTH, +1);
                    // If the end of the current work week is more than one day
                    // before
                    // the beginning of the next
                    if (calendar1.getTime().compareTo(nextStartDate) < 0) {
                        calendar2.setTime(nextStartDate);
                        calendar1.set(Calendar.DAY_OF_MONTH, -1);
                        // Adds a new default calendar week in the hole
                        calendarDataDTOs.add(toCalendarWeekDTO(
                                calendar1.getTime(), calendar2.getTime(),
                                projectCalendar));
                    }
                }
            }

            Date endWorkWeekCalendarDate = workWeeks.get(workWeeks.size())
                    .getDateRange().getEnd();

            // If the end of the last work week is earlier than the end of the
            // default we have to fill the hole
            if (endCalendarDate.compareTo(endWorkWeekCalendarDate) > 0) {
                calendar1.setTime(endWorkWeekCalendarDate);
                calendar1.set(Calendar.DAY_OF_MONTH, +1);
                calendarDataDTOs.add(toCalendarWeekDTO(calendar1.getTime(),
                        endCalendarDate, projectCalendar));
            }

        }

        return calendarDataDTOs;
    }

    /**
     * Private Method
     *
     * Get {@link CalendarWeekDTO} from a ProjectCalendarWeek
     * @param parentEndDate
     *            End date.
     * @param parentStartDate
     *            Start date.
     * @param projectCalendarWeek
     *            ProjectCalendarWeek to extract data from.
     * @return CalendarDataDTO  with the calendar data that we want to import.
     */
    private static CalendarWeekDTO toCalendarWeekDTO(
Date parentStartDate,
            Date parentEndDate, ProjectCalendarWeek projectCalendarWeek) {

        CalendarWeekDTO calendarDataDTO = new CalendarWeekDTO();

        if (projectCalendarWeek.getDateRange() != null) {

            calendarDataDTO.startDate = projectCalendarWeek.getDateRange()
                    .getStart();

            calendarDataDTO.endDate = projectCalendarWeek.getDateRange()
                    .getEnd();

        } else {

            calendarDataDTO.startDate = null;

            calendarDataDTO.endDate = null;

        }
        List<CalendarDayHoursDTO> calendarDaysHourDTOs = new ArrayList<CalendarDayHoursDTO>();

        CalendarDayHoursDTO calendarDayHoursDTO;

        for (int i = 0; i < 7; i++) {

            calendarDayHoursDTO = new CalendarDayHoursDTO();

            calendarDayHoursDTO.type = toCalendarTypeDayDTO(projectCalendarWeek
                    .getDays()[i]);
            calendarDayHoursDTO.day = CalendarDayDTO.values()[i];

            List<Integer> duration = toHours(projectCalendarWeek.getHours()[i]);

            if (duration != null) {
                calendarDayHoursDTO.hours = duration.get(0);

                calendarDayHoursDTO.minutes = duration.get(1);
            } else {
                if (calendarDayHoursDTO.type == CalendarTypeDayDTO.WORKING) {
                    calendarDayHoursDTO.hours = 8;
                } else if (calendarDayHoursDTO.type == CalendarTypeDayDTO.NOT_WORKING) {
                    calendarDayHoursDTO.hours = 0;
                } else if (calendarDayHoursDTO.type == CalendarTypeDayDTO.DEFAULT) {
                    // TODO Grab the ones form default
                    calendarDayHoursDTO.hours = 0;
                }
                calendarDayHoursDTO.minutes = 0;

            }
            calendarDaysHourDTOs.add(calendarDayHoursDTO);
        }

        calendarDataDTO.hoursPerDays = calendarDaysHourDTOs;

        return calendarDataDTO;
    }

    private static CalendarTypeDayDTO toCalendarTypeDayDTO(DayType dayType) {

        switch (dayType) {
        case DEFAULT:

            return CalendarTypeDayDTO.DEFAULT;

        case NON_WORKING:

            return CalendarTypeDayDTO.NOT_WORKING;

        case WORKING:

            return CalendarTypeDayDTO.WORKING;

        default:

            return null;

        }

    }

    /**
     * Private Method
     *
     * Get the number of hours of a ProjectCalendarHours
     *
     * @param projectCalendarDateRanges
     *            ProjectCalendarDateRanges to extract data from.
     * @return Integer  with the total number of hours or null if the projectCalendarHours is null.
     */
    private static List<Integer> toHours(
            ProjectCalendarDateRanges projectCalendarDateRanges) {

        if (projectCalendarDateRanges != null) {

            List<Integer> duration = new ArrayList<Integer>();

            int hours = 0;

            int minutes = 0;

            for (DateRange dateRange : projectCalendarDateRanges) {

                DateTime start = new DateTime(dateRange.getStart());

                DateTime end = new DateTime(dateRange.getEnd());

                Period period = new Period(start, end);

                int days = period.getDays();
                if (period.getDays() != 0) {

                    hours += 24 * days;

                }
                hours += period.getHours();

                minutes += period.getMinutes();
            }

            duration.add(hours);

            duration.add(minutes);

            return duration;

        } else {
            return null;
        }
    }

    /**
     * Converts a ProjectFile into a {@link OrderDTO}.
     *
     * Assumes that the ProjectFile comes for a .planner file.
     *
     * @param file
     *            ProjectFile to extract data from.
     * @return ImportData with the data that we want to import.
     */
    private static OrderDTO getImportDataFromPlanner(ProjectFile file,
            String filename) {

        OrderDTO importData = new OrderDTO();

        mapTask = new HashMap<Task, IHasTaskAssociated>();

        importData.name = filename
                .substring(0, filename.length() - 8/* ".planner" */);

        header = file.getProjectHeader();

        importData.startDate = header.getStartDate();

        importData.tasks = getImportTasks(file.getChildTasks());

        importData.milestones = getImportMilestones(file.getChildTasks());

        // MPXJ don't provide a deadline for the project so we take the finish
        // date
        importData.deadline = header.getFinishDate();

        importData.dependencies = createDependencies();

        return importData;

    }

    /**
     * Private Method
     *
     * Uses the map mapTask to create the list of {@link DependencyDTO}
     *
     * @return List<DependencyDTO>
     *            List with all the dependencies
     */
    private static List<DependencyDTO> createDependencies() {

        List<DependencyDTO> dependencies = new ArrayList<DependencyDTO>();

        List<Relation> successors;

        Task task;

        DependencyDTO dependencyDTO;

        for (Map.Entry<Task, IHasTaskAssociated> mapEntry : mapTask.entrySet()) {

            task = mapEntry.getKey();

            successors = task.getSuccessors();

            if (successors != null) {

                for (Relation successor : successors) {

                    dependencyDTO = new DependencyDTO();

                    dependencyDTO.origin = mapEntry.getValue();

                    dependencyDTO.destination = mapTask.get(successor
                            .getTargetTask());

                    dependencyDTO.type = toDependencyDTOType(successor
                            .getType());

                    dependencies.add(dependencyDTO);

                }

            }

        }

        return dependencies;

    }


    /**
     * Private Method
     *
     * Mapping between LP and MPXJ relationships
     *
     * @param type
     *            MPXJ RelationType to map.
     * @return TypeOfDependencyDTO
     *            Type of the dependency for DependencyDTO
     */
    private static TypeOfDependencyDTO toDependencyDTOType(RelationType type) {

        switch (type) {

        case FINISH_FINISH:

            return TypeOfDependencyDTO.END_END;

        case FINISH_START:

            return TypeOfDependencyDTO.END_START;

        case START_FINISH:

            return TypeOfDependencyDTO.START_END;

        case START_START:

            return TypeOfDependencyDTO.START_START;

        default:

            return null;

        }
    }


    /**
     * Converts a ProjectFile into a {@link OrderDTO}
     *
     * Assumes that the ProjectFile comes for a .mpp file.
     *
     * @param file
     *            ProjectFile to extract data from.
     * @return ImportData with the data that we want to import.
     */
    private static OrderDTO getImportDataFromMPP(ProjectFile file,
            String filename) {

        OrderDTO importData = new OrderDTO();

        mapTask = new HashMap<Task, IHasTaskAssociated>();

        header = file.getProjectHeader();

        importData.startDate = header.getStartDate();

        // MPXJ don't provide a deadline for the project so we take the finish
        // date
        importData.deadline = header.getFinishDate();

        for (Task task : file.getChildTasks()) {
            // Projects are represented as a level 0 task with all
            // real task as its children. Ignore all other top level tasks.
            // See
            // http://mpxj.sourceforge.net/faq.html#extra-tasks-and-resources
            if (task.getChildTasks().size() != 0) {

                String name = task.getName();

                if (name != null) {

                    importData.name = name;

                } else {
                    // Take the filename if the project name is not set.
                    importData.name = filename.substring(0,
                            filename.length() - 4/* ".mpp" */);

                }

                importData.tasks = getImportTasks(task.getChildTasks());

                importData.milestones = getImportMilestones(task
                        .getChildTasks());


                break;
            }

        }

        importData.dependencies = createDependencies();

        return importData;
    }

    /**
     * Converts a List of MPXJ Tasks into a List of {@link MilestoneDTO}.
     *
     * @param childTasks
     *            List of MPXJ Tasks to extract data from.
     * @return List<MilestoneDTO> List of MilestoneDTO with the data that we want to
     *         import.
     */
    private static List<MilestoneDTO> getImportMilestones(List<Task> childTasks) {

        List<MilestoneDTO> milestones = new ArrayList<MilestoneDTO>();

        for (Task task : childTasks) {

            if (task.getMilestone()) {

                MilestoneDTO milestone = getMilestoneData(task);

                mapTask.put(task, milestone);

                milestones.add(milestone);

            }

        }

        return milestones;
    }

    /**
     * Converts a MPXJ Task into a {@link MilestoneDTO}.
     *
     * @param task
     *            MPXJ Task to extract data from.
     * @return MilestoneDTO MilestoneDTO with the data that we want to import.
     */
    private static MilestoneDTO getMilestoneData(Task task) {

        MilestoneDTO milestone = new MilestoneDTO();

        milestone.name = task.getName();

        milestone.startDate = task.getStart();

        toLibreplanConstraint(task);

        milestone.constraint = constraint;

        milestone.constraintDate = constraintDate;

        return milestone;

    }


    /**
     * Private method
     *
     * Converts the Duration into an integer that represent hours
     *
     * @param duration
     *            Duration to convert.
     * @param header
     *            ProjectHeader needed to convert
     * @return int Integer with the rounded duration in hours
     */
    private static int durationToIntHours(Duration duration,
            ProjectHeader header) {

        Duration durationInHours = duration
                .convertUnits(TimeUnit.HOURS, header);

        return (int) Math.floor(durationInHours.getDuration());
    }

    /**
     * Converts a List of MPXJ Tasks into a List of {@link OrderElementDTO}.
     *
     * @param tasks
     *            List of MPXJ Tasks to extract data from.
     * @return List<OrderElementDTO> List of ImportTask with the data that we want to
     *         import.
     */
    private static List<OrderElementDTO> getImportTasks(List<Task> tasks) {

        List<OrderElementDTO> importTasks = new ArrayList<OrderElementDTO>();

        for (Task task : tasks) {

            if (!task.getMilestone()) {

                OrderElementDTO importTask = getTaskData(task);

                importTask.children = getImportTasks(task.getChildTasks());

                importTask.milestones = getImportMilestones(task
                        .getChildTasks());

                mapTask.put(task, importTask);

                importTasks.add(importTask);

            }
        }

        return importTasks;

    }

    /**
     * Converts a MPXJ Task into a {@link OrderElementDTO}.
     *
     * @param task
     *            MPXJ Task to extract data from.
     * @return OrderElementDTO OrderElementDTO with the data that we want to import.
     */
    private static OrderElementDTO getTaskData(Task task) {

        OrderElementDTO importTask = new OrderElementDTO();

        importTask.name = task.getName();

        importTask.startDate = task.getStart();

        importTask.endDate = task.getFinish();

        importTask.totalHours = durationToIntHours(task.getDuration(), header);

        importTask.deadline = task.getDeadline();

        toLibreplanConstraint(task);

        importTask.constraint = constraint;

        importTask.constraintDate = constraintDate;

        return importTask;

    }

    private static ConstraintDTO constraint;

    private static Date constraintDate;

    /**
     * Private Method
     *
     * Set the attributes constraint y constraintDate with the correct value.
     *
     * Because MPXJ has more types of constraints than Libreplan we had
     * choose to convert some of them.
     *
     *
     * @param task
     *            MPXJ Task to extract data from.
     */
    private static void toLibreplanConstraint(Task task) {

        switch (task.getConstraintType()) {

        case AS_SOON_AS_POSSIBLE:

            constraint = ConstraintDTO.AS_SOON_AS_POSSIBLE;

            constraintDate = task.getConstraintDate();

            return;

        case AS_LATE_AS_POSSIBLE:

            constraint = ConstraintDTO.AS_LATE_AS_POSSIBLE;

            constraintDate = task.getConstraintDate();

            return;

        case MUST_START_ON:

            constraint = ConstraintDTO.START_IN_FIXED_DATE;

            constraintDate = task.getConstraintDate();

            return;

        case MUST_FINISH_ON:

            constraint = ConstraintDTO.START_IN_FIXED_DATE;

            constraintDate = recalculateConstraintDateMin(task);

            return;

        case START_NO_EARLIER_THAN:

            constraint = ConstraintDTO.START_NOT_EARLIER_THAN;

            constraintDate = task.getConstraintDate();

            return;

        case START_NO_LATER_THAN:

            constraint = ConstraintDTO.FINISH_NOT_LATER_THAN;

            constraintDate = recalculateConstraintDateSum(task);

            return;

        case FINISH_NO_EARLIER_THAN:

            constraint = ConstraintDTO.START_NOT_EARLIER_THAN;

            constraintDate = recalculateConstraintDateMin(task);


            return;

        case FINISH_NO_LATER_THAN:

            constraint = ConstraintDTO.FINISH_NOT_LATER_THAN;

            constraintDate = task.getConstraintDate();

            return;

        }

    }

    /**
     * Private Method
     *
     * Get the new date based on the task date adding duration.
     *
     *
     * @param task
     *            MPXJ Task to extract data from.
     * @return Date new recalculated date
     */
    private static Date recalculateConstraintDateSum(Task task) {

        return new Date(
                    task.getConstraintDate().getTime()
                            + (durationToIntHours(task.getDuration(), header) * 60 * 60 * 1000));
    }

    /**
     * Private Method
     *
     * Get the new date based on the task date and substracting duration.
     *
     *
     * @param task
     *            MPXJ Task to extract data from.
     * @return Date new recalculated date
     */
    private static Date recalculateConstraintDateMin(Task task) {

        return new Date(
                    task.getConstraintDate().getTime()
                            - (durationToIntHours(task.getDuration(), header) * 60 * 60 * 1000));
    }
}
