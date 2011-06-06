/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.orders.entities;

import org.navalplanner.business.common.BaseEntity;

/**
 * It represents the hours charged to an {@link OrderElement}, avoiding the
 * need to iterate among the work report lines to get this information. <br />
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class SumChargedHours extends BaseEntity {

    private Integer directChargedHours = 0;

    private Integer indirectChargedHours = 0;

    protected SumChargedHours() {}

    public static SumChargedHours create() {
        return create(new SumChargedHours());
    }

    public void setDirectChargedHours(Integer directChargedHours) {
        this.directChargedHours = directChargedHours;
    }

    public void addDirectChargedHours(Integer directChargedHours) {
        this.directChargedHours += directChargedHours;
    }

    public Integer getDirectChargedHours() {
        return directChargedHours;
    }

    public void setIndirectChargedHours(Integer indirectChargedHours) {
        this.indirectChargedHours = indirectChargedHours;
    }

    public void addIndirectChargedHours(Integer indirectChargedHours) {
        this.indirectChargedHours += indirectChargedHours;
    }

    public Integer getIndirectChargedHours() {
        return indirectChargedHours;
    }

    public Integer getTotalChargedHours() {
        return directChargedHours + indirectChargedHours;
    }

}
