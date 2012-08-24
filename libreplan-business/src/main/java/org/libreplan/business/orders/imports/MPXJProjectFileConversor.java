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

package org.libreplan.business.orders.imports;

import java.util.ArrayList;
import java.util.List;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;

/**
 * Class that is a conversor from the MPXJ format File to {@link OrderDTO}.
 *
 * At these moment it only converts the tasks and its subtasks.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 * @todo It last relationships. resources, calendars, hours, etc.
 */
public class MPXJProjectFileConversor {

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

        importData.tasks = getImportTasks(file.getChildTasks());

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

                break;
            }

        }

        return importData;
    }

    /**
     * Converts a List of MPXJ Tasks into a List of {@link OrderElementDTO}.
     *
     * @param tasks
     *            List of MPXJ Tasks to extract data from.
     * @return List<ImportTask> List of ImportTask with the data that we want to
     *         import.
     */
    private static List<OrderElementDTO> getImportTasks(List<Task> tasks) {

        List<OrderElementDTO> importTasks = new ArrayList<OrderElementDTO>();

        for (Task task : tasks) {

            OrderElementDTO importTask = getTaskData(task);

            importTask.children = getImportTasks(task.getChildTasks());

            importTasks.add(importTask);

        }

        return importTasks;

    }

    /**
     * Converts a MPXJ Task into a {@link OrderElementDTO}.
     *
     * @param task
     *            MPXJ Task to extract data from.
     * @return ImportTask ImportTask with the data that we want to import.
     */
    private static OrderElementDTO getTaskData(Task task) {

        OrderElementDTO importTask = new OrderElementDTO();

        importTask.name = task.getName();

        return importTask;

    }
}
