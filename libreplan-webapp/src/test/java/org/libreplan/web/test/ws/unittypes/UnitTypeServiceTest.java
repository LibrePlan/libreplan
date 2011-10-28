/*
 * This file is part of LibrePlan
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

package org.libreplan.web.test.ws.unittypes;

import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.daos.IUnitTypeDAO;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.unittypes.api.IUnitTypeService;
import org.libreplan.ws.unittypes.api.UnitTypeDTO;
import org.libreplan.ws.unittypes.api.UnitTypeListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>IUnitTypeService</code>.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class UnitTypeServiceTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IUnitTypeService unitTypeService;

    @Autowired
    private IUnitTypeDAO unitTypeDAO;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap unitTypeBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    private void loadRequiredaData() {
        configurationBootstrap.loadRequiredData();
        unitTypeBootstrap.loadRequiredData();
    }

    @Test
    public void testAddUnitTypeRepeatedMeasure() {
        loadRequiredaData();

        /* Build material with same code (1 constraint violations). */
        UnitTypeDTO m1 = new UnitTypeDTO("CodeA", "measure1");
        UnitTypeDTO m2 = new UnitTypeDTO("CodeB", "measure1");
        UnitTypeDTO m3 = new UnitTypeDTO("measure1");

        List<UnitTypeDTO> unitTypeDTOs = new ArrayList<UnitTypeDTO>();
        unitTypeDTOs.add(m1);
        unitTypeDTOs.add(m2);
        unitTypeDTOs.add(m3);

        UnitTypeListDTO unitTypeListDTO = createUnitTypeListDTO(m1, m2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = unitTypeService
                .addUnitTypes(unitTypeListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    @NotTransactional
    public void testAddAndUpdateMaterialCategory() {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                loadRequiredaData();
                return null;
            }
        });

        /* Build unittype (0 constraint violations). */
        UnitTypeDTO m1 = new UnitTypeDTO("XXX", "measureX");
        UnitTypeDTO m2 = new UnitTypeDTO("YYY", "measureY");

        List<UnitTypeDTO> unitTypeDTOs = new ArrayList<UnitTypeDTO>();
        unitTypeDTOs.add(m1);
        unitTypeDTOs.add(m2);

        UnitTypeListDTO unitTypeListDTO = createUnitTypeListDTO(m1, m2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = unitTypeService
                .addUnitTypes(unitTypeListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    UnitType typeX = unitTypeDAO.findByCode("XXX");
                    assertTrue(typeX.getMeasure().equalsIgnoreCase("measureX"));
                    UnitType typeY = unitTypeDAO.findByCode("YYY");
                    assertTrue(typeY.getMeasure().equalsIgnoreCase("measureY"));
                    unitTypeDAO.flush();
                    sessionFactory.getCurrentSession().evict(typeX);
                    sessionFactory.getCurrentSession().evict(typeY);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        /* Update the measure unit type */
        m1 = new UnitTypeDTO("XXX", "update-measureX");
        m2 = new UnitTypeDTO("YYY", "update-measureY");

        unitTypeDTOs = new ArrayList<UnitTypeDTO>();
        unitTypeDTOs.add(m1);
        unitTypeDTOs.add(m2);

        unitTypeListDTO = createUnitTypeListDTO(m1, m2);

        instanceConstraintViolationsList = unitTypeService
                .addUnitTypes(unitTypeListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    UnitType typeX = unitTypeDAO.findByCode("XXX");
                    assertTrue(typeX.getMeasure().equalsIgnoreCase(
                            "update-measureX"));
                    UnitType typeY = unitTypeDAO.findByCode("YYY");
                    assertTrue(typeY.getMeasure().equalsIgnoreCase(
                            "update-measureY"));
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    private UnitTypeListDTO createUnitTypeListDTO(UnitTypeDTO... unitTypeDTOs) {

        List<UnitTypeDTO> unitTypeList = new ArrayList<UnitTypeDTO>();

        for (UnitTypeDTO c : unitTypeDTOs) {
            unitTypeList.add(c);
        }

        return new UnitTypeListDTO(unitTypeList);

    }

}