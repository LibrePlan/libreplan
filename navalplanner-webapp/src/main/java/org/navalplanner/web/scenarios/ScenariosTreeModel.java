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

package org.navalplanner.web.scenarios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.scenarios.entities.Scenario;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for the {@link Scenario} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ScenariosTreeModel extends SimpleTreeModel {

    private static Map<Scenario, List<Scenario>> relationParentChildren = new HashMap<Scenario, List<Scenario>>();

    public ScenariosTreeModel(ScenarioTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootScenarios(), root
                .getDerivedScenarios()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            ScenarioTreeRoot root, List<Scenario> rootScenarios,
            List<Scenario> derivedScenarios) {

        fillHashParentChildren(rootScenarios, derivedScenarios);

        return new SimpleTreeNode(root, asNodes(rootScenarios));
    }

    private static List<SimpleTreeNode> asNodes(List<Scenario> scenarios) {
        if (scenarios == null) {
            return new ArrayList<SimpleTreeNode>();
        }

        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (Scenario scenario : scenarios) {
            result.add(asNode(scenario));
        }

        return result;
    }

    private static SimpleTreeNode asNode(Scenario scenario) {
        List<Scenario> children = relationParentChildren.get(scenario);
        return new SimpleTreeNode(scenario, asNodes(children));
    }

    private static void fillHashParentChildren(
            List<Scenario> rootScenarios,
            List<Scenario> derivedScenarios) {
        for (Scenario root : rootScenarios) {
            relationParentChildren.put(root, new ArrayList<Scenario>());
        }

        for (Scenario derived : derivedScenarios) {
            Scenario parent = derived.getPredecessor();
            List<Scenario> siblings = relationParentChildren.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<Scenario>();
                siblings.add(derived);
                relationParentChildren.put(parent, siblings);
            } else {
                siblings.add(derived);
            }
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node == null) {
            return true;
        }

        SimpleTreeNode simpleTreeNode = (SimpleTreeNode) node;
        Scenario scenario = (Scenario) simpleTreeNode.getData();

        List<Scenario> children = relationParentChildren.get(scenario);
        if (children == null) {
            return true;
        }

        return children.isEmpty();
    }

}
