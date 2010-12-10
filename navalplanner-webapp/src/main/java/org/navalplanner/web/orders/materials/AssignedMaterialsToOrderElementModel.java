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
import java.util.List;
import java.util.Set;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedMaterialsToOrderElementModel extends
        AssignedMaterialsModel<OrderElement, MaterialAssignment> implements
        IAssignedMaterialsToOrderElementModel {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private OrderElement orderElement;

    @Override
    protected void assignAndReattach(OrderElement element) {
        this.orderElement = element;
        orderElementDAO.reattach(this.orderElement);
    }

    @Override
    protected void initializeMaterialAssigments() {
        initializeMaterialAssigments(this.orderElement.getMaterialAssignments());
    }

    private void initializeMaterialAssigments(
            Set<MaterialAssignment> materialAssignments) {
        for (MaterialAssignment each : materialAssignments) {
            each.getStatus();
            reattachMaterial(each.getMaterial());
            initializeMaterialCategory(each.getMaterial().getCategory());
        }
    }

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    protected List<MaterialAssignment> getAssignments() {
        return new ArrayList<MaterialAssignment>(orderElement
                .getMaterialAssignments());
    }

    @Override
    protected Material getMaterial(MaterialAssignment assignment) {
        return assignment.getMaterial();
    }

    @Override
    protected MaterialCategory removeAssignment(
            MaterialAssignment materialAssignment) {
        orderElement.removeMaterialAssignment(materialAssignment);
        return materialAssignment.getMaterial().getCategory();
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
    protected MaterialCategory addAssignment(
            MaterialAssignment materialAssignment) {
        orderElement.addMaterialAssignment(materialAssignment);
        return materialAssignment.getMaterial().getCategory();
    }

    @Override
    protected BigDecimal getUnits(MaterialAssignment assigment) {
        return assigment.getUnits();
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

    @Override
    protected BigDecimal getTotalPrice(MaterialAssignment materialAssignment) {
        return materialAssignment.getTotalPrice();
    }

    @Override
    protected boolean isInitialized() {
        return orderElement != null;
    }

    @Override
    public boolean isCurrentUnitType(Object assigment, UnitType unitType) {
        MaterialAssignment material = (MaterialAssignment) assigment;
        return ((material != null)
                && (material.getMaterial().getUnitType() != null) && (unitType
                .getId().equals(material.getMaterial().getUnitType().getId())));
    }

}
