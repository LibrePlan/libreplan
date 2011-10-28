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

package org.libreplan.web.scenarios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libreplan.business.scenarios.entities.Scenario;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for the {@link Scenario} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ScenariosTreeModel extends SimpleTreeModel {

    public ScenariosTreeModel(ScenarioTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootScenarios(), root
                .getDerivedScenarios()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            ScenarioTreeRoot root, List<Scenario> rootScenarios,
            List<Scenario> derivedScenarios) {
        return new SimpleTreeNode(root, asNodes(fillHashParentChildren(
                rootScenarios, derivedScenarios), rootScenarios));
    }

    private static List<SimpleTreeNode> asNodes(
            Map<Scenario, List<Scenario>> childrenMap,
            List<Scenario> scenarios) {
        if (scenarios == null) {
            return new ArrayList<SimpleTreeNode>();
        }

        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (Scenario scenario : scenarios) {
            result.add(asNode(childrenMap, scenario));
        }

        return result;
    }

    private static SimpleTreeNode asNode(
            Map<Scenario, List<Scenario>> childrenMap, Scenario scenario) {
        List<Scenario> children = childrenMap.get(scenario);
        return new SimpleTreeNode(scenario, asNodes(childrenMap, children));
    }

    private static Map<Scenario, List<Scenario>> fillHashParentChildren(
            List<Scenario> rootScenarios,
            List<Scenario> derivedScenarios) {
        Map<Scenario, List<Scenario>> result = new HashMap<Scenario, List<Scenario>>();
        for (Scenario root : rootScenarios) {
            result.put(root, new ArrayList<Scenario>());
        }

        for (Scenario derived : derivedScenarios) {
            Scenario parent = derived.getPredecessor();
            List<Scenario> siblings = result.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<Scenario>();
                siblings.add(derived);
                result.put(parent, siblings);
            } else {
                siblings.add(derived);
            }
        }
        return result;
    }

}
