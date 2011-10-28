/*
 * This file is part of LibrePlan
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

package org.libreplan.ws.common.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * DTO for modeling a constraint violation.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ConstraintViolationDTO {

    public final static String FIELD_NAME_ATTRIBUTE_NAME = "field-name";
    public final static String MESSAGE_ATTRIBUTE_NAME = "message";

    @XmlAttribute(name=FIELD_NAME_ATTRIBUTE_NAME)
    public String fieldName;

    @XmlAttribute(name=MESSAGE_ATTRIBUTE_NAME)
    public String message;

    public ConstraintViolationDTO() {}

    public ConstraintViolationDTO(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }

    @Override
    public String toString() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        if (fieldName != null) {
            printWriter.print(FIELD_NAME_ATTRIBUTE_NAME + " = " + fieldName +
                " - ");
        }
        printWriter.println(MESSAGE_ATTRIBUTE_NAME + " = " + message);
        printWriter.close();

        return stringWriter.toString();

    }

}
