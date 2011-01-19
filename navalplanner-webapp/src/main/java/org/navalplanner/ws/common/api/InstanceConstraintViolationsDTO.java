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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * DTO for modeling the list of constraint violations on a given instance.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class InstanceConstraintViolationsDTO {

    public final static String NUM_ITEM_ATTRIBUTE_NAME = "num-item";
    public final static String CODE_ATTRIBUTE_NAME =
        IntegrationEntityDTO.CODE_ATTRIBUTE_NAME;
    public final static String ENTITY_TYPE_ATTRIBUTE_NAME = "entity-type";

    @Deprecated
    public final static String INSTANCE_ID_ATTRIBUTE_NAME = "instance-id";

    @Deprecated
    @XmlAttribute(name=INSTANCE_ID_ATTRIBUTE_NAME)
    public String instanceId;

    @XmlAttribute(name=NUM_ITEM_ATTRIBUTE_NAME)
    public Long numItem;

    @XmlAttribute(name=CODE_ATTRIBUTE_NAME)
    public String code;

    @XmlAttribute(name=ENTITY_TYPE_ATTRIBUTE_NAME)
    public String entityType;

    @XmlElement(name="constraint-violation")
    public List<ConstraintViolationDTO> constraintViolations;

    @XmlElement(name="recoverable-error")
    public RecoverableErrorDTO recoverableError;

    @XmlElement(name="internal-error")
    public InternalErrorDTO internalError;

    public InstanceConstraintViolationsDTO() {}

    @Deprecated
    public InstanceConstraintViolationsDTO(String instanceId,
        List<ConstraintViolationDTO> constraintViolations) {

        this.instanceId = instanceId;
        this.constraintViolations = constraintViolations;

    }

    private InstanceConstraintViolationsDTO(
        InstanceConstraintViolationsDTOId instanceId) {

        this.numItem = instanceId.getNumItem();
        this.code = instanceId.getCode();
        this.entityType = instanceId.getEntityType();

    }

    public InstanceConstraintViolationsDTO(
        InstanceConstraintViolationsDTOId instanceId,
        List<ConstraintViolationDTO> constraintViolations) {

        this(instanceId);
        this.constraintViolations = constraintViolations;

    }

    public InstanceConstraintViolationsDTO(
        InstanceConstraintViolationsDTOId instanceId,
        RecoverableErrorDTO recoverableError) {

        this(instanceId);
        this.recoverableError = recoverableError;

    }

    public InstanceConstraintViolationsDTO(
        InstanceConstraintViolationsDTOId instanceId,
        InternalErrorDTO internalError) {

        this(instanceId);
        this.internalError = internalError;

    }

    @Deprecated
    public static InstanceConstraintViolationsDTO create(String instanceId,
        String message) {

        List<ConstraintViolationDTO> constraintViolations =
            new ArrayList<ConstraintViolationDTO>();

        constraintViolations.add(new ConstraintViolationDTO(null, message));

        return new InstanceConstraintViolationsDTO(instanceId,
            constraintViolations);

    }

    public static InstanceConstraintViolationsDTO create(
        InstanceConstraintViolationsDTOId instanceId, String message) {

        List<ConstraintViolationDTO> constraintViolations =
            new ArrayList<ConstraintViolationDTO>();

        constraintViolations.add(new ConstraintViolationDTO(null, message));

        return new InstanceConstraintViolationsDTO(instanceId,
            constraintViolations);

    }

    @Override
    public String toString() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("** " + INSTANCE_ID_ATTRIBUTE_NAME + " = " +
            instanceId + " **");

        printWriter.println("** " +
            NUM_ITEM_ATTRIBUTE_NAME + " = " + numItem + " - " +
            CODE_ATTRIBUTE_NAME + " = " + code + " - " +
            ENTITY_TYPE_ATTRIBUTE_NAME + " = " + entityType +
            " **");

        if (internalError != null) {

            printWriter.println("Internal error:");
            printWriter.println(internalError);

        } else if (constraintViolations != null) {

            printWriter.println("Constraint violations:");
            for (ConstraintViolationDTO i : constraintViolations) {
                printWriter.println(i);
            }

        } else if (recoverableError != null) {

            printWriter.println("Recoverable error:");
            printWriter.println(recoverableError);

        }

        printWriter.close();

        return stringWriter.toString();

    }

}
