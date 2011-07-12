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

package org.navalplanner.ws.resources.criterion.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeListDTO;
import org.navalplanner.ws.resources.criterion.api.ICriterionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of <code>ICriterionService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Path("/criteriontypes/")
@Produces("application/xml")
@Service("criterionServiceREST")
public class CriterionServiceREST
    extends GenericRESTService<CriterionType, CriterionTypeDTO>
    implements ICriterionService {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public CriterionTypeListDTO getCriterionTypes() {
        return new CriterionTypeListDTO(findAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addCriterionTypes(
        CriterionTypeListDTO criterionTypes) {

        return save(criterionTypes.criterionTypes);

    }

    @Override
    protected CriterionType toEntity(CriterionTypeDTO entityDTO) {
        return CriterionConverter.toEntity(entityDTO);
    }

    @Override
    protected CriterionTypeDTO toDTO(CriterionType entity) {
        return CriterionConverter.toDTO(entity);
    }

    @Override
    protected IIntegrationEntityDAO<CriterionType> getIntegrationEntityDAO() {
        return criterionTypeDAO;
    }

    @Override
    protected void updateEntity(CriterionType entity,
        CriterionTypeDTO entityDTO) throws ValidationException {

        CriterionConverter.updateCriterionType(entity, entityDTO);

    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getCriterion(@PathParam("code") String code) {
        return getDTOByCode(code);
    }

}
