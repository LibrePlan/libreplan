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

package org.libreplan.business.test.cashflow.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.cashflow.entities.CashflowOutput;
import org.libreplan.business.cashflow.entities.CashflowPlan;
import org.libreplan.business.cashflow.entities.CashflowType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link CashflowPlan} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CashflowPlanTest {

    @Test
    public void checkCashflowOutputs() {
        CashflowPlan plan = CashflowPlan.create(null);

        LocalDate date1 = new LocalDate();
        BigDecimal amount1 = new BigDecimal(100);

        LocalDate date2 = new LocalDate().plusDays(15);
        BigDecimal amount2 = new BigDecimal(200);

        plan.addOutput(date1, amount1);
        plan.addOutput(date2, amount2);

        List<CashflowOutput> outputs = plan.getOutputs();
        assertEquals(outputs.size(), 2);
        assertEquals(outputs.get(0).getDate(), date1);
        assertEquals(outputs.get(0).getAmount(), amount1);
        assertEquals(outputs.get(1).getDate(), date2);
        assertEquals(outputs.get(1).getAmount(), amount2);

        assertEquals(plan.calculateTotal(), amount1.add(amount2));
    }

    @Test
    public void checkRemoveCashflowOutput() {
        CashflowPlan plan = CashflowPlan.create(null);

        LocalDate date1 = new LocalDate();
        BigDecimal amount1 = new BigDecimal(100);

        LocalDate date2 = new LocalDate().plusDays(15);
        BigDecimal amount2 = new BigDecimal(200);

        plan.addOutput(date1, amount1);
        plan.addOutput(date2, amount2);

        // date1 - amount2 output doesn't exist
        plan.removeOutput(date1, amount2);
        assertEquals(plan.getOutputs().size(), 2);
        assertEquals(plan.calculateTotal(), amount1.add(amount2));

        // date1 - amount1 output exists
        plan.removeOutput(date1, amount1);

        List<CashflowOutput> outputs = plan.getOutputs();
        assertEquals(outputs.size(), 1);
        assertEquals(outputs.get(0).getDate(), date2);
        assertEquals(outputs.get(0).getAmount(), amount2);

        assertEquals(plan.calculateTotal(), amount2);
    }

    @Test
    public void checkDelayDaysInDeferredPaymentType() {
        CashflowPlan plan = CashflowPlan.create(null);
        plan.setType(CashflowType.DEFERRED_PAYMENT);

        Integer days = 10;
        plan.setDelayDays(days);
        assertEquals(plan.getDelayDays(), days);

        try {
            plan.setDelayDays(null);
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        try {
            plan.setDelayDays(-10);
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkDelayDaysInManualType() {
        CashflowPlan plan = CashflowPlan.create(null);
        plan.setDelayDays(10);
    }

}
