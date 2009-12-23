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

package org.navalplanner.business.materials.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class MaterialAssignment extends BaseEntity implements Comparable {

    private Material material;

    private Double units = 0.0;

    private BigDecimal unitPrice = BigDecimal.ZERO;

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

    @NotNull(message = "material not specified")
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @NotNull(message = "units not specified")
    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }

    @NotNull(message = "unit price not specified")
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal result = new BigDecimal(getUnits());
        return result.multiply(getUnitPrice());
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        BigDecimal unitPrice = totalPrice;
        unitPrice = unitPrice.divide(new BigDecimal(units), 3, RoundingMode.HALF_UP);
        setUnitPrice(unitPrice);
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
