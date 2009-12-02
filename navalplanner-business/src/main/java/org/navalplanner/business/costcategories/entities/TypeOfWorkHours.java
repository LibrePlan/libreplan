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

package org.navalplanner.business.costcategories.entities;

import java.math.BigDecimal;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class TypeOfWorkHours extends BaseEntity {

    @NotEmpty
    private String code;

    @NotEmpty
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

    protected TypeOfWorkHours(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
}
