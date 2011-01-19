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

package org.navalplanner.web.test.ws.resources.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.ws.common.Util.assertNoConstraintViolations;
import static org.navalplanner.web.test.ws.common.Util.assertOneConstraintViolation;
import static org.navalplanner.web.test.ws.common.Util.assertOneConstraintViolationPerInstance;
import static org.navalplanner.web.test.ws.common.Util.assertOneRecoverableError;
import static org.navalplanner.web.test.ws.common.Util.getUniqueName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
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
import org.navalplanner.ws.resources.api.ResourceCalendarDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
import org.navalplanner.ws.resources.api.ResourcesCostCategoryAssignmentDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;
import org.navalplanner.ws.resources.impl.ResourceConverter;
import org.springframework.beans.factory.annotation.Autowired;
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
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
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

        IOnTransaction<Void> load =
            new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                configurationBootstrap.loadRequiredData();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);

    }

    @Test
    public void testAddResourcesWithBasicContraintViolations() {

        /* Create resource DTOs. */
        MachineDTO m1 = new MachineDTO("name", "desc");
        MachineDTO m2 = new MachineDTO(" ", null, ""); // Missing code and name
                                                      // (description is
                                                      // optional).

        WorkerDTO w1 = new WorkerDTO(getUniqueName(), "w1-surname", "w1-nif");
        WorkerDTO w2 = new WorkerDTO(null, "", null, ""); // Missing code, first
                                                          // name, surname, and
                                                          // nif.

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(
                createResourceListDTO(m1, m2, w1, w2)).
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
            constraintViolations.size() == 4); // w2 constraint violations.
        assertTrue(resourceDAO.existsByCode(m1.code));
        assertTrue(resourceDAO.existsByCode(w1.code));

    }

    @Test
    public void testAddWorkerWithExistingFirstNameSurnameAndNif() {

        /* Create a worker. */
        Worker w1 = Worker.createUnvalidated(getUniqueName(), getUniqueName(),
            "surname", "nif");
        saveResource(w1);

        /*
         * Create a worker DTO with the same first name, surname, and ID as
         * the previous one.
         */
        WorkerDTO w2 = new WorkerDTO(w1.getFirstName(), w1.getSurname(),
            w1.getNif());

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(w2)));
        assertFalse(resourceDAO.existsByCode(w2.code));

    }

    @Test
    public void testAddResourceWithCriterionSatisfactions() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType(ResourceEnum.MACHINE);

        /* Create a resource DTO. */
        MachineDTO m1 = new MachineDTO("name", "desc");
        CriterionSatisfactionDTO cs1m1 =
            new CriterionSatisfactionDTO(
                ' ' + ct.getName().toUpperCase() +  // Upper case and blank
                ' ', " C1 ",                        // spaces intentionally
                                                    // added (OK).
                getDate(2001, 1, 1), getDate(2001, 2, 1));
        m1.criterionSatisfactions.add(cs1m1);
        m1.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), "c2",
                getDate(2001, 1, 1), null));

        MachineDTO m2 = new MachineDTO("name", "desc");
        m2.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(cs1m1.code, ct.getName(), "c1",
                getDate(2001, 1, 1), null)); // Repeated criterion satisfaction
                                             // code (used by another machine).
        m2.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(null, ct.getName(), "c2",
                getDate(2001, 1, 1), null)); // Missing criterion satisfaction
        // code.(autogenerated code)

        MachineDTO m3 = new MachineDTO("name", "desc");
        CriterionSatisfactionDTO cs1m3 =
            new CriterionSatisfactionDTO(ct.getName(), "c1",
                getDate(2001, 1, 1), getDate(2001, 2, 1));
        m3.criterionSatisfactions.add(cs1m3);
        m3.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                cs1m3.code, // Repeated criterion satisfaction code in this
                            // machine.
                ct.getName(), "c2",
                getDate(2001, 1, 1), null));

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
        resourceService.addResources(createResourceListDTO(m1, m2, m3)).
            instanceConstraintViolationsList;

        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 2);
        assertTrue(
            instanceConstraintViolationsList.get(0).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(0).
constraintViolations
                        .size() == 1); // m2 constraint violations.
        assertTrue(
            instanceConstraintViolationsList.get(1).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(1).
            constraintViolations.size() == 1); // m3 constraint violations.
        assertFalse(resourceDAO.existsByCode(m2.code));
        assertFalse(resourceDAO.existsByCode(m3.code));

        Machine machine = machineDAO.findExistingEntityByCode(m1.code);
        assertTrue(machine.getCriterionSatisfactions().size() == 2);

        for (CriterionSatisfaction cs : machine.getCriterionSatisfactions()) {
            if (!(cs.getCriterion().getName().equals("c1") ||
                cs.getCriterion().getName().equals("c2"))) {
                fail("Criterion not expected");
            }
        }

        assertFalse(resourceDAO.existsByCode(m2.code));

    }

    @Test
    public void testAddResourceWithCriterionSatisfactionWithoutStartDate() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType(ResourceEnum.MACHINE);

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName() , "c1",
                null, getDate(2001, 1, 1))); // Missing start date.

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithCriterionSatisfactionWithNegativeInterval() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType(ResourceEnum.MACHINE);

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName() , "c1",
                getDate(2000, 2, 1), getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithOverlappingCriterionSatisfactionsAllowed() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType(ResourceEnum.MACHINE);

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
        assertTrue(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithOverlappingCriterionSatisfactions() {

        /* Create criterion types. */
        CriterionType ct1 = createCriterionType(ResourceEnum.MACHINE);
        CriterionType ct2 = createCriterionType(ResourceEnum.MACHINE, false);

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
                resourceDAO.existsByCode(((MachineDTO) r).code));
        }

    }

    @Test
    public void testAddResourcesWithCriterionSatisfactionsWithIncorrectCriterionType() {

        /* Create two criterion types. */
        CriterionType machineCt = createCriterionType(ResourceEnum.MACHINE);
        CriterionType workerCt = createCriterionType(ResourceEnum.WORKER);

        /* Create resource DTOs. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(workerCt.getName() , "c1",
                getDate(2001, 1, 1), null)); // Incorrect type.
        WorkerDTO workerDTO = new WorkerDTO(getUniqueName(), "surname",
                getUniqueName());
        workerDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(machineCt.getName() , "c1",
                getDate(2001, 1, 1), null)); // Incorrect type.

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(
            resourceDAO.existsByCode(machineDTO.code));
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(workerDTO)));
        assertFalse(resourceDAO.existsByCode(workerDTO.code));

    }

    @Test
    public void testAddResourcesWithCriterionSatisfactionsWithMissingNames() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create machines DTOs. */
        MachineDTO m1 = new MachineDTO("m1", "desc");
        m1.criterionSatisfactions.add(
            new CriterionSatisfactionDTO("", "X", // Missing criterion type.
                getDate(2001, 1, 1), null));
        MachineDTO m2 = new MachineDTO("m2", "desc");
        m2.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), // Missing criterion.
                null, getDate(2001, 1, 1), null));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(m1, m2);

        assertOneConstraintViolationPerInstance(
            resourceService.addResources(resourceDTOs),
            resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse(
                "Machine " + m.name + " not expected",
                resourceDAO.existsByCode(((MachineDTO) r).code));
        }

    }

    @Test
    public void testAddResourceWithCriterionSatisfactionsWithNonExistentCriterionType() {

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(getUniqueName() , "c1",
                getDate(2000, 1, 1), null));

        /* Test. */
        assertOneRecoverableError(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithCriterionSatisfactionsWithNonExistentCriterion() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), getUniqueName(),
                getDate(2000, 1, 1), null));

        /* Test. */
        assertOneRecoverableError(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithDefaultCalendar() {

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");

        /* Test. */
        assertNoConstraintViolations(resourceService.
             addResources(createResourceListDTO(machineDTO)));
        Machine machine = machineDAO.findExistingEntityByCode(machineDTO.code);
        assertEquals(getDefaultCalendar().getId(),
            machine.getCalendar().getParent().getId());

    }

    @Test
    public void testAddResourceWithSpecificCalendar() {

        /* Create a resource calendar DTO. */
        BaseCalendar baseCalendar = createBaseCalendar();
        ResourceCalendar resourceCalendar = baseCalendar
                .newDerivedResourceCalendar();
        ResourceCalendarDTO resourceCalendarDTO = ResourceConverter
                .toDTO(resourceCalendar);

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.calendar = resourceCalendarDTO;

        /* Test. */
        assertNoConstraintViolations(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        Machine machine = machineDAO.findExistingEntityByCode(machineDTO.code);
        assertEquals(baseCalendar.getId(), machine.getCalendar().getParent()
                .getId());

    }

    @Test
    public void testAddResourceWithNonExistentCalendar() {

        /* Create invalid calendar */
        ResourceCalendarDTO calendarDTO = new ResourceCalendarDTO("",
                "ParentNoExist", null,
                null, null, null);
        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.calendar = calendarDTO;

        /* Test. */
        assertOneRecoverableError(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithCostAssignments() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create resource DTOs. */
        MachineDTO m1 = new MachineDTO("name", "desc");
        ResourcesCostCategoryAssignmentDTO a1m1 = new ResourcesCostCategoryAssignmentDTO(
                ' ' + costCategory.getName().toUpperCase() + ' ', getDate(2001,
                        1, 1), null);
        m1.resourcesCostCategoryAssignments.add(a1m1);
        m1.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(costCategory
                        .getName(), getDate(2000, 1, 1), getDate(2000, 4, 1)));

        MachineDTO m2 = new MachineDTO("name", "desc");
        m2.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(a1m1.code,
                        costCategory.getName().toUpperCase(), getDate(2001, 1,
                                1), null)); // Repeated assignment code
        // (used by another machine).
        m2.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(null, costCategory
                        .getName().toUpperCase(), getDate(2000, 1, 1), getDate(
                        2000, 4, 1))); // Missing
        // assignment code (autogenerated code).

        MachineDTO m3 = new MachineDTO("name", "desc");
        ResourcesCostCategoryAssignmentDTO a1m3 = new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(), getDate(2001, 1, 1), null);
        m3.resourcesCostCategoryAssignments.add(a1m3);
        m3.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(a1m3.code, // Repeated
                                                                       // assignment
                                                                       // code
                                                                       // in
                                                                       // this
                                                                       // machine.
                        costCategory.getName(), getDate(2000, 1, 1), getDate(
                                2000, 4, 1)));

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = resourceService
                .addResources(createResourceListDTO(m1, m2, m3)).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 2);
        assertTrue(instanceConstraintViolationsList.get(0).constraintViolations
                .toString(),
                instanceConstraintViolationsList.get(0).constraintViolations
                        .size() == 1); // m2 constraint violations.
        assertTrue(instanceConstraintViolationsList.get(1).constraintViolations
                .toString(),
                instanceConstraintViolationsList.get(1).constraintViolations
                        .size() == 1); // m3 constraint violations.

        assertTrue(resourceDAO.existsByCode(m1.code));
        assertFalse(resourceDAO.existsByCode(m2.code));

    }

    @Test
    public void testAddResourcesWithCostAssignmentWithMissingCostCategoryName() {

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO("", null, getDate(
                        2000, 1, 1), null));

        /* Test. */
        assertOneConstraintViolation(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourcesWithCostAssignmentWithNonExistentCostCategory() {

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(getUniqueName(),
                        getDate(2000, 1, 1), null));

        /* Test. */
        assertOneRecoverableError(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithCostAssignmentWithoutStartDate() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(costCategory
                        .getName(), null, // Start date not specified.
                        getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourceWithCostAssignmentWithNegativeInterval() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /* Create a resource DTO. */
        MachineDTO machineDTO = new MachineDTO("name", "desc");
        machineDTO.resourcesCostCategoryAssignments
                .add(new ResourcesCostCategoryAssignmentDTO(costCategory
                        .getName(), getDate(2000, 2, 1), getDate(2000, 1, 1)));

        /* Test. */
        assertOneConstraintViolation(resourceService
                .addResources(createResourceListDTO(machineDTO)));
        assertFalse(resourceDAO.existsByCode(machineDTO.code));

    }

    @Test
    public void testAddResourcesWithOverlappingInCostAssignments() {

        /* Create a CostCategory. */
        CostCategory costCategory = createCostCategory();

        /*
         * Create a resource DTOs. Each resource contains one cost assignment
         * overlapping.
         */
        MachineDTO m1 = createMachineDTOWithTwoCostsAssignments("m1",
                costCategory.getName(), getDate(2000, 1, 1), null, getDate(
                        2000, 2, 1), null);

        MachineDTO m2 = createMachineDTOWithTwoCostsAssignments("m2",
                costCategory.getName(), getDate(2000, 2, 1), null, getDate(
                        2000, 1, 1), getDate(2000, 3, 1));

        MachineDTO m3 = createMachineDTOWithTwoCostsAssignments("m3",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 4, 1), getDate(2000, 3, 1), null);

        MachineDTO m4 = createMachineDTOWithTwoCostsAssignments("m4",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 1, 1), getDate(2000, 3, 1));

        MachineDTO m5 = createMachineDTOWithTwoCostsAssignments("m5",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 3, 1), getDate(2000, 4, 1));

        MachineDTO m6 = createMachineDTOWithTwoCostsAssignments("m6",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 4, 1), getDate(2000, 6, 1));

        MachineDTO m7 = createMachineDTOWithTwoCostsAssignments("m7",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 1, 1), getDate(2000, 2, 1));

        MachineDTO m8 = createMachineDTOWithTwoCostsAssignments("m8",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 5, 1), getDate(2000, 6, 1));

        MachineDTO m9 = createMachineDTOWithTwoCostsAssignments("m9",
                costCategory.getName(), getDate(2000, 2, 1),
                getDate(2000, 5, 1), getDate(2000, 2, 1), getDate(2000, 5, 1));

        /* Test. */
        ResourceListDTO resourceDTOs = createResourceListDTO(m1, m2, m3, m4,
                m5, m6, m7, m8, m9);

        assertOneConstraintViolationPerInstance(resourceService
                .addResources(resourceDTOs), resourceDTOs.resources.size());

        for (ResourceDTO r : resourceDTOs.resources) {
            MachineDTO m = (MachineDTO) r;
            assertFalse("Machine " + m.name + " not expected", resourceDAO
                    .existsByCode(((MachineDTO) r).code));
        }

    }

    @Test
    public void testUpdateResources() throws InstanceNotFoundException {

        CriterionType ctMachine = createCriterionType(ResourceEnum.MACHINE);
        CriterionType ctWorker = createCriterionType(ResourceEnum.WORKER);
        CostCategory costCategory = createCostCategory();

        /* Create a machine DTO. */
        MachineDTO m1 = new MachineDTO("name", "desc");
        CriterionSatisfactionDTO m1s1 = new CriterionSatisfactionDTO(
                ctMachine
                .getName(), "c1", getDate(2000, 1, 1), getDate(2000, 2, 1));
        m1.criterionSatisfactions.add(m1s1);
        ResourcesCostCategoryAssignmentDTO m1a1 = new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(), getDate(2000, 1, 1),
                getDate(2000, 2, 1));
        m1.resourcesCostCategoryAssignments.add(m1a1);

        /* Create a worker DTO. */
        String nif = getUniqueName();
        WorkerDTO w1 = new WorkerDTO(getUniqueName(), "surname", nif);
        CriterionSatisfactionDTO w1s1 = new CriterionSatisfactionDTO(
                ctWorker
                .getName(), "c1", getDate(2000, 1, 1), getDate(2000, 2, 1));
        w1.criterionSatisfactions.add(w1s1);
        ResourcesCostCategoryAssignmentDTO w1a1 = new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(), getDate(2000, 1, 1),
                getDate(2000, 2, 1));
        w1.resourcesCostCategoryAssignments.add(w1a1);

        /* Add resources. */
        assertNoConstraintViolations(resourceService
                .addResources(createResourceListDTO(m1, w1)));

        /*
         * Build DTOs for making the following update: + m1: update name, m1s1's
         * start date, and add a new cost category assignment. + w1: update
         * surname, w1a1's start date, and add a new criterion satisfaction.
         */
        MachineDTO m1Updated = new MachineDTO(m1.code, "name" + "UPDATED", null);
        CriterionSatisfactionDTO m1s1Updated = new CriterionSatisfactionDTO(
                m1s1.code, null, null, getDate(2000, 1, 2), null);
        m1Updated.criterionSatisfactions.add(m1s1Updated);
        ResourcesCostCategoryAssignmentDTO m1a2 = new ResourcesCostCategoryAssignmentDTO(
                costCategory.getName(), getDate(2000, 3, 1),
                getDate(2000, 4, 1));
        m1Updated.resourcesCostCategoryAssignments.add(m1a2);

        WorkerDTO w1Updated = new WorkerDTO(w1.code, null, "surname"
                + "UPDATED", null);
        CriterionSatisfactionDTO w1s2 = new CriterionSatisfactionDTO(
                ctWorker
                .getName(), "c1", getDate(2000, 3, 1), getDate(2000, 4, 1));
        w1Updated.criterionSatisfactions.add(w1s2);
        ResourcesCostCategoryAssignmentDTO w1a1Updated = new ResourcesCostCategoryAssignmentDTO(
                w1a1.code, null, getDate(2000, 2, 1), null);
        w1Updated.resourcesCostCategoryAssignments.add(w1a1Updated);

        /* Update resources and test. */
        assertNoConstraintViolations(resourceService
                .addResources(createResourceListDTO(m1Updated, w1Updated)));

        /* Test machine update. */
        Machine m1Entity = machineDAO.findByCode(m1.code);

        assertEquals(m1Updated.name, m1Entity.getName()); // Modified.
        assertEquals(m1.description, m1Entity.getDescription()); //Not modified.
        assertTrue(datesEquals( // Modified.
            m1s1Updated.startDate,
            m1Entity.getCriterionSatisfactionByCode(m1s1.code).getStartDate()));
        assertTrue(datesEquals( // Not modified.
            m1s1.endDate,
            m1Entity.getCriterionSatisfactionByCode(m1s1.code).getEndDate()));
        m1Entity.getResourcesCostCategoryAssignmentByCode(m1a2.code); // New.

        /* Test worker update. */
        Worker w1Entity = workerDAO.findByCode(w1.code);

        assertEquals(w1Updated.surname, w1Entity.getSurname()); // Modified.
        assertEquals(w1.firstName, w1Entity.getFirstName()); // Not modified.
        w1Entity.getCriterionSatisfactionByCode(w1s2.code); // New.
        assertTrue(datesEquals( // Modified.
            w1a1Updated.startDate,
            w1Entity.getResourcesCostCategoryAssignmentByCode(w1a1.code).
                getInitDate()));
        assertTrue(datesEquals( // Not modified.
            w1a1.endDate,
            w1Entity.getResourcesCostCategoryAssignmentByCode(w1a1.code).
                getEndDate()));

    }

    private CriterionType createCriterionType() {
        return createCriterionType(ResourceEnum.WORKER, true);
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

        return transactionService.runOnAnotherTransaction(createCriterionType);

    }

    private void saveResource(final Resource resource) {

        IOnTransaction<Void> save = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                resourceDAO.save(resource);
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(save);

    }

    private BaseCalendar getDefaultCalendar() {

        IOnTransaction<BaseCalendar> find = new IOnTransaction<BaseCalendar>() {

            @Override
            public BaseCalendar execute() {
                return configurationDAO.getConfiguration().getDefaultCalendar();
            }
        };

        return transactionService.runOnAnotherTransaction(find);

    }

    private BaseCalendar createBaseCalendar() {

        IOnTransaction<BaseCalendar> create = new IOnTransaction<BaseCalendar>() {

            @Override
            public BaseCalendar execute() {
                BaseCalendar baseCalendar = BaseCalendar.create();
                baseCalendar.setName(getUniqueName());
                baseCalendarDAO.save(baseCalendar);
                return baseCalendar;
            }
        };

        return transactionService.runOnAnotherTransaction(create);

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

        return transactionService.runOnAnotherTransaction(create);

    }


    private MachineDTO createMachineDTOWithTwoCriterionSatisfactions(
        String machineName, String criterionTypeName,
        String criterionName1, XMLGregorianCalendar startDate1,
        XMLGregorianCalendar endDate1,
        String criterionName2, XMLGregorianCalendar startDate2,
        XMLGregorianCalendar endDate2) {

        MachineDTO machineDTO = new MachineDTO(machineName, "desc");

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

        MachineDTO machineDTO = new MachineDTO(machineName, "desc");

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

    private boolean datesEquals(XMLGregorianCalendar date1, Date date2) {

        GregorianCalendar date2AsGC = new GregorianCalendar();
        date2AsGC.setTime(date2);

        return datesEquals(date1.toGregorianCalendar(), date2AsGC);

    }

    private boolean datesEquals(XMLGregorianCalendar date1, LocalDate date2) {

        GregorianCalendar date2AsGC = new GregorianCalendar(
            date2.getYear(), date2.getMonthOfYear()-1, date2.getDayOfMonth());

        return datesEquals(date1.toGregorianCalendar(), date2AsGC);


    }

    public boolean datesEquals(GregorianCalendar date1,
        GregorianCalendar date2) {

        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
            date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
            date1.get(Calendar.DAY_OF_MONTH) ==
                date2.get(Calendar.DAY_OF_MONTH);

    }

}
