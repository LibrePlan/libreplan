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
import java.util.Collections;
import java.util.List;

import org.navalplanner.business.orders.entities.OrderLine;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OrderLineTemplate extends OrderElementTemplate {

    public static OrderLineTemplate create(OrderLine orderLine) {
        return create(new OrderLineTemplate(), orderLine);
    }

    public static OrderLineTemplate createNew() {
        return createNew(new OrderLineTemplate());
    }

    @Override
    public List<OrderElementTemplate> getChildrenTemplates() {
        return Collections.emptyList();
    }

    @Override
    public OrderElementTemplate toLeaf() {
        return this;
    }

    @Override
    public OrderLineGroupTemplate toContainer() {
        OrderLineGroupTemplate result = OrderLineGroupTemplate.createNew();
        copyTo(result);
        return result;
    }

    @Override
    public List<OrderElementTemplate> getChildren() {
        return new ArrayList<OrderElementTemplate>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

}
