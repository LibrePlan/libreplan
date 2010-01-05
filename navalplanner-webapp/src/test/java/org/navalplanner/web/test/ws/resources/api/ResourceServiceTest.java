/*
 * This file is part of ###PROJECT_NAME###
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
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.resources.api.CriterionSatisfactionDTO;
import org.navalplanner.ws.resources.api.IResourceService;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
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
    WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
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

        assertTrue(instanceConstraintViolationsList.size() == 2);
        assertTrue(instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 2); // m2 constraint violations.
        assertTrue(instanceConstraintViolationsList.get(1).
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
        Worker w1 = Worker.createUnvalidated(getUniqueName(), "surname", "nif");
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

        assertTrue(instanceConstraintViolationsList.size() == 2);
        assertTrue(instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 1);
        assertTrue(instanceConstraintViolationsList.get(1).
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
                ' ' + ct.getName() + ' ', " c1 ", // Blank spaces intentionally
                                                  // added (OK).
                Calendar.getInstance().getTime(), null));
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), "c2",
                Calendar.getInstance().getTime(), null));

        /* Test. */
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(createResourceListDTO(machineDTO)).
                instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.isEmpty());

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
    public void testAddResourceWithCriterionSatisfactionsWithoutStartDate() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create a machine DTO. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName() , "c1",
                null, Calendar.getInstance().getTime())); // Missing start date.

        /* Test. */
        assertOneConstraintViolation(
            resourceService.addResources(createResourceListDTO(machineDTO)));
        assertFalse(machineDAO.existsMachineWithCodeInAnotherTransaction(
            machineDTO.code));

    }

    @Test
    @NotTransactional
    public void testAddResourceWithCriterionSatisfactionsWithIncorrectType() {

        /* Create two criterion types. */
        CriterionType machineCt = createCriterionType(ResourceEnum.MACHINE);
        CriterionType workerCt = createCriterionType(ResourceEnum.WORKER);

        /* Create resource DTOs. */
        MachineDTO machineDTO = new MachineDTO(getUniqueName(), "name", "desc");
        machineDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(workerCt.getName() , "c1",
                Calendar.getInstance().getTime(), null)); // Incorrect type.
        WorkerDTO workerDTO = new WorkerDTO(getUniqueName(), "surname", "nif");
        workerDTO.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(machineCt.getName() , "c1",
                Calendar.getInstance().getTime(), null)); // Incorrect type.

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
    public void testAddResourceWithCriterionSatisfactionsWithIncorrectNames() {

        /* Create a criterion type. */
        CriterionType ct = createCriterionType();

        /* Create machines DTOs. */
        MachineDTO m1 = new MachineDTO(getUniqueName(), "name", "desc");
        m1.criterionSatisfactions.add(
            new CriterionSatisfactionDTO("", "X", // Missing criterion type.
                Calendar.getInstance().getTime(), null));
        MachineDTO m2 = new MachineDTO(getUniqueName(), "name", "desc");
        m2.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(ct.getName(), // Missing criterion.
                null, Calendar.getInstance().getTime(), null));
        MachineDTO m3 = new MachineDTO(getUniqueName(), "name", "desc");
        m3.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                ct.getName() + 'X', // Non-existent criterion type.
                "c1", Calendar.getInstance().getTime(), null));
        MachineDTO m4 = new MachineDTO(getUniqueName(), "name", "desc");
        m4.criterionSatisfactions.add(
            new CriterionSatisfactionDTO(
                 ct.getName(),
                 "c1" + 'X', // Criterion name is not of ct's type.
                 Calendar.getInstance().getTime(), null));

        /* Test. */
        List<MachineDTO> machines = new ArrayList<MachineDTO>();
        machines.add(m1);
        machines.add(m2);
        machines.add(m3);
        machines.add(m4);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            resourceService.addResources(new ResourceListDTO(machines)).
                instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.size() == machines.size());

        for (InstanceConstraintViolationsDTO i :
            instanceConstraintViolationsList) {
            assertTrue(i.constraintViolations.size() == 1);
        }

        for (MachineDTO m : machines) {
            assertFalse(
                machineDAO.existsMachineWithCodeInAnotherTransaction(m.code));
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

    private CriterionType createCriterionType() {
        return createCriterionType(ResourceEnum.RESOURCE);
    }

    private CriterionType createCriterionType(final ResourceEnum resourceType) {

        IOnTransaction<CriterionType> createCriterionType =
            new IOnTransaction<CriterionType>() {

            @Override
            public CriterionType execute() {

                CriterionType ct = CriterionType.create(getUniqueName(),
                    "desc");
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

    public BaseCalendar getDefaultCalendar() {

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

    private void assertNoConstraintViolations(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO) {

        assertTrue(instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList.size() == 0);

    }

    private void assertOneConstraintViolation(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            instanceConstraintViolationsListDTO.
                instanceConstraintViolationsList;

         assertTrue(instanceConstraintViolationsList.size() == 1);
         assertTrue(instanceConstraintViolationsList.get(0).
             constraintViolations.size() == 1);

    }

    private String getUniqueName() {
        return UUID.randomUUID().toString();
    }

}
