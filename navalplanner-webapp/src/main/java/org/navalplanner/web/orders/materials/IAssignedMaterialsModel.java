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
package org.navalplanner.web.orders.materials;

import java.math.BigDecimal;
import java.util.List;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.zkoss.zul.TreeModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface IAssignedMaterialsModel<T, A> {

    void initEdit(T entity);

    void addMaterialAssignment(Material material);

    void addMaterialAssignment(A materialAssignment);

    TreeModel getAllMaterialCategories();

    List<A> getAssignedMaterials(MaterialCategory materialCategory);

    abstract boolean isCurrentUnitType(Object assigment, UnitType unitType);

    List<Material> getMatchingMaterials();

    TreeModel getMaterialCategories();

    BigDecimal getPrice(MaterialCategory materialCategory);

    BigDecimal getUnits(MaterialCategory materialCategory);

    void removeMaterialAssignment(A materialAssignment);

    void searchMaterials(String text, MaterialCategory materialCategory);

    void loadUnitTypes();

    List<UnitType> getUnitTypes();

}
