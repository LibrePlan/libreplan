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
package org.libreplan.web.materials;

import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * Interface for the model which lets the client of this model
 * list the unit types, create new unit types, edit existing unit types
 * and remove unity types
 *
 * <strong>Conversation state:</strong> A unit type being edited or created.
 *
 * <strong>Not conversational methods:</strong>
 * <ul>
 *  <li>getUnitTypes</li>
 *  <li>existsAnotherUnitTypeWithName</li>
 *  <li>existsAnotherUnitTypeWithCode</li>
 *  <li>isUnitTypeUsedInAnyMaterial</li>
 *  <li>isUnitTypeUsedInAnyMaterial</li>
 *  <li>remove</li>
 * </ul>
 *
 * <strong>Conversational methods:</strong>
 * <ul>
 *   <li>initCreate</li>
 *   <li>initEdit</li>
 *   <li>getCurrentUnitType</li>
 *   <li>confirmSave</li>
 * </ul>
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public interface IUnitTypeModel extends IIntegrationEntityModel {

    // Non conversational methods

    /**
     * Query the database to get all the unit types in the database
     */
    List<UnitType> getUnitTypes();

    /**
     * This method check if there is another UnitType in the database
     * different from the one in the state of the model which had the same
     * measure name as the parameter.
     *
     * @param name the measure name to be checked as unique in the unit types
     * @return     the boolean with the result
     */
    boolean existsAnotherUnitTypeWithName(String name);

    /**
     * This method check if there is another UnitType in the database
     * different from the one in the state of the model which had teh same
     * code as the parameter
     *
     * @param code the code to be checked as unique
     * @return     the boolean showing the result
     */
    boolean existsAnotherUnitTypeWithCode(String code);

    /**
     * This method finds out if the unit type passed as parameter is
     * used to measure any material
     *
     * @param unitType the unitType to check
     * @return         the boolean with the result
     */
    boolean isUnitTypeUsedInAnyMaterial(UnitType unitType);

    /**
     * This method removes the unit type passed as parameter from the
     * database
     *
     * @param unitType the unitType which is wanted to be deleted
     */
    void remove(UnitType unitType);

    //Conversational methods

    /**
     * First method of the conversational state. Prepares the state with the
     * unit type to edit
     */
    void initEdit(UnitType unitType);

    /**
     * First method of the conversational state. Creates an empty unit type
     * to be saved
     */
    void initCreate();

    /**
     * Get the current unit type which is in the state of the
     *
     * @return
     */
    UnitType getCurrentUnitType();

    /**
     * Last method of the conversation. It ends with the saving of the unit
     * type in the state to the database
     *
     * @throws ValidationException
     */
    void confirmSave() throws ValidationException;

}