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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
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
@Transactional
public class TypeOfWorkHoursDAOTest {

    @Autowired
    ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Test
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
    public void testSaveTypeOfWorkHours() {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        assertTrue(typeOfWorkHours.getId() != null);
    }

    @Test
    public void testRemoveTypeOfWorkHours() throws InstanceNotFoundException {
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        typeOfWorkHoursDAO.remove(typeOfWorkHours.getId());
        assertFalse(typeOfWorkHoursDAO.exists(typeOfWorkHours.getId()));
    }

    @Test
    public void testListTypesOfWorkHours() {
        int previous = typeOfWorkHoursDAO.list(TypeOfWorkHours.class).size();
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);
        List<TypeOfWorkHours> list = typeOfWorkHoursDAO.list(TypeOfWorkHours.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
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
    public void testFindTypesOfWorkHoursByCodeException() throws InstanceNotFoundException{
        TypeOfWorkHours typeOfWorkHours = createValidTypeOfWorkHours();
        typeOfWorkHoursDAO.save(typeOfWorkHours);

        typeOfWorkHoursDAO.remove(typeOfWorkHours.getId());

        //this call should throw the exception
        typeOfWorkHoursDAO.findUniqueByCode(typeOfWorkHours.getCode());
    }
}
