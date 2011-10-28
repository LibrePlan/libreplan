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
package org.libreplan.business.materials.entities;

import java.math.BigDecimal;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.templates.entities.OrderElementTemplate;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MaterialAssignmentTemplate extends BaseEntity implements
        Comparable<MaterialAssignmentTemplate> {

    public static MaterialAssignmentTemplate copyFrom(
            MaterialAssignmentTemplate assignment) {
        MaterialAssignmentTemplate result = new MaterialAssignmentTemplate();
        result.materialInfo = assignment.getMaterialInfo().copy();
        result.orderElementTemplate = assignment.getOrderElementTemplate();
        return BaseEntity.create(result);
    }

    public static MaterialAssignmentTemplate create() {
        return BaseEntity.create(new MaterialAssignmentTemplate());
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public MaterialAssignmentTemplate() {

    }

    public static MaterialAssignmentTemplate create(
            OrderElementTemplate template, Material material) {
        MaterialAssignmentTemplate result = create();
        result.setUnitPrice(material.getDefaultUnitPrice());
        result.setMaterial(material);
        result.orderElementTemplate = template;
        return BaseEntity.create(result);
    }

    public static MaterialAssignmentTemplate copyFrom(
            MaterialAssignment assignment, OrderElementTemplate destination) {
        MaterialAssignmentTemplate result = new MaterialAssignmentTemplate();
        result.materialInfo = assignment.getMaterialInfo().copy();
        result.orderElementTemplate = destination;
        return BaseEntity.create(result);
    }

    private MaterialInfo materialInfo = new MaterialInfo();

    private OrderElementTemplate orderElementTemplate;

    public MaterialInfo getMaterialInfo() {
        if (materialInfo == null) {
            materialInfo = new MaterialInfo();
        }
        return materialInfo;
    }

    public void setMaterialInfo(MaterialInfo materialInfo) {
        this.materialInfo = materialInfo;
    }

    public OrderElementTemplate getOrderElementTemplate() {
        return orderElementTemplate;
    }

    public Material getMaterial() {
        return getMaterialInfo().getMaterial();
    }

    public void setMaterial(Material material) {
        getMaterialInfo().setMaterial(material);
    }

    public BigDecimal getUnits() {
        return getMaterialInfo().getUnits();
    }

    public void setUnits(BigDecimal units) {
        getMaterialInfo().setUnits(units);
    }

    public BigDecimal getUnitPrice() {
        return getMaterialInfo().getUnitPrice();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        getMaterialInfo().setUnitPrice(unitPrice);
    }

    public BigDecimal getTotalPrice() {
        return getMaterialInfo().getTotalPrice();
    }

    @Override
    public int compareTo(MaterialAssignmentTemplate o) {
        return getMaterial().compareTo(o.getMaterial());
    }

    public MaterialAssignment createAssignment(OrderElement element) {
        return MaterialAssignment.createFrom(materialInfo, element);
    }

}
