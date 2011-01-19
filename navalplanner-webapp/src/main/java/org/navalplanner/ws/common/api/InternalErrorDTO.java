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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for modeling an internal error.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@XmlRootElement(name="internal-error")
public class InternalErrorDTO {

    public final static String MESSAGE_ATTRIBUTE_NAME = "message";
    public final static String STACK_TRACE_ATTRIBUTE_NAME = "stack-trace";

    @XmlAttribute(name=MESSAGE_ATTRIBUTE_NAME)
    public String message;

    @XmlElement(name=STACK_TRACE_ATTRIBUTE_NAME)
    public String stackTrace;

    public InternalErrorDTO() {}

    public InternalErrorDTO(String message, String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println(MESSAGE_ATTRIBUTE_NAME + " = " + message);
        printWriter.println(STACK_TRACE_ATTRIBUTE_NAME + " = " + stackTrace);
        printWriter.close();

        return stringWriter.toString();

    }

}
