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

package org.navalplanner.business.workreports.ValueObjects;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.INewObject;

/**
 * Value Object <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class DescriptionField implements INewObject {

    public static DescriptionField create() {
        DescriptionField descriptionField = new DescriptionField();
        descriptionField.setNewObject(true);
        return descriptionField;
    }

    public static DescriptionField create(String fieldName, Integer lenght) {
        DescriptionField descriptionField = new DescriptionField(fieldName,
                lenght);
        descriptionField.setNewObject(true);
        return descriptionField;
    }

    private DescriptionField() {
    }

    private DescriptionField(String fieldName, Integer lenght) {
        this.fieldName = fieldName;
        this.length = lenght;
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

    @NotEmpty
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Min(value = 1)
    @NotNull
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer lenght) {
        this.length = lenght;
    }

}
