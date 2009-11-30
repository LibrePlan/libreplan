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

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

/**
 * Material entity
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class Material extends BaseEntity implements Comparable {

    @NotNull
    @NotEmpty
    private String code;

    private String description;

    private BigDecimal defaultUnitPrice = new BigDecimal(0);

    private UnitTypeEnum unitType;

    private boolean disabled;

    @NotNull
    private MaterialCategory category = null;

    // Default constructor, needed by Hibernate
    protected Material() {

    }

    public static Material create(String code) {
        return (Material) create(new Material(code));
    }

    protected Material(String code) {
        this.code = code;
    }

    public MaterialCategory getCategory() {
        return category;
    }

    public void setCategory(MaterialCategory category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public BigDecimal getDefaultUnitPrice() {
        return defaultUnitPrice;
    }

    public void setDefaultUnitPrice(BigDecimal defaultUnitPrice) {
        this.defaultUnitPrice = defaultUnitPrice;
    }

    public UnitTypeEnum getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitTypeEnum unitType) {
        this.unitType = unitType;
    }

    @Override
    public int compareTo(Object arg0) {
      final Material material = (Material) arg0;
      return code.compareTo(material.getCode());
    }

}