/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.costcategories.entities;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class TypeOfWorkHours extends IntegrationEntity {

    private String code;

    private String name;

    private BigDecimal defaultPrice;

    boolean enabled = true;

    // Default constructor, needed by Hibernate
    protected TypeOfWorkHours() {

    }

    public static TypeOfWorkHours create() {
        return (TypeOfWorkHours) create(new TypeOfWorkHours());
    }

    public static TypeOfWorkHours create(String code, String name) {
        return (TypeOfWorkHours) create(new TypeOfWorkHours(code, name));
    }

    public static TypeOfWorkHours createUnvalidated(String code, String name,
            Boolean enabled, BigDecimal defaultPrice) {

        TypeOfWorkHours typeOfWorkHours = create(
                new TypeOfWorkHours(code, name), code);

        if (enabled != null) {
            typeOfWorkHours.enabled = enabled;
        }
        if (defaultPrice != null) {
            typeOfWorkHours.defaultPrice = defaultPrice;
        }
        return typeOfWorkHours;
    }

    public void updateUnvalidated(String name, Boolean enabled,
            BigDecimal defaultPrice) {
        if (!StringUtils.isBlank(name)) {
            this.name = name;
        }
        if (enabled != null) {
            this.enabled = enabled;
        }
        if (defaultPrice != null) {
            this.defaultPrice = defaultPrice;
        }
    }

    protected TypeOfWorkHours(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @NotEmpty(message = "name not specified")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty(message = "code not specified")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal price) {
        this.defaultPrice = price;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @AssertTrue(message="type code has to be unique. It is already used")
    public boolean checkConstraintUniqueCode() {

        if (code == null) {
            return true;
        }

        boolean result;
        if (isNewObject()) {
            result = !existsTypeWithTheCode();
        } else {
            result = isTheExistentTypeThisOne();
        }
        return result;
    }

    private boolean existsTypeWithTheCode() {
        ITypeOfWorkHoursDAO dao = Registry.getTypeOfWorkHoursDAO();
        return dao.existsTypeWithCodeInAnotherTransaction(code);
    }

    private boolean isTheExistentTypeThisOne() {
        ITypeOfWorkHoursDAO dao = Registry.getTypeOfWorkHoursDAO();
        try {
            TypeOfWorkHours type =
                dao.findUniqueByCodeInAnotherTransaction(code);
            return type.getId().equals(getId());
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    @Override
    protected ITypeOfWorkHoursDAO getIntegrationEntityDAO() {
        return Registry.getTypeOfWorkHoursDAO();
    }
}
