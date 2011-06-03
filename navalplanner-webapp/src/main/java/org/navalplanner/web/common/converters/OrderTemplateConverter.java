/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.web.common.converters;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderTemplateConverter implements IConverter<OrderTemplate> {

    @Autowired
    private IOrderElementTemplateDAO orderElementTemplateDAO;

    @Override
    public OrderTemplate asObject(String stringRepresentation) {
        try {
            return (OrderTemplate) orderElementTemplateDAO.find(Long
                    .parseLong(stringRepresentation));
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(OrderTemplate entity) {
        return entity.getId() + "";
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((OrderTemplate) entity);
    }

    @Override
    public Class<OrderTemplate> getType() {
        return OrderTemplate.class;
    }
}
