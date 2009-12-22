/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.ws.resources.impl;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;

/**
 * Converter from/to resource-related entities to/from DTOs.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ResourceConverter {

    private ResourceConverter() {}

    public final static Resource toEntity(ResourceDTO resourceDTO) {

        if (resourceDTO instanceof MachineDTO) {
            return toEntity((MachineDTO) resourceDTO);
        } else if (resourceDTO instanceof WorkerDTO) {
            return toEntity((WorkerDTO) resourceDTO);
        } else {
            throw new RuntimeException(
                _("Service does not manages resource of type: {0}",
                    resourceDTO.getClass().getName()));
        }

    }

    public final static Machine toEntity(MachineDTO machineDTO) {
        return Machine.create(machineDTO.code,machineDTO.name,
            machineDTO.description);
    }

    public final static Worker toEntity(WorkerDTO workerDTO) {
        return Worker.create(workerDTO.firstName, workerDTO.surname,
            workerDTO.nif);
    }

}
