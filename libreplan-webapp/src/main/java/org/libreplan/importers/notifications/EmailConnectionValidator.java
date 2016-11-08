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

import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.ConnectorProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.List;
import java.util.Properties;

/**
 * Validate Email Connection properties.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailConnectionValidator {

    @Autowired
    private IConnectorDAO connectorDAO;

    /**
     * Needed for EmailTest.
     */
    public static Exception exceptionType;

    public boolean validConnection() {
        List<ConnectorProperty> emailConnectorProperties = getEmailConnectorProperties();

        String protocol = null;
        String host = null;
        String port = null;
        String usrnme = null;
        String psswrd = null;

        for (int i = 0; i < emailConnectorProperties.size(); i++) {
            switch ( i ) {
                case 1:
                    protocol = emailConnectorProperties.get(1).getValue();
                    break;

                case 2:
                    host = emailConnectorProperties.get(2).getValue();
                    break;

                case 3:
                    port = emailConnectorProperties.get(3).getValue();
                    break;

                case 5:
                    usrnme = emailConnectorProperties.get(5).getValue();
                    break;

                case 6:
                    psswrd = emailConnectorProperties.get(6).getValue();
                    break;

                default:
                    /* Nothing */
                    break;
            }
        }

        // Set properties of connection
        Properties properties = new Properties();

        Transport transport = null;

        try {
            if ( "SMTP".equals(protocol) ) {
                properties.setProperty("mail.smtp.port", port);
                properties.setProperty("mail.smtp.host", host);
                properties.setProperty("mail.smtp.connectiontimeout", Integer.toString(3000));
                Session session = Session.getInstance(properties, null);

                transport = session.getTransport("smtp");
                if ( "".equals(usrnme) && "".equals(psswrd) ) {
                    transport.connect();
                }


            } else if ( "STARTTLS".equals(protocol) ) {
                properties.setProperty("mail.smtps.port", port);
                properties.setProperty("mail.smtps.host", host);
                properties.setProperty("mail.smtps.connectiontimeout", Integer.toString(3000));
                Session session = Session.getInstance(properties, null);

                transport = session.getTransport("smtps");

                if ( !"".equals(usrnme) && psswrd != null ) {
                    transport.connect(host, usrnme, psswrd);
                }

            }
            if ( transport != null && transport.isConnected() )
                return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            // FIXME must be a better way to send exception type to test class
            exceptionType = e;
        }

        return false;
    }

    public List<ConnectorProperty> getEmailConnectorProperties() {
        return connectorDAO.findUniqueByName("E-mail").getProperties();
    }

    public boolean isConnectionActivated() {
        List<ConnectorProperty> emailConnectorProperties = getEmailConnectorProperties();

        for (ConnectorProperty item : emailConnectorProperties) {
            if ( "Activated".equals(item.getKey()) ) {
                if ( "Y".equals(item.getValue()) ) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

}
