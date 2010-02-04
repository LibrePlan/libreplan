/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.test.ws.resources.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.ws.common.Util.assertNoConstraintViolations;
import static org.navalplanner.web.test.ws.common.Util.assertOneConstraintViolation;
import static org.navalplanner.web.test.ws.common.Util.assertOneConstraintViolationPerInstance;
import static org.navalplanner.web.test.ws.common.Util.getUniqueName;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.IConfigurationBootstrap;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.resources.api.CriterionSatisfactionDTO;
import org.navalplanner.ws.resources.api.IResourceService;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
import org.navalplanner.ws.resources.api.ResourcesCostCategoryAssignmentDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>IResourceService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE })
@Transactional
public class ResourceServiceTest {

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IMachineDAO machineDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Autowired
    private IConfigurationBootstrap configurationBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Before
    public void loadConfiguration() {
        configurationBootstrap.loadRequiredData();
    }

    @Test
    public void testAddResourcesWithBasicContraintViolations()
        throws InstanceNotFoundException {

        /* Create resource DTOs. */
        String m1Code = ' ' + getUniqueName() + ' '; // Blank spaces
                                                     // intentionally
                                                     // added (OK).
        MachineDTO m1 = new MachineDTO(m1Code, "name", "desc");
        MachineDTO m2 = new MachineDTO("", null, ""); // Missing code and name
                                                      // (description is
                                                      // optional).
        String w1Nif = ' ' + getUniqueName() + ' '; // Blank spaces
                                                    // intentionally
                                                     // added (OK).
        WorkerDTO w1 = new WorkerDTO("w1-first-name", "w1-surname", w1Nif);
        WorkerDTO w2 = new WorkerDTO("", null, ""); // Missing first name,
                                                    // surname, and nif.

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(createResourceListDTO(m1, m2, w1, w2)).
                instanceConstraintViolationsList;

        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 2);
        assertTrue(
            instanceConstraintViolationsList.get(0).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 2); // m2 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(1).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 3); // w2 constraint violations.
        machineDAO.findUniqueByCode(m1Code.trim());
        assertTrue(
            workerDAO.findByFirstNameSecondNameAndNif(
                w1.firstName, w1.surname, w1.nif.trim()).size() == 1);

    }

    @Test
    @NotTransactional
    public void testAddMachineWithExistingCode()
        throws InstanceNotFoundException {

        /* Create a machine. */
        Machine m1 = Machine.createUnvalidated(getUniqueName(), "name", "desc");
        saveResource(m1);

        /* Create a machine DTO with the same code. */
        MachineDTO m2 = new MachineDTO(m1.getCode(), "name", "desc");

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(m2)));
        machineDAO.findUniqueByCodeInAnotherTransaction(m1.getCode());

    }

    @Test
    @NotTransactional
    public void testAddWorkerWithExistingFirstNameSurnameAndNif() {

        /* Create a worker. */
        Worker w1 = Worker.createUnvalidated(getUniqueName(), getUniqueName(),
            "surname", "nif");
        saveResource(w1);

        /*
         * Create a worker DTO with the same first name, surname, and nif as
         * the previous one.
         */
        WorkerDTO w2 = new WorkerDTO(w1.getFirstName(), w1.getSurname(),
            w1.getNif());

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(w2)));
        assertTrue(
            workerDAO.findByFirstNameSecondNameAndNifAnotherTransaction(
                w2.firstName, w2.surname, w2.nif).size() == 1);

    }


    @Test
    public void testAddResourcesWithDuplicateResourcesBeingImported()
        throws InstanceNotFoundException {

        /* Create resource DTOs. */
        MachineDTO m1 = new MachineDTO(getUniqueName(), "m1-name", "m1-desc");
        MachineDTO m2 = new MachineDTO(' ' + m1.code.toUpperCase() + ' ',
            "m2-name", "m2-desc");
        WorkerDTO w1 = new WorkerDTO(getUniqueName(), "w1-surname", "w1-nif");
        WorkerDTO w2 = new WorkerDTO(w1.firstName,
            ' ' + w1.surname.toUpperCase() + ' ', w1.nif);

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(createResourceListDTO(m1, m2, w1, w2)).
                instanceConstraintViolationsList;

        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 2);
        assertTrue(
            instanceConstraintViolationsList.get(0).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 1);
        assertTrue(
            instanceConstraintViolationsList.get(1).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 1);
        machineDAO.findUniqueByCode(m1.code);
        assertTrue(
            workerDAO.findByFirstNameSecondNameAndNif(
                w1.firstName, w1.surname, w1.nif.trim()).size() == 1);

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCriterionSatisfactions()
        throws InstanceNotFoundException {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                ' ' + ct.getName().toUpperCase() +  // Upper case and blank
                ' ', " C1 ",                        // spaces intentionally
                                                    // added (OK).
                getDate(2001, 1, 1), getDate(2001, 2, 1)));
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), "c2",
                getDate(2001, 1, 1), null));

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(createResourceListDTO(machineDTO)).
                instanceConstraintViolationsList;

        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.isEmpty());

        Machine machine = findUniqueMachineByCodeInitialized(machineDTO.code);
        assertTrue(machine.getCriterionSatisfactions().size() == 2);

        for (CriterionSatisfaction cs : machine.getCriterionSatisfactions()) {
            if (!(cs.getCriterion().getName().equals("c1") ||
                cs.getCriterion().getName().equals("c2"))) {
                fail("Criterion not expected");
            }
        }

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCriterionSatisfactionWithoutStartDate() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName() , "c1",
                null, getDate(2001, 1, 1))); // Missing start date.

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCriterionSatisfactionWithNegativeInterval() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName() , "c1",
                getDate(2000, 2, 1), getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithOverlappingCriterionSatisfactionsAllowed() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /*
         * Create a machine DTO. OK, because
         * ct.isAllowSimultaneousCriterionsPerResource() is true.
         */
        MachineDTO machineDTO = createMachineDTOWithTwoCriterionSatisfactions(
            "machine", ct.getName(),
            "c1", getDate(2000, 1, 1), getDate(2000, 2, 1),
            "c2", getDate(2000, 1, 15), getDate(2000, 2, 1));

        /* Test. */
        assertNoConstraintViolations(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertTrue(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithOverlappingCriterionSatisfactions() {

        /* Create criterion types. */
        CriterionType ct1 = createCriterionType();
        CriterionType ct2 = createCriterionType(ResourceEnum.RESOURCE, false);

        /*
         * Create resource DTOs. Each resource contains one criterion
         * satisfaction overlapping.
         *
         */
        MachineDTO m1 = createMachineDTOWithTwoCriterionSatisfactions(
            "m1", ct1.getName(), // Interval overlapping in "c1".
            "c1", getDate(2000, 1, 1), getDate(2000, 2, 1),
            "c1", getDate(2000, 1, 15), getDate(2000, 2, 1));

        MachineDTO m2 = createMachineDTOWithTwoCriterionSatisfactions(
            "m2", ct2.getName(), // Overlapping because "ct2" does not allow
                                 // simultaneous criterion satisfactions in
                                 // intervals that overlap.
            "c1", getDate(2000, 1, 1), getDate(2000, 2, 1),
            "c2", getDate(2000, 1, 15), getDate(2000, 2, 1));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(
            m1, m2);

        assertOneConstraintViolationPerInstance(
            resourceService.addResources(resourceDTOs),
            resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse(
                "Machine " + m.name + " not expected",
                machineDAO.existsMachineWithCodeInAnotherTransaction(
                    ((MachineDTO) r).code));
        }

    }

    @Test
    @NotTransactional
    public void testAddResourcesWithCriterionSatisfactionsWithIncorrectType() {

        /* Create two criterion types. */
        CriterionType machineCt = createCriterionType(ResourceEnum.MACHINE);
        CriterionType workerCt = createCriterionType(ResourceEnum.WORKER);

        /* Create resource DTOs. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(workerCt.getName() , "c1",
                getDate(2001, 1, 1), null)); // Incorrect type.
        WorkerDTO workerDTO = new WorkerDTO(getUniqueName(), "surname", "nif");
        workerDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(machineCt.getName() , "c1",
                getDate(2001, 1, 1), null)); // Incorrect type.

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
                machineDTO.code));
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(workerDTO)));
        assertTrue(workerDAO.findByFirstNameSecondNameAndNifAnotherTransaction(
            workerDTO.firstName, workerDTO.surname, workerDTO.nif).size() == 0);

    }

    @Test
    @NotTransactional
    public void testAddResourcesWithCriterionSatisfactionsWithIncorrectNames() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create machines DTOs. */
        MachineDTO m1 = new MachineDTO(getUniqueName(), "m1", "desc");
        m1.criterionSatisfactions.add(
            new CriterionSatisfactionDTO("", "X", // Missing criterion type.
                getDate(2001, 1, 1), null));
        MachineDTO m2 = new MachineDTO(getUniqueName(), "m2", "desc");
        m2.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), // Missing criterion.
                null, getDate(2001, 1, 1), null));
        MachineDTO m3 = new MachineDTO(getUniqueName(), "m3", "desc");
        m3.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                ct.getName() + 'X', // Non-existent criterion type.
                "c1", getDate(2001, 1, 1), null));
        MachineDTO m4 = new MachineDTO(getUniqueName(), "m4", "desc");
        m4.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                 ct.getName(),
                 "c1" + 'X', // Criterion name is not of ct's type.
                 getDate(2001, 1, 1), null));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(m1, m2, m3, m4);

        assertOneConstraintViolationPerInstance(
            resourceService.addResources(resourceDTOs),
            resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse(
                "Machine " + m.name + " not expected",
                machineDAO.existsMachineWithCodeInAnotherTransaction(
                    ((MachineDTO) r).code));
        }

    }

    @Test
    @NotTransactional
    public void testAddResourceWithDefaultCalendar()
        throws InstanceNotFoundException {

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");

        /* Test. */
        assertNoConstraintViolations(resourceService.
             addResources(createResourceListDTO(machineDTO)));
        Machine machine = findUniqueMachineByCodeInitialized(machineDTO.code);
        assertEquals(getDefaultCalendar().getId(),
            machine.getCalendar().getParent().getId());

    }

    @Test
    @NotTransactional
    public void testAddResourceWithSpecificCalendar()
        throws InstanceNotFoundException {

        /* Create a base calendar. */
        BaseCalendar baseCalendar = createBaseCalendar();

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.calendarName =
            ' ' + baseCalendar.getName().toUpperCase() + ' ';

        /* Test. */
        assertNoConstraintViolations(resourceService.
             addResources(createResourceListDTO(machineDTO)));
        Machine machine = findUniqueMachineByCodeInitialized(machineDTO.code);
        assertEquals(baseCalendar.getId(),
            machine.getCalendar().getParent().getId());

    }

    @Test
    @NotTransactional
    public void testAddResourceWithNonExistentCalendar()
        throws InstanceNotFoundException {

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.calendarName = getUniqueName();

        /* Test. */
        assertOneConstraintViolation(resourceService.
            addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCostAssignments() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create resource DTOs. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                ' ' + costCategory.getName().toUpperCase() + ' ',
                getDate(2001, 1, 1), null));
        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(),
                getDate(2000, 1, 1), getDate(2000, 4, 1)));

        /* Test. */
        assertNoConstraintViolations(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertTrue(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourcesWithCostAssignmentWithIncorrectCategoryNames() {

        /* Create a resource DTOs. */
        MachineDTO m1 = new MachineDTO(getUniqueName(), "m1", "desc");
        m1.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                null,  // Cost category not specified.
                getDate(2000, 1, 1), null));

        MachineDTO m2 = new MachineDTO(getUniqueName(), "m2", "desc");
        m2.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                getUniqueName(),  // Non-existent cost category.
                getDate(2000, 1, 1), null));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(m1, m2);

        assertOneConstraintViolationPerInstance(
            resourceService.addResources(resourceDTOs),
            resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse(
                "Machine " + m.name + " not expected",
                machineDAO.existsMachineWithCodeInAnotherTransaction(m.code));
        };

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCostAssignmentWithoutStartDate() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(), null, // Start date not specified.
                getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCostAssignmentWithNegativeInterval() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(),
                getDate(2000, 2, 1), getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourcesWithOverlappingInCostAssignments() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /*
         * Create a resource DTOs. Each resource contains one cost assignment
         * overlapping.
         */
        MachineDTO m1 = createMachineDTOWithTwoCostsAssignments(
            "m1", costCategory.getName(),
            getDate(2000, 1, 1), null,
            getDate(2000, 2, 1), null);

        MachineDTO m2 = createMachineDTOWithTwoCostsAssignments(
            "m2", costCategory.getName(),
            getDate(2000, 2, 1), null,
            getDate(2000, 1, 1), getDate(2000, 3, 1));

        MachineDTO m3 = createMachineDTOWithTwoCostsAssignments(
            "m3", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 4, 1),
            getDate(2000, 3, 1), null);

        MachineDTO m4 = createMachineDTOWithTwoCostsAssignments(
            "m4", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 1, 1), getDate(2000, 3, 1));

        MachineDTO m5 = createMachineDTOWithTwoCostsAssignments(
            "m5", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 3, 1), getDate(2000, 4, 1));

        MachineDTO m6 = createMachineDTOWithTwoCostsAssignments(
            "m6", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 4, 1), getDate(2000, 6, 1));

        MachineDTO m7 = createMachineDTOWithTwoCostsAssignments(
            "m7", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 1, 1), getDate(2000, 2, 1));

        MachineDTO m8 = createMachineDTOWithTwoCostsAssignments(
            "m8", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 5, 1), getDate(2000, 6, 1));

        MachineDTO m9 = createMachineDTOWithTwoCostsAssignments(
            "m9", costCategory.getName(),
            getDate(2000, 2, 1), getDate(2000, 5, 1),
            getDate(2000, 2, 1), getDate(2000, 5, 1));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(
            m1, m2, m3, m4, m5, m6, m7, m8, m9);

        assertOneConstraintViolationPerInstance(
            resourceService.addResources(resourceDTOs),
            resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse(
                "Machine " + m.name + " not expected",
                machineDAO.existsMachineWithCodeInAnotherTransaction(
                    ((MachineDTO) r).code));
        }

    }

    private CriterionType createCriterionType() {
        return createCriterionType(ResourceEnum.RESOURCE, true);
    }

    private CriterionType createCriterionType(final ResourceEnum resourceType) {
        return createCriterionType(resourceType, true);
    }

    private CriterionType createCriterionType(final ResourceEnum resourceType,
        final boolean allowSimultaneousCriterionsPerResource) {

        IOnTransaction<CriterionType> createCriterionType =
            new IOnTransaction<CriterionType>() {

            @Override
            public CriterionType execute() {

                CriterionType ct = CriterionType.create(getUniqueName(),
                    "desc");
                ct.setAllowSimultaneousCriterionsPerResource(
                    allowSimultaneousCriterionsPerResource);
                ct.setResource(resourceType);
                Criterion c1 = Criterion.create("c1", ct);
                Criterion c2 = Criterion.create("c2", ct);
                ct.getCriterions().add(c1);
                ct.getCriterions().add(c2);
                criterionTypeDAO.save(ct);

                return ct;

            }
        };

        return transactionService.runOnTransaction(createCriterionType);

    }

    private Machine findUniqueMachineByCodeInitialized(final String code)
        throws InstanceNotFoundException {

        IOnTransaction<Machine> find = new IOnTransaction<Machine>() {

            @Override
            public Machine execute() {
                try {
                    return (Machine) initializeResource(
                        machineDAO.findUniqueByCode(code));
                } catch (InstanceNotFoundException e) {
                    return null;
                }
            }
        };

        Machine machine = transactionService.runOnTransaction(find);

        if (machine == null) {
            throw new InstanceNotFoundException(code, Machine.class.getName());
        } else {
            return machine;
        }

    }

    private Resource initializeResource(Resource resource) {

        for (CriterionSatisfaction cs : resource.getCriterionSatisfactions()) {
            cs.getCriterion().getType().getName();
        }

        resource.getCalendar().getParent();

        return resource;

    }

    private void saveResource(final Resource resource) {

        IOnTransaction<Void> save = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                resourceDAO.save(resource);
                return null;
            }
        };

        transactionService.runOnTransaction(save);

    }

    private BaseCalendar getDefaultCalendar() {

        IOnTransaction<BaseCalendar> find = new IOnTransaction<BaseCalendar>() {

            @Override
            public BaseCalendar execute() {
                return configurationDAO.getConfiguration().getDefaultCalendar();
            }
        };

        return transactionService.runOnTransaction(find);

    }

    private BaseCalendar createBaseCalendar() {

        IOnTransaction<BaseCalendar> create =
            new IOnTransaction<BaseCalendar>() {

            @Override
            public BaseCalendar execute() {
                BaseCalendar baseCalendar = BaseCalendar.create();
                baseCalendar.setName(getUniqueName());
                baseCalendarDAO.save(baseCalendar);
                return baseCalendar;
            }
        };

        return transactionService.runOnTransaction(create);

    }

    private ResourceListDTO createResourceListDTO(ResourceDTO... resources) {

        List<ResourceDTO> resourceList = new ArrayList<ResourceDTO>();

        for (ResourceDTO r : resources) {
            resourceList.add(r);
        }


        return new ResourceListDTO(resourceList);

    }

    private CostCategory createCostCategory() {

        IOnTransaction<CostCategory> create =
            new IOnTransaction<CostCategory>() {

            @Override
            public CostCategory execute() {
                CostCategory costCategory =
                    CostCategory.create(getUniqueName());
                costCategoryDAO.save(costCategory);
                return costCategory;
            }
        };

        return transactionService.runOnTransaction(create);

    }


    private MachineDTO createMachineDTOWithTwoCriterionSatisfactions(
        String machineName, String criterionTypeName,
        String criterionName1, XMLGregorianCalendar startDate1,
        XMLGregorianCalendar endDate1,
        String criterionName2, XMLGregorianCalendar startDate2,
        XMLGregorianCalendar endDate2) {

        MachineDTO machineDTO = new MachineDTO(getUniqueName(), machineName,
            "desc");

        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(criterionTypeName, criterionName1,
                startDate1, endDate1));
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(criterionTypeName, criterionName2,
                startDate2, endDate2));

        return machineDTO;

    }

    private MachineDTO createMachineDTOWithTwoCostsAssignments(
        String machineName, String costCategoryName,
        XMLGregorianCalendar startDate1, XMLGregorianCalendar endDate1,
        XMLGregorianCalendar startDate2, XMLGregorianCalendar endDate2) {

        MachineDTO machineDTO = new MachineDTO(getUniqueName(), machineName,
            "desc");

        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                costCategoryName, startDate1, endDate1));
        machineDTO.resourcesCostCategoryAssignments.add(
            new ResourcesCostCategoryAssignmentDTO(
                costCategoryName, startDate2, endDate2));

        return machineDTO;

    }

    private XMLGregorianCalendar getDate(int year, int month, int day) {

        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                year, month, day, DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

}
