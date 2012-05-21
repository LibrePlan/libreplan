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

package org.libreplan.business.orders.daos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetLineDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumExpenses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract for {@link SumExpensesDAO}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SumExpensesDAO extends GenericDAOHibernate<SumExpenses, Long> implements
        ISumExpensesDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IExpenseSheetLineDAO expenseSheetLineDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderDAO orderDAO;

    private Map<OrderElement, SumExpenses> mapSumExpenses;

    @Override
    public void updateRelatedSumExpensesWithExpenseSheetLineSet(
            Set<ExpenseSheetLine> expenseSheetLineSet) {
        resetMapSumExpenses();

        for (ExpenseSheetLine expenseSheetLine : expenseSheetLineSet) {
            updateRelatedSumExpensesWithAddedOrModifiedExpenseSheetLine(expenseSheetLine);
        }
    }

    private void updateRelatedSumExpensesWithAddedOrModifiedExpenseSheetLine(
            final ExpenseSheetLine expenseSheetLine) {
        boolean increase = true;
        BigDecimal value = expenseSheetLine.getValue();
        if (!expenseSheetLine.isNewObject()) {
            BigDecimal previousValue = transactionService
                    .runOnAnotherTransaction(new IOnTransaction<BigDecimal>() {
                        @Override
                        public BigDecimal execute() {
                            try {
                                return expenseSheetLineDAO.find(expenseSheetLine.getId())
                                        .getValue();
                            } catch (InstanceNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
            if (value.compareTo(previousValue) >= 0) {
                value = value.subtract(previousValue);
            } else {
                increase = false;
                value = previousValue.subtract(value);
            }
        }

        if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
            if (increase) {
                addDirectExpenses(expenseSheetLine.getOrderElement(), value);
            } else {
                substractDirectExpenses(expenseSheetLine.getOrderElement(), value);
            }
        }
    }

    private void addDirectExpenses(OrderElement orderElement, BigDecimal value) {
        SumExpenses sumExpenses = getByOrderElement(orderElement);
        if (sumExpenses == null) {
            sumExpenses = SumExpenses.create(orderElement);
        }

        sumExpenses.addDirectExpenses(value);
        save(sumExpenses);

        addIndirectExpensesRecursively(orderElement.getParent(), value);
    }

    private void addIndirectExpensesRecursively(OrderElement orderElement, BigDecimal value) {
        if (orderElement != null) {
            SumExpenses sumExpenses = getByOrderElement(orderElement);
            if (sumExpenses == null) {
                sumExpenses = SumExpenses.create(orderElement);
            }

            sumExpenses.addIndirectExpenses(value);
            save(sumExpenses);

            addIndirectExpensesRecursively(orderElement.getParent(), value);
        }
    }

    @Override
    public void updateRelatedSumExpensesWithDeletedExpenseSheetLineSet(
            Set<ExpenseSheetLine> expenseSheetLineSet) {
        resetMapSumExpenses();

        for (ExpenseSheetLine expenseSheetLine : expenseSheetLineSet) {
            updateRelatedSumExpensesWithDeletedExpenseSheetLine(expenseSheetLine);
        }
    }

    private void resetMapSumExpenses() {
        mapSumExpenses = new HashMap<OrderElement, SumExpenses>();
    }

    private void updateRelatedSumExpensesWithDeletedExpenseSheetLine(
            ExpenseSheetLine expenseSheetLine) {
        if (expenseSheetLine.isNewObject()) {
            // If the line hasn't been saved, we have nothing to update
            return;
        }

        // Refresh data from database, because of changes not saved are not
        // useful for the following operations
        sessionFactory.getCurrentSession().refresh(expenseSheetLine);

        substractDirectExpenses(expenseSheetLine.getOrderElement(), expenseSheetLine.getValue());
    }

    private void substractDirectExpenses(OrderElement orderElement, BigDecimal value) {
        SumExpenses sumExpenses = getByOrderElement(orderElement);

        sumExpenses.subtractDirectExpenses(value);
        save(sumExpenses);

        substractIndirectExpensesRecursively(orderElement.getParent(), value);
    }

    private void substractIndirectExpensesRecursively(OrderElement orderElement, BigDecimal value) {
        if (orderElement != null) {
            SumExpenses sumExpenses = getByOrderElement(orderElement);

            sumExpenses.subtractIndirectExpenses(value);
            save(sumExpenses);

            substractIndirectExpensesRecursively(orderElement.getParent(), value);
        }
    }

    private SumExpenses getByOrderElement(OrderElement orderElement) {
        SumExpenses sumExpenses = mapSumExpenses.get(orderElement);
        if (sumExpenses == null) {
            sumExpenses = findByOrderElement(orderElement);
            mapSumExpenses.put(orderElement, sumExpenses);
        }
        return sumExpenses;
    }

    @Override
    public SumExpenses findByOrderElement(OrderElement orderElement) {
        return (SumExpenses) getSession().createCriteria(getEntityClass())
                .add(Restrictions.eq("orderElement", orderElement)).uniqueResult();
    }

    @Override
    @Transactional
    public void recalculateSumExpenses(Long orderId) {
        try {
            Order order = orderDAO.find(orderId);
            resetMapSumExpenses();
            resetSumExpenses(order);
            calculateDirectExpenses(order);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetSumExpenses(OrderElement orderElement) {
        SumExpenses sumExpenses = getByOrderElement(orderElement);
        if (sumExpenses == null) {
            sumExpenses = SumExpenses.create(orderElement);
        }
        sumExpenses.reset();

        for (OrderElement each : orderElement.getChildren()) {
            resetSumExpenses(each);
        }
    }

    private void calculateDirectExpenses(OrderElement orderElement) {
        for (OrderElement each : orderElement.getChildren()) {
            calculateDirectExpenses(each);
        }

        BigDecimal value = BigDecimal.ZERO;
        for (ExpenseSheetLine line : expenseSheetLineDAO.findByOrderElement(orderElement)) {
            value = value.add(line.getValue());
        }
        addDirectExpenses(orderElement, value);
    }
}