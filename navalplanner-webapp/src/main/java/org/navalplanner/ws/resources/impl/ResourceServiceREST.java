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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.exceptions.CreateUnvalidatedException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.resources.api.IResourceService;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourceListDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addResources(
        ResourceListDTO resources) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            new ArrayList<InstanceConstraintViolationsDTO>();
        int instanceNumber = 1;
        Set<String> resourceUserProvidedIds = new HashSet<String>();

        /* Process resources. */
        for (ResourceDTO resourceDTO : resources.resources) {

            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO =
                null;
            Resource resource = null;

            /* Convert DTO to entity. */
            try {
                resource = ResourceConverter.toEntity(resourceDTO);
            } catch (CreateUnvalidatedException e) {
                instanceConstraintViolationsDTO =
                    InstanceConstraintViolationsDTO.create(
                        Util.generateInstanceId(instanceNumber,
                            getUserProvidedId(resourceDTO)),
                        e.getMessage());
            }

            /* Validate resource. */
            if (resource != null) {
                try {

                    if (resourceUserProvidedIds.contains(
                        getUserProvidedId(resourceDTO).toLowerCase())) {

                        instanceConstraintViolationsDTO =
                            InstanceConstraintViolationsDTO.create(
                                Util.generateInstanceId(instanceNumber,
                                    getUserProvidedId(resourceDTO)),
                                    getDuplicatedImportedResourceErrorMessage(
                                        resourceDTO));

                    } else {

                        /*
                         * See CriterionServiceREST::addCriterionTypes for a
                         * justification of the explicit use of
                         * BaseEntity::validate.
                         *
                         */
                        resource.validate();
                        resourceDAO.save(resource);
                        resourceUserProvidedIds.add(
                            getUserProvidedId(resourceDTO).toLowerCase());

                    }

                } catch (ValidationException e) {
                    instanceConstraintViolationsDTO =
                        ConstraintViolationConverter.toDTO(
                            Util.generateInstanceId(instanceNumber,
                                getUserProvidedId(resourceDTO)),
                            e.getInvalidValues());
                }
            }

            /* Add constraint violations (if any). */
            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList.add(
                    instanceConstraintViolationsDTO);
            }

            instanceNumber++;

        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);

    }

    private String getUserProvidedId(ResourceDTO resourceDTO) {

        if (resourceDTO instanceof MachineDTO) {
            MachineDTO m = (MachineDTO) resourceDTO;
            return "machine" + '-' + StringUtils.trim(m.code);
        } else if (resourceDTO instanceof WorkerDTO) {
            WorkerDTO w = (WorkerDTO) resourceDTO;
            return "worker" + '-' + StringUtils.trim(w.firstName) +
                '-' + StringUtils.trim(w.surname) + '-' +
                StringUtils.trim(w.nif);
        } else {
            throw new RuntimeException(
                _("Service does not manage resource of type: {0}",
                    resourceDTO.getClass().getName()));
        }

    }

    private String getDuplicatedImportedResourceErrorMessage(
        ResourceDTO resourceDTO) {

        if (resourceDTO instanceof MachineDTO) {
            return _("code is used by another machine being imported");
        } else if (resourceDTO instanceof WorkerDTO) {
            return _("first name, surname, and nif are used by another " +
                "worker being imported");
        } else {
            throw new RuntimeException(
                 _("Service does not manage resource of type: {0}",
                    resourceDTO.getClass().getName()));
        }

    }

}
