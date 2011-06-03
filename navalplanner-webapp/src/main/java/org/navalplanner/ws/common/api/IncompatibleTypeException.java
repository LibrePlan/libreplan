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

package org.navalplanner.ws.common.api;

/**
 * {@link Exception} used in update process when two types are not compatible.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings(value = { "serial", "unchecked" })
public class IncompatibleTypeException extends Exception {

    private Object identifier;
    private Class expectedType;
    private Class actualType;

    public IncompatibleTypeException(Object identifier, Class expectedType,
            Class actualType) {
        super("Incompatible type (identifier = '" + identifier
                + "' - expectedType = '" + expectedType.getName()
                + "' - actualType = '" + actualType.getName() + "')");

        this.identifier = identifier;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public Object getIdentifier() {
        return identifier;
    }

    public Class getExpectedType() {
        return expectedType;
    }

    public Class getActualType() {
        return actualType;
    }

}
