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

package org.navalplanner.web.calendars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarsTreeModel extends SimpleTreeModel {

    private static Map<BaseCalendar, List<BaseCalendar>> relationParentChildren = new HashMap<BaseCalendar, List<BaseCalendar>>();

    public BaseCalendarsTreeModel(BaseCalendarTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootCalendars(), root
                .getDerivedCalendars()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            BaseCalendarTreeRoot root, List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {

        fillHashParentChildren(rootCalendars, derivedCalendars);

        return new SimpleTreeNode(root, asNodes(rootCalendars));
    }

    private static List<SimpleTreeNode> asNodes(List<BaseCalendar> baseCalendars) {
        if (baseCalendars == null) {
            return new ArrayList<SimpleTreeNode>();
        }

        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (BaseCalendar baseCalendar : baseCalendars) {
            result.add(asNode(baseCalendar));
        }

        return result;
    }

    private static SimpleTreeNode asNode(BaseCalendar baseCalendar) {
        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);
        return new SimpleTreeNode(baseCalendar, asNodes(children));
    }

    private static void fillHashParentChildren(
            List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {
        for (BaseCalendar root : rootCalendars) {
            relationParentChildren.put(root, new ArrayList<BaseCalendar>());
        }

        for (BaseCalendar derived : derivedCalendars) {
            BaseCalendar parent = derived.getParent();
            List<BaseCalendar> siblings = relationParentChildren.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<BaseCalendar>();
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
        BaseCalendar baseCalendar = (BaseCalendar) simpleTreeNode.getData();

        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);
        if (children == null) {
            return true;
        }

        return children.isEmpty();
    }

}
