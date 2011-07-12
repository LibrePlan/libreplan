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

package org.navalplanner.ws.costcategories.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.daos.ICostCategoryDAO;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.costcategories.api.CostCategoryDTO;
import org.navalplanner.ws.costcategories.api.CostCategoryListDTO;
import org.navalplanner.ws.costcategories.api.ICostCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of <code>ICostCategoryService</code>.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Path("/costcategories/")
@Produces("application/xml")
@Service("costCategoryServiceREST")
public class CostCategoryServiceREST extends
        GenericRESTService<CostCategory, CostCategoryDTO> implements
        ICostCategoryService {

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public CostCategoryListDTO getCostCotegories() {
        return new CostCategoryListDTO(findAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addCostCategories(
            CostCategoryListDTO costCategoryListDTO) {

        return save(costCategoryListDTO.costCategories);

    }

    @Override
    protected CostCategory toEntity(CostCategoryDTO entityDTO) {
        return CostCategoryConverter.toEntity(entityDTO);
    }

    @Override
    protected CostCategoryDTO toDTO(CostCategory entity) {
        return CostCategoryConverter.toDTO(entity);
    }

    @Override
    protected IIntegrationEntityDAO<CostCategory> getIntegrationEntityDAO() {
        return costCategoryDAO;
    }

    @Override
    protected void updateEntity(CostCategory entity, CostCategoryDTO entityDTO)
            throws ValidationException {

        CostCategoryConverter.updateCostCategory(entity, entityDTO);

    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getCostCategory(@PathParam("code") String code) {
        return getDTOByCode(code);
    }
}
