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
package org.navalplanner.business.planner.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;

/**
 * Object containing the {@link DerivedDayAssignment derived day assignments}
 * for a {@link DerivedAllocation} at a {@link Scenario} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DerivedDayAssignmentsContainer extends BaseEntity {

    private DerivedAllocation resourceAllocation;

    @OnCopy(Strategy.SHARE)
    private Scenario scenario;

    private Set<DerivedDayAssignment> dayAssignments = new HashSet<DerivedDayAssignment>();

    /**
     * Constructor for HIBERNATE. DO NOT USE!
     */
    public DerivedDayAssignmentsContainer() {
    }

    Set<DerivedDayAssignment> getDayAssignments() {
        return new HashSet<DerivedDayAssignment>(dayAssignments);
    }

    public DerivedAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    public Scenario getScenario() {
        return scenario;
    }
}
