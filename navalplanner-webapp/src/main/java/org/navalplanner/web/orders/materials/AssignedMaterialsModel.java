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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.daos.IUnitTypeDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class AssignedMaterialsModel<T, A> implements
        IAssignedMaterialsModel<T, A> {

    @Autowired
    private IMaterialCategoryDAO categoryDAO;

    @Autowired
    private IMaterialDAO materialDAO;

    @Autowired
    private IUnitTypeDAO unitTypeDAO;

    private MutableTreeModel<MaterialCategory> materialCategories = MutableTreeModel
            .create(MaterialCategory.class);

    private MutableTreeModel<MaterialCategory> allMaterialCategories = MutableTreeModel
            .create(MaterialCategory.class);

    private List<Material> matchingMaterials = new ArrayList<Material>();

    private List<UnitType> unitTypes = new ArrayList<UnitType>();

    @Transactional(readOnly = true)
    public void initEdit(T element) {
        assignAndReattach(element);
        materialCategories = MutableTreeModel.create(MaterialCategory.class);
        initializeMaterialAssigments();
        // Initialize matching materials
        matchingMaterials.clear();
        matchingMaterials.addAll(materialDAO.getAll());
        initializeMaterials(matchingMaterials);
    }

    protected abstract void initializeMaterialAssigments();

    protected abstract void assignAndReattach(T element);

    protected void reattachMaterial(Material material) {
        materialDAO.reattachUnmodifiedEntity(material);
    }

    private void initializeMaterials(Collection<Material> materials) {
        for (Material each : materials) {
            initializeMaterial(each);
        }
    }

    protected void initializeMaterialCategories(
            Collection<MaterialCategory> materialCategories) {
        for (MaterialCategory each : materialCategories) {
            initializeMaterialCategory(each);
        }
    }

    protected void initializeMaterialCategory(MaterialCategory materialCategory) {
        categoryDAO.reattach(materialCategory);
        materialCategory.getName();
        initializeMaterials(materialCategory.getMaterials());
        initializeMaterialCategories(materialCategory.getSubcategories());
    }

    private void initializeMaterial(Material material) {
        material.getDescription();
        material.getCategory().getName();
    }

    @Transactional(readOnly = true)
    public MutableTreeModel<MaterialCategory> getMaterialCategories() {
        if (isInitialized() && materialCategories.isEmpty()) {
            feedTree(materialCategories, getAssignments());
            initializeMaterialCategories(materialCategories.asList());
        }
        return materialCategories;
    }

    protected abstract List<A> getAssignments();

    protected abstract Material getMaterial(A assignment);

    private void feedTree(MutableTreeModel<MaterialCategory> tree,
            Collection<? extends A> materialAssignments) {
        for (A each : materialAssignments) {
            final Material material = getMaterial(each);
            addCategory(tree, material.getCategory());
        }
    }

    /**
     * Adds category to treeModel If category.parent is not in treeModel add it
     * to treeModel recursively.
     */
    private void addCategory(
            MutableTreeModel<MaterialCategory> materialCategories,
            MaterialCategory materialCategory) {

        categoryDAO.reattach(materialCategory);
        final MaterialCategory parent = materialCategory.getParent();
        if (parent == null) {
            if (!materialCategories.contains(parent, materialCategory)) {
                materialCategories.addToRoot(materialCategory);
            }
        } else {
            if (!materialCategories.contains(parent, materialCategory)) {
                addCategory(materialCategories, parent);
                materialCategories.add(parent, materialCategory);
            }
        }
    }

    @Transactional(readOnly = true)
    public TreeModel getAllMaterialCategories() {
        if (allMaterialCategories.isEmpty()) {
            feedTree(allMaterialCategories, categoryDAO.getAll());
            initializeMaterialCategories(allMaterialCategories.asList());
        }
        return allMaterialCategories;
    }

    private void feedTree(MutableTreeModel<MaterialCategory> tree,
            List<MaterialCategory> materialCategories) {
        for (MaterialCategory each : materialCategories) {
            addCategory(tree, each);
        }
    }

    protected abstract boolean isInitialized();

    public List<A> getAssignedMaterials(MaterialCategory materialCategory) {
        List<A> result = new ArrayList<A>();
        if (isInitialized()) {
            for (A each : getAssignments()) {
                final Material material = getMaterial(each);
                if (materialCategory == null
                        || materialCategory.getId().equals(
                                material.getCategory().getId())) {
                    result.add(each);
                }
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public void searchMaterials(String text, MaterialCategory materialCategory) {
        matchingMaterials = materialDAO
                .findMaterialsInCategoryAndSubCategories(text, materialCategory);
        initializeMaterials(matchingMaterials);
    }

    public List<Material> getMatchingMaterials() {
        return matchingMaterials;
    }

    @Transactional(readOnly = true)
    public void addMaterialAssignment(A materialAssignment) {
        MaterialCategory category = addAssignment(materialAssignment);
        addCategory(materialCategories, category);
    }

    protected abstract MaterialCategory addAssignment(A materialAssignment);

    protected abstract MaterialCategory removeAssignment(A materialAssignment);

    public void removeMaterialAssignment(A materialAssignment) {
        MaterialCategory materialCategory = removeAssignment(materialAssignment);
        removeCategory(materialCategories, materialCategory);
    }

    private void removeCategory(
            MutableTreeModel<MaterialCategory> materialCategories,
            MaterialCategory materialCategory) {

        categoryDAO.reattach(materialCategory);
        final boolean canDelete = materialCategory.getSubcategories().isEmpty()
                && getAssignedMaterials(materialCategory).isEmpty();
        if (canDelete) {
            materialCategories.remove(materialCategory);
            final MaterialCategory parent = materialCategory.getParent();
            if (parent != null) {
                removeCategory(materialCategories, parent);
            }
        }
    }

    @Override
    public BigDecimal getUnits(MaterialCategory materialCategory) {
        BigDecimal result = BigDecimal.ZERO;
        if (isInitialized()) {
            for (A each : getAssignments()) {
                final Material material = getMaterial(each);
                if (materialCategory.equals(material.getCategory())) {
                    result = result.add(getUnits(each));
                }
            }
        }
        return result;
    }

    protected abstract BigDecimal getUnits(A assigment);

    public BigDecimal getPrice(MaterialCategory category) {
        BigDecimal result = new BigDecimal(0);
        if (isInitialized()) {
            for (A each : getAssignments()) {
                final Material material = getMaterial(each);
                if (category.equals(material.getCategory())) {
                    result = result.add(getTotalPrice(each));
                }
            }
        }
        return result;
    }

    protected abstract BigDecimal getTotalPrice(A each);

    @Override
    @Transactional(readOnly = true)
    public void loadUnitTypes() {
        unitTypes = unitTypeDAO.findAll();
    }

    @Override
    public List<UnitType> getUnitTypes() {
        return unitTypes;
    }
}
