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

package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedMaterialsToOrderElementModel implements
        IAssignedMaterialsToOrderElementModel {

    @Autowired
    private IMaterialCategoryDAO categoryDAO;

    @Autowired
    private IMaterialDAO materialDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private IOrderElementModel orderElementModel;

    private OrderElement orderElement;

    private MutableTreeModel<MaterialCategory> materialCategories = MutableTreeModel
            .create(MaterialCategory.class);

    private MutableTreeModel<MaterialCategory> allMaterialCategories = MutableTreeModel
            .create(MaterialCategory.class);

    private List<Material> matchingMaterials = new ArrayList<Material>();

    @Override
    @Transactional(readOnly = true)
    public void initEdit(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
        this.orderElement = this.orderElementModel.getOrderElement();
        orderElementDAO.reattach(this.orderElement);
        materialCategories = MutableTreeModel.create(MaterialCategory.class);
        initializeMaterialAssigments(this.orderElement.getMaterialAssignments());

        // Initialize matching materials
        matchingMaterials.clear();
        matchingMaterials.addAll(materialDAO.getAll());
        initializeMaterials(matchingMaterials);
    }

    private void initializeMaterialAssigments(
            Set<MaterialAssignment> materialAssignments) {
        for (MaterialAssignment each : materialAssignments) {
            each.getStatus();
            initializeMaterialCategory(each.getMaterial().getCategory());
        }
    }

    @Transactional(readOnly = true)
    private void initializeMaterialCategories(
            Collection<MaterialCategory> materialCategories) {
        for (MaterialCategory each : materialCategories) {
            initializeMaterialCategory(each);
        }
    }

    private void initializeMaterialCategory(MaterialCategory materialCategory) {
        materialCategory.getName();
        initializeMaterials(materialCategory.getMaterials());
        initializeMaterialCategories(materialCategory.getSubcategories());
    }

    private void initializeMaterials(Collection<Material> materials) {
        for (Material each : materials) {
            initializeMaterial(each);
        }
    }

    private void initializeMaterial(Material material) {
        material.getDescription();
        material.getCategory().getName();
    }

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public MutableTreeModel<MaterialCategory> getMaterialCategories() {
        if (orderElement != null && materialCategories.isEmpty()) {
            feedTree(materialCategories, orderElement.getMaterialAssignments());
            initializeMaterialCategories(materialCategories.asList());
        }
        return materialCategories;
    }

    private void feedTree(MutableTreeModel<MaterialCategory> tree,
            Set<MaterialAssignment> materialAssignments) {
        for (MaterialAssignment each : materialAssignments) {
            final Material material = (Material) each.getMaterial();
            addCategory(tree, material.getCategory());
        }
    }

    /**
     * Adds category to treeModel
     *
     * If category.parent is not in treeModel add it to treeModel recursively.
     *
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

    @Override
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

    @Override
    public List<MaterialAssignment> getAssignedMaterials(
            MaterialCategory materialCategory) {
        List<MaterialAssignment> result = new ArrayList<MaterialAssignment>();
        if (orderElement != null) {
            for (MaterialAssignment materialAssigment : orderElement
                    .getMaterialAssignments()) {
                final Material material = materialAssigment.getMaterial();
                if (materialCategory == null
                        || materialCategory.getId().equals(material.getCategory().getId())) {
                    result.add(materialAssigment);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public void searchMaterials(String text, MaterialCategory materialCategory) {
        matchingMaterials = materialDAO
                .findMaterialsInCategoryAndSubCategories(text, materialCategory);
        initializeMaterials(matchingMaterials);
    }

    @Override
    public List<Material> getMatchingMaterials() {
        return matchingMaterials;
    }

    @Override
    @Transactional(readOnly = true)
    public void addMaterialAssignment(Material material) {
        MaterialAssignment materialAssigment = MaterialAssignment
                .create(material);
        materialAssigment.setEstimatedAvailability(orderElement.getInitDate());
        addMaterialAssignment(materialAssigment);
    }

    @Override
    @Transactional(readOnly = true)
    public void addMaterialAssignment(MaterialAssignment materialAssignment) {
        orderElement.addMaterialAssignment(materialAssignment);
        // Add material category to materialCategories tree
        final MaterialCategory materialCategory = materialAssignment.getMaterial().getCategory();
        addCategory(materialCategories, materialCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public void removeMaterialAssignment(MaterialAssignment materialAssignment) {
        orderElement.removeMaterialAssignment(materialAssignment);
        // Remove material category from materialCategories tree
        final MaterialCategory materialCategory = materialAssignment.getMaterial().getCategory();
        removeCategory(materialCategories, materialCategory);
    }

    private void removeCategory(
            MutableTreeModel<MaterialCategory> materialCategories,
            MaterialCategory materialCategory) {

        categoryDAO.reattach(materialCategory);
        final boolean canDelete = materialCategory.getSubcategories().isEmpty() &&  getAssignedMaterials(materialCategory).isEmpty();
        if (canDelete) {
            materialCategories.remove(materialCategory);
            removeCategory(materialCategories, materialCategory.getParent());
        }
    }

    @Override
    public double getUnits(MaterialCategory materialCategory) {
        double result = 0;
        if (orderElement != null) {
            for (MaterialAssignment materialAssignment : orderElement
                    .getMaterialAssignments()) {
                final Material material = materialAssignment.getMaterial();
                if (materialCategory.equals(material.getCategory())) {
                    result += materialAssignment.getUnits();
                }
            }
        }
        return result;
    }

    @Override
    public BigDecimal getPrice(MaterialCategory materialCategory) {
        BigDecimal result = new BigDecimal(0);
        if (orderElement != null) {
            for (MaterialAssignment materialAssignment : orderElement
                    .getMaterialAssignments()) {
                final Material material = materialAssignment.getMaterial();
                if (materialCategory.equals(material.getCategory())) {
                    result = result.add(materialAssignment.getTotalPrice());
                }
            }
        }
        return result;
    }

}
