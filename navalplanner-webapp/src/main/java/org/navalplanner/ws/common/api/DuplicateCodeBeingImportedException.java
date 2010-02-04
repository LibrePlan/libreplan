/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.common.api;

/**
 * An exception for notifying that an instance is already being imported in
 * a bulk import because it has a code used by another instance of the same
 * type.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@SuppressWarnings("serial")
public class DuplicateCodeBeingImportedException
    extends DuplicateInstanceBeingImportedException {

    private String code;

    public DuplicateCodeBeingImportedException(String instanceType,
        String code) {

        super(instanceType);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
