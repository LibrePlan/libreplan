/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.costcategories;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.web.common.IIntegrationEntityModel;

/**
 * Model for UI operations related to {@link TypeOfWorkHours}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface ITypeOfWorkHoursModel extends IIntegrationEntityModel {

    /**
     * Makes some operations needed before edit a {@link TypeOfWorkHours}.
     *
     * @param typeOfWorkHours
     *            The object to be edited
     */
    void initEdit(TypeOfWorkHours typeOfWorkHours);

    /**
     * Makes some operations needed before create a new {@link TypeOfWorkHours}.
     */
    void initCreate();

    /**
     * Get all {@link TypeOfWorkHours} elements
     *
     * @return
     */
    List<TypeOfWorkHours> getTypesOfWorkHours();

    /**
     * Gets the current {@link TypeOfWorkHours}.
     *
     * @return A {@link TypeOfWorkHours}
     */
    TypeOfWorkHours getTypeOfWorkHours();

	/**
	 * Stores the current {@link WorkReport}.
	 *
	 * @throws ValidationException
	 *             If validation fails
	 */
	void confirmSave() throws ValidationException;

}
