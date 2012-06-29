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

package org.libreplan.ws.workreports.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.ws.common.api.LabelReferenceDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.common.impl.LabelReferenceConverter;
import org.libreplan.ws.workreports.api.DescriptionValueDTO;
import org.libreplan.ws.workreports.api.IBindingOrderElementStrategy;
import org.libreplan.ws.workreports.api.OneOrderElementPerWorkReportLine;
import org.libreplan.ws.workreports.api.WorkReportDTO;
import org.libreplan.ws.workreports.api.WorkReportLineDTO;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.lang.Strings;

/**
 * Converter from/to work report related entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public final class WorkReportConverter {

    private static IBindingOrderElementStrategy bindingStrategy = OneOrderElementPerWorkReportLine
            .getInstance();

    public static WorkReport toEntity(WorkReportDTO workReportDTO)
            throws InstanceNotFoundException {

        WorkReport workReport = WorkReport.create();

        // Mandatory fields
        workReport.setCode(workReportDTO.code);

        try {
            WorkReportType workReportType = Registry.getWorkReportTypeDAO()
                    .findUniqueByCode(workReportDTO.workReportType);
            workReport.setWorkReportType(workReportType);
        } catch (InstanceNotFoundException e) {
            throw new ValidationException(
                    "There is no type of work report with this code");
        }

        for (WorkReportLineDTO workReportLineDTO : workReportDTO.workReportLines) {
            workReport
                    .addWorkReportLine(toEntity(workReportLineDTO, workReport));
        }

        // Optional fields
        if (workReportDTO.date != null) {
            workReport.setDate(DateConverter.toDate(workReportDTO.date));
        }

        bindingStrategy.assignOrderElementsToWorkReportLine(workReport,
                bindingStrategy.getOrderElementsBound(workReportDTO));

        if (workReportDTO.resource != null) {
            try {
                Resource resource = Registry.getResourceDAO().findByCode(
                        workReportDTO.resource);
                workReport.setResource(resource);
            } catch (InstanceNotFoundException e) {
                workReport.setResource(null);
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

        if (workReportDTO.labels != null && !workReportDTO.labels.isEmpty()) {
            try {
                workReport.setLabels(LabelReferenceConverter
                        .toEntity(workReportDTO.labels));
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(MessageFormat.format(
                        "There is no label with this code ",
                        (String) e.getKey()));
            }
        }

        if (workReportDTO.descriptionValues != null
                && !workReportDTO.descriptionValues.isEmpty()) {
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
        workReportLine.setCode(workReportLineDTO.code);
        workReportLine.setEffort(EffortDuration
                .parseFromFormattedString(workReportLineDTO.numHours));

        if (workReportLineDTO.typeOfWorkHours != null) {
            try {
                TypeOfWorkHours typeOfWorkHours = Registry
                        .getTypeOfWorkHoursDAO().findUniqueByCode(
                                workReportLineDTO.typeOfWorkHours);
                workReportLine.setTypeOfWorkHours(typeOfWorkHours);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no type of work hours with this code");
            }
        }

        // Optional fields
        if (workReportLineDTO.date != null) {
            workReportLine
                    .setDate(DateConverter.toDate(workReportLineDTO.date));
        }

        bindingStrategy.assignOrderElementsToWorkReportLine(workReportLine,
                bindingStrategy.getOrderElementsBound(workReportLineDTO));

        if (workReportLineDTO.resource != null) {
            try {
                Resource resource = Registry.getResourceDAO().findByCode(
                        workReportLineDTO.resource);
                workReportLine.setResource(resource);
            } catch (InstanceNotFoundException e) {
                workReportLine.setResource(null);
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

        if (workReportLineDTO.clockStart != null) {
            workReportLine.setClockStart(DateConverter
                    .toLocalTime(workReportLineDTO.clockStart));
        }
        if (workReportLineDTO.clockFinish != null) {
            workReportLine.setClockFinish(DateConverter
                    .toLocalTime(workReportLineDTO.clockFinish));
        }

        if (workReportLineDTO.labels != null
                && !workReportLineDTO.labels.isEmpty()) {
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

    public final static WorkReportDTO toDTO(WorkReport workReport) {

        String code = workReport.getCode();

        String workReportTypeCode = null;
        if (workReport.getWorkReportType() != null) {
            workReportTypeCode = workReport.getWorkReportType()
                    .getCode();
        } else {
            throw new ValidationException(
                    "missing work report code in a work report");
        }

        // Optional fields
        XMLGregorianCalendar date = null;
        if (workReport.getDate() != null) {
            date = DateConverter.toXMLGregorianCalendar(workReport.getDate());
        }

        String orderElementCode = bindingStrategy
                .getOrderElementCodesBound(workReport);

        String resourceCode = null;
        if ((workReport.getResource() != null)) {
            resourceCode = workReport.getResource().getCode();
        }

        Set<LabelReferenceDTO> labelDTOs = LabelReferenceConverter
                .toDTO(workReport.getLabels());
        if (labelDTOs.isEmpty()) {
            labelDTOs = null;
        }

        Set<DescriptionValueDTO> descriptionValuesDTOs = toDTO(workReport
                .getDescriptionValues());
        if (descriptionValuesDTOs.isEmpty()) {
            descriptionValuesDTOs = null;
        }

        Set<WorkReportLineDTO> workReportLineDTOs = new HashSet<WorkReportLineDTO>();

        for (WorkReportLine line : workReport.getWorkReportLines()) {
            workReportLineDTOs.add(toDTO(line));
        }
        if (workReportLineDTOs.isEmpty()) {
            workReportLineDTOs = null;
        }

        return new WorkReportDTO(code, workReportTypeCode, date, resourceCode,
                orderElementCode, labelDTOs, descriptionValuesDTOs,
                workReportLineDTOs);

    }

    public final static WorkReportLineDTO toDTO(WorkReportLine line){
        String code = line.getCode();
        XMLGregorianCalendar date = DateConverter.toXMLGregorianCalendar(line
                .getDate());

        String resource = null;
        if (line.getResource() != null) {
            resource = line.getResource().getCode();
        }

        String orderElement = bindingStrategy.getOrderElementCodesBound(line);

        String typeOfWorkHours = null;
        if(line.getTypeOfWorkHours() != null){
            typeOfWorkHours = line.getTypeOfWorkHours().getCode();
        }

        XMLGregorianCalendar clockStart = null;
        if(line.getClockStart() != null){
            clockStart = DateConverter.toXMLGregorianCalendar(line.getClockStart());
        }

        XMLGregorianCalendar clockFinish = null;
        if(line.getClockFinish() != null){
            clockFinish = DateConverter.toXMLGregorianCalendar(line
                    .getClockFinish());
        }

        String numHours = null;
        if (line.getEffort() != null) {
            numHours = line.getEffort().toFormattedString();
        }

        Set<LabelReferenceDTO> labelDTOs = LabelReferenceConverter.toDTO(line
                .getLabels());
        if(labelDTOs.isEmpty()){
            labelDTOs = null;
        }

        Set<DescriptionValueDTO> descriptionValuesDTOs = toDTO(line
                .getDescriptionValues());
        if (descriptionValuesDTOs.isEmpty()) {
            descriptionValuesDTOs = null;
        }

        WorkReportLineDTO workReportLineDTO = new WorkReportLineDTO(code, date,
                resource, orderElement, typeOfWorkHours, clockStart,
                clockFinish, numHours, labelDTOs, descriptionValuesDTOs);

        return workReportLineDTO;
    }

    private static Set<DescriptionValueDTO> toDTO(
            Set<DescriptionValue> descriptionValues) {
        Set<DescriptionValueDTO> result = new HashSet<DescriptionValueDTO>();
        for (DescriptionValue descriptionValue : descriptionValues) {
            result.add(toDTO(descriptionValue));
        }
        return result;
    }

    private static DescriptionValueDTO toDTO(DescriptionValue descriptionValue) {
        return new DescriptionValueDTO(descriptionValue.getFieldName(),
                descriptionValue.getValue());
    }


    public final static void updateWorkReport(WorkReport workReport,
            WorkReportDTO workReportDTO) throws ValidationException {

        if (StringUtils.isBlank(workReportDTO.code)) {
            throw new ValidationException("missing code in a work report.");
        }

        /*
         * 1: Update the existing work report line or add new
         * work report line.
         */
        for (WorkReportLineDTO lineDTO : workReportDTO.workReportLines) {

            /* Step 1.1: requires each work report line DTO to have a code. */
            if (StringUtils.isBlank(lineDTO.code)) {
                throw new ValidationException(
                        "missing code in a work report line");
            }

            try {
                WorkReportLine line = workReport
                        .getWorkReportLineByCode(lineDTO.code);
                updateWorkReportLine(line, lineDTO);
            } catch (InstanceNotFoundException e) {
                try {
                    workReport.addWorkReportLine(toEntity(lineDTO, workReport));
                } catch (InstanceNotFoundException o) {
                    throw new ValidationException(
                            "missing type of work hours in a work report line");
                }
            }
        }

        /*
         * 2: Update the existing labels
         */
        if (workReportDTO.labels != null) {
            for (LabelReferenceDTO labelDTO : workReportDTO.labels) {

                /* Step 2.1: requires each label reference DTO to have a code. */
                if (StringUtils.isBlank(labelDTO.code)) {
                    throw new ValidationException("missing code in a label");
                }

                try {
                    Set<Label> labels = workReport.getLabels();
                    updateLabel(labelDTO, labels);
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(
                            "work report has not this label type assigned");
                }
            }
        }

        /*
         * 3: Update the existing description values
         */
        if (workReportDTO.descriptionValues != null) {
            for (DescriptionValueDTO valueDTO : workReportDTO.descriptionValues) {

                /* Step 3.1: requires each description value DTO to have a code. */
                if (StringUtils.isBlank(valueDTO.fieldName)) {
                    throw new ValidationException(
                            "missing field name in a description value");
                }

                try {
                    DescriptionValue value = workReport
                            .getDescriptionValueByFieldName(valueDTO.fieldName);
                    value.setValue(StringUtils.trim(valueDTO.value));
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(
                            "work report has not any description value with this field name");
                }
            }
        }

        /*
         * 4: Update basic properties in existing work report
         */

        /* Step 4.1: Update the date. */
        Date date = DateConverter.toDate(workReportDTO.date);
        workReport.setDate(date);

        /* Step 4.2: Update the resource. */
        String resourceCode = workReportDTO.resource;
        if (!Strings.isBlank(resourceCode)) {
            try {
                Resource resource = Registry.getResourceDAO().findByCode(
                        resourceCode);
                workReport.setResource(resource);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

        /* Step 4.3: Update the order element. */
        String orderElementCode = workReportDTO.orderElement;
        if ((orderElementCode != null) && (!orderElementCode.isEmpty())) {
            try {
                OrderElement orderElement = Registry.getOrderElementDAO()
                    .findUniqueByCode(orderElementCode);
                workReport.setOrderElement(orderElement);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException("There is no task with this code");
            }
        }
    }

    public final static void updateWorkReportLine(
            WorkReportLine workReportLine, WorkReportLineDTO workReportLineDTO)
            throws ValidationException {

        /*
         * 1: Update the existing labels
         */
        if (workReportLineDTO.labels != null) {
                for (LabelReferenceDTO labelDTO : workReportLineDTO.labels) {

                // * Step 2.1: requires each label reference DTO to have a code.
                // */
                if (StringUtils.isBlank(labelDTO.code)) {
                    throw new ValidationException("missing code in a label");
                }

                try {
                    Set<Label> labels = workReportLine.getLabels();
                    updateLabel(labelDTO, labels);
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(
                            "there are not work report lines with assigned labels of this type");
                }
            }
        }
        /*
         * 2: Update the existing description values
         */
        updateDescriptionValues(workReportLineDTO.descriptionValues,
                workReportLine);

        /*
         * 3: Update basic properties in existing work report line
         */

        /* Step 3.1: Update the date. */
        Date date = DateConverter.toDate(workReportLineDTO.date);
        workReportLine.setDate(date);

        /* Step 3.2: Update the resource. */
        String resourceCode = workReportLineDTO.resource;
        if (!Strings.isBlank(resourceCode)) {
            try {
                Resource resource = Registry.getResourceDAO().findByCode(
                        resourceCode);
                workReportLine.setResource(resource);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

        /* Step 3.3: Update the order element. */
        String orderElementCode = workReportLineDTO.orderElement;
        try {
            OrderElement orderElement = Registry.getOrderElementDAO()
                    .findUniqueByCode(orderElementCode);
            workReportLine.setOrderElement(orderElement);
        } catch (InstanceNotFoundException e) {
            throw new ValidationException("There is no task with this code");
        }

        /* Step 3.4: Update the type of work hours. */
        if(workReportLineDTO.typeOfWorkHours != null){
            try{
                TypeOfWorkHours typeOfWorkHours = Registry.getTypeOfWorkHoursDAO().findUniqueByCode(workReportLineDTO.typeOfWorkHours);
                workReportLine.setTypeOfWorkHours(typeOfWorkHours);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no type of work hours with this code");
            }
        }

        /*
         * Step 3.4: Update the clock start and the clock end and the number of
         * hours.
         */
        if (workReportLineDTO.clockStart != null) {
            workReportLine.setClockStart(DateConverter
                    .toLocalTime(workReportLineDTO.clockStart));
        }
        if (workReportLineDTO.clockFinish != null) {
            workReportLine.setClockFinish(DateConverter
                    .toLocalTime(workReportLineDTO.clockFinish));
        }

        if (workReportLineDTO.numHours != null) {
            workReportLine.setEffort(EffortDuration
                    .parseFromFormattedString(workReportLineDTO.numHours));
        }

    }

    private static void updateDescriptionValues(
            Set<DescriptionValueDTO> descriptionValues,
            WorkReportLine workReportLine) {
        if (descriptionValues != null) {
            for (DescriptionValueDTO valueDTO : descriptionValues) {

                    /* Step 3.1: requires each description value DTO to have a code. */
                if (StringUtils.isBlank(valueDTO.fieldName)) {
                    throw new ValidationException(
                            "missing field name in a description value");
                }

                try {
                    DescriptionValue value = workReportLine
                        .getDescriptionValueByFieldName(valueDTO.fieldName);
                    value.setValue(StringUtils.trim(valueDTO.value));
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(
                            "work report have not any description value with this field name");
                }
            }
        }
    }

    @Transactional
    private static void updateLabel(LabelReferenceDTO labelDTO,
            Set<Label> labels)
            throws InstanceNotFoundException {
        Label labelToAdd = Registry.getLabelDAO().findByCode(labelDTO.code);
        LabelType labelType = labelToAdd.getType();

        Label labelToChange = getLabelByLabelType(labels, labelType);
        if (labelToAdd.getCode() != labelToChange.getCode()) {
            labels.remove(labelToChange);
            labels.add(labelToAdd);
        }
    }

    private static Label getLabelByLabelType(Set<Label> labels, LabelType type)
            throws InstanceNotFoundException {

        Validate.notNull(type);

        for (Label l : labels) {
            if (l.getType().equals(type)) {
                return l;
            }
        }

        throw new InstanceNotFoundException(type, LabelType.class.getName());

    }

}
