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

package org.navalplanner.business.workreports.entities;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class WorkReportLabelTypeAssigment extends BaseEntity {

    public static WorkReportLabelTypeAssigment create(boolean newObject) {
        WorkReportLabelTypeAssigment workReportLabelTypeAssigment = new WorkReportLabelTypeAssigment(
                newObject);
        workReportLabelTypeAssigment.setNewObject(true);
        return workReportLabelTypeAssigment;
    }

    public static WorkReportLabelTypeAssigment create(
            boolean labelsSharedByLines, boolean newObject) {
        WorkReportLabelTypeAssigment workReportLabelTypeAssigment = new WorkReportLabelTypeAssigment(
                labelsSharedByLines, newObject);
        workReportLabelTypeAssigment.setNewObject(true);
        return workReportLabelTypeAssigment;
    }

    public WorkReportLabelTypeAssigment() {
    }

    public WorkReportLabelTypeAssigment(boolean labelsSharedByLines,
            boolean newObject) {
        this.labelsSharedByLines = labelsSharedByLines;
        this.newObject = newObject;
    }

    public WorkReportLabelTypeAssigment(boolean newObject) {
        this.newObject = newObject;
    }

    public boolean isNewObject() {
        return newObject;
    }

    private boolean newObject = false;

    private Boolean labelsSharedByLines = false;

    @NotNull
    private LabelType labelType;

    @NotNull
    private Label defaultLabel;

    private PositionInWorkReportEnum position = PositionInWorkReportEnum.LINE;

    public PositionInWorkReportEnum getPosition() {
        return position;
    }

    public void setPosition(PositionInWorkReportEnum position) {
        this.position = position;
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

    public void setLabelsSharedByLines(boolean labelsSharedByLines) {
        this.labelsSharedByLines = labelsSharedByLines;
    }
}
