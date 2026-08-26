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

package org.libreplan.business.test.externalcompanies.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNull;

/**
 * Test for {@link org.libreplan.business.externalcompanies.daos.ExternalCompanyDAO}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE, BUSINESS_SPRING_CONFIG_TEST_FILE })
public class ExternalCompanyDAOTest {

    @Autowired
    IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    IUserDAO userDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Before
    public void loadRequiredData() {
        configurationBootstrap.loadRequiredData();
        scenariosBootstrap.loadRequiredData();
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    private OrderLine createOrderLine() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("bla");
        orderLine.setCode("code-" + UUID.randomUUID());
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        orderLine.addHoursGroup(hoursGroup);
        Order order = Order.create();
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, order);
        order.setName("bla-" + UUID.randomUUID());
        order.setInitDate(new Date());
        order.setCode("code-" + UUID.randomUUID());
        order.useSchedulingDataFor(orderVersion);
        order.add(orderLine);
        order.setCalendar(configurationDAO.getConfiguration().getDefaultCalendar());
        try {
            orderDAO.save(order);
            sessionFactory.getCurrentSession().flush();
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return orderLine;
    }

    private Task createValidTask() {
        HoursGroup associatedHoursGroup = new HoursGroup();
        associatedHoursGroup.setCode("hours-group-code-" + UUID.randomUUID());
        OrderLine orderLine = createOrderLine();
        orderLine.addHoursGroup(associatedHoursGroup);
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, orderLine.getOrder());
        orderLine.useSchedulingDataFor(orderVersion);
        SchedulingDataForVersion schedulingDataForVersion = orderLine.getCurrentSchedulingDataForVersion();

        TaskSource taskSource =
                TaskSource.create(schedulingDataForVersion, Collections.singletonList(associatedHoursGroup));

        TaskSourceSynchronization mustAdd = TaskSource.mustAdd(taskSource);
        mustAdd.apply(TaskSource.persistTaskSources(taskSourceDAO));

        return (Task) taskSource.getTask();
    }

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(externalCompanyDAO);
    }

    @Test
    @Transactional
    public void testSaveExternalCompany() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertTrue(externalCompany.getId() != null);
    }

    @Test
    @Transactional
    public void testRemoveExternalCompany() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.remove(externalCompany.getId());
        assertFalse(externalCompanyDAO.exists(externalCompany.getId()));
    }

    @Test
    @Transactional
    public void testListExternalCompanies() {
        int previous = externalCompanyDAO.list(ExternalCompany.class).size();
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertEquals(previous + 1, externalCompanyDAO.list(ExternalCompany.class).size());
    }

    @Test
    public void testRelationWithUser() throws InstanceNotFoundException {
        final User user = createValidUser();
        final ExternalCompany externalCompany = createValidExternalCompany();
        externalCompany.setCompanyUser(user);

        IOnTransaction<Void> saveEntities = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                userDAO.save(user);
                externalCompanyDAO.save(externalCompany);
                return null;
            }
        };

        transactionService.runOnTransaction(saveEntities);

        IOnTransaction<Void> retrieveEntitiesInOtherTransaction = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try{
                    ExternalCompany retrievedCompany = externalCompanyDAO.find(externalCompany.getId());
                    assertEquals(user.getLoginName(), retrievedCompany.getCompanyUser().getLoginName());
                }
                catch (InstanceNotFoundException e) {
                    fail("Unexpected InstanceNotFoundException");
                }
                return null;
            }
        };

        transactionService.runOnTransaction(retrieveEntitiesInOtherTransaction);
    }

    @Test
    @Transactional
    public void testFindUniqueByName() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertEquals(externalCompany.getId(), externalCompanyDAO.findUniqueByName(externalCompany.getName()).getId());
    }

    @Test
    @Transactional
    public void testExistsByName() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        assertFalse(externalCompanyDAO.existsByName(externalCompany.getName()));
        externalCompanyDAO.save(externalCompany);
        assertTrue(externalCompanyDAO.existsByName(externalCompany.getName()));
    }

    @Test(expected=ValidationException.class)
    public void testUniqueCompanyNameCheck() throws ValidationException {
        final ExternalCompany externalCompany1 = createValidExternalCompany();

        IOnTransaction<Void> createCompany = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                externalCompanyDAO.save(externalCompany1);
                return null;
            }
        };

        // A second, genuinely different company - not the same reused instance - with the same
        // name: reusing the same instance here (as this test used to) makes Hibernate 6 correctly
        // recognize it as a save of the already-persisted first company instead of a save of a
        // second, conflicting one, once it's no longer being fooled by a stale "new" flag (see
        // BaseEntity.newObject) - so it goes through as a harmless no-op update rather than
        // failing this test's own uniqueness validation.
        IOnTransaction<Void> createCompanyWithRepeatedName = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                ExternalCompany externalCompany2 = createValidExternalCompany();
                externalCompany2.setName(externalCompany1.getName());
                externalCompanyDAO.save(externalCompany2);
                return null;
            }
        };

        transactionService.runOnTransaction(createCompany);

        // The second company has the same name, an exception is thrown when saving it
        transactionService.runOnTransaction(createCompanyWithRepeatedName);
    }

    @Test(expected=ValidationException.class)
    public void testUniqueCompanyNifCheck() throws ValidationException {
        final ExternalCompany externalCompany1 = createValidExternalCompany();

        IOnTransaction<Void> createCompany = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                externalCompanyDAO.save(externalCompany1);
                return null;
            }
        };

        IOnTransaction<Void> createCompanyWithRepeatedNif = new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                ExternalCompany externalCompany2 = createValidExternalCompany();
                externalCompany2.setNif(externalCompany1.getNif());
                externalCompanyDAO.save(externalCompany2);
                return null;
            }
        };

        transactionService.runOnTransaction(createCompany);

        // The second object has the same cif, a exception is thrown when saving it
        transactionService.runOnTransaction(createCompanyWithRepeatedNif);
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findUniqueByNif/existsByNif/findSubcontractor/getAll/
     * getExternalCompaniesAreClient/isAlreadyInUse had no test coverage before.
     */

    @Test
    @Transactional
    public void testFindUniqueByNif() throws InstanceNotFoundException {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        assertEquals(externalCompany.getId(), externalCompanyDAO.findUniqueByNif(externalCompany.getNif()).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindUniqueByNifThrowsWhenNotFound() throws InstanceNotFoundException {
        externalCompanyDAO.findUniqueByNif("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testExistsByNif() {
        ExternalCompany externalCompany = createValidExternalCompany();
        assertFalse(externalCompanyDAO.existsByNif(externalCompany.getNif()));
        externalCompanyDAO.save(externalCompany);
        assertTrue(externalCompanyDAO.existsByNif(externalCompany.getNif()));
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);

        boolean found = false;
        for (ExternalCompany c : externalCompanyDAO.getAll()) {
            if (c.getId().equals(externalCompany.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindSubcontractorOnlyReturnsSubcontractors() {
        ExternalCompany subcontractor = createValidExternalCompany();
        subcontractor.setSubcontractor(true);
        externalCompanyDAO.save(subcontractor);

        ExternalCompany notSubcontractor = createValidExternalCompany();
        notSubcontractor.setSubcontractor(false);
        externalCompanyDAO.save(notSubcontractor);

        boolean found = false;
        for (ExternalCompany c : externalCompanyDAO.findSubcontractor()) {
            assertTrue(c.isSubcontractor());
            if (c.getId().equals(subcontractor.getId())) {
                found = true;
            }
            assertFalse(c.getId().equals(notSubcontractor.getId()));
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetExternalCompaniesAreClientOnlyReturnsClients() {
        ExternalCompany client = createValidExternalCompany();
        client.setClient(true);
        externalCompanyDAO.save(client);

        ExternalCompany notClient = createValidExternalCompany();
        notClient.setClient(false);
        externalCompanyDAO.save(notClient);

        boolean found = false;
        for (ExternalCompany c : externalCompanyDAO.getExternalCompaniesAreClient()) {
            assertTrue(c.isClient());
            if (c.getId().equals(client.getId())) {
                found = true;
            }
            assertFalse(c.getId().equals(notClient.getId()));
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseFalseForNewObject() {
        ExternalCompany notSaved = createValidExternalCompany();
        assertFalse(externalCompanyDAO.isAlreadyInUse(notSaved));
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseFalseWhenUnused() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        // ExternalCompany.create() marks the object new, and save() never clears that flag;
        // isAlreadyInUse() short-circuits to false for isNewObject()==true, so this must be
        // unmarked to actually exercise the query logic rather than the early-return branch.
        externalCompany.dontPoseAsTransientObjectAnymore();
        assertFalse(externalCompanyDAO.isAlreadyInUse(externalCompany));
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseTrueWhenUsedAsOrderCustomer() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompanyDAO.save(externalCompany);
        externalCompany.dontPoseAsTransientObjectAnymore();

        Order order = Order.create();
        order.setName("order-" + UUID.randomUUID());
        order.setCode(UUID.randomUUID().toString());
        order.setInitDate(new Date());
        order.setCustomer(externalCompany);
        order.setCalendar(configurationDAO.getConfiguration().getDefaultCalendar());
        OrderVersion orderVersion = ResourceAllocationDAOTest.setupVersionUsing(scenarioManager, order);
        order.useSchedulingDataFor(orderVersion);
        orderDAO.save(order);
        orderDAO.flush();

        assertTrue(externalCompanyDAO.isAlreadyInUse(externalCompany));
    }

    @Test
    @Transactional
    public void testIsAlreadyInUseTrueWhenUsedInSubcontractedTaskData() {
        ExternalCompany externalCompany = createValidExternalCompany();
        externalCompany.setSubcontractor(true);
        externalCompanyDAO.save(externalCompany);
        externalCompany.dontPoseAsTransientObjectAnymore();

        Task task = createValidTask();
        SubcontractedTaskData subcontractedTaskData = SubcontractedTaskData.create(task);
        subcontractedTaskData.setExternalCompany(externalCompany);
        subcontractedTaskData.addRequiredDeliveringDates(
                org.libreplan.business.planner.entities.SubcontractorDeliverDate.create(
                        new Date(), new Date(), new Date()));
        subcontractedTaskDataDAO.save(subcontractedTaskData);

        assertTrue(externalCompanyDAO.isAlreadyInUse(externalCompany));
    }

    public static ExternalCompany createValidExternalCompany() {
        return ExternalCompany.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    private User createValidUser() {
        return User.create(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new HashSet<>());
    }
}
