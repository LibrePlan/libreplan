/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.reports.dtos;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.common.Registry;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskStatusEnum;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class WorkingArrangementPerOrderDTO {

    private IWorkReportLineDAO workReportLineDAO;

    private String orderCode;

    private String orderName;

    private Date estimatedStartingDate;

    private Date estimatedEndingDate;

    private Date firstWorkReportDate;

    private Date lastWorkReportDate;

    private Date deadline;

    private BigDecimal measuredProgress;

    private String status;

    private Boolean overrun;

    private String dependencyName;

    private String dependencyCode;

    private String dependencyType;

    private BigDecimal dependencyProgress;

    private Boolean hasDependencies = false;

    private WorkingArrangementPerOrderDTO() {
        workReportLineDAO = Registry.getWorkReportLineDAO();
    }

    public WorkingArrangementPerOrderDTO(Date deadLineOrder, Task task,
            TaskStatusEnum taskStatus,
            Boolean hasDependencies) {
        this();

        final OrderElement orderElement = task.getOrderElement();
        this.orderCode = orderElement.getCode();
        this.orderName = orderElement.getName();
        this.estimatedStartingDate = task.getStartDate();
        this.estimatedEndingDate = task.getEndDate();

        // Calculate date for first and last work reports
        final List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElementAndChildren(orderElement, true);
        if (!workReportLines.isEmpty()) {
            final WorkReportLine firstWorkReportLine = workReportLines.get(0);
            this.firstWorkReportDate = firstWorkReportLine.getDate();

            final WorkReportLine lastWorkReportLine = workReportLines
                    .get(workReportLines.size() - 1);
            this.lastWorkReportDate = lastWorkReportLine.getDate();
        }

        if (orderElement.getDeadline() == null) {
            this.deadline = deadLineOrder;
        } else {
            this.deadline = orderElement.getDeadline();
        }

        this.measuredProgress = getAdvanceSpread(orderElement);
        this.status = (taskStatus != null) ? taskStatus.toString() : "";
        this.overrun = calculateOverrun();
        this.hasDependencies = hasDependencies;
    }

    public WorkingArrangementPerOrderDTO(Task task,
            TaskStatusEnum taskStatus,
            DependencyWorkingArrangementDTO dependencyDTO) {

        this(null, task, taskStatus, true);

        this.dependencyName = dependencyDTO.getName();
        this.dependencyCode = dependencyDTO.getCode();
        this.dependencyType = dependencyDTO.getType();
        this.dependencyProgress = dependencyDTO.getAdvance();
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getOrderName() {
        return orderName;
    }

    public Date getEstimatedStartingDate() {
        return estimatedStartingDate;
    }

    public Date getEstimatedEndingDate() {
        return estimatedEndingDate;
    }

    public Date getFirstWorkReportDate() {
        return firstWorkReportDate;
    }

    public Date getLastWorkReportDate() {
        return lastWorkReportDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public BigDecimal getMeasuredProgress() {
        return measuredProgress;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getOverrun() {
        return overrun;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public String getDependencyCode() {
        return dependencyCode;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public BigDecimal getDependencyProgress() {
        return dependencyProgress;
    }

    /**
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    public static class DependencyWorkingArrangementDTO {

        private String name;

        private String code;

        private String type;

        private BigDecimal advance;

        public DependencyWorkingArrangementDTO(String name, String code, String type, BigDecimal advance) {
            this.name = name;
            this.code = code;
            this.type = type;
            this.advance = advance;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getAdvance() {
            return advance;
        }

    }

    public Boolean getHasDependencies() {
        return hasDependencies;
    }

    private BigDecimal getAdvanceSpread(OrderElement order) {
        BigDecimal advance = order.getAdvancePercentage();
        if (advance != null) {
            return advance;
        }
        return BigDecimal.ZERO;
    }

    private boolean calculateOverrun() {
        if (this.deadline != null) {
            return (new Date()).compareTo(this.deadline) > 0;
        }
        return false;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
