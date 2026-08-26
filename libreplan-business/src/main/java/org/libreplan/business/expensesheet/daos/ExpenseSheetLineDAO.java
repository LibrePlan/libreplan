/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseSheetLine> findByOrderElement(OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return new ArrayList<ExpenseSheetLine>();
        }

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExpenseSheetLine> cq = cb.createQuery(ExpenseSheetLine.class);
        Root<ExpenseSheetLine> root = cq.from(ExpenseSheetLine.class);
        cq.where(cb.equal(root.get("orderElement"), orderElement));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<ExpenseSheetLine> findByOrderElementAndChildren(OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return new ArrayList<ExpenseSheetLine>();
        }
        return findByOrderAndItsChildren(orderElement);
    }

    @Transactional(readOnly = true)
    public List<ExpenseSheetLine> findByOrderAndItsChildren(OrderElement orderElement) {
        // Create collection with current orderElement and all its children
        Collection<OrderElement> orderElements = orderElement.getAllChildren();
        orderElements.add(orderElement);

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<ExpenseSheetLine> cq = cb.createQuery(ExpenseSheetLine.class);
        Root<ExpenseSheetLine> root = cq.from(ExpenseSheetLine.class);
        cq.where(root.get("orderElement").in(orderElements));
        return getSession().createQuery(cq).getResultList();
    }

}
