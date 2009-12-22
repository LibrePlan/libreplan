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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.resources.api.IResourceService;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST-based implementation of <code>IResourceService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Path("/resources/")
@Produces("application/xml")
@Service("resourceServiceREST")
public class ResourceServiceREST implements IResourceService {

    @Autowired
    private IResourceDAO resourceDAO;

    @Override
    public InstanceConstraintViolationsListDTO addResources(
        ResourceListDTO resources) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            new ArrayList<InstanceConstraintViolationsDTO>();
        int instanceNumber = 1;

        for (ResourceDTO resourceDTO : resources.resources) {

            Resource resource = ResourceConverter.toEntity(resourceDTO);
            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO =
                null;

            try {

                /*
                 * See CriterionServiceREST::addCriterionTypes for a
                 * justification of the explicit use of BaseEntity::validate.
                 *
                 */
                resource.validate();
                resourceDAO.save(resource);

            } catch (ValidationException e) {
                instanceConstraintViolationsDTO =
                    ConstraintViolationConverter.toDTO(
                        Util.generateInstanceId(instanceNumber,
                            resourceDTO.getUserProvidedId()),
                        e.getInvalidValues());
            }

            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList.add(
                    instanceConstraintViolationsDTO);
            }

            instanceNumber++;

        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);

    }

}
