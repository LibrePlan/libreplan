/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.reports.dtos;

import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.common.Registry;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class CompletedEstimatedHoursPerTaskDTO {

    private IWorkReportLineDAO workReportLineDAO;

    private String taskName;

    private Integer estimatedHours;

    private Integer totalPlannedHours;

    private Integer partialPlannedHours;

    private EffortDuration realHours;

    private CompletedEstimatedHoursPerTaskDTO() {
        workReportLineDAO = Registry.getWorkReportLineDAO();
    }

    public CompletedEstimatedHoursPerTaskDTO(Task task, LocalDate date) {
        this();
        this.taskName = getTaskName(task);
        this.estimatedHours = task.getHoursSpecifiedAtOrder();
        this.totalPlannedHours = calculatePlannedHours(task, null);
        this.partialPlannedHours = calculatePlannedHours(task, date);
        this.realHours = calculateRealHours(task, date);
    }

    public String getTaskName(Task task) {
        String result = task.getName();
        if (result == null || result.isEmpty()) {
            result = task.getOrderElement().getName();
        }
        return result;
    }

    public Integer calculatePlannedHours(Task task, LocalDate date) {
        Integer result = new Integer(0);

        final List<DayAssignment> dayAssignments = task.getDayAssignments();
        if (dayAssignments.isEmpty()) {
            return result;
        }

        for (DayAssignment dayAssignment : dayAssignments) {
            if (date == null || dayAssignment.getDay().compareTo(date) <= 0) {
                result += dayAssignment.getHours();
            }
        }
        return result;
    }

    public EffortDuration calculateRealHours(Task task, LocalDate date) {
        EffortDuration result = EffortDuration.zero();

        final List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElementAndChildren(task.getOrderElement());
        if (workReportLines.isEmpty()) {
            return result;
        }

        for (WorkReportLine workReportLine : workReportLines) {
            final LocalDate workReportLineDate = new LocalDate(workReportLine.getDate());
            if (date == null || workReportLineDate.compareTo(date) <= 0) {
                result = EffortDuration.sum(result, workReportLine.getEffort());
            }
        }
        return result;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getTotalPlannedHours() {
        return totalPlannedHours;
    }

    public void setTotalPlannedHours(Integer totalPlannedHours) {
        this.totalPlannedHours = totalPlannedHours;
    }

    public Integer getPartialPlannedHours() {
        return partialPlannedHours;
    }

    public void setPartialPlannedHours(Integer partialPlannedHours) {
        this.partialPlannedHours = partialPlannedHours;
    }

    public EffortDuration getRealHours() {
        return realHours;
    }

    public void setRealHours(EffortDuration realHours) {
        this.realHours = realHours;
    }

    public String getTaskName() {
        return taskName;
    }

}
