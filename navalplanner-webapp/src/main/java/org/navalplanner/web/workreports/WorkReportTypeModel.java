/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.web.workreports;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.workreports.ValueObjects.DescriptionField;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.PositionInWorkReportEnum;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link WorkReportType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkReportTypeModel implements IWorkReportTypeModel {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private ILabelDAO labelDAO;

    private WorkReportType workReportType;

    private boolean editing = false;

    private static final Map<LabelType, List<Label>> mapLabels = new HashMap<LabelType, List<Label>>();

    @Override
    public WorkReportType getWorkReportType() {
        return this.workReportType;
    }

    @Override
    public Map<LabelType, List<Label>> getMapLabelTypes() {
        final Map<LabelType, List<Label>> result = new HashMap<LabelType, List<Label>>();
        result.putAll(mapLabels);
        return result;
    }

    @Transactional(readOnly = true)
    public boolean thereAreWorkReportsFor() {
        return thereAreWorkReportsFor(getWorkReportType());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean thereAreWorkReportsFor(WorkReportType workReportType) {
        if (isEditing()) {
            final List<WorkReport> workReports = workReportDAO
                    .getAllByWorkReportType(workReportType);
            return (workReports != null && !workReports.isEmpty());
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeDAO.list(WorkReportType.class);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate() {
        loadLabels();
        editing = false;
        this.workReportType = WorkReportType.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(WorkReportType workReportType) {
        editing = true;
        Validate.notNull(workReportType);
        loadLabels();
        this.workReportType = getFromDB(workReportType);
    }

    private WorkReportType getFromDB(WorkReportType workReportType) {
        return getFromDB(workReportType.getId());
    }

    @Transactional(readOnly = true)
    private WorkReportType getFromDB(Long id) {
        try {
            WorkReportType result = workReportTypeDAO.find(id);
            loadCollections(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCollections(WorkReportType workReportType) {
        for (DescriptionField line : workReportType.getLineFields()) {
            line.getFieldName();
        }

        for (DescriptionField head : workReportType.getHeadingFields()) {
            head.getFieldName();
        }

        for (WorkReportLabelTypeAssigment assignedLabel : workReportType
                .getWorkReportLabelTypeAssigments()) {
            assignedLabel.getDefaultLabel().getName();
            assignedLabel.getLabelType().getName();
        }
    }

    private void loadLabels() {
        mapLabels.clear();
        List<LabelType> labelTypes = labelTypeDAO.getAll();
        for (LabelType labelType : labelTypes) {
            List<Label> labels = new ArrayList<Label>(labelDAO
                    .findByType(labelType));

            mapLabels.put(labelType, labels);
        }
    }

    @Override
    public void prepareForRemove(WorkReportType workReportType) {
        this.workReportType = workReportType;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        workReportTypeDAO.save(workReportType);
    }

    @Override
    @Transactional
    public void confirmRemove(WorkReportType workReportType) {
        try {
            workReportTypeDAO.remove(workReportType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

    /* Operations to manage the Description field */

    public List<DescriptionField> getDescriptionFields() {
        List<DescriptionField> descriptionFields = new ArrayList<DescriptionField>();
        if (getWorkReportType() != null) {
            descriptionFields.addAll(workReportType.getLineFields());
            descriptionFields.addAll(workReportType.getHeadingFields());
        }
        return descriptionFields;
    }

    public void addNewDescriptionField() {
        DescriptionField descriptionField = DescriptionField.create();
        workReportType.getLineFields().add(descriptionField);
    }

    public void removeDescriptionField(DescriptionField descriptionField) {
        if (isHeadingDescriptionField(descriptionField)) {
            workReportType.getHeadingFields().remove(descriptionField);
        } else {
            workReportType.getLineFields().remove(descriptionField);
        }
    }

    public void changePositionDescriptionField(
            PositionInWorkReportEnum newPosition,
            DescriptionField descriptionField) {
        removeDescriptionField(descriptionField);
        if (newPosition.equals(PositionInWorkReportEnum.HEADING)) {
            workReportType.getHeadingFields().add(descriptionField);
        } else {
            workReportType.getLineFields().add(descriptionField);
        }
    }

    public PositionInWorkReportEnum getPosition(
            DescriptionField descriptionField) {
        if (workReportType.getHeadingFields().contains(descriptionField)) {
            return PositionInWorkReportEnum.HEADING;
        } else {
            return PositionInWorkReportEnum.LINE;
        }
    }

    public boolean isHeadingDescriptionField(DescriptionField descriptionField) {
        return workReportType.getHeadingFields().contains(descriptionField);
    }

    /* Operations to manage the WorkReportLabelTypesAssigment */

    public Set<WorkReportLabelTypeAssigment> getWorkReportLabelTypeAssigments() {
        if (getWorkReportType() != null) {
            return getWorkReportType().getWorkReportLabelTypeAssigments();
        }
        return new HashSet<WorkReportLabelTypeAssigment>();
    }

    public void addNewWorkReportLabelTypeAssigment() {
        if (getWorkReportType() != null) {
            WorkReportLabelTypeAssigment newWorkReportLabelTypeAssigment = WorkReportLabelTypeAssigment
                    .create();
            getWorkReportType().getWorkReportLabelTypeAssigments().add(
                newWorkReportLabelTypeAssigment);
        }
    }

    public void removeWorkReportLabelTypeAssigment(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        workReportType.getWorkReportLabelTypeAssigments().remove(
                workReportLabelTypeAssigment);
    }

    public PositionInWorkReportEnum getLabelAssigmentPosition(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        if (getWorkReportType() != null) {
            return getPosition(workReportLabelTypeAssigment
                    .getLabelsSharedByLines());
        }
        return null;
    }

    public void setLabelAssigmentPosition(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment,
            PositionInWorkReportEnum position) {
        workReportLabelTypeAssigment
                .setLabelsSharedByLines(isSharedByLines(position));
    }

    /* Operation to manage the requirements fields */
    @Override
    public PositionInWorkReportEnum getDatePosition() {
        if (getWorkReportType() != null) {
            return getPosition(getWorkReportType().getDateIsSharedByLines());
        }
        return null;
    }

    @Override
    public PositionInWorkReportEnum getResourcePosition() {
        if (getWorkReportType() != null) {
            return getPosition(getWorkReportType().getResourceIsSharedInLines());
        }
        return null;
    }

    @Override
    public PositionInWorkReportEnum getOrderElementPosition() {
        if (getWorkReportType() != null) {
            return getPosition(getWorkReportType()
                    .getOrderElementIsSharedInLines());
        }
        return null;
    }

    private PositionInWorkReportEnum getPosition(boolean sharedByLines) {
        if (sharedByLines)
            return PositionInWorkReportEnum.HEADING;
        else
            return PositionInWorkReportEnum.LINE;
    }

    @Override
    public void setDatePosition(PositionInWorkReportEnum position) {
        getWorkReportType().setDateIsSharedByLines(isSharedByLines(position));
    }

    @Override
    public void setResourcePosition(PositionInWorkReportEnum position) {
        getWorkReportType().setResourceIsSharedInLines(
                isSharedByLines(position));
    }

    @Override
    public void setOrderElementPosition(PositionInWorkReportEnum position) {
        getWorkReportType().setOrderElementIsSharedInLines(
                isSharedByLines(position));
    }

    private boolean isSharedByLines(PositionInWorkReportEnum position) {
        return PositionInWorkReportEnum.HEADING.equals(position);
    }

    /* Operations that realize the data validations */

    @Transactional(readOnly = true)
    public void validateWorkReportTypeName(String name)
            throws IllegalArgumentException {
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException(
                    _("the name must be not null or not empty"));
        }

        getWorkReportType().setName(name);
        if (!getWorkReportType().checkConstraintUniqueWorkReportTypeName()) {
            throw new IllegalArgumentException(
                    _("Exist other workReportType with the same name."));
        }
        // for (WorkReportType workReportType : getWorkReportTypes()) {
        // if ((!workReportType.getId().equals(getWorkReportType().getId()))
        // && (workReportType.getName().equals(name))) {
        // throw new IllegalArgumentException(
        // _("Exist other workReportType with the same name."));
        // }
        // }
    }

    @Transactional(readOnly = true)
    public void validateWorkReportTypeCode(String code)
            throws IllegalArgumentException {
        if ((code == null) || (code.isEmpty())) {
            throw new IllegalArgumentException(
                    _("the code must be not null or not empty"));
        }
        if (code.contains("_")) {
            throw new IllegalArgumentException(
                    _("Value is not valid.\n Code cannot contain chars like '_'."));
        }

        getWorkReportType().setCode(code);
        if (!getWorkReportType().checkConstraintUniqueWorkReportTypeCode()) {
            throw new IllegalArgumentException(
                    _("Exist other workReportType with the same code."));
        }
        // for (WorkReportType workReportType : getWorkReportTypes()) {
        // if ((!workReportType.getId().equals(getWorkReportType().getId()))
        // && (workReportType.getCode().equals(code))) {
        // throw new IllegalArgumentException(
        // _("Exist other workReportType with the same code."));
        // }
        // }
    }

    public DescriptionField validateLengthLineFields() {
        for(DescriptionField line : getWorkReportType().getLineFields()){
            if ((line.getLength() == null) || (line.getLength() <= 0)) {
                return line;
            }
        }
        return null;
    }

    public DescriptionField validateFieldNameLineFields() {
        for (DescriptionField line : getDescriptionFields()) {
            if ((line.getFieldName() == null)
                    || (line.getFieldName().isEmpty())
                    || (getWorkReportType().existSameFieldName(line))) {
                return line;
            }
        }
        return null;
    }

    public WorkReportLabelTypeAssigment validateLabelTypes() {
        for (WorkReportLabelTypeAssigment labelTypeAssigment : getWorkReportLabelTypeAssigments()) {
            if (labelTypeAssigment.getLabelType() == null) {
                return labelTypeAssigment;
            }
        }
        return null;
    }

    public WorkReportLabelTypeAssigment validateLabels() {
        for (WorkReportLabelTypeAssigment labelTypeAssigment : getWorkReportLabelTypeAssigments()) {
            if (labelTypeAssigment.getDefaultLabel() == null) {
                return labelTypeAssigment;
            }
        }
        return null;
    }

}
