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

package org.libreplan.business.workreports.valueobjects;

import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.libreplan.business.INewObject;

/**
 * Value Object.
 * <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class DescriptionField implements INewObject {

    public static DescriptionField create() {
        DescriptionField descriptionField = new DescriptionField();
        descriptionField.setNewObject(true);
        return descriptionField;
    }

    public static DescriptionField create(String fieldName, Integer length) {
        DescriptionField descriptionField = new DescriptionField(fieldName, length);
        descriptionField.setNewObject(true);
        return descriptionField;
    }

    public DescriptionField() {
    }

    private DescriptionField(String fieldName, Integer length) {
        this.fieldName = fieldName;
        this.length = length;
    }

    private String fieldName;

    private Integer length;

    private boolean newObject = false;

    public boolean isNewObject() {
        return newObject;
    }

    public boolean setNewObject(boolean newObject) {
        return this.newObject = newObject;
    }

    @NotEmpty(message = "field name not specified or empty")
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Min(message = "length less than 1", value = 1)
    @NotNull(message = "length not specified")
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    private Integer positionNumber;

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
        this.positionNumber = positionNumber;
    }

}
