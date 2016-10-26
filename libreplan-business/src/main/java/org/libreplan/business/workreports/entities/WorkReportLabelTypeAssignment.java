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

package org.libreplan.business.workreports.entities;

import javax.validation.constraints.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLabelTypeAssignment extends BaseEntity implements Comparable {

    private Boolean labelsSharedByLines = false;

    @NotNull(message = "label type not specified")
    private LabelType labelType;

    @NotNull(message = "default label not specified")
    private Label defaultLabel;

    private Integer positionNumber;

    public WorkReportLabelTypeAssignment() {}

    public WorkReportLabelTypeAssignment(boolean labelsSharedByLines) {
        this.labelsSharedByLines = labelsSharedByLines;
    }

    public static WorkReportLabelTypeAssignment create() {
        WorkReportLabelTypeAssignment workReportLabelTypeAssignment = new WorkReportLabelTypeAssignment();
        workReportLabelTypeAssignment.setNewObject(true);

        return workReportLabelTypeAssignment;
    }

    public static WorkReportLabelTypeAssignment create(boolean labelsSharedByLines) {
        WorkReportLabelTypeAssignment workReportLabelTypeAssignment =
                new WorkReportLabelTypeAssignment(labelsSharedByLines);

        workReportLabelTypeAssignment.setNewObject(true);

        return workReportLabelTypeAssignment;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }

    public Label getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(Label defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

    public Boolean getLabelsSharedByLines() {
        return labelsSharedByLines == null ? false : labelsSharedByLines;
    }

    void setLabelsSharedByLines(boolean labelsSharedByLines) {
        this.labelsSharedByLines = labelsSharedByLines;
    }

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
        this.positionNumber = positionNumber;
    }

    @Override
    public int compareTo(Object arg0) {
        return labelType != null
                ? labelType.compareTo(((WorkReportLabelTypeAssignment) arg0).getLabelType())
                : -1;
    }

}
