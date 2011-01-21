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

import org.hibernate.validator.NotNull;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class MaterialInfo {

    private Material material;

    private BigDecimal units = BigDecimal.ZERO;

    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "material not specified")
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @NotNull(message = "units not specified")
    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units != null ? units : BigDecimal.ZERO;
    }

    public void setUnitsWithoutNullCheck(BigDecimal units) {
        this.units = units;
    }

    @NotNull(message = "unit price not specified")
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public void setUnitPriceWithoutNullCheck(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public MaterialInfo copy() {
        MaterialInfo result = new MaterialInfo();
        result.setMaterial(getMaterial());
        result.setUnits(getUnits());
        result.setUnitPrice(getUnitPrice());
        return result;
    }

    public BigDecimal getTotalPrice() {
        return getUnits().multiply(getUnitPrice());
    }

}
