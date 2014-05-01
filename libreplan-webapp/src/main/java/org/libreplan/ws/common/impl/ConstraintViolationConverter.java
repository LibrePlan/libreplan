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

package org.libreplan.ws.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.common.exceptions.ValidationException.InvalidValue;
import org.libreplan.ws.common.api.ConstraintViolationDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTOId;
import org.libreplan.ws.common.api.InternalErrorDTO;
import org.libreplan.ws.common.api.PropertyDTO;
import org.libreplan.ws.common.api.RecoverableErrorDTO;

/**
 * Converter for constraint violations.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ConstraintViolationConverter {

    private ConstraintViolationConverter() {}

    public final static ConstraintViolationDTO toDTO(InvalidValue each) {
        return new ConstraintViolationDTO(getFieldName(each), each.getMessage());
    }

    private static String getFieldName(InvalidValue invalidValue) {
        String propertyPath = invalidValue.getPropertyPath();
        if (invalidValue.getRootBean() == null || propertyPath == null
                || isCheckConstraint(propertyPath)) {
            return null;
        }
        final String rootObjectClassName = invalidValue.getRootBean()
                .getClass().getSimpleName();
        return rootObjectClassName + "::" + invalidValue.getPropertyPath();
    }

    private static boolean isCheckConstraint(String property) {
        return property.toString().contains("checkConstraint");
    }

    public final static InstanceConstraintViolationsDTO toDTO(
        InstanceConstraintViolationsDTOId instanceId,
        ValidationException validationException) {

        List<ConstraintViolationDTO> constraintViolationDTOs =
            new ArrayList<ConstraintViolationDTO>();

        if (validationException.getInvalidValues().isEmpty()) {
            constraintViolationDTOs.add(new ConstraintViolationDTO(null,
                validationException.getMessage()));
        } else {
            for (InvalidValue each : validationException
                    .getInvalidValues()) {
                constraintViolationDTOs.add(toDTO(each));
            }
        }

        return new InstanceConstraintViolationsDTO(instanceId,
            constraintViolationDTOs);
    }

    @Deprecated
    public final static InstanceConstraintViolationsDTO toDTO(
            String instanceId, Collection<? extends InvalidValue> invalidValues) {

        List<ConstraintViolationDTO> constraintViolationDTOs =
            new ArrayList<ConstraintViolationDTO>();

        for (InvalidValue i : invalidValues) {
            constraintViolationDTOs.add(toDTO(i));
        }

        return new InstanceConstraintViolationsDTO(instanceId,
            constraintViolationDTOs);
    }

    public final static InstanceConstraintViolationsDTO toDTO(
        InstanceConstraintViolationsDTOId instanceId,
        RecoverableErrorException recoverableErrorException) {

        List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

        for (Map.Entry<String, String> p :
            recoverableErrorException.getProperties().entrySet()) {

            properties.add(new PropertyDTO(p.getKey(), p.getValue()));

        }

        RecoverableErrorDTO recoverableErrorDTO = new RecoverableErrorDTO(
            recoverableErrorException.getErrorCode().ordinal() + 1,
            recoverableErrorException.getMessage(),
            properties);

        return new InstanceConstraintViolationsDTO(instanceId,
            recoverableErrorDTO);

    }

    public final static InstanceConstraintViolationsDTO toDTO(
        InstanceConstraintViolationsDTOId instanceId,
        RuntimeException runtimeException) {

        InternalErrorDTO internalErrorDTO = new InternalErrorDTO(
            runtimeException.getMessage(),
            Util.getStackTrace(runtimeException));

        return new InstanceConstraintViolationsDTO(instanceId,
            internalErrorDTO);

    }

}
