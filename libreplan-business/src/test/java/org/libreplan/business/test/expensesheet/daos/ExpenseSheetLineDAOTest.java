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

package org.libreplan.business.test.expensesheet.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;
import org.libreplan.business.expensesheet.daos.IExpenseSheetLineDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link ExpenseSheetLineDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class ExpenseSheetLineDAOTest {

    @Autowired
    private IExpenseSheetLineDAO expenseSheetLineDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IExpenseSheetDAO expenseSheetDAO;

    private OrderElement createValidOrderElement() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("orderline-" + UUID.randomUUID());
        orderLine.setCode("code-" + UUID.randomUUID());
        orderElementDAO.save(orderLine);
        // OrderLine.create() marks the object new, and save() never clears that flag; both
        // findByOrderElement and findByOrderElementAndChildren short-circuit to an empty list
        // for isNewObject()==true, so this must be unmarked to match how a real DB-loaded
        // OrderElement would behave.
        orderLine.dontPoseAsTransientObjectAnymore();
        return orderLine;
    }

    private ExpenseSheetLine createValidLine(OrderElement orderElement) {
        ExpenseSheetLine line = ExpenseSheetLine.create(
                BigDecimal.TEN, "concept-" + UUID.randomUUID(), new LocalDate(2020, 1, 1), orderElement);

        ExpenseSheet sheet =
                ExpenseSheet.create(new LocalDate(2020, 1, 1), new LocalDate(2020, 1, 31), BigDecimal.TEN);
        sheet.add(line);
        line.setExpenseSheet(sheet);

        expenseSheetDAO.save(sheet);
        expenseSheetLineDAO.save(line);
        return line;
    }

    @Test
    @Transactional
    public void testFindByOrderElementReturnsEmptyForNewObject() {
        OrderElement notSaved = OrderLine.create();
        notSaved.setName("orderline-" + UUID.randomUUID());
        notSaved.setCode("code-" + UUID.randomUUID());

        assertTrue(expenseSheetLineDAO.findByOrderElement(notSaved).isEmpty());
    }

    @Test
    @Transactional
    public void testFindByOrderElementReturnsMatch() {
        OrderElement orderElement = createValidOrderElement();
        ExpenseSheetLine line = createValidLine(orderElement);

        OrderElement other = createValidOrderElement();
        createValidLine(other);

        List<ExpenseSheetLine> result = expenseSheetLineDAO.findByOrderElement(orderElement);
        assertEquals(1, result.size());
        assertEquals(line.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    public void testFindByOrderElementAndChildrenReturnsEmptyForNewObject() {
        OrderElement notSaved = OrderLine.create();
        notSaved.setName("orderline-" + UUID.randomUUID());
        notSaved.setCode("code-" + UUID.randomUUID());

        assertTrue(expenseSheetLineDAO.findByOrderElementAndChildren(notSaved).isEmpty());
    }

    @Test
    @Transactional
    public void testFindByOrderElementAndChildrenReturnsMatchWithNoChildren() {
        OrderElement orderElement = createValidOrderElement();
        ExpenseSheetLine line = createValidLine(orderElement);

        List<ExpenseSheetLine> result = expenseSheetLineDAO.findByOrderElementAndChildren(orderElement);
        assertEquals(1, result.size());
        assertEquals(line.getId(), result.get(0).getId());
    }

}
