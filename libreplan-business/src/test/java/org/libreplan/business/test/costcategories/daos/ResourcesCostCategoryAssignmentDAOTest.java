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

package org.libreplan.business.test.costcategories.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.daos.ICostCategoryDAO;
import org.libreplan.business.costcategories.daos.IResourcesCostCategoryAssignmentDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
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
public class ResourcesCostCategoryAssignmentDAOTest {

    @Autowired
    IResourcesCostCategoryAssignmentDAO resourcesCostCategoryAssignmentDAO;

    @Autowired
    ICostCategoryDAO costCategoryDAO;

    @Autowired
    IWorkerDAO workerDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(resourcesCostCategoryAssignmentDAO);
    }

    private ResourcesCostCategoryAssignment createValidResourcesCostCategoryAssignment() {
        CostCategory costCategory = createValidCostCategory();
        costCategoryDAO.save(costCategory);
        Worker worker = createValidWorker();
        workerDAO.save(worker);

        ResourcesCostCategoryAssignment assignment = ResourcesCostCategoryAssignment.create();
        assignment.setInitDate(new LocalDate());
        assignment.setCostCategory(costCategory);
        assignment.setResource(worker);
        return assignment;
    }

    private CostCategory createValidCostCategory() {
        CostCategory costCategory = CostCategory.create(UUID.randomUUID().toString());
        return costCategory;
    }

    private Worker createValidWorker() {
        return Worker.create(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test
    public void testSaveResourcesCostCategoryAssignment() {
        ResourcesCostCategoryAssignment assignment = createValidResourcesCostCategoryAssignment();
        resourcesCostCategoryAssignmentDAO.save(assignment);
        assertTrue(assignment.getId() != null);
    }

    @Test
    public void testRemoveResourcesCostCategoryAssignment() throws InstanceNotFoundException {
        ResourcesCostCategoryAssignment assignment = createValidResourcesCostCategoryAssignment();
        resourcesCostCategoryAssignmentDAO.save(assignment);
        resourcesCostCategoryAssignmentDAO.remove(assignment.getId());
        assertFalse(resourcesCostCategoryAssignmentDAO.exists(assignment.getId()));
    }

    @Test
    public void testListResourcesCostCategoryAssignments() {
        int previous = resourcesCostCategoryAssignmentDAO.list(ResourcesCostCategoryAssignment.class).size();
        ResourcesCostCategoryAssignment assignment = createValidResourcesCostCategoryAssignment();
        resourcesCostCategoryAssignmentDAO.save(assignment);
        List<ResourcesCostCategoryAssignment> list = resourcesCostCategoryAssignmentDAO.list(ResourcesCostCategoryAssignment.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testNavigateRelations() {
        ResourcesCostCategoryAssignment assignment = createValidResourcesCostCategoryAssignment();
        resourcesCostCategoryAssignmentDAO.save(assignment);
        Resource resource = assignment.getResource();

        assertTrue(costCategoryDAO.list(CostCategory.class).contains(assignment.getCostCategory()));
        assertTrue(resource.getResourcesCostCategoryAssignments().contains(assignment));

        assignment.setResource(null);
        assertFalse(resource.getResourcesCostCategoryAssignments().contains(assignment));
    }

    @Test(expected=ValidationException.class)
    public void testPositiveTimeInterval() {
        ResourcesCostCategoryAssignment assignment = createValidResourcesCostCategoryAssignment();
        assignment.setInitDate(new LocalDate(2000,12,31));
        assignment.setEndDate(new LocalDate(2000,12,1));

        resourcesCostCategoryAssignmentDAO.save(assignment);
    }

    @Test
    public void testGetResourcesCostCategoryAssignmentsByCostCategory() {
        ResourcesCostCategoryAssignment assignment1 = createValidResourcesCostCategoryAssignment();
        ResourcesCostCategoryAssignment assignment2 = createValidResourcesCostCategoryAssignment();
        resourcesCostCategoryAssignmentDAO.save(assignment1);
        resourcesCostCategoryAssignmentDAO.save(assignment2);

        assertTrue(resourcesCostCategoryAssignmentDAO.getResourcesCostCategoryAssignmentsByCostCategory(
                assignment1.getCostCategory()).contains(assignment1));
        assertFalse(resourcesCostCategoryAssignmentDAO.getResourcesCostCategoryAssignmentsByCostCategory(
                assignment1.getCostCategory()).contains(assignment2));

        assignment2.setCostCategory(assignment1.getCostCategory());
        resourcesCostCategoryAssignmentDAO.save(assignment2);
        assertTrue(resourcesCostCategoryAssignmentDAO.getResourcesCostCategoryAssignmentsByCostCategory(
                assignment1.getCostCategory()).contains(assignment2));
    }
}
