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

package org.navalplanner.web.orders.materials;

import java.math.BigDecimal;
import java.util.List;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.web.orders.IOrderElementModel;
import org.zkoss.zul.TreeModel;


/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IAssignedMaterialsToOrderElementModel {

    /**
     * Add a new {@link MaterialAssignment} out of {@link Material}
     *
     * @param each
     */
    void addMaterialAssignment(Material material);

    /**
     * Add a new {@link MaterialAssignment} out of existing {@link MaterialAssignment}
     *
     * @param materialAssignment
     */
    void addMaterialAssignment(MaterialAssignment materialAssignment);

    TreeModel getAllMaterialCategories();

    List<MaterialAssignment> getAssignedMaterials(MaterialCategory materialCategory);

    /**
     * Returns list of {@link Material} found after executing latest search
     *
     * @return
     */
    List<Material> getMatchingMaterials();

    /**
     * Get list of {@link MaterialCategory} assigned to current {@link OrderElement}
     *
     * @return
     */
    TreeModel getMaterialCategories();

    OrderElement getOrderElement();

    /**
     * Calculates sum of price for all {@link MaterialAssignment} which belong
     * to {@link MaterialCategory}
     *
     * @param materialCategory
     * @return
     */
    BigDecimal getPrice(MaterialCategory materialCategory);

    /**
     * Calculates sum of units for all {@link MaterialAssignment} which belong
     * to {@link MaterialCategory}
     *
     * @param materialCategory
     * @return
     */
    double getUnits(MaterialCategory materialCategory);

    void initEdit(IOrderElementModel orderElementModel);

    void removeMaterialAssignment(MaterialAssignment materialAssignment);

    /**
     * Searches materials in categories and its subcategories by code and
     * description
     *
     * @param code
     * @param materialCategory
     */
    void searchMaterials(String text, MaterialCategory materialCategory);
}