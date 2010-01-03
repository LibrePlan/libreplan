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
package org.navalplanner.web.templates;

import java.util.List;

import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderTemplatesModel implements IOrderTemplatesModel {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderElementTemplateDAO dao;

    @Override
    public List<OrderElementTemplate> getTemplates() {
        return dao.list(OrderElementTemplate.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderElementTemplate createTemplateFrom(OrderElement orderElement) {
        loadParentsInOrderToAvoidProxies(orderElement);
        OrderElement reloaded = orderElementDAO
                .findExistingEntity(orderElement.getId());
        OrderElementTemplate result = reloaded.createTemplate();
        return result;
    }

    private void loadParentsInOrderToAvoidProxies(OrderElement orderElement) {
        OrderElement current = orderElement;
        while (current != null) {
            current = orderElementDAO.findParent(current);
        }
    }

}
