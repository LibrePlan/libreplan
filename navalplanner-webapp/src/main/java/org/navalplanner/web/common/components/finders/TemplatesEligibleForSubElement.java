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
package org.navalplanner.web.common.components.finders;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Repository
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplatesEligibleForSubElement extends
        TemplateFinder<OrderElementTemplate> {

    @Autowired
    private IOrderElementTemplateDAO dao;

    public TemplatesEligibleForSubElement() {
        super(OrderElementTemplate.class);
    }

    @Override
    protected List<OrderElementTemplate> getTemplates() {
        List<OrderElementTemplate> all = dao
                .list(OrderElementTemplate.class);
        List<OrderElementTemplate> result = new ArrayList<OrderElementTemplate>();
        for (OrderElementTemplate each : all) {
            if (!(each instanceof OrderTemplate) && each.isRoot()) {
                result.add(each);
            }
        }
        return result;
    }

}
