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

package org.navalplanner.business.materials.daos;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;

/**
 * Interface IMaterialDAO
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IMaterialDAO extends IGenericDAO<Material, Long> {

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

    List<Material> getAll();

    /**
     * Returns all subcategories for materialCategory
     *
     * @param materialCategory
     * @return
     */
    Set<MaterialCategory> getAllSubcategories(MaterialCategory materialCategory);

}
