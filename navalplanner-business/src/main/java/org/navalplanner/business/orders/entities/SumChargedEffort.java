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
import org.navalplanner.business.workingday.EffortDuration;

/**
 * It represents the efforts charged to an {@link OrderElement}, avoiding the
 * need to iterate among the work report lines to get this information.
 *
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class SumChargedEffort extends BaseEntity {

    private EffortDuration directChargedEffort = EffortDuration.zero();

    private EffortDuration indirectChargedEffort = EffortDuration.zero();

    protected SumChargedEffort() {}

    public static SumChargedEffort create() {
        return create(new SumChargedEffort());
    }

    public void addDirectChargedEffort(EffortDuration directChargedEffort) {
        this.directChargedEffort = this.directChargedEffort
                .plus(directChargedEffort);
    }

    public void subtractDirectChargedEffort(EffortDuration directChargedEffort) {
        this.directChargedEffort = this.directChargedEffort
                .minus(directChargedEffort);
    }

    public EffortDuration getDirectChargedEffort() {
        return directChargedEffort;
    }

    public void addIndirectChargedEffort(EffortDuration indirectChargedEffort) {
        this.indirectChargedEffort = this.indirectChargedEffort
                .plus(indirectChargedEffort);
    }

    public EffortDuration getIndirectChargedEffort() {
        return indirectChargedEffort;
    }

    public EffortDuration getTotalChargedEffort() {
        return directChargedEffort.plus(indirectChargedEffort);
    }

}
