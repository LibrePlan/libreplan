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

package org.navalplanner.business.test.costcategories.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@CostCategoryDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class CostCategoryDAOTest {

    @Autowired
    ICostCategoryDAO costCategoryDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(costCategoryDAO);
    }

    private CostCategory createValidCostCategory() {
        CostCategory costCategory = CostCategory.create(UUID.randomUUID().toString());
        return costCategory;
    }

    @Test
    public void testSaveCostCategory() {
        CostCategory costCategory = createValidCostCategory();
        costCategoryDAO.save(costCategory);
        assertTrue(costCategory.getId() != null);
    }

    @Test
    public void testRemoveCostCategory() throws InstanceNotFoundException {
        CostCategory costCategory = createValidCostCategory();
        costCategoryDAO.save(costCategory);
        costCategoryDAO.remove(costCategory.getId());
        assertFalse(costCategoryDAO.exists(costCategory.getId()));
    }

    @Test
    public void testListCostCategories() {
        int previous = costCategoryDAO.list(CostCategory.class).size();
        CostCategory costCategory = createValidCostCategory();
        costCategoryDAO.save(costCategory);
        List<CostCategory> list = costCategoryDAO.list(CostCategory.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testCanAddHourCost() {
        CostCategory costCategory = createValidCostCategory();
        HourCost hourCost1 = HourCost.create(BigDecimal.ONE, new LocalDate(2009, 11,1));
        hourCost1.setEndDate(new LocalDate(2009, 11,10));
        costCategory.addHourCost(hourCost1);

        HourCost hourCost2 = HourCost.create(BigDecimal.ONE, new LocalDate(2009, 11,1));
        hourCost2.setEndDate(new LocalDate(2009, 11,10));
        assertFalse(costCategory.canAddHourCost(hourCost2));

        hourCost2.setInitDate(new LocalDate(2009,10,15));
        hourCost2.setEndDate(new LocalDate(2009,11,1));
        assertFalse(costCategory.canAddHourCost(hourCost2));

        hourCost2.setInitDate(new LocalDate(2009,11,10));
        hourCost2.setEndDate(new LocalDate(2009,11,10));
        assertFalse(costCategory.canAddHourCost(hourCost2));

        hourCost2.setInitDate(new LocalDate(2009,10,15));
        hourCost2.setEndDate(new LocalDate(2009,10,20));
        assertTrue(costCategory.canAddHourCost(hourCost2));
    }

    @Test
    public void testListHourCosts() {
        CostCategory costCategory = createValidCostCategory();
        HourCost hourCost = HourCost.create(BigDecimal.ONE, new LocalDate(2009,11,1));
        int previous = costCategory.getHourCosts().size();

        costCategory.addHourCost(hourCost);
        costCategoryDAO.save(costCategory);
        assertEquals(previous + 1, costCategory.getHourCosts().size());

        costCategory.removeHourCost(hourCost);
        costCategoryDAO.save(costCategory);
        assertEquals(previous, costCategory.getHourCosts().size());
        assertNull(hourCost.getCategory());
    }
}
