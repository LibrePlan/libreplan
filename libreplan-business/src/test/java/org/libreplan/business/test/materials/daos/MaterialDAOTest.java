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

package org.libreplan.business.test.materials.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
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


/**
 * Test for {@link org.libreplan.business.materials.daos.MaterialDAO}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class MaterialDAOTest {

    @Autowired
    private IMaterialDAO materialDAO;

    @Autowired
    private IMaterialCategoryDAO materialCategoryDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(materialDAO);
    }

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap materialCategoryBootstrap;

    @Resource
    private IDataBootstrap unitTypeBootstrap;

    @Before
    public void loadRequiredData() {
        configurationBootstrap.loadRequiredData();
        materialCategoryBootstrap.loadRequiredData();
        unitTypeBootstrap.loadRequiredData();
    }

    private MaterialCategory createValidMaterialCategory() {
        return MaterialCategory.create(UUID.randomUUID().toString());
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
    @Transactional
    public void testSaveMaterial() {
        Material material = createValidMaterial();
        materialDAO.save(material);
        assertTrue(material.getId() != null);
    }

    @Test(expected = ValidationException.class)
    @Transactional
    public void testSaveMaterialWithoutDescription() {
        Material material = createValidMaterial();
        material.setDescription(null);
        materialDAO.save(material);
    }

    @Test
    @Transactional
    public void testRemoveMaterial() throws InstanceNotFoundException {
        Material material = createValidMaterial();
        materialDAO.save(material);
        materialDAO.remove(material.getId());
        assertFalse(materialDAO.exists(material.getId()));
    }

    @Test
    @Transactional
    public void testListMaterials() {
        int previous = materialDAO.list(Material.class).size();
        Material material = createValidMaterial();
        materialDAO.save(material);
        List<Material> list = materialDAO.list(Material.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    @Transactional
    public void testListMaterialsFromCategory() {
        MaterialCategory category = createValidMaterialCategory();
        int previous = category.getMaterials().size();
        Material material = createValidMaterial();
        category.addMaterial(material);

        materialCategoryDAO.save(category);
        try {
            category = materialCategoryDAO.find(category.getId());
            assertEquals(previous + 1, category.getMaterials().size());
        } catch (InstanceNotFoundException ignored) {
        }
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findMaterialsInCategories/findMaterialsInCategoryAndSubCategories/
     * getAllSubcategories/existsMaterialWithCodeInAnotherTransaction had no test coverage before.
     */

    @Test
    @Transactional
    public void testFindMaterialsInCategoriesMatchesCodeOrDescriptionCaseInsensitively() {
        String token = "TOK-" + UUID.randomUUID();

        Material byCode = createValidMaterial();
        byCode.setCode(token.toLowerCase() + "-code");
        materialDAO.save(byCode);

        Material byDescription = createValidMaterial();
        byDescription.setDescription("has " + token.toLowerCase() + " in description");
        materialDAO.save(byDescription);

        Material notMatching = createValidMaterial();
        materialDAO.save(notMatching);

        List<Material> result = materialDAO.findMaterialsInCategories(token, null);

        boolean codeFound = false;
        boolean descriptionFound = false;
        for (Material m : result) {
            if (m.getId().equals(byCode.getId())) {
                codeFound = true;
            }
            if (m.getId().equals(byDescription.getId())) {
                descriptionFound = true;
            }
            assertFalse(m.getId().equals(notMatching.getId()));
        }
        assertTrue(codeFound);
        assertTrue(descriptionFound);
    }

    @Test
    @Transactional
    public void testFindMaterialsInCategoriesExcludesDisabled() {
        String token = "TOK-" + UUID.randomUUID();

        Material disabled = createValidMaterial();
        disabled.setCode(token + "-disabled");
        disabled.setDisabled(true);
        materialDAO.save(disabled);

        List<Material> result = materialDAO.findMaterialsInCategories(token, null);
        for (Material m : result) {
            assertFalse(m.getId().equals(disabled.getId()));
        }
    }

    @Test
    @Transactional
    public void testFindMaterialsInCategoriesFiltersByCategory() {
        String token = "TOK-" + UUID.randomUUID();

        MaterialCategory category1 = createValidMaterialCategory();
        materialCategoryDAO.save(category1);
        MaterialCategory category2 = createValidMaterialCategory();
        materialCategoryDAO.save(category2);

        Material inCategory1 = Material.create(token + "-1");
        inCategory1.setDescription("material");
        inCategory1.setCategory(category1);
        materialDAO.save(inCategory1);

        Material inCategory2 = Material.create(token + "-2");
        inCategory2.setDescription("material");
        inCategory2.setCategory(category2);
        materialDAO.save(inCategory2);

        Set<MaterialCategory> categories = new HashSet<>();
        categories.add(category1);
        List<Material> result = materialDAO.findMaterialsInCategories(token, categories);

        boolean found = false;
        for (Material m : result) {
            if (m.getId().equals(inCategory1.getId())) {
                found = true;
            }
            assertFalse(m.getId().equals(inCategory2.getId()));
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindMaterialsInCategoryAndSubCategoriesIncludesSubcategoryMaterials() {
        String token = "TOK-" + UUID.randomUUID();

        MaterialCategory parent = createValidMaterialCategory();
        MaterialCategory child = createValidMaterialCategory();
        parent.addSubcategory(child);
        materialCategoryDAO.save(parent);

        Material inChild = Material.create(token + "-child");
        inChild.setDescription("material");
        inChild.setCategory(child);
        materialDAO.save(inChild);

        List<Material> result = materialDAO.findMaterialsInCategoryAndSubCategories(token, parent);
        boolean found = false;
        for (Material m : result) {
            if (m.getId().equals(inChild.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetAllSubcategoriesReturnsNestedDescendants() {
        MaterialCategory root = createValidMaterialCategory();
        MaterialCategory child = createValidMaterialCategory();
        MaterialCategory grandchild = createValidMaterialCategory();
        child.addSubcategory(grandchild);
        root.addSubcategory(child);
        materialCategoryDAO.save(root);

        Set<MaterialCategory> subcategories = materialDAO.getAllSubcategories(root);
        assertEquals(2, subcategories.size());
    }

    @Test
    @Transactional
    public void testExistsMaterialWithCodeInAnotherTransactionFalseWhenNotFound() {
        assertFalse(materialDAO.existsMaterialWithCodeInAnotherTransaction("does-not-exist-" + UUID.randomUUID()));
    }

    @Test
    public void testExistsMaterialWithCodeInAnotherTransactionTrueWhenFound() {
        final String code = "code-" + UUID.randomUUID();

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                Material material = createValidMaterial();
                material.setCode(code);
                materialDAO.save(material);
                return null;
            }
        });

        assertTrue(materialDAO.existsMaterialWithCodeInAnotherTransaction(code));
    }

}
