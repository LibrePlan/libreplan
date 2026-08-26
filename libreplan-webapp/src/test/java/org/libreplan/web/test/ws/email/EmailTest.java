/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2016 Vova Perebykivskyi <vova@libreplan-enterprise.com>
 *
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
package org.libreplan.web.test.ws.email;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorProperty;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.email.daos.IEmailNotificationDAO;
import org.libreplan.business.email.daos.IEmailTemplateDAO;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;

import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.SchedulingDataForVersion;
import org.libreplan.business.orders.entities.TaskSource;

import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.entities.OrderVersion;

import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.importers.notifications.EmailConnectionValidator;
import org.libreplan.importers.notifications.IEmailNotificationJob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


import jakarta.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;


import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;


/**
 * Tests for {@link EmailTemplate}, {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,

        WEBAPP_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_TEST_FILE,

        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
public class EmailTest {

    @Autowired
    private IEmailTemplateDAO emailTemplateDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IEmailNotificationDAO emailNotificationDAO;

    @Qualifier("sendEmailOnTaskShouldStart")
    @Autowired
    private IEmailNotificationJob taskShouldStart;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private org.libreplan.business.planner.daos.ITaskSourceDAO taskSourceDAO;

    @Autowired
    private org.libreplan.business.orders.daos.IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private org.libreplan.business.orders.daos.IOrderElementDAO orderElementDAO;

    @Autowired
    private org.libreplan.business.scenarios.daos.IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IUserDAO userDAO;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    @Test
    @Transactional
    public void testACreateEmailTemplate() {
        EmailTemplate emailTemplate = createEmailTemplate();

        emailTemplateDAO.save(emailTemplate);

        EmailTemplate newEmailTemplate = emailTemplateDAO.findByTypeAndLanguage(
                EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START, Language.ENGLISH_LANGUAGE);

        assertEquals(emailTemplate, newEmailTemplate);
    }

    @Test
    @Transactional
    public void testBCreateEmailNotification() {
        emailTemplateDAO.save(createEmailTemplate());

        EmailNotification emailNotification = createEmailNotification();

        emailNotificationDAO.save(emailNotification);

        try {
            EmailNotification newEmailNotification = emailNotificationDAO.find(emailNotification.getId());
            assertEquals(emailNotification, newEmailNotification);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Transactional
    public void testCSendEmail() {
        EmailTemplate emailTemplate = createEmailTemplate();
        emailTemplateDAO.save(emailTemplate);

        EmailNotification emailNotification = createEmailNotification();
        emailNotificationDAO.save(emailNotification);

        // Before sending an Email I should specify email connector properties
        createEmailConnector();

        /*
         * Now I should call taskShouldStart.sendEmail();
         * But I will drop on checking email connection properties.
         * So I will get exception. Test is over.
         * There is no possibility to send message without real connection data.
         */

        taskShouldStart.sendEmail();

        emailTemplateDAO.delete(emailTemplate);
        emailNotificationDAO.deleteAll();

        assertTrue(EmailConnectionValidator.exceptionType instanceof MessagingException);
    }

    @Test
    @Transactional
    public void testDDeleteEmailNotification() {
        EmailTemplate emailTemplate = createEmailTemplate();
        emailTemplateDAO.save(emailTemplate);

        EmailNotification emailNotification = createEmailNotification();
        emailNotificationDAO.save(emailNotification);

        emailTemplateDAO.delete(emailTemplate);
        boolean result = emailNotificationDAO.deleteByProject(emailNotification.getProject());
        assertTrue(result);
    }

    private EmailTemplate createEmailTemplate() {
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setType(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);
        emailTemplate.setLanguage(Language.ENGLISH_LANGUAGE);
        emailTemplate.setSubject("Last words of Dunkan");
        emailTemplate.setContent("May He watch over us all...");

        return emailTemplate;
    }

    private EmailNotification createEmailNotification() {
        EmailTemplate emailTemplate = emailTemplateDAO.findByTypeAndLanguage(
                EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START, Language.ENGLISH_LANGUAGE);

        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setType(emailTemplate.getType());
        emailNotification.setUpdated(new Date());
        emailNotification.setResource(createWorker());
        emailNotification.setProject(createProjectWithTask());
        emailNotification.setTask(emailNotification.getProject().getChildren().get(0));

        return emailNotification;
    }

    private Worker createWorker() {
        Worker warden = Worker.create();
        warden.setFirstName("Alistair");
        warden.setSurname("Theirin");
        warden.setNif("9:10 Dragon");
        warden.setUser(createUser());

        workerDAO.save(warden);

        return warden;
    }

    private User createUser() {
        User user = User.create("Cole", "Spirit", "vova235@gmail.com");
        user.addRole(UserRole.ROLE_EMAIL_TASK_SHOULD_START);

        userDAO.save(user);

        return user;
    }

    private TaskGroup createProjectWithTask() {
        TaskGroup parent = createTaskGroup();
        Task child = createTask();

        parent.addTaskElement(child);

        // TaskElement.taskSource is NOT cascade="save-update" - plain taskElementDAO.save(parent)
        // leaves both TaskSources genuinely unpersisted (id stays null). Production code always
        // goes through TaskSource.persistTaskSources()/RealPersistence.save() for this. TaskSource
        // uses a "foreign" id generator keyed off its own task association, so the TaskElement
        // must be saved (and get a real id) FIRST, and only then can the TaskSource itself be
        // saved. Without the explicit save+dontPoseAsTransientObjectAnymore() pair, a later query
        // in this same @Transactional test's session auto-flushes into a TransientObjectException
        // on the still-"new" TaskSource.
        taskElementDAO.save(parent);

        persistTaskSource(parent.getTaskSource());
        persistTaskSource(child.getTaskSource());

        return parent;
    }

    private void persistTaskSource(TaskSource taskSource) {
        // TaskSource.hoursGroups is mapped cascade="none" (Orders.hbm.xml) - HoursGroup's real
        // persistence owner is OrderLine.hoursGroups (cascade="all,delete-orphan"), which this
        // minimal fixture never populates, so it needs its own explicit save here.
        for (HoursGroup hoursGroup : taskSource.getHoursGroups()) {
            hoursGroupDAO.saveWithoutValidating(hoursGroup);
            hoursGroup.dontPoseAsTransientObjectAnymore();
        }

        taskSourceDAO.saveWithoutValidating(taskSource);
        taskSource.dontPoseAsTransientObjectAnymore();
    }

    private TaskGroup createTaskGroup() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode(UUID.randomUUID().toString());
        hoursGroup.setWorkingHours(6);
        Order order = new Order();
        OrderVersion orderVersion = realOrderVersion();
        order.useSchedulingDataFor(orderVersion);
        order.setInitDate(new Date());

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("Project: Send Email");
        order.add(orderLine);
        // order.add() happens after order.useSchedulingDataFor() above, so the recursive cascade
        // never reached this line - it needs its own scheduling data set up explicitly.
        orderLine.useSchedulingDataFor(orderVersion);

        persistOrderGraph(order, orderLine);

        SchedulingDataForVersion version = orderLine.getCurrentSchedulingDataForVersion();
        TaskSource taskSource = TaskSource.create(version, Collections.singletonList(hoursGroup));

        // TaskGroup.create(taskSource) only sets the TaskElement -> TaskSource direction
        // (TaskElement.create()); it never reciprocally sets TaskSource.task, which is private and
        // only set by TaskSource's own linking factory methods below. Without that reverse link,
        // TaskSource's "foreign" id generator (keyed off its task) has nothing to derive an id
        // from once actually saved.
        TaskGroup result = taskSource.createTaskGroupWithoutDatesInitializedAndLinkItToTaskSource();
        result.setIntraDayEndDate(IntraDayDate.startOfDay(result.getIntraDayStartDate().getDate().plusDays(10)));

        return result;
    }

    // TaskSource.orderElement is mapped cascade="save-update" - saving a TaskGroup/Task built on
    // top of an EasyMock SchedulingDataForVersion/OrderVersion (as this used to do) cascades onto
    // the mock, which Hibernate 6 now validates strictly enough to throw
    // TransientObjectException/PropertyValueException on. Needs a genuine, if minimal, entity
    // graph instead - same established pattern as OrderElementTreeModelTest.givenOrder().
    private OrderVersion realOrderVersion() {
        // Used as a map key in OrderElement.schedulingDataForVersion - Hibernate needs the key
        // entity itself to already have an id before that map gets flushed.
        OrderVersion orderVersion = OrderVersion.createInitialVersion(PredefinedScenarios.MASTER.getScenario());
        orderVersionDAO.save(orderVersion);
        orderVersion.dontPoseAsTransientObjectAnymore();

        return orderVersion;
    }

    // OrderElement.schedulingDataForVersion is the association actually mapped
    // cascade="all-delete-orphan" (Orders.hbm.xml) - TaskSource.schedulingData itself is
    // cascade="none", so the SchedulingDataForVersion can only become genuinely persisted by
    // saving it through its real owner, the OrderElement/Order tree.
    private void persistOrderGraph(Order order, OrderLine orderLine) {
        order.setName("Order " + UUID.randomUUID());
        order.setCode(UUID.randomUUID().toString());
        orderLine.setCode(UUID.randomUUID().toString());

        orderElementDAO.saveWithoutValidating(order);
        order.dontPoseAsTransientObjectAnymore();
        orderLine.dontPoseAsTransientObjectAnymore();
        orderLine.getCurrentSchedulingDataForVersion().dontPoseAsTransientObjectAnymore();
    }

    private Task createTask() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setCode(UUID.randomUUID().toString());
        hoursGroup.setWorkingHours(5);

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("Task: use Quartz");

        Order order = new Order();
        OrderVersion orderVersion = realOrderVersion();
        order.useSchedulingDataFor(orderVersion);
        order.setInitDate(new Date());
        order.add(orderLine);
        orderLine.useSchedulingDataFor(orderVersion);

        persistOrderGraph(order, orderLine);

        SchedulingDataForVersion version = orderLine.getCurrentSchedulingDataForVersion();
        TaskSource taskSource = TaskSource.create(version, Collections.singletonList(hoursGroup));

        // Same reciprocal-link requirement as createTaskGroup() above. This skips the
        // Task-specific initializeDates() override that Task.createTask(taskSource) would have
        // called (protected, inaccessible from here) - set intraDayEndDate the same way
        // createTaskGroup() already does for the parent.
        Task result = taskSource.createTaskWithoutDatesInitializedAndLinkItToTaskSource();
        result.setIntraDayEndDate(IntraDayDate.startOfDay(result.getIntraDayStartDate().getDate().plusDays(1)));

        return result;
    }

    private void createEmailConnector() {
        Connector connector = Connector.create("E-mail");
        List<ConnectorProperty> properties = new ArrayList<>();

        properties.add(ConnectorProperty.create("Activated", "Y"));
        properties.add(ConnectorProperty.create("Protocol", "SMTP"));
        properties.add(ConnectorProperty.create("Host", "127.0.0.2"));
        properties.add(ConnectorProperty.create("Port", "25"));
        properties.add(ConnectorProperty.create("Email sender", "dunkan@libreplan-enterprise.com"));
        properties.add(ConnectorProperty.create("Email username", ""));
        properties.add(ConnectorProperty.create("Email password", ""));

        connector.setProperties(properties);

        Registry.getConnectorDAO().save(connector);
    }
}
