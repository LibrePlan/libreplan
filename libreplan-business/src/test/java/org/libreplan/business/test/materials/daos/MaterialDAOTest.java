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

package org.libreplan.business.test.materials.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.materials.daos.IMaterialCategoryDAO;
import org.libreplan.business.materials.daos.IMaterialDAO;
import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })

/**
 * Test for {@MaterialDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class MaterialDAOTest {

    @Autowired
    IMaterialDAO materialDAO;

    @Autowired
    IMaterialCategoryDAO materialCategoryDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(materialDAO);
    }

    @Resource
    private IDataBootstrap materialCategoryBootstrap;

    @Resource
    private IDataBootstrap unitTypeBootstrap;

    @Before
    public void loadRequiredaData() {
        materialCategoryBootstrap.loadRequiredData();
        unitTypeBootstrap.loadRequiredData();
    }

    private MaterialCategory createValidMaterialCategory() {
        MaterialCategory materialCategory = MaterialCategory.create(UUID.randomUUID().toString());
        return materialCategory;
    }

    private Material createValidMaterial() {
        MaterialCategory materialCategory = MaterialCategory.create(UUID.randomUUID().toString());
        materialCategoryDAO.save(materialCategory);
        Material material = Material.create(UUID.randomUUID().toString());
        material.setDescription("material");
        material.setCategory(materialCategory);
        return material;
    }

    @Test
    public void testSaveMaterial() {
        Material material = createValidMaterial();
        materialDAO.save(material);
        assertTrue(material.getId() != null);
    }

    @Test(expected = ValidationException.class)
    public void testSaveMaterialWithoutDescription() {
        Material material = createValidMaterial();
        material.setDescription(null);
        materialDAO.save(material);
    }

    @Test
    public void testRemoveMaterial() throws InstanceNotFoundException {
        Material material = createValidMaterial();
        materialDAO.save(material);
        materialDAO.remove(material.getId());
        assertFalse(materialDAO.exists(material.getId()));
    }

    @Test
    public void testListMaterials() {
        int previous = materialDAO.list(Material.class).size();
        Material material = createValidMaterial();
        materialDAO.save(material);
        List<Material> list = materialDAO.list(Material.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testListMaterialsFromCategory() {
        MaterialCategory category = createValidMaterialCategory();
        int previous = category.getMaterials().size();
        Material material = createValidMaterial();
        category.addMaterial(material);

        materialCategoryDAO.save(category);
        try {
            category = materialCategoryDAO.find(category.getId());
            assertEquals(previous + 1, category.getMaterials().size());
        } catch (InstanceNotFoundException e) {

        }
    }

}
