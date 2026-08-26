/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.test.orders.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.daos.ISumExpensesDAO;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SumExpenses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link SumExpensesDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class SumExpensesDAOTest {

    @Autowired
    private ISumExpensesDAO sumExpensesDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    private OrderLine createValidOrderElement() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("orderline-" + UUID.randomUUID());
        orderLine.setCode("code-" + UUID.randomUUID());
        orderElementDAO.save(orderLine);
        return orderLine;
    }

    @Test
    @Transactional
    public void testFindByOrderElementReturnsNullWhenAbsent() {
        OrderLine orderLine = createValidOrderElement();
        assertNull(sumExpensesDAO.findByOrderElement(orderLine));
    }

    @Test
    @Transactional
    public void testFindByOrderElementReturnsMatch() {
        OrderLine orderLine = createValidOrderElement();
        SumExpenses sumExpenses = SumExpenses.create(orderLine);
        sumExpensesDAO.save(sumExpenses);

        SumExpenses found = sumExpensesDAO.findByOrderElement(orderLine);
        assertEquals(sumExpenses.getId(), found.getId());
    }

    @Test
    @Transactional
    public void testFindByOrderElementDoesNotConfuseDifferentOrderElements() {
        OrderLine orderLine1 = createValidOrderElement();
        OrderLine orderLine2 = createValidOrderElement();
        SumExpenses sumExpenses = SumExpenses.create(orderLine1);
        sumExpensesDAO.save(sumExpenses);

        assertNull(sumExpensesDAO.findByOrderElement(orderLine2));
    }

}
