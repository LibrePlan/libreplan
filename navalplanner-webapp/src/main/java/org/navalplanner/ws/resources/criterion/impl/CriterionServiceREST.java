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

package org.navalplanner.ws.resources.criterion.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
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
public class CriterionServiceREST implements ICriterionService {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public CriterionTypeListDTO getCriterionTypes() {
        return CriterionConverter.toDTO(criterionTypeDAO.getCriterionTypes());
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addCriterionTypes(
        CriterionTypeListDTO criterionTypes) {

        // FIXME: now validations are executed twice: when calling
        // CriterionType::validate and when Hibernate runs automatically
        // validations. Not running validations explicitly
        // (CriterionType::validate) makes more difficult/obscure to catch
        // validation errors (a basic usage of IGeneriDAO::flush and
        // InvalidStateException is not enough; furthermore, take into account
        // that CriterionType::validate resolves an issue with our current
        // usage of Hibernate).
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            new ArrayList<InstanceConstraintViolationsDTO>();
        int instanceNumber = 1;

        for (CriterionTypeDTO criterionTypeDTO :
            criterionTypes.criterionTypes) {

            CriterionType criterionType =
                CriterionConverter.toEntity(criterionTypeDTO);

            try {
                criterionTypeDAO.save(criterionType);
            } catch (ValidationException e) {
                instanceConstraintViolationsList.add(
                    ConstraintViolationConverter.toDTO(
                        generateInstanceId(instanceNumber,
                            criterionTypeDTO.name),
                        e.getInvalidValues()));
            }

            instanceNumber++;

        }

        return new InstanceConstraintViolationsListDTO(
            instanceConstraintViolationsList);

    }

    private String generateInstanceId(int instanceNumber, String name) {

        String instanceId = instanceNumber + "";

        if (name != null &&  name.length() >= 0) {
            instanceId += " (" + name + ")";
        }

        return instanceId;

    }

}
