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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectHeader;
import net.sf.mpxj.Relation;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;

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
