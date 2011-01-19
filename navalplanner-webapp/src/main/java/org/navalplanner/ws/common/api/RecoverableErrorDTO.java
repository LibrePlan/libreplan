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

package org.navalplanner.ws.common.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * DTO for modeling a recoverable error.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class RecoverableErrorDTO {

    public final static String ERROR_CODE_ATTRIBUTE_NAME = "error-code";
    public final static String MESSAGE_ATTRIBUTE_NAME = "message";

    @XmlAttribute(name=ERROR_CODE_ATTRIBUTE_NAME)
    public int errorCode;

    @XmlAttribute(name=MESSAGE_ATTRIBUTE_NAME)
    public String message;

    @XmlElement(name="property")
    public List<PropertyDTO> properties;

    public RecoverableErrorDTO() {}

    public RecoverableErrorDTO(int errorCode, String message,
        List<PropertyDTO> properties) {

        this.errorCode = errorCode;
        this.message = message;
        this.properties = properties;

    }

    @Override
    public String toString() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println(
            ERROR_CODE_ATTRIBUTE_NAME + " = " + errorCode + " - " +
            MESSAGE_ATTRIBUTE_NAME +  " = " + message);

        for (PropertyDTO p : properties) {
            printWriter.println(p);
        }

        printWriter.close();

        return stringWriter.toString();

    }

}
