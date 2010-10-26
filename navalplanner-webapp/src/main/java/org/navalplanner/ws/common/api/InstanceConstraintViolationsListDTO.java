/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for modeling the constraint violations on a list of instances.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@XmlRootElement(name="instance-constraint-violations-list")
public class InstanceConstraintViolationsListDTO {

    @XmlElement(name="instance-constraint-violations")
    public List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList;

    public InstanceConstraintViolationsListDTO() {
        instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();
    }

    public InstanceConstraintViolationsListDTO(
            List<InstanceConstraintViolationsDTO> violations) {
        this.instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>(
                violations);
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println("*** " + this.getClass().getName() + " ***");
        for (InstanceConstraintViolationsDTO i : instanceConstraintViolationsList) {
            printWriter.println(i);
        }
        printWriter.close();
        return stringWriter.toString();
    }

}
