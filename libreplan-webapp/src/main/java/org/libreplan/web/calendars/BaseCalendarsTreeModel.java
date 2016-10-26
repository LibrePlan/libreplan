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

package org.libreplan.web.calendars;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;

/**
 * Model for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarsTreeModel extends DefaultTreeModel {

    /**
     * Method BaseCalendarsTreeModel#createRootNodeAndDescendants and included methods should be static!
     * Link to:
     * http://stackoverflow.com/questions/10963775/cannot-reference-x-before-supertype-constructor-has-been-called-where-x-is-a
     */
    public BaseCalendarsTreeModel(BaseCalendarTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootCalendars(), root.getDerivedCalendars()));
    }

    private static DefaultTreeNode<Object> createRootNodeAndDescendants(
            BaseCalendarTreeRoot root, List<BaseCalendar> rootCalendars, List<BaseCalendar> derivedCalendars) {

        Map<BaseCalendar, List<BaseCalendar>> parentChildren =
                createRelationParentChildren(rootCalendars, derivedCalendars);

        return new DefaultTreeNode<>(root, asNodes(parentChildren, rootCalendars));
    }

    private static List<DefaultTreeNode<Object>> asNodes(
            Map<BaseCalendar, List<BaseCalendar>> relationParentChildren, List<BaseCalendar> baseCalendars) {

        if (baseCalendars == null) {
            return new ArrayList<>();
        }

        ArrayList<DefaultTreeNode<Object>> result = new ArrayList<>();
        for (BaseCalendar baseCalendar : baseCalendars) {
            result.add(asNode(relationParentChildren, baseCalendar));
        }

        return result;
    }

    private static DefaultTreeNode<Object> asNode(
            Map<BaseCalendar, List<BaseCalendar>> relationParentChildren, BaseCalendar baseCalendar) {

        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);

        if (children != null && !children.isEmpty()) {
            return new DefaultTreeNode<>(baseCalendar, asNodes(relationParentChildren, children));
        } else {
            return new DefaultTreeNode<>(baseCalendar);
        }
    }

    private static Map<BaseCalendar, List<BaseCalendar>> createRelationParentChildren(
            List<BaseCalendar> rootCalendars, List<BaseCalendar> derivedCalendars) {

        Map<BaseCalendar, List<BaseCalendar>> result = new HashMap<>();
        for (BaseCalendar root : rootCalendars) {
            result.put(root, new ArrayList<>());
        }

        for (BaseCalendar derived : derivedCalendars) {
            BaseCalendar parent = derived.getCalendarData(LocalDate.fromDateFields(new Date())).getParent();
            List<BaseCalendar> siblings = result.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<>();
                siblings.add(derived);
                result.put(parent, siblings);
            } else {
                siblings.add(derived);
            }
        }

        return result;
    }

}
