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

package org.navalplanner.web.test.ws.typeofworkhours;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.typeofworkhours.api.ITypeOfWorkHoursService;
import org.navalplanner.ws.typeofworkhours.api.TypeOfWorkHoursDTO;
import org.navalplanner.ws.typeofworkhours.api.TypeOfWorkHoursListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>ITypeOfWorkHoursService</code>.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class TypeOfWorkHoursServiceTest {

    @Autowired
    private ITypeOfWorkHoursService typeOfWorkHoursService;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    public void testAddAndGetTypeOfWorkHours() {

        // Missing TypeOfWorkHours name.
        TypeOfWorkHoursDTO cc1 = new TypeOfWorkHoursDTO(null, true,
                new BigDecimal(5));
        // Valid TypeOfWorkHours DTO without hour cost
        TypeOfWorkHoursDTO cc2 = new TypeOfWorkHoursDTO("codeB", "cc2", true,
                new BigDecimal(5));

        /* TypeOfWorkHours list. */
        TypeOfWorkHoursListDTO typeOfWorkHoursListDTO = createTypeOfWorkHoursListDTO(
                cc1, cc2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = typeOfWorkHoursService
                .addTypeOfWorkHours(typeOfWorkHoursListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
        assertTrue(instanceConstraintViolationsList.get(0).constraintViolations
                .toString(),
                instanceConstraintViolationsList.get(0).constraintViolations
                        .size() == 1); // cc1 constraint violations.

        /* Test. */
        assertFalse(typeOfWorkHoursDAO.existsByCode(cc1.code));
        assertTrue(typeOfWorkHoursDAO.existsByCode(cc2.code));
    }

    @Test
    @NotTransactional
    public void testUpdateTypeOfWorkHours() throws InstanceNotFoundException {

        // First one it creates valid type of work hours

        final TypeOfWorkHoursDTO cc1 = new TypeOfWorkHoursDTO(
                "newTypeOfWorkHours", true, new BigDecimal(5));
        TypeOfWorkHoursListDTO typeOfWorkHoursListDTO = createTypeOfWorkHoursListDTO(cc1);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = typeOfWorkHoursService
                .addTypeOfWorkHours(typeOfWorkHoursListDTO).instanceConstraintViolationsList;
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                typeOfWorkHoursDAO.flush();
                return null;
            }
        });

        /* Test. */
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                assertTrue(typeOfWorkHoursDAO.existsByCode(cc1.code));
                return null;
            }
        });

        final TypeOfWorkHours typeOfWorkHours = transactionService
                .runOnTransaction(new IOnTransaction<TypeOfWorkHours>() {
                    @Override
                    public TypeOfWorkHours execute() {
                        try {
                            return typeOfWorkHoursDAO.findByCode(cc1.code);
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        assertTrue(typeOfWorkHours.getName().equalsIgnoreCase(
                "newTypeOfWorkHours"));
        assertTrue(typeOfWorkHours.getEnabled());
        assertTrue(typeOfWorkHours.getDefaultPrice().compareTo(
                new BigDecimal(5)) == 0);

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                typeOfWorkHoursDAO.flush();
                sessionFactory.getCurrentSession().evict(typeOfWorkHours);
                return null;
            }
        });

        typeOfWorkHours.dontPoseAsTransientObjectAnymore();

        // Update the previous type of work hours
        TypeOfWorkHoursDTO cc2 = new TypeOfWorkHoursDTO(cc1.code, "updateCC1",
                false, new BigDecimal(100));

        typeOfWorkHoursListDTO = createTypeOfWorkHoursListDTO(cc2);

        instanceConstraintViolationsList = typeOfWorkHoursService
                .addTypeOfWorkHours(typeOfWorkHoursListDTO).instanceConstraintViolationsList;

        /* Test. */
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                assertTrue(typeOfWorkHoursDAO.existsByCode(cc1.code));
                return null;
            }
        });

        // Check if the changes was updated
        TypeOfWorkHours typeOfWorkHours2 = transactionService
                .runOnTransaction(new IOnTransaction<TypeOfWorkHours>() {
                    @Override
                    public TypeOfWorkHours execute() {
                        try {
                            return typeOfWorkHoursDAO.findByCode(cc1.code);
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertTrue(typeOfWorkHours2.getName().equalsIgnoreCase("updateCC1"));
        assertFalse(typeOfWorkHours2.getEnabled());
        assertTrue(typeOfWorkHours2.getDefaultPrice().compareTo(
                new BigDecimal(100)) == 0);
    }

    private TypeOfWorkHoursListDTO createTypeOfWorkHoursListDTO(
            TypeOfWorkHoursDTO... typeOfWorkHours) {

        List<TypeOfWorkHoursDTO> typeOfWorkHoursList = new ArrayList<TypeOfWorkHoursDTO>();

        for (TypeOfWorkHoursDTO c : typeOfWorkHours) {
            typeOfWorkHoursList.add(c);
        }

        return new TypeOfWorkHoursListDTO(typeOfWorkHoursList);

    }

}
