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
import java.math.RoundingMode;
import java.util.HashMap;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.orders.entities.OrderElement;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.TreeModel;

/**
 * Controller for showing {@link OrderElement} assigned {@link Material}
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignedMaterialsToOrderElementController extends
        AssignedMaterialsController<OrderElement, MaterialAssignment> {

    private IAssignedMaterialsToOrderElementModel assignedMaterialsToOrderElementModel;

    @Override
    protected IAssignedMaterialsModel<OrderElement, MaterialAssignment> getModel() {
        return assignedMaterialsToOrderElementModel;
    }

    @Override
    protected void createAssignmentsBoxComponent(Component parent) {
        Executions.createComponents("/orders/_assignmentsBox.zul", parent,
                new HashMap<String, String>());
    }

    @Override
    protected void initializeEdition(OrderElement orderElement) {
        assignedMaterialsToOrderElementModel.initEdit(orderElement);
    }

    public TreeModel getMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getMaterialCategories();
    }

    public TreeModel getAllMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getAllMaterialCategories();
    }

    @Override
    public BigDecimal getTotalUnits() {
        BigDecimal result = BigDecimal.ZERO;

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = result.add(orderElement.getTotalMaterialAssigmentUnits());
        }
        return result;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal result = new BigDecimal(0);

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = orderElement.getTotalMaterialAssigmentPrice();
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private OrderElement getOrderElement() {
        return assignedMaterialsToOrderElementModel.getOrderElement();
    }

    @Override
    protected MaterialAssignment copyFrom(MaterialAssignment assignment) {
        return MaterialAssignment.create(assignment);
    }

    @Override
    protected Material getMaterial(MaterialAssignment materialAssignment) {
        return materialAssignment.getMaterial();
    }

    @Override
    protected Double getTotalPrice(MaterialAssignment materialAssignment) {
        return materialAssignment.getTotalPrice().doubleValue();
    }

    @Override
    protected BigDecimal getUnits(MaterialAssignment assignment) {
        return assignment.getUnits();
    }

    protected void setUnits(MaterialAssignment assignment, BigDecimal units) {
        assignment.setUnits(units);
    }

}
