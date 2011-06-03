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

package org.navalplanner.business.test.materials.daos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.materials.daos.IMaterialAssignmentDAO;
import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.daos.IUnitTypeDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@ResourcesCostCategoryDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
@Transactional
public class MaterialAssignmentDAOTest {

    @Autowired
    IUnitTypeDAO unitTypeDAO;

    @Autowired
    IMaterialDAO materialDAO;

    @Autowired
    IMaterialCategoryDAO materialCategoryDAO;

    @Autowired
    IMaterialAssignmentDAO materialAssignmentDAO;

    private Material createValidMaterial() {
        MaterialCategory materialCategory = MaterialCategory.create(UUID.randomUUID().toString());
        materialCategoryDAO.save(materialCategory);
        UnitType unitType = UnitType.create("m");
        unitTypeDAO.save(unitType);
        Material material = Material.create(UUID.randomUUID().toString());
        material.setCategory(materialCategory);
        material.setUnitType(unitType);
        materialDAO.save(material);
        return material;
    }

    private MaterialAssignment createValidMaterialAssignment() {
        MaterialAssignment assignment =
            MaterialAssignment.create(createValidMaterial());
        return assignment;
    }

    @Test
    public void testGetByMaterial() {
        MaterialAssignment assignment1 = createValidMaterialAssignment();
        MaterialAssignment assignment2 = createValidMaterialAssignment();
        materialAssignmentDAO.save(assignment1);
        materialAssignmentDAO.save(assignment2);

        assertTrue(materialAssignmentDAO.getByMaterial(assignment1.getMaterial()).contains(assignment1));
        assertFalse(materialAssignmentDAO.getByMaterial(assignment1.getMaterial()).contains(assignment2));

        assignment2.setMaterial(assignment1.getMaterial());
        assertTrue(materialAssignmentDAO.getByMaterial(assignment1.getMaterial()).contains(assignment2));
    }

}
