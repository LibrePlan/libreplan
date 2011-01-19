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

package org.navalplanner.ws.common.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTOId;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * Utilities class related with web service.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class Util {

    public static InstanceConstraintViolationsDTOId
        generateInstanceConstraintViolationsDTOId(Long numItem,
             IntegrationEntityDTO integrationEntityDTO) {

        return new InstanceConstraintViolationsDTOId(numItem,
            integrationEntityDTO.code,
            integrationEntityDTO.getEntityType());

    }

    @Deprecated
    public static String generateInstanceId(int instanceNumber,
            String instanceIdentifier) {
        String instanceId = instanceNumber + "";

        if (instanceIdentifier != null && instanceIdentifier.length() >= 0) {
            instanceId += " (" + instanceIdentifier + ")";
        }

        return instanceId;
    }

    public static String getStackTrace(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public static String getAuthorizationHeader(String username, String password) {
        String authorization = Base64Utility.encode((username + ":" + password)
                .getBytes());
        return "Basic " + authorization;
    }

    public static void addAuthorizationHeader(WebClient client, String login,
            String password) {
        String authorizationHeader = getAuthorizationHeader(login, password);
        client.header("Authorization", authorizationHeader);
    }

}
