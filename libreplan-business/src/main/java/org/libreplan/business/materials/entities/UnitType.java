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

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.daos.IUnitTypeDAO;

/**
 * UnitType entity
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Javier Moran Rua <jmoran@ialia.com>
 */
public class UnitType extends IntegrationEntity implements IHumanIdentifiable {

    public static UnitType create(String code, String measure) {
        UnitType unitType = new UnitType(measure);
        unitType.setNewObject(true);
        return (UnitType) create(unitType, code);
    }

    public static UnitType create(String measure) {
        UnitType unitType = new UnitType(measure);
        unitType.setNewObject(true);
        return (UnitType) create(unitType);
    }

    public static UnitType create() {
        UnitType unitType = new UnitType();
        unitType.setNewObject(true);
        return create(unitType);
    }

    public void updateUnvalidated(String measure) {
        if (!StringUtils.isBlank(measure)) {
            this.measure = measure;
        }
    }

    private String measure;

    // Default constructor, needed by Hibernate
    protected UnitType() {

    }

    private UnitType(String measure) {
        this.measure = measure;
    }

    @NotEmpty(message = "measure not specified")
    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    @AssertTrue(message = "the measure unit type has to be unique. It is already used")
    public boolean checkConstraintUniqueName() {
        if (StringUtils.isBlank(measure)) {
            return true;
        }

        boolean result;
        if (isNewObject()) {
            result = !existsUnitTypeWithTheName();
        } else {
            result = isIfExistsTheExistentUnitTypeThisOne();
        }
        return result;
    }

    private boolean existsUnitTypeWithTheName() {
        IUnitTypeDAO unitTypeDAO = Registry.getUnitTypeDAO();
        return unitTypeDAO.existsUnitTypeByNameInAnotherTransaction(measure);
    }

    private boolean isIfExistsTheExistentUnitTypeThisOne() {
        IUnitTypeDAO unitTypeDAO = Registry.getUnitTypeDAO();
        try {
            UnitType unitType = unitTypeDAO
                    .findUniqueByNameInAnotherTransaction(measure);
            return unitType.getId().equals(getId());
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    @Override
    protected IUnitTypeDAO getIntegrationEntityDAO() {
        return Registry.getUnitTypeDAO();
    }

    @Override
    public String getHumanId() {
        return measure;
    }
}
