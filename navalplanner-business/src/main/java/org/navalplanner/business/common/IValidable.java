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

package org.navalplanner.business.common;

import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Entities implementing this interface have a method <code>checkValid</code>.
 * This method validates the business rules of the entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IValidable {

    /**
     * Checks if an entity is or not valid.
     *
     * @throws ValidationException
     *             if entity is not valid.
     */
    void checkValid() throws ValidationException;

}
