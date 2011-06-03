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

/**
 * DTO for modeling a property, containing a name and a value.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class PropertyDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String value;

    public PropertyDTO() {}

    public PropertyDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.print(name + " = " + value);
        printWriter.close();

        return stringWriter.toString();

    }

}
