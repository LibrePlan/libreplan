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

package org.navalplanner.business.test.costcategories.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.daos.IHourCostDAO;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@HourCostDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class HourCostDAOTest {

    @Autowired
    IHourCostDAO hourCostDAO;

    @Autowired
    ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    ICostCategoryDAO costCategoryDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(hourCostDAO);
    }

    private HourCost createValidHourCost() {
        HourCost hourCost = HourCost.create(BigDecimal.ONE, new LocalDate());

        TypeOfWorkHours type =
                TypeOfWorkHours.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        hourCost.setType(type);
        typeOfWorkHoursDAO.save(type);

        CostCategory costCategory = CostCategory.create(UUID.randomUUID().toString());
        hourCost.setCategory(costCategory);
        costCategoryDAO.save(costCategory);

        return hourCost;
    }

    @Test
    public void testSaveHourCost() {
        HourCost hourCost = createValidHourCost();
        hourCostDAO.save(hourCost);
        assertTrue(hourCost.getId() != null);
    }

    @Test
    public void testRemoveHourCost() throws InstanceNotFoundException {
        HourCost hourCost = createValidHourCost();
        hourCostDAO.save(hourCost);
        hourCostDAO.remove(hourCost.getId());
        assertFalse(hourCostDAO.exists(hourCost.getId()));
    }

    @Test
    public void testListHourCost() {
        int previous = hourCostDAO.list(HourCost.class).size();
        HourCost hourCost = createValidHourCost();
        hourCostDAO.save(hourCost);
        List<HourCost> list = hourCostDAO.list(HourCost.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testCategoryNavigation() {
        HourCost hourCost = createValidHourCost();
        assertTrue(hourCost.getCategory().getHourCosts().contains(hourCost));
    }

    @Test
    public void testHourCostNotInTwoCategories() {
        HourCost hourCost = createValidHourCost();
        CostCategory costCategory1 = CostCategory.create(UUID.randomUUID().toString());
        CostCategory costCategory2 = CostCategory.create(UUID.randomUUID().toString());

        hourCost.setCategory(costCategory1);
        hourCost.setCategory(costCategory2);
        hourCostDAO.save(hourCost);

        assertFalse(costCategory1.getHourCosts().contains(hourCost));
        assertTrue(costCategory2.getHourCosts().contains(hourCost));

        costCategory1.addHourCost(hourCost);
        costCategory2.addHourCost(hourCost);
        hourCostDAO.save(hourCost);

        assertFalse(costCategory1.getHourCosts().contains(hourCost));
        assertTrue(costCategory2.getHourCosts().contains(hourCost));
    }

    @Test(expected=ValidationException.class)
    public void testPositiveTimeInterval() {
        HourCost hourCost = createValidHourCost();
        hourCost.setInitDate(new LocalDate(2000,12,31));
        hourCost.setEndDate(new LocalDate(2000,12,1));

        hourCostDAO.save(hourCost);
    }
}
