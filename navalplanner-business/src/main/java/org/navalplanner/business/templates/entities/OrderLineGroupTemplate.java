/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.business.templates.entities;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OrderLineGroupTemplate extends OrderElementTemplate {

    public static OrderLineGroupTemplate create(OrderLineGroup group) {
        return create(new OrderLineGroupTemplate(), group);
    }

    protected static <T extends OrderLineGroupTemplate> T create(T beingBuilt,
            OrderLineGroup group) {
        List<OrderElementTemplate> result = buildChildrenTemplates(beingBuilt,
                group.getChildren());
        beingBuilt.children = result;
        return OrderElementTemplate.create(beingBuilt, group);
    }

    private static List<OrderElementTemplate> buildChildrenTemplates(
            OrderLineGroupTemplate parent, List<OrderElement> children) {
        List<OrderElementTemplate> result = new ArrayList<OrderElementTemplate>();
        for (OrderElement each : children) {
            OrderElementTemplate template = each.createTemplate();
            template.setParent(parent);
            result.add(template);
        }
        return result;
    }

    private List<OrderElementTemplate> children = new ArrayList<OrderElementTemplate>();

}
