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
import org.libreplan.business.orders.entities.OrderElement;

import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
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


import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
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

        taskElementDAO.save(parent);

        return parent;
    }

    private TaskGroup createTaskGroup() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(6);
        Order order = new Order();
        order.useSchedulingDataFor(mockOrderVersion());
        order.setInitDate(new Date());

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("Project: Send Email");
        order.add(orderLine);

        SchedulingDataForVersion version = mockSchedulingDataForVersion(orderLine);
        TaskSource taskSource = TaskSource.create(version, Collections.singletonList(hoursGroup));

        TaskGroup result = TaskGroup.create(taskSource);
        result.setIntraDayEndDate(IntraDayDate.startOfDay(result.getIntraDayStartDate().getDate().plusDays(10)));

        return result;
    }

    private OrderVersion mockOrderVersion() {
        OrderVersion result = createNiceMock(OrderVersion.class);
        replay(result);

        return result;
    }

    private SchedulingDataForVersion mockSchedulingDataForVersion(OrderElement orderElement) {
        SchedulingDataForVersion result = createNiceMock(SchedulingDataForVersion.class);
        TaskSource taskSource = createNiceMock(TaskSource.class);

        expect(result.getOrderElement()).andReturn(orderElement).anyTimes();
        expect(taskSource.getOrderElement()).andReturn(orderElement).anyTimes();
        expect(result.getTaskSource()).andReturn(taskSource).anyTimes();

        replay(result, taskSource);

        return result;
    }

    private Task createTask() {
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setWorkingHours(5);

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("Task: use Quartz");

        Order order = new Order();
        order.useSchedulingDataFor(mockOrderVersion());
        order.setInitDate(new Date());
        order.add(orderLine);

        SchedulingDataForVersion version = mockSchedulingDataForVersion(orderLine);
        TaskSource taskSource = TaskSource.create(version, Collections.singletonList(hoursGroup));

        return Task.createTask(taskSource);
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
