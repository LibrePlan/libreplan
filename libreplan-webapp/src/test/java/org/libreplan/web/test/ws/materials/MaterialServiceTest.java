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

package org.libreplan.web.test.ws.materials;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.libreplan.web.test.ws.common.Util.getUniqueName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.bootstrap.UnitTypeBootstrap;
import org.libreplan.business.materials.daos.IMaterialCategoryDAO;
import org.libreplan.business.materials.daos.IUnitTypeDAO;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.UnitType;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.materials.api.IMaterialService;
import org.libreplan.ws.materials.api.MaterialCategoryDTO;
import org.libreplan.ws.materials.api.MaterialCategoryListDTO;
import org.libreplan.ws.materials.api.MaterialDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
/**
 * Tests for <code>IMaterialService</code>.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class MaterialServiceTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IMaterialService materialService;

    @Autowired
    private IMaterialCategoryDAO materialCategoryDAO;

    @Autowired
    private IUnitTypeDAO unitTypeDAO;

    @Resource
    private IDataBootstrap materialCategoryBootstrap;

    @Resource
    private IDataBootstrap unitTypeBootstrap;

    private String unitTypeCodeA = "unitTypeCodeA";

    private String unitTypeCodeB = "unitTypeCodeB";

    @Before
    public void loadRequiredaData() {
        IOnTransaction<Void> load = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                materialCategoryBootstrap.loadRequiredData();
                unitTypeBootstrap.loadRequiredData();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);

    }

    @Test
    @Rollback(false)
    public void CreateUnitType() {
        UnitType entityA = UnitType.create(unitTypeCodeA, getUniqueName());
        UnitType entityB = UnitType.create(unitTypeCodeB, getUniqueName());
        unitTypeDAO.save(entityA);
        unitTypeDAO.save(entityB);
        unitTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(entityA);
        sessionFactory.getCurrentSession().evict(entityB);
    }

    @Test
    public void testAddAndGetMaterialCategories() {
        /* Build materialCategory (0 constraint violations). */
        // Missing material name and the unit type.
        MaterialDTO m1 = new MaterialDTO(null, new BigDecimal(13),
                unitTypeCodeA, true);
        // Missing default unit price
        MaterialDTO m2 = new MaterialDTO("material 2", null, unitTypeCodeA,
                true);
        // Missing unit type
        MaterialDTO m3 = new MaterialDTO("material 3", new BigDecimal(13),
                null, true);
        // Missing unit type, same name
        MaterialDTO m4 = new MaterialDTO("material 3", new BigDecimal(13),
                unitTypeCodeA, null);

        List<MaterialDTO> materialDTOs = new ArrayList<MaterialDTO>();
        materialDTOs.add(m1);
        materialDTOs.add(m2);
        materialDTOs.add(m3);
        materialDTOs.add(m4);

        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "categoryA", null, null, materialDTOs);

        MaterialCategoryListDTO materialCategoryListDTO = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testAddMaterialRepeatedCodes() {
        /* Build material with same code (1 constraint violations). */
        MaterialDTO m1 = new MaterialDTO("CodeA", "material1", new BigDecimal(
                13), unitTypeCodeA, true);
        MaterialDTO m2 = new MaterialDTO("CodeA", "material2", new BigDecimal(
                13), unitTypeCodeA, true);

        List<MaterialDTO> materialDTOs = new ArrayList<MaterialDTO>();
        materialDTOs.add(m1);
        materialDTOs.add(m2);

        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "category1", null, null, materialDTOs);

        MaterialCategoryListDTO materialCategoryListDTO = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testAddValidMaterialCategory() {
        /* Build material (0 constraint violations). */
        MaterialDTO m1 = new MaterialDTO("CodeM1", "material1", new BigDecimal(
                13), unitTypeCodeA, true);
        MaterialDTO m2 = new MaterialDTO("CodeM2", "material2", new BigDecimal(
                13), unitTypeCodeA, true);

        List<MaterialDTO> materialDTOs1 = new ArrayList<MaterialDTO>();
        List<MaterialDTO> materialDTOs2 = new ArrayList<MaterialDTO>();
        materialDTOs1.add(m1);
        materialDTOs2.add(m2);

        /* Build material (0 constraint violations). */
        MaterialCategoryDTO mc1 = new MaterialCategoryDTO("CodeMC1",
                "subCategory1", "mainMaterialCode", null, materialDTOs1);
        MaterialCategoryDTO mc2 = new MaterialCategoryDTO("CodeMC2",
                "subCategory2", null, null, materialDTOs2);
        MaterialCategoryListDTO subCategoryListDTO = createMaterialCategoryListDTO(
                mc1, mc2);

        /* Build main material category */
        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "mainMaterialCode", "mainCategory1", null, subCategoryListDTO,
                null);

        MaterialCategoryListDTO materialCategoryListDTO = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);
    }

    @Test
    public void testAddMaterialCategoryWithSameName() {
        /* Build material (0 constraint violations). */
        MaterialCategoryDTO mc1 = new MaterialCategoryDTO("subMC1",
                "subCategory", "subMC2", null, null);
        MaterialCategoryListDTO subCategoryListDTOC = createMaterialCategoryListDTO(mc1);

        MaterialCategoryDTO mc2 = new MaterialCategoryDTO("subMC2",
                "subCategory", null, subCategoryListDTOC, null);
        MaterialCategoryListDTO subCategoryListDTOB = createMaterialCategoryListDTO(mc2);

        /* Build main material category */
        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "mainMaterialCode", "mainCategory1", null, subCategoryListDTOB,
                null);

        MaterialCategoryListDTO materialCategoryListDTOA = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTOA).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testAddMaterialCategoryWithInconsistentParent() {
        /* Build material (0 constraint violations). */
        MaterialCategoryDTO mc1 = new MaterialCategoryDTO("subMCX1",
                "subCategoryC", "mainMaterialCode", null, null);
        MaterialCategoryListDTO subCategoryListDTOC = createMaterialCategoryListDTO(mc1);

        MaterialCategoryDTO mc2 = new MaterialCategoryDTO("subMCX2",
                "subCategoryB", null, subCategoryListDTOC, null);
        MaterialCategoryListDTO subCategoryListDTOB = createMaterialCategoryListDTO(mc2);

        /* Build main material category */
        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "mainMaterialCodeX", "mainCategory1", null,
                subCategoryListDTOB,
                null);

        MaterialCategoryListDTO materialCategoryListDTOA = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTOA).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testAddAndUpdateMaterialCategory() {

        String unitTypeCodeA = getUniqueName();
        String unitTypeCodeB = getUniqueName();
        UnitType entityA = UnitType.create(unitTypeCodeA, "UnitTypeA");
        UnitType entityB = UnitType.create(unitTypeCodeB, "UnitTypeB");
        unitTypeDAO.save(entityA);
        unitTypeDAO.save(entityB);
        unitTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(entityA);
        sessionFactory.getCurrentSession().evict(entityB);

        /* Build material (0 constraint violations). */
        MaterialDTO m1 = new MaterialDTO("M-1", "tornillos",
                new BigDecimal(13), UnitTypeBootstrap.getDefaultUnitType()
                        .getCode(), true);

        List<MaterialDTO> materialDTOs1 = new ArrayList<MaterialDTO>();
        materialDTOs1.add(m1);

        MaterialCategoryDTO mc1 = new MaterialCategoryDTO("MC-C", "MC-C",
                "MC-B",
                null, null);
        MaterialCategoryListDTO subCategoryListDTOC = createMaterialCategoryListDTO(mc1);

        MaterialCategoryDTO mc2 = new MaterialCategoryDTO("MC-B", "MC-B",
                "C-A",
                subCategoryListDTOC, materialDTOs1);
        MaterialCategoryListDTO subCategoryListDTOB = createMaterialCategoryListDTO(mc2);

        /* Build main material category */
        MaterialCategoryDTO materialCategoryDTO = new MaterialCategoryDTO(
                "C-A", "C-A", null, subCategoryListDTOB, null);

        MaterialCategoryListDTO materialCategoryListDTOA = createMaterialCategoryListDTO(materialCategoryDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = materialService
                .addMaterials(materialCategoryListDTOA).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        try {
            MaterialCategory mc = materialCategoryDAO.findByCode("MC-B");
            assertTrue(mc.getMaterials().size() == 1);
            assertTrue(mc.getSubcategories().size() == 1);
            assertTrue(mc.getName().equalsIgnoreCase("MC-B"));
            materialCategoryDAO.flush();
            sessionFactory.getCurrentSession().evict(mc);
        } catch (InstanceNotFoundException e) {
            fail();
        }

    }

    private MaterialCategoryListDTO createMaterialCategoryListDTO(
            MaterialCategoryDTO... materialCategoryDTOs) {

        List<MaterialCategoryDTO> materialCategoryList = new ArrayList<MaterialCategoryDTO>();

        for (MaterialCategoryDTO c : materialCategoryDTOs) {
            materialCategoryList.add(c);
        }

        return new MaterialCategoryListDTO(materialCategoryList);

    }

}
