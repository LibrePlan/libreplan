/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.business.expensesheet.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.OrderElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link ExpenseSheetLine}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExpenseSheetLineDAO extends IntegrationEntityDAO<ExpenseSheetLine> implements
        IExpenseSheetLineDAO {

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<ExpenseSheetLine> findByOrderElement(OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return new ArrayList<ExpenseSheetLine>();
        }

        // Prepare criteria
        final Criteria criteria = getSession().createCriteria(ExpenseSheetLine.class);
        criteria.add(Restrictions.eq("orderElement", orderElement));
        return criteria.list();
    }

    @Override
    public List<ExpenseSheetLine> findByOrderElementAndChildren(OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return new ArrayList<ExpenseSheetLine>();
        }
        return findByOrderAndItsChildren(orderElement);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<ExpenseSheetLine> findByOrderAndItsChildren(OrderElement orderElement) {
        // Create collection with current orderElement and all its children
        Collection<OrderElement> orderElements = orderElement.getAllChildren();
        orderElements.add(orderElement);

        // Prepare criteria
        final Criteria criteria = getSession().createCriteria(ExpenseSheetLine.class);
        criteria.add(Restrictions.in("orderElement", orderElements));
        return criteria.list();
    }

}
