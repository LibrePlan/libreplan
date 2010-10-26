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

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.materials.bootstrap.UnitTypeBootstrap;
import org.navalplanner.business.materials.daos.IMaterialDAO;
/**
 * Material entity
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class Material extends IntegrationEntity implements Comparable {

    private String description;

    private BigDecimal defaultUnitPrice = new BigDecimal(0);

    private UnitType unitType;

    private Boolean disabled = Boolean.FALSE;

    private MaterialCategory category = null;

    // Default constructor, needed by Hibernate
    protected Material() {

    }

    public static Material create(String code) {
        Material material = (Material) create(new Material(), code);
        material.unitType = UnitTypeBootstrap.getDefaultUnitType();
        return material;
    }

    public static Material createUnvalidated(String code, String description,
            BigDecimal defaultPrice, Boolean disabled) {
        Material material = create(new Material(), code);
        material.description = description;
        material.defaultUnitPrice = defaultPrice;
        material.disabled = disabled;
        return material;
    }

    public void updateUnvalidated(String description,
            BigDecimal defaultUnitPrice,
            Boolean disabled) {

        if (!StringUtils.isBlank(description)) {
            this.description = description;
        }

        if (defaultUnitPrice != null) {
            this.defaultUnitPrice = defaultUnitPrice;
        }

        if (disabled != null) {
            this.disabled = disabled;
        }

    }

    protected Material(String code) {
        this.setCode(code);
    }

    @NotNull
    public MaterialCategory getCategory() {
        return category;
    }

    public void setCategory(MaterialCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDisabled() {
        return disabled == null ? false : disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public BigDecimal getDefaultUnitPrice() {
        return defaultUnitPrice;
    }

    public void setDefaultUnitPrice(BigDecimal defaultUnitPrice) {
        this.defaultUnitPrice = defaultUnitPrice;
    }

    @NotNull
    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    @Override
    public int compareTo(Object arg0) {
      final Material material = (Material) arg0;
        return getCode().compareTo(material.getCode());
    }

    @Override
    protected IMaterialDAO getIntegrationEntityDAO() {
        return Registry.getMaterialDAO();
    }

}
