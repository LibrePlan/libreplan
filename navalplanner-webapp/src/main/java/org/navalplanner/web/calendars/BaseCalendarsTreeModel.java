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

package org.navalplanner.web.calendars;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarsTreeModel extends SimpleTreeModel {

    public BaseCalendarsTreeModel(BaseCalendarTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootCalendars(), root
                .getDerivedCalendars()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            BaseCalendarTreeRoot root, List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {

        Map<BaseCalendar, List<BaseCalendar>> parentChildren = createRelationParentChildren(
                        rootCalendars, derivedCalendars);
        return new SimpleTreeNode(root, asNodes(parentChildren, rootCalendars));
    }

    private static List<SimpleTreeNode> asNodes(
            Map<BaseCalendar, List<BaseCalendar>> relationParentChildren,
            List<BaseCalendar> baseCalendars) {
        if (baseCalendars == null) {
            return new ArrayList<SimpleTreeNode>();
        }

        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (BaseCalendar baseCalendar : baseCalendars) {
            result.add(asNode(relationParentChildren, baseCalendar));
        }

        return result;
    }

    private static SimpleTreeNode asNode(
            Map<BaseCalendar, List<BaseCalendar>> relationParentChildren,
            BaseCalendar baseCalendar) {
        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);
        return new SimpleTreeNode(baseCalendar, asNodes(relationParentChildren,
                children));
    }

    private static Map<BaseCalendar, List<BaseCalendar>> createRelationParentChildren(
            List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {
        Map<BaseCalendar, List<BaseCalendar>> result = new HashMap<BaseCalendar, List<BaseCalendar>>();
        for (BaseCalendar root : rootCalendars) {
            result.put(root, new ArrayList<BaseCalendar>());
        }

        for (BaseCalendar derived : derivedCalendars) {
            BaseCalendar parent = derived.getCalendarData(
                    LocalDate.fromDateFields(new Date())).getParent();
            List<BaseCalendar> siblings = result.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<BaseCalendar>();
                siblings.add(derived);
                result.put(parent, siblings);
            } else {
                siblings.add(derived);
            }
        }
        return result;
    }

}
