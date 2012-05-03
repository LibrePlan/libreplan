/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.orders.entities;

import org.hibernate.validator.NotEmpty;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class InfoComponentWithCode extends InfoComponent {

    private String code;

    public void setCode(String code) {
        this.code = code;
    }

    @NotEmpty(message = "code not specified")
    public String getCode() {
        return code;
    }

    public InfoComponentWithCode copy() {
        InfoComponentWithCode result = new InfoComponentWithCode();
        result.setCode(getCode());
        result.setName(getName());
        result.setDescription(getDescription());
        return result;
    }

}
