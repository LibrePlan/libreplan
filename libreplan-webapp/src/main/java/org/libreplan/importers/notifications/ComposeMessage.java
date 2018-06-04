/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2016 LibrePlan
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

package org.libreplan.importers.notifications;

//import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.entities.ConnectorProperty;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.email.IEmailTemplateModel;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.AbstractEnvironment;

import org.springframework.web.context.WebApplicationContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.servlet.ServletContext;

import org.zkoss.zul.Messagebox;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.Context;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.String;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.io.UnsupportedEncodingException;

import org.springframework.core.env.MapPropertySource;

import static org.libreplan.web.I18nHelper._;

/**
 * Sends E-mail to users with data that storing in notification_queue table and
 * that are treat to incoming {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Configuration
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComposeMessage {

	@Autowired
	private IWorkerDAO workerDAO;

	@Autowired
	private IEmailTemplateModel emailTemplateModel;

	@Autowired
	private EmailConnectionValidator emailConnectionValidator;

	private String protocol;

	private String host;

	private String port;

	private String sender;

	private String usrnme;

	private String psswrd;

	private Properties properties;

	private static final Log LOG = LogFactory.getLog(ComposeMessage.class);

	public boolean composeMessageForUser(EmailNotification notification) {
		// Gather data about EmailTemplate needs to be used
		Resource resource = notification.getResource();
		EmailTemplateEnum type = notification.getType();
		Locale locale;
		Worker currentWorker = workerDAO.getCurrentWorker(resource.getId());

		UserRole currentUserRole = getCurrentUserRole(notification.getType());

		if (currentWorker != null && (currentWorker.getUser() != null) && currentWorker.getUser().isInRole(currentUserRole)) {
			if (currentWorker.getUser().getApplicationLanguage().equals(Language.BROWSER_LANGUAGE)) {
				locale = new Locale(System.getProperty("user.language"));
			} else {
				locale = new Locale(currentWorker.getUser().getApplicationLanguage().getLocale().getLanguage());
			}

			EmailTemplate currentEmailTemplate = findCurrentEmailTemplate(type, locale);

			if (currentEmailTemplate == null) {
				LOG.error("Email template is null");
				return false;
			}

			// Modify text that will be composed
			String text = currentEmailTemplate.getContent();
			text = replaceKeywords(text, currentWorker, notification);

			String receiver = currentWorker.getUser().getEmail();

			setupConnectionProperties();

			final String username = usrnme;
			final String password = psswrd;

			// It is very important to use Session.getInstance() instead of
			// Session.getDefaultInstance()
			Session mailSession = Session.getInstance(properties, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			// Send message
			try {
				MimeMessage message = new MimeMessage(mailSession);

				message.setFrom(new InternetAddress(sender));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

				String subject = currentEmailTemplate.getSubject();
				message.setSubject(subject);

				message.setText(text);

				Transport.send(message);

				return true;

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			} catch (NullPointerException e) {
				if (receiver == null) {
					Messagebox.show(_(currentWorker.getUser().getLoginName() + " - this user have not filled E-mail"),
							_("Error"), Messagebox.OK, Messagebox.ERROR);
				}
			}
		}
		return false;
	}

	private EmailTemplate findCurrentEmailTemplate(EmailTemplateEnum templateEnum, Locale locale) {
		List<EmailTemplate> emailTemplates;
		emailTemplates = emailTemplateModel.getAll();

		for (EmailTemplate item : emailTemplates) {
			if (item.getType().equals(templateEnum) && item.getLanguage().getLocale().equals(locale)) {
				return item;
			}

		}

		return null;
	}

	private String replaceKeywords(String text, Worker currentWorker, EmailNotification notification) {
		String newText = text;

		// replace {url} in all messages even timesheet reminder emails
		// as a link may be helpful 
		newText = newText.replaceAll("\\{url\\}", MultipleTabsPlannerController.WELCOME_URL);
		if (notification.getType().equals(EmailTemplateEnum.TEMPLATE_ENTER_DATA_IN_TIMESHEET)) {
			// It is because there is no other data for
			// EmailNotification of TEMPLATE_ENTER_DATA_IN_TIMESHEET
			// notification type
			newText = newText.replaceAll("\\{resource\\}", notification.getResource().getName());
		} else {
			newText = newText.replaceAll("\\{username\\}", currentWorker.getUser().getLoginName());
			newText = newText.replaceAll("\\{firstname\\}", currentWorker.getUser().getFirstName());
			newText = newText.replaceAll("\\{lastname\\}", currentWorker.getUser().getLastName());
			newText = newText.replaceAll("\\{project\\}", notification.getProject().getName());
			newText = newText.replaceAll("\\{resource\\}", notification.getResource().getName());
			newText = newText.replaceAll("\\{task\\}", notification.getTask().getName());
			newText = newText.replaceAll("\\{projecturl\\}", MultipleTabsPlannerController.WELCOME_URL+ ";order=" + notification.getProject().getProjectCode());
		}
		return newText;
	}

	List<String> getEndPoints() throws MalformedObjectNameException, NullPointerException, UnknownHostException,
			AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
		
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		
		Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
				Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		
		String hostname = InetAddress.getLocalHost().getHostName();
		
		InetAddress[] addresses = InetAddress.getAllByName(hostname);
		
		ArrayList<String> endPoints = new ArrayList<String>();
		for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
			ObjectName obj = i.next();
			String scheme = mbs.getAttribute(obj, "scheme").toString();
			String port = obj.getKeyProperty("port");
			for (InetAddress addr : addresses) {
				String host = addr.getHostAddress();
				String ep = scheme + "://" + host + ":" + port;
				endPoints.add(ep);
			}
		}
		return endPoints;
	}

	private void setupConnectionProperties() {
		List<ConnectorProperty> emailConnectorProperties = emailConnectionValidator.getEmailConnectorProperties();

		for (int i = 0; i < emailConnectorProperties.size(); i++) {
			switch (i) {

			case 1:
				protocol = emailConnectorProperties.get(1).getValue();
				break;

			case 2:
				host = emailConnectorProperties.get(2).getValue();
				break;

			case 3:
				port = emailConnectorProperties.get(3).getValue();
				break;

			case 4:
				sender = emailConnectorProperties.get(4).getValue();
				break;

			case 5:
				usrnme = emailConnectorProperties.get(5).getValue();
				break;

			case 6:
				psswrd = emailConnectorProperties.get(6).getValue();
				break;

			default:
				break;

			}
		}

		properties = new Properties();

		if ("STARTTLS".equals(protocol)) {
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.socketFactory.port", port);
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.port", port);
		} else if ("SMTP".equals(protocol)) {
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", port);
		}
	}

	private UserRole getCurrentUserRole(EmailTemplateEnum type) {
		switch (type) {

		case TEMPLATE_TASK_ASSIGNED_TO_RESOURCE:
			return UserRole.ROLE_EMAIL_TASK_ASSIGNED_TO_RESOURCE;

		case TEMPLATE_RESOURCE_REMOVED_FROM_TASK:
			return UserRole.ROLE_EMAIL_RESOURCE_REMOVED_FROM_TASK;

		case TEMPLATE_MILESTONE_REACHED:
			return UserRole.ROLE_EMAIL_MILESTONE_REACHED;

		case TEMPLATE_TODAY_TASK_SHOULD_START:
			return UserRole.ROLE_EMAIL_TASK_SHOULD_START;

		case TEMPLATE_TODAY_TASK_SHOULD_FINISH:
			return UserRole.ROLE_EMAIL_TASK_SHOULD_FINISH;

		case TEMPLATE_ENTER_DATA_IN_TIMESHEET:
			return UserRole.ROLE_EMAIL_TIMESHEET_DATA_MISSING;

		default:
			/* There is no other template */
			return null;
		}
	}
}
