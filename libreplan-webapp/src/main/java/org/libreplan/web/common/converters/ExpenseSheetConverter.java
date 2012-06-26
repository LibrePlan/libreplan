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
package org.libreplan.web.common.converters;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A {@link IConverter} for {@link ExpenseSheet} objects.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExpenseSheetConverter implements IConverter<ExpenseSheet> {

    @Autowired
    private IExpenseSheetDAO expenseSheetDAO;

    @Override
    public Class<ExpenseSheet> getType() {
        return ExpenseSheet.class;
    }

    @Override
    public String asString(ExpenseSheet entity) {
        return entity.getId().toString();
    }

    @Override
    public ExpenseSheet asObject(String stringRepresentation) {
        try {
            return expenseSheetDAO.find(Long.parseLong(stringRepresentation));
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((ExpenseSheet) entity);
    }

}
