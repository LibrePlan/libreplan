/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.workreports.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.joda.time.LocalDate.Property;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;

/**
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class WorkReport extends IntegrationEntity implements
        IWorkReportsElements {

    public static final String DATE = "date";
    public static final String RESOURCE = "resource";
    public static final String ORDERELEMENT = "orderElement";

    public static WorkReport create() {
        return create(new WorkReport());
    }

    public static WorkReport create(WorkReportType workReportType) {
        return create(new WorkReport(workReportType));
    }

    public static WorkReport create(Date date,
            WorkReportType workReportType, Set<WorkReportLine> workReportLines,
            Resource resource, OrderElement orderElement) {
        WorkReport workReport = new WorkReport(date, workReportType,
                workReportLines, resource, orderElement);
        return create(workReport);
    }

    private Date date;

    private WorkReportType workReportType;

    private Resource resource;

    private OrderElement orderElement;

    private Set<Label> labels = new HashSet<Label>();

    private Set<WorkReportLine> workReportLines = new HashSet<WorkReportLine>();

    private Set<DescriptionValue> descriptionValues = new HashSet<DescriptionValue>();

    private Integer lastWorkReportLineSequenceCode = 0;
    /**
     * Constructor for hibernate. Do not use!
     */
    public WorkReport() {

    }

    private WorkReport(WorkReportType workReportType) {
        this.setWorkReportType(workReportType);
    }

    private WorkReport(Date date, WorkReportType workReportType,
            Set<WorkReportLine> workReportLines, Resource resource,
            OrderElement orderElement) {
        this.date = date;
        this.setWorkReportType(workReportType);
        this.workReportLines = workReportLines;
        this.resource = resource;
        this.orderElement = orderElement;
    }

    @Override
    public Date getDate() {
        return date != null ? new Date(date.getTime()) : null;
    }

    @Override
    public void setDate(Date date) {
        this.date = date != null ? new Date(date.getTime()) : null;
        if (workReportType != null) {
            if (workReportType.getDateIsSharedByLines()) {
                updateSharedDateByLines(date);
            } else {
                this.date = null;
            }
        }
    }

    @NotNull(message = "timesheet template not specified")
    public WorkReportType getWorkReportType() {
        return workReportType;
    }

    /**
     * Set the new {@link WorkReportType} and validate if the new
     * {@link WorkReportType} is different to the old {@link WorkReportType}.If
     * the new {@link WorkReportType} is different it updates the assigned
     * fields and labels of the new {@link WorkReportType}.
     * @param {@link WorkReportType}
     */
    public void setWorkReportType(WorkReportType workReportType) {
        this.workReportType = workReportType;

        updateSharedDateByLines(date);
        updateSharedResourceByLines(resource);
        updateSharedOrderElementByLines(orderElement);
        updateItsFieldsAndLabels(workReportType);
    }

    @Valid
    public Set<WorkReportLine> getWorkReportLines() {
        return Collections.unmodifiableSet(workReportLines);
    }

    public void addWorkReportLine(WorkReportLine workReportLine) {
        workReportLines.add(workReportLine);
    }

    public void removeWorkReportLine(WorkReportLine workReportLine) {
        workReportLines.remove(workReportLine);
    }

    @Override
    @Valid
    public Set<DescriptionValue> getDescriptionValues() {
        return Collections.unmodifiableSet(descriptionValues);
    }

    @Override
    public void setDescriptionValues(Set<DescriptionValue> descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    @Override
    public Set<Label> getLabels() {
        return labels;
    }

    @Override
    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        if (workReportType != null) {
            if (workReportType.getResourceIsSharedInLines()) {
                updateSharedResourceByLines(resource);
            } else {
                this.resource = null;
            }
        }
    }

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
        if (workReportType != null) {
            if (workReportType.getOrderElementIsSharedInLines()) {
                this.updateSharedOrderElementByLines(orderElement);
            } else {
                this.orderElement = null;
            }
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "date:the date must be not null if is shared by lines")
    public boolean checkConstraintDateMustBeNotNullIfIsSharedByLines() {
        if (!firstLevelValidationsPassed()) {
            return true;
        }

        if (workReportType.getDateIsSharedByLines()) {
            return (getDate() != null);
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "resource:the resource must be not null if is shared by lines")
    public boolean checkConstraintResourceMustBeNotNullIfIsSharedByLines() {
        if (!firstLevelValidationsPassed()) {
            return true;
        }

        if (workReportType.getResourceIsSharedInLines()) {
            return (getResource() != null);
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "orderElement:the order element must be not null if is shared by lines")
    public boolean checkConstraintOrderElementMustBeNotNullIfIsSharedByLines() {
        if (!firstLevelValidationsPassed()) {
            return true;
        }

        if (workReportType.getOrderElementIsSharedInLines()) {
            return (getOrderElement() != null);
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "label type:the timesheet have not assigned this label type")
    public boolean checkConstraintAssignedLabelTypes() {
        if (this.workReportType == null) {
            return true;
        }

        if (this.workReportType.getHeadingLabels().size() != this.labels.size()) {
            return false;
        }

        for (WorkReportLabelTypeAssigment typeAssigment : this.workReportType
                .getHeadingLabels()) {
            try {
                getLabelByType(typeAssigment.getLabelType());
            } catch (InstanceNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "description value:the timesheet have not assigned the description field")
    public boolean checkConstraintAssignedDescriptionValues() {
        if (this.workReportType == null) {
            return true;
        }

        if (this.workReportType.getHeadingFields().size() > this.descriptionValues
                .size()) {
            return false;
        }

        for(DescriptionField field : this.workReportType.getHeadingFields()){
            try{
                getDescriptionValueByFieldName(field.getFieldName());
            }
            catch(InstanceNotFoundException e){
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "There are repeated description values in the timesheet ")
    public boolean checkConstraintAssignedRepeatedDescriptionValues() {

        Set<String> textFields = new HashSet<String>();

        for (DescriptionValue v : this.descriptionValues) {

            String name = v.getFieldName();

            if (!StringUtils.isBlank(name)) {
                if (textFields.contains(name.toLowerCase())) {
                    return false;
                } else {
                    textFields.add(name.toLowerCase());
                }
            }
        }
        return true;
    }

    public DescriptionValue getDescriptionValueByFieldName(String fieldName)
            throws InstanceNotFoundException {

        if (StringUtils.isBlank(fieldName)) {
            throw new InstanceNotFoundException(fieldName,
                    DescriptionValue.class.getName());
        }

        for (DescriptionValue v : this.descriptionValues) {
            if (v.getFieldName().equalsIgnoreCase(StringUtils.trim(fieldName))) {
                return v;
            }
        }

        throw new InstanceNotFoundException(fieldName, DescriptionValue.class
                .getName());
    }

    public Label getLabelByType(LabelType type)
            throws InstanceNotFoundException {
        Validate.notNull(type);

        for (Label l : this.labels) {
            if (l.getType().getId().equals(type.getId())) {
                return l;
            }
        }

        throw new InstanceNotFoundException(type, LabelType.class.getName());
    }

    private void updateItsFieldsAndLabels(WorkReportType workReportType) {
        assignItsDescriptionValues(workReportType);
        assignItsLabels(workReportType);

        // it updates the fields and labels of its timesheet lines
        for (WorkReportLine line : getWorkReportLines()) {
            line.updateItsFieldsAndLabels();
        }
    }

    private void assignItsLabels(WorkReportType workReportType){
        if (workReportType != null) {
            labels.clear();
            for (WorkReportLabelTypeAssigment labelTypeAssigment : workReportType.getHeadingLabels()) {
                labels.add(labelTypeAssigment.getDefaultLabel());
            }
        }
    }

    private void assignItsDescriptionValues(WorkReportType workReportType) {
        if (workReportType != null) {
            descriptionValues.clear();
            for (DescriptionField descriptionField : workReportType
                    .getHeadingFields()) {
                DescriptionValue descriptionValue = DescriptionValue.create(
                        descriptionField.getFieldName(), null);
                descriptionValues.add(descriptionValue);
            }
        }
    }

    private void updateSharedDateByLines(Date date) {
        for (WorkReportLine line : getWorkReportLines()) {
            line.updateSharedDateByLines();
        }
    }

    private void updateSharedResourceByLines(Resource resource) {
        for (WorkReportLine line : getWorkReportLines()) {
            line.updateSharedResourceByLines();
        }
    }

    private void updateSharedOrderElementByLines(OrderElement orderElement) {
        for (WorkReportLine line : getWorkReportLines()) {
            line.updateSharedOrderElementByLines();
        }
    }

    @Override
    protected IWorkReportDAO getIntegrationEntityDAO() {
        return Registry.getWorkReportDAO();
    }

    private boolean firstLevelValidationsPassed() {
        return (workReportType != null);
    }

    public WorkReportLine getWorkReportLineByCode(String code)
            throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(code, WorkReportLine.class
                    .getName());
        }

        for (WorkReportLine l : this.workReportLines) {
            if (l.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return l;
            }
        }

        throw new InstanceNotFoundException(code, WorkReportLine.class
                .getName());

    }

    @AssertTrue(message = "The timesheet line codes must be unique.")
    public boolean checkConstraintNonRepeatedWorkReportLinesCodes() {
        return getFirstRepeatedCode(this.workReportLines) == null;
    }

    public void generateWorkReportLineCodes(int numberOfDigits) {
        for (WorkReportLine line : this.getWorkReportLines()) {
            if ((line.getCode() == null) || (line.getCode().isEmpty())
                    || (!line.getCode().startsWith(this.getCode()))) {
                this.incrementLastWorkReportLineSequenceCode();
                String lineCode = EntitySequence.formatValue(numberOfDigits,
                        this.getLastWorkReportLineSequenceCode());
                line.setCode(this.getCode()
                        + EntitySequence.CODE_SEPARATOR_CHILDREN + lineCode);
            }
        }
    }

    public void incrementLastWorkReportLineSequenceCode() {
        if(lastWorkReportLineSequenceCode==null){
            lastWorkReportLineSequenceCode = 0;
        }
        lastWorkReportLineSequenceCode++;
    }

    @NotNull(message = "last order element sequence code not specified")
    public Integer getLastWorkReportLineSequenceCode() {
        return lastWorkReportLineSequenceCode;
    }

    public EffortDuration getTotalEffortDuration() {
        EffortDuration result = EffortDuration.zero();
        for (WorkReportLine line : workReportLines) {
            result = result.plus(line.getEffort());
        }
        return result;
    }

    @AssertTrue(message = "only one timesheet line per day and task is allowed in monthly timesheets")
    public boolean checkConstraintOnlyOneWorkReportLinePerDayAndOrderElementInMonthlyTimesheet() {
        if (!getWorkReportType().isMonthlyTimesheetsType()) {
            return true;
        }

        Map<OrderElement, Set<LocalDate>> map = new HashMap<OrderElement, Set<LocalDate>>();
        for (WorkReportLine line : workReportLines) {
            OrderElement orderElement = line.getOrderElement();
            if (map.get(orderElement) == null) {
                map.put(orderElement, new HashSet<LocalDate>());
            }

            LocalDate date = LocalDate.fromDateFields(line.getDate());
            if (map.get(orderElement).contains(date)) {
                return false;
            }
            map.get(orderElement).add(date);
        }
        return true;
    }

    @AssertTrue(message = "all timesheet lines have to be in the same month in monthly timesheets")
    public boolean checkConstraintAllWorkReportLinesInTheSameMonthInMonthlyTimesheet() {
        if (!getWorkReportType().isMonthlyTimesheetsType()) {
            return true;
        }

        if (workReportLines.isEmpty()) {
            return true;
        }

        Property dayOfMonth = LocalDate.fromDateFields(
                workReportLines.iterator().next().getDate()).dayOfMonth();
        LocalDate min = dayOfMonth.withMinimumValue();
        LocalDate max = dayOfMonth.withMaximumValue();

        for (WorkReportLine line : workReportLines) {
            LocalDate date = LocalDate.fromDateFields(line.getDate());
            if ((date.compareTo(min) < 0) || (date.compareTo(max) > 0)) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "resource has to be bound to a user in monthly timesheets")
    public boolean checkConstraintResourceIsBoundInMonthlyTimesheet() {
        if (!getWorkReportType().isMonthlyTimesheetsType()) {
            return true;
        }

        if (resource != null) {
            try {
                Worker worker = Registry.getWorkerDAO().find(resource.getId());
                return worker.getUser() != null;
            } catch (InstanceNotFoundException e) {
                // Do nothing
            }
        }

        return false;
    }

}
