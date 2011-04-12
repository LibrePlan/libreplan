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

package org.navalplanner.web.costcategories;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.web.common.IIntegrationEntityModel;

/**
 * Model for UI operations related to {@link CostCategory}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ICostCategoryModel extends IIntegrationEntityModel {

    /**
     * Get all {@link CostCategory} elements
     *
     * @return
     */
    List<CostCategory> getCostCategories();

    Set<HourCost> getHourCosts();

    CostCategory getCostCategory();

    void initCreate();

    void initEdit(CostCategory costCategory);

    void confirmSave() throws ValidationException;

    void addHourCost();

    void removeHourCost(HourCost hourCost);

    void confirmRemoveCostCategory(CostCategory category)
        throws InstanceNotFoundException;

    boolean canRemoveCostCategory(CostCategory category);

    void validateHourCostsOverlap() throws ValidationException;

    List<TypeOfWorkHours> getAllHoursType();

}