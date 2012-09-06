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
import java.util.List;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectHeader;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;

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

        importData.name = filename
                .substring(0, filename.length() - 8/* ".planner" */);

        header = file.getProjectHeader();

        importData.startDate = header.getStartDate();

        importData.tasks = getImportTasks(file.getChildTasks());

        importData.milestones = getImportMilestones(file.getChildTasks());

        // MPXJ don't provide a deadline for the project so we take the finish
        // date
        importData.deadline = header.getFinishDate();

        return importData;

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

        return importTask;

    }
}
