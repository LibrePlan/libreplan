/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.email.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.daos.IEmailNotificationDAO;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link EmailNotificationDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class EmailNotificationDAOTest {

    @Autowired
    private IEmailNotificationDAO emailNotificationDAO;

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

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
        scenariosBootstrap.loadRequiredData();
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
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

    private EmailNotification createValid(EmailTemplateEnum type) {
        EmailNotification notification = new EmailNotification();
        notification.setType(type);
        notification.setUpdated(new Date());
        emailNotificationDAO.save(notification);
        return notification;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        EmailNotification notification = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        boolean found = false;
        for (EmailNotification n : emailNotificationDAO.getAll()) {
            if (n.getId().equals(notification.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetAllByTypeOnlyReturnsMatchingType() {
        EmailNotification matching = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        createValid(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);

        List<EmailNotification> result =
                emailNotificationDAO.getAllByType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);

        boolean found = false;
        for (EmailNotification n : result) {
            assertEquals(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, n.getType());
            if (n.getId().equals(matching.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testGetAllByProjectAndGetAllByTask() {
        Task task = createValidTask();

        EmailNotification projectNotification = new EmailNotification();
        projectNotification.setType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        projectNotification.setUpdated(new Date());
        projectNotification.setProject(task);
        emailNotificationDAO.save(projectNotification);

        EmailNotification taskNotification = new EmailNotification();
        taskNotification.setType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        taskNotification.setUpdated(new Date());
        taskNotification.setTask(task);
        emailNotificationDAO.save(taskNotification);

        List<EmailNotification> byProject = emailNotificationDAO.getAllByProject(task);
        assertEquals(1, byProject.size());
        assertEquals(projectNotification.getId(), byProject.get(0).getId());

        List<EmailNotification> byTask = emailNotificationDAO.getAllByTask(task);
        assertEquals(1, byTask.size());
        assertEquals(taskNotification.getId(), byTask.get(0).getId());
    }

    @Test
    @Transactional
    public void testDeleteAllRemovesEverything() {
        createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        createValid(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);

        assertTrue(emailNotificationDAO.deleteAll());
        assertTrue(emailNotificationDAO.getAll().isEmpty());
    }

    // deleteAllByType used to have a pre-existing bug unrelated to this migration: after deleting
    // the matches, it re-queried with Restrictions.eq("type", enumeration.ordinal()) - comparing
    // the enum-typed "type" column against a raw int (enumeration.ordinal()) instead of the enum
    // itself. Hibernate 5's legacy Criteria API rejected that client-side with a ClassCastException
    // (Integer cannot be cast to Enum) even though "type" is stored as the enum's ordinal in the
    // database, so the comparison was valid at the SQL level. Hibernate 6's JPA Criteria API
    // doesn't perform that same client-side check, so the query now runs and returns the correct
    // result - a genuine fix, not a masked failure (the DAO method itself no longer uses
    // enumeration.ordinal(), for clarity, but that's a stylistic cleanup: comparing against the
    // enum directly compiles to the identical ordinal-based SQL).
    @Test
    @Transactional
    public void testDeleteAllByTypeRemovesMatchingNotifications() {
        EmailNotification matching = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        EmailNotification other = createValid(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);

        assertTrue(emailNotificationDAO.deleteAllByType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED));

        boolean otherStillFound = false;
        for (EmailNotification n : emailNotificationDAO.getAll()) {
            assertFalse(n.getId().equals(matching.getId()));
            if (n.getId().equals(other.getId())) {
                otherStillFound = true;
            }
        }
        assertTrue(otherStillFound);
    }

    @Test
    @Transactional
    public void testDeleteByIdRemovesOnlyThatOne() {
        EmailNotification toDelete = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        EmailNotification other = createValid(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);

        assertTrue(emailNotificationDAO.deleteById(toDelete));

        boolean otherStillFound = false;
        for (EmailNotification n : emailNotificationDAO.getAll()) {
            assertFalse(n.getId().equals(toDelete.getId()));
            if (n.getId().equals(other.getId())) {
                otherStillFound = true;
            }
        }
        assertTrue(otherStillFound);
    }

    @Test
    @Transactional
    public void testDeleteByProjectAndDeleteByTask() {
        Task task = createValidTask();

        EmailNotification projectNotification = new EmailNotification();
        projectNotification.setType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        projectNotification.setUpdated(new Date());
        projectNotification.setProject(task);
        emailNotificationDAO.save(projectNotification);

        assertTrue(emailNotificationDAO.deleteByProject(task));
        assertTrue(emailNotificationDAO.getAllByProject(task).isEmpty());

        EmailNotification taskNotification = new EmailNotification();
        taskNotification.setType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
        taskNotification.setUpdated(new Date());
        taskNotification.setTask(task);
        emailNotificationDAO.save(taskNotification);

        assertTrue(emailNotificationDAO.deleteByTask(task));
        assertTrue(emailNotificationDAO.getAllByTask(task).isEmpty());
    }

}
