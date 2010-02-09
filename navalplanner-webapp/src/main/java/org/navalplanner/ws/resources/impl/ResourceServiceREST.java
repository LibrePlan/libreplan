/*
 * This file is part of NavalPlan
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
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
public class ResourceServiceREST
    extends GenericRESTService<Resource, ResourceDTO>
    implements IResourceService {

    @Autowired
    private IResourceDAO resourceDAO;

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addResources(
        ResourceListDTO resources) {

        return save(resources.resources);

    }

    @Override
    protected Resource toEntity(ResourceDTO entityDTO) {
        return ResourceConverter.toEntity(entityDTO);
    }

    @Override
    protected ResourceDTO toDTO(Resource entity) {
       return null; // This service does not provide finder methods.
    }

    @Override
    protected IIntegrationEntityDAO<Resource> getIntegrationEntityDAO() {
        return resourceDAO;
    }

    @Override
    protected void updateEntity(Resource entity, ResourceDTO entityDTO)
        throws ValidationException {
        // FIXME: updated functionality not implemented yet.
    }

}
