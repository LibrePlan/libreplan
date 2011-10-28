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

package org.libreplan.web.common.components.finders;

import org.apache.commons.lang.StringUtils;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class FilterPair extends Object {

    private IFilterEnum type;

    private String typeDescription;

    private String pattern;

    private Object value;

    public FilterPair() {
    }

    public FilterPair(IFilterEnum type, String typeDescription, String pattern,
            Object value) {
        this.type = type;
        this.typeDescription = typeDescription;
        this.value = value;
        this.pattern = pattern;
    }

    public FilterPair(IFilterEnum type, String pattern, Object value) {
        this.type = type;
        this.value = value;
        this.pattern = pattern;
    }

    public IFilterEnum getType() {
        return type;
    }

    public void setType(OrderFilterEnum type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getTypeComplete() {
        if (getType() == null) {
            return "";
        }

        String descriptionComplete = getType().toString();
        if (!StringUtils.isBlank(this.getTypeDescription())) {
            descriptionComplete += " ( " + getTypeDescription() + " )";
        }
        return descriptionComplete;
    }
}
