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

package org.navalplanner.business.advance.exceptions;


/**
 * An exception for modeling a problem with duplicated values to true for the
 * property reportGlobalAdvance of the advanceAsigment class for an order
 * element. It contains a message, the key of the instance, and its class name.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class DuplicateValueTrueReportGlobalAdvanceException extends Exception {

    private Object key;
    private String className;

    public DuplicateValueTrueReportGlobalAdvanceException(
            String specificMessage, Object key, String className) {
        super(specificMessage + " (key = '" + key + "' - className = '"
                + className + "')");
        this.key = key;
        this.className = className;
    }

    public DuplicateValueTrueReportGlobalAdvanceException(
            String specificMessage, Object key, Class<?> klass) {
        this(specificMessage, key, klass.getName());
    }

    public Object getKey() {
        return key;
    }

    public String getClassName() {
        return className;
    }
}