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
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.daos.IMaterialCategoryDAO;
import org.libreplan.business.materials.daos.IMaterialDAO;
import org.libreplan.business.materials.daos.IUnitTypeDAO;
import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialCategory;
import org.libreplan.business.materials.entities.UnitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link UnitTypeDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class UnitTypeDAOTest {

    @Autowired
    private IUnitTypeDAO unitTypeDAO;

    @Autowired
    private IMaterialDAO materialDAO;

    @Autowired
    private IMaterialCategoryDAO materialCategoryDAO;

    private UnitType createValid(String measure) {
        UnitType unitType = UnitType.create(measure);
        unitTypeDAO.save(unitType);
        return unitType;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        UnitType unitType = createValid("measure-" + UUID.randomUUID());
        boolean found = false;
        for (UnitType u : unitTypeDAO.getAll()) {
            if (u.getId().equals(unitType.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByNameIsCaseSensitive() {
        String measure = "MiXeD-" + UUID.randomUUID();
        UnitType unitType = createValid(measure);

        try {
            assertEquals(unitType.getId(), unitTypeDAO.findByName(measure).getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByNameThrowsWhenNotFound() throws InstanceNotFoundException {
        unitTypeDAO.findByName("does-not-exist-" + UUID.randomUUID());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByNameThrowsWhenBlank() throws InstanceNotFoundException {
        unitTypeDAO.findByName("   ");
    }

    @Test
    @Transactional
    public void testFindByNameCaseInsensitiveMatchesAnyCase() throws InstanceNotFoundException {
        String mixedCaseMeasure = "MiXeD-" + UUID.randomUUID();
        UnitType unitType = createValid(mixedCaseMeasure);

        assertEquals(unitType.getId(), unitTypeDAO.findByNameCaseInsensitive(mixedCaseMeasure).getId());
        assertEquals(unitType.getId(), unitTypeDAO.findByNameCaseInsensitive(mixedCaseMeasure.toLowerCase()).getId());
        assertEquals(unitType.getId(), unitTypeDAO.findByNameCaseInsensitive(mixedCaseMeasure.toUpperCase()).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByNameCaseInsensitiveThrowsWhenNotFound() throws InstanceNotFoundException {
        unitTypeDAO.findByNameCaseInsensitive("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testIsUnitTypeUsedInAnyMaterialFalseWhenUnused() {
        UnitType unitType = createValid("measure-" + UUID.randomUUID());
        assertFalse(unitTypeDAO.isUnitTypeUsedInAnyMaterial(unitType));
    }

    @Test
    @Transactional
    public void testIsUnitTypeUsedInAnyMaterialTrueWhenUsed() {
        UnitType unitType = createValid("measure-" + UUID.randomUUID());

        MaterialCategory category = MaterialCategory.create(UUID.randomUUID().toString());
        materialCategoryDAO.save(category);

        Material material = Material.create(UUID.randomUUID().toString());
        material.setDescription("material");
        material.setCategory(category);
        material.setUnitType(unitType);
        materialDAO.save(material);

        assertTrue(unitTypeDAO.isUnitTypeUsedInAnyMaterial(unitType));
    }

}
