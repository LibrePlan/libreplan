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

package org.navalplanner.business.materials.daos;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.MaterialStatusEnum;
import org.navalplanner.business.orders.entities.Order;

/**
 * Interface IMaterialDAO
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IMaterialDAO extends IIntegrationEntityDAO<Material> {

    /**
     * Returns true if {@link Material} exits
     *
     * @param code
     * @return
     */
    boolean existsMaterialWithCodeInAnotherTransaction(String code);

    /**
     * Searches {@link Material} by code and description (ilike matching) within
     * categories
     *
     * @param text
     * @param categories
     * @return
     */
    List<Material> findMaterialsInCategories(String text,
            Set<MaterialCategory> categories);

    /**
     * Searches {@link Material} by code and description (ilike matching) within
     * category and its subcategories
     *
     * @param text
     * @param materialCategory
     * @return
     */
    List<Material> findMaterialsInCategoryAndSubCategories(String text,
            MaterialCategory materialCategory);

    /**
     * Returns {@link Material} by code
     *
     * @param code
     * @return
     */
    Material findUniqueByCodeInAnotherTransaction(String code) throws InstanceNotFoundException;

    List<Material> getAll();

    /**
     * Returns all subcategories for materialCategory
     *
     * @param materialCategory
     * @return
     */
    Set<MaterialCategory> getAllSubcategories(MaterialCategory materialCategory);

    /**
     * Returns all @ MaterialAssignment} which match with the filters
     * @param
     * @return
     */
    List<MaterialAssignment> getFilterMaterial(MaterialStatusEnum filterStatus,
            List<Order> orders, List<MaterialCategory> categories,
            List<Material> materials);
}
