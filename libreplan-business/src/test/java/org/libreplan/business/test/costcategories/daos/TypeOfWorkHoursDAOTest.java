/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.test.costcategories.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.daos.ICostCategoryDAO;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@TypeOfWorkHoursDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class TypeOfWorkHoursDAOTest extends
        org.libreplan.business.test.workreports.daos.AbstractWorkReportTest {

    @Autowired
    ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    ICostCategoryDAO costCategoryDAO;

    @Autowired
    IWorkReportLineDAO workReportLineDAO;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(typeOfWorkHoursDAO);
    }

    private TypeOfWorkHours createValidTypeOfWorkHours() {
        TypeOfWorkHours typeOfWorkHours =
            TypeOfWorkHours.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        typeOfWorkHours.setDefaultPrice(BigDecimal.TEN);
        return typeOfWorkHours;
    }

    @Test
    @Transactional
    public void testSaveTypeOfWorkHours() {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        assertTrue(typeOfWorkHours.getId() != null);
    }

    @Test
    @Transactional
    public void testRemoveTypeOfWorkHours() throws InstanceNotFoundException {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        typeOfWorkHoursDAO.remove(typeOfWorkHours.getId());
        assertFalse(typeOfWorkHoursDAO.exists(typeOfWorkHours.getId()));
    }

    @Test
    @Transactional
    public void testListTypesOfWorkHours() {
        int previous = typeOfWorkHoursDAO.list(TypeOfWorkHours.class).size();
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        List<TypeOfWorkHours> list = typeOfWorkHoursDAO.list(TypeOfWorkHours.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    @Transactional
    public void testFindTypesOfWorkHoursByCode() {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        try {
            TypeOfWorkHours found = typeOfWorkHoursDAO.findUniqueByCode(typeOfWorkHours.getCode());
            assertNotNull(found);
            assertTrue(found.equals(typeOfWorkHours));
        }
        catch (InstanceNotFoundException e) {

        }
    }

    @Test(expected=InstanceNotFoundException.class)
    @Transactional
    public void testFindTypesOfWorkHoursByCodeException() throws InstanceNotFoundException{
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);

        typeOfWorkHoursDAO.remove(typeOfWorkHours.getId());

        //this call should throw the exception
        typeOfWorkHoursDAO.findUniqueByCode(typeOfWorkHours.getCode());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findUniqueByCode(TypeOfWorkHours)/findActive/existsByCode/
     * findUniqueByName/hoursTypeByNameAsc/existsByName/checkHasHourCost/checkHasWorkReportLine
     * had no test coverage before.
     */

    @Test
    @Transactional
    public void testFindUniqueByCodeEntityOverloadReturnsMatch() throws InstanceNotFoundException {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);

        assertEquals(typeOfWorkHours.getId(), typeOfWorkHoursDAO.findUniqueByCode(typeOfWorkHours).getId());
    }

    @Test
    @Transactional
    public void testFindActiveOnlyReturnsEnabledOnes() {
        TypeOfWorkHours active = createValidTypeOfWorkHours();
        active.setEnabled(true);
        typeOfWorkHoursDAO.save(active);

        TypeOfWorkHours inactive = createValidTypeOfWorkHours();
        inactive.setEnabled(false);
        typeOfWorkHoursDAO.save(inactive);

        boolean activeFound = false;
        for (TypeOfWorkHours t : typeOfWorkHoursDAO.findActive()) {
            assertTrue(t.getEnabled());
            if (t.getId().equals(active.getId())) {
                activeFound = true;
            }
            assertFalse(t.getId().equals(inactive.getId()));
        }
        assertTrue(activeFound);
    }

    @Test
    @Transactional
    public void testExistsByCodeTrueWhenPresent() {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        assertTrue(typeOfWorkHoursDAO.existsByCode(typeOfWorkHours));
    }

    @Test
    @Transactional
    public void testExistsByCodeFalseWhenAbsent() {
        TypeOfWorkHours notSaved = TypeOfWorkHours.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertFalse(typeOfWorkHoursDAO.existsByCode(notSaved));
    }

    @Test
    @Transactional
    public void testFindUniqueByNameIsCaseInsensitiveAndTrims() throws InstanceNotFoundException {
        String mixedCaseName = "MiXeD-" + UUID.randomUUID();
        TypeOfWorkHours typeOfWorkHours =
                TypeOfWorkHours.create(UUID.randomUUID().toString(), mixedCaseName);
        typeOfWorkHours.setDefaultPrice(BigDecimal.TEN);
        typeOfWorkHoursDAO.save(typeOfWorkHours);

        assertEquals(typeOfWorkHours.getId(), typeOfWorkHoursDAO.findUniqueByName(mixedCaseName).getId());
        assertEquals(typeOfWorkHours.getId(),
                typeOfWorkHoursDAO.findUniqueByName(mixedCaseName.toLowerCase()).getId());
        assertEquals(typeOfWorkHours.getId(),
                typeOfWorkHoursDAO.findUniqueByName("  " + mixedCaseName + "  ").getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindUniqueByNameThrowsWhenNotFound() throws InstanceNotFoundException {
        typeOfWorkHoursDAO.findUniqueByName("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testHoursTypeByNameAscOrdersByName() {
        String prefix = UUID.randomUUID().toString();

        TypeOfWorkHours first = TypeOfWorkHours.create(UUID.randomUUID().toString(), prefix + "-1-aaa");
        first.setDefaultPrice(BigDecimal.TEN);
        typeOfWorkHoursDAO.save(first);

        TypeOfWorkHours second = TypeOfWorkHours.create(UUID.randomUUID().toString(), prefix + "-2-bbb");
        second.setDefaultPrice(BigDecimal.TEN);
        typeOfWorkHoursDAO.save(second);

        List<TypeOfWorkHours> ours = new java.util.ArrayList<>();
        for (TypeOfWorkHours t : typeOfWorkHoursDAO.hoursTypeByNameAsc()) {
            if (t.getName().startsWith(prefix)) {
                ours.add(t);
            }
        }

        assertEquals(2, ours.size());
        assertEquals(first.getId(), ours.get(0).getId());
        assertEquals(second.getId(), ours.get(1).getId());
    }

    @Test
    @Transactional
    public void testExistsByNameTrueWhenPresent() {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        assertTrue(typeOfWorkHoursDAO.existsByName(typeOfWorkHours));
    }

    @Test
    @Transactional
    public void testExistsByNameFalseWhenAbsent() {
        TypeOfWorkHours notSaved = TypeOfWorkHours.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertFalse(typeOfWorkHoursDAO.existsByName(notSaved));
    }

    @Test(expected = ValidationException.class)
    @Transactional
    public void testCheckIsReferencedByOtherEntitiesThrowsWhenHourCostUsesType() {
        TypeOfWorkHours type = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(type);

        CostCategory costCategory = CostCategory.create(UUID.randomUUID().toString());
        HourCost hourCost = HourCost.create(BigDecimal.ONE, new LocalDate(2020, 1, 1));
        hourCost.setType(type);
        costCategory.addHourCost(hourCost);
        costCategoryDAO.save(costCategory);

        typeOfWorkHoursDAO.checkIsReferencedByOtherEntities(type);
    }

    @Test(expected = ValidationException.class)
    @Transactional
    public void testCheckIsReferencedByOtherEntitiesThrowsWhenWorkReportLineUsesType() {
        TypeOfWorkHours type = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(type);

        WorkReportLine workReportLine = createValidWorkReportLine();
        workReportLine.setTypeOfWorkHours(type);
        workReportLineDAO.save(workReportLine);

        typeOfWorkHoursDAO.checkIsReferencedByOtherEntities(type);
    }
}
