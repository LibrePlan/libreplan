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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.ws.common.api.ResourceEnumDTO;

/**
 * Converter from/to {@link ResourceEnum.type} entities to/from DTOs..
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ResourceEnumConverter {


    private final static Map<ResourceEnum, ResourceEnumDTO> resourceEnumToDTO = new HashMap<ResourceEnum, ResourceEnumDTO>();

    private final static Map<ResourceEnumDTO, ResourceEnum> resourceEnumFromDTO = new HashMap<ResourceEnumDTO, ResourceEnum>();

    static {

        resourceEnumToDTO.put(ResourceEnum.WORKER, ResourceEnumDTO.RESOURCE);
        resourceEnumFromDTO.put(ResourceEnumDTO.RESOURCE, ResourceEnum.WORKER);

        resourceEnumToDTO.put(ResourceEnum.WORKER, ResourceEnumDTO.WORKER);
        resourceEnumFromDTO.put(ResourceEnumDTO.WORKER, ResourceEnum.WORKER);

        resourceEnumToDTO.put(ResourceEnum.MACHINE, ResourceEnumDTO.MACHINE);
        resourceEnumFromDTO.put(ResourceEnumDTO.MACHINE, ResourceEnum.MACHINE);

    }

    public final static ResourceEnumDTO toDTO(ResourceEnum resource) {
        ResourceEnumDTO value = resourceEnumToDTO.get(resource);

        if (value == null) {
            throw new RuntimeException(MessageFormat.format(
                    "Unable to convert {0} " + "value to {1} type",
                    resource.toString(), ResourceEnumDTO.class.getName()));
        } else {
            return value;
        }
    }

    /**
     * It returns <code>null</code> if the parameter is <code>null</code>.
     */
    public final static ResourceEnum fromDTO(ResourceEnumDTO resource) {

        if (resource == null) {
            return null;
        }

        ResourceEnum value = resourceEnumFromDTO.get(resource);

        if (value == null) {
            throw new RuntimeException(MessageFormat.format(
                    "Unable to convert value to {0} type",
                    ResourceEnum.class.getName()));
        } else {
            return value;
        }

    }

}
