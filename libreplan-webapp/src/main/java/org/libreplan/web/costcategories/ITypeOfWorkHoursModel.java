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

package org.libreplan.web.costcategories;

import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * Model for UI operations related to {@link TypeOfWorkHours}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ITypeOfWorkHoursModel extends IIntegrationEntityModel {

    void checkIsReferencedByOtherEntities(TypeOfWorkHours typeOfWorkHours) throws ValidationException;

    void confirmRemove(TypeOfWorkHours typeOfWorkHours);

    /**
     * Stores the current {@link WorkReport}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void confirmSave() throws ValidationException;

    /**
     * Gets the current {@link TypeOfWorkHours}.
     *
     * @return A {@link TypeOfWorkHours}
     */
    TypeOfWorkHours getTypeOfWorkHours();

    /**
     * Get all {@link TypeOfWorkHours} elements
     *
     * @return
     */
    List<TypeOfWorkHours> getTypesOfWorkHours();

    /**
     * Makes some operations needed before create a new {@link TypeOfWorkHours}.
     */
    void initCreate();

    /**
     * Makes some operations needed before edit a {@link TypeOfWorkHours}.
     *
     * @param typeOfWorkHours
     *            The object to be edited
     */
    void initEdit(TypeOfWorkHours typeOfWorkHours);

}
