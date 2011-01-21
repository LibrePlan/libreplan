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

package org.navalplanner.business.materials.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class MaterialAssignment extends BaseEntity implements Comparable {

    private MaterialInfo materialInfo = new MaterialInfo();

    private Date estimatedAvailability;

    private MaterialStatusEnum status = MaterialStatusEnum.PENDING;

    private OrderElement orderElement;

    public static MaterialAssignment create() {
        return BaseEntity.create(new MaterialAssignment());
    }

    protected MaterialAssignment() {

    }

    public static MaterialAssignment create(Material material) {
        MaterialAssignment materialAssignment = create();
        materialAssignment.setUnitPrice(material.getDefaultUnitPrice());
        materialAssignment.setMaterial(material);
        return materialAssignment;
    }

    public static MaterialAssignment create(MaterialAssignment materialAssignment) {
        MaterialAssignment result = create();
        result.setMaterial(materialAssignment.getMaterial());
        result.setUnits(materialAssignment.getUnits());
        result.setUnitPrice(materialAssignment.getUnitPrice());
        result.setEstimatedAvailability(materialAssignment.getEstimatedAvailability());
        result.setStatus(materialAssignment.getStatus());
        return result;
    }

    public static MaterialAssignment createFrom(MaterialInfo materialInfo,
            OrderElement element) {
        MaterialAssignment result = create();
        result.materialInfo = materialInfo.copy();
        result.orderElement = element;
        return result;
    }

    @Valid
    public MaterialInfo getMaterialInfo() {
        if (materialInfo == null) {
            materialInfo = new MaterialInfo();
        }
        return materialInfo;
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

    public void setUnitsWithoutNullCheck(BigDecimal units) {
        this.materialInfo.setUnitsWithoutNullCheck(units);
    }

    public BigDecimal getUnitPrice() {
        return getMaterialInfo().getUnitPrice();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        getMaterialInfo().setUnitPrice(unitPrice);
    }

    public void setUnitPriceWithoutNullCheck(BigDecimal unitPrice) {
        this.materialInfo.setUnitPriceWithoutNullCheck(unitPrice);
    }

    public BigDecimal getTotalPrice() {
        return getMaterialInfo().getTotalPrice();
    }

    public Date getEstimatedAvailability() {
        return estimatedAvailability;
    }

    public void setEstimatedAvailability(Date estimatedAvailability) {
        this.estimatedAvailability = estimatedAvailability;
    }

    public MaterialStatusEnum getStatus() {
        return status;
    }

    public void setStatus(MaterialStatusEnum status) {
        this.status = status;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    public int compareTo(Object arg0) {
        final MaterialAssignment materialAssignment = (MaterialAssignment) arg0;
        return materialAssignment.getMaterial().compareTo(getMaterial());
    }

}
