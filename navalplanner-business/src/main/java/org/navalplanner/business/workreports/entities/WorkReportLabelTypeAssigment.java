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

package org.navalplanner.business.workreports.entities;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLabelTypeAssigment extends BaseEntity implements
        Comparable {

    public static WorkReportLabelTypeAssigment create() {
        WorkReportLabelTypeAssigment workReportLabelTypeAssigment = new WorkReportLabelTypeAssigment();
        workReportLabelTypeAssigment.setNewObject(true);
        return workReportLabelTypeAssigment;
    }

    public static WorkReportLabelTypeAssigment create(
            boolean labelsSharedByLines) {
        WorkReportLabelTypeAssigment workReportLabelTypeAssigment = new WorkReportLabelTypeAssigment(
                labelsSharedByLines);
        workReportLabelTypeAssigment.setNewObject(true);
        return workReportLabelTypeAssigment;
    }

    public WorkReportLabelTypeAssigment() {

    }

    public WorkReportLabelTypeAssigment(boolean labelsSharedByLines) {
        this.labelsSharedByLines = labelsSharedByLines;
    }

    private Boolean labelsSharedByLines = false;

    @NotNull(message = "label type not specified")
    private LabelType labelType;

    @NotNull(message = "default label not specified")
    private Label defaultLabel;

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

    /* Methos for the index */

    private Integer positionNumber;

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
        this.positionNumber = positionNumber;
    }

    @Override
    public int compareTo(Object arg0) {
        if (labelType != null) {
            return labelType.compareTo(
                    ((WorkReportLabelTypeAssigment) arg0).getLabelType());
        }
        return -1;
    }

}
