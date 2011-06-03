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

package org.navalplanner.web.scenarios;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.scenarios.entities.Scenario;

/**
 * Class that represents a root node for the {@link Scenario} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ScenarioTreeRoot {

    private List<Scenario> rootScenarios = new ArrayList<Scenario>();
    private List<Scenario> derivedScenarios = new ArrayList<Scenario>();

    /**
     * Creates a {@link ScenarioTreeRoot} using the list of {@link Scenario}
     * passed as argument.
     *
     * @param scenarios
     *            All the {@link Scenario} that will be shown in the tree.
     */
    public ScenarioTreeRoot(List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            if (scenario.isDerived()) {
                derivedScenarios.add(scenario);
            } else {
                rootScenarios.add(scenario);
            }
        }
    }

    /**
     * Returns all the {@link Scenario} that has no parent.
     */
    public List<Scenario> getRootScenarios() {
        return rootScenarios;
    }

    /**
     * Returns all the {@link Scenario} that has a parent.
     */
    public List<Scenario> getDerivedScenarios() {
        return derivedScenarios;
    }

}
