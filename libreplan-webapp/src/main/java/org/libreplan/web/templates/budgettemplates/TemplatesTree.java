/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.web.templates.budgettemplates;

import static org.libreplan.web.I18nHelper._;

import org.libreplan.business.templates.entities.BudgetLineTemplate;
import org.libreplan.business.templates.entities.BudgetLineTypeEnum;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.tree.EntitiesTree;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class TemplatesTree extends EntitiesTree<OrderElementTemplate> {

    public TemplatesTree(OrderElementTemplate root) {
        super(OrderElementTemplate.class, root);
    }

    @Override
    protected OrderElementTemplate createNewElement() {
        return createNewElement(_("New code"), _("New template"), null, 0);
    }

    @Override
    protected OrderElementTemplate createNewElement(String name, int hours) {
        return createNewElement(_("New code"), name, null, hours);
    }

    @Override
    protected OrderElementTemplate createNewElement(String name,
            BudgetLineTypeEnum type) {
        return createNewElement(_("New code"), name, type, 0);
    }

    @Override
    protected OrderElementTemplate createNewElement(String code, String name,
            BudgetLineTypeEnum type) {
        return createNewElement(code, name, type, 0);
    }

    private OrderElementTemplate createNewElement(String code, String name,
            BudgetLineTypeEnum type, int hours) {
        BudgetLineTemplate result = BudgetLineTemplate.createNew();
        result.setName(name);
        result.setCode(code);
        result.setBudgetLineType(type);
        result.setWorkHours(hours);
        return result;
    }

}
