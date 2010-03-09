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

package org.navalplanner.ws.workreports.impl;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.business.workreports.valueobjects.DescriptionValue;
import org.navalplanner.ws.common.impl.LabelReferenceConverter;
import org.navalplanner.ws.workreports.api.DescriptionValueDTO;
import org.navalplanner.ws.workreports.api.WorkReportDTO;
import org.navalplanner.ws.workreports.api.WorkReportLineDTO;

/**
 * Converter from/to work report related entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class WorkReportConverter {

    public static WorkReport toEntity(WorkReportDTO workReportDTO)
            throws InstanceNotFoundException {
        WorkReport workReport = WorkReport.create();

        // Mandatory fields
        workReport.setCode(workReportDTO.code);

        WorkReportType workReportType = Registry.getWorkReportTypeDAO()
                .findUniqueByCode(workReportDTO.workReportType);
        workReport.setWorkReportType(workReportType);

        for (WorkReportLineDTO workReportLineDTO : workReportDTO.workReportLines) {
            workReport
                    .addWorkReportLine(toEntity(workReportLineDTO, workReport));
        }

        // Optional fields
        if (workReportDTO.date != null) {
            workReport.setDate(workReportDTO.date);
        }

        if (workReportDTO.orderElement != null) {
            OrderElement orderElement = Registry.getOrderElementDAO()
                    .findUniqueByCode(workReportDTO.orderElement);
            workReport.setOrderElement(orderElement);
        }

        if (workReportDTO.resource != null) {
            Worker worker = Registry.getWorkerDAO().findUniqueByNif(
                    workReportDTO.resource);
            workReport.setResource(worker);
        }

        if (workReportDTO.labels != null) {
            workReport.setLabels(LabelReferenceConverter.toEntity(workReportDTO.labels));
        }

        if (workReportDTO.descriptionValues != null) {
            workReport
                    .setDescriptionValues(toEntity(workReportDTO.descriptionValues));
        }

        return workReport;
    }

    private static WorkReportLine toEntity(WorkReportLineDTO workReportLineDTO,
            WorkReport workReport)
            throws InstanceNotFoundException {
        WorkReportLine workReportLine = WorkReportLine.create(workReport);

        // Mandatory fields
        workReportLine.setNumHours(workReportLineDTO.numHours);

        TypeOfWorkHours typeOfWorkHours = Registry.getTypeOfWorkHoursDAO()
                .findUniqueByCode(workReportLineDTO.typeOfWorkHours);
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);

        // Optional fields
        if (workReportLineDTO.date != null) {
            workReportLine.setDate(workReportLineDTO.date);
        }

        if (workReportLineDTO.orderElement != null) {
            OrderElement orderElement = Registry.getOrderElementDAO()
                    .findUniqueByCode(workReportLineDTO.orderElement);
            workReportLine.setOrderElement(orderElement);
        }

        if (workReportLineDTO.resource != null) {
            Worker worker = Registry.getWorkerDAO().findUniqueByNif(
                    workReportLineDTO.resource);
            workReportLine.setResource(worker);
        }

        if (workReportLineDTO.clockStart != null) {
            workReportLine.setClockStart(workReportLineDTO.clockStart);
        }
        if (workReportLineDTO.clockFinish != null) {
            workReportLine.setClockFinish(workReportLineDTO.clockFinish);
        }

        if (workReportLineDTO.labels != null) {
            workReportLine.setLabels(LabelReferenceConverter
                    .toEntity(workReportLineDTO.labels));
        }

        if (workReportLineDTO.descriptionValues != null) {
            workReportLine
                    .setDescriptionValues(toEntity(workReportLineDTO.descriptionValues));
        }

        return workReportLine;
    }

    private static Set<DescriptionValue> toEntity(
            Set<DescriptionValueDTO> descriptionValues) {
        Set<DescriptionValue> result = new HashSet<DescriptionValue>();
        for (DescriptionValueDTO descriptionValueDTO : descriptionValues) {
            result.add(toEntity(descriptionValueDTO));
        }
        return result;
    }

    private static DescriptionValue toEntity(
            DescriptionValueDTO descriptionValueDTO) {
        return DescriptionValue.create(descriptionValueDTO.fieldName,
                descriptionValueDTO.value);
    }

}
