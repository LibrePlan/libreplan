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

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.libreplan.business.INewObject;

/**
 * Value Object <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class DescriptionValue implements INewObject {

    public static DescriptionValue create() {
        DescriptionValue descriptionValue = new DescriptionValue();
        descriptionValue.setNewObject(true);
        return descriptionValue;
    }

    public static DescriptionValue create(String fieldName, String value) {
        DescriptionValue descriptionValue = new DescriptionValue(fieldName,
                value);
        descriptionValue.setNewObject(true);
        return descriptionValue;
    }

    public DescriptionValue() {

    }

    public DescriptionValue(String fieldName, String value) {
        this.fieldName = fieldName;
        setValue(value);
    }

    private String fieldName;

    private String value = "";

    private boolean newObject = false;

    public boolean isNewObject() {
        return newObject;
    }

    private void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    @NotEmpty(message = "field name not specified or empty")
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @NotNull(message = "value cannot be null")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }
}
