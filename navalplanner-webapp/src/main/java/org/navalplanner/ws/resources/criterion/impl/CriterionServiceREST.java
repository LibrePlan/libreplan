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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.navalplanner.ws.common.impl.Util;
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

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            new ArrayList<InstanceConstraintViolationsDTO>();
        int instanceNumber = 1;
        Set<String> criterionTypeNames = new HashSet<String>();

        /* Process criterion types. */
        for (CriterionTypeDTO criterionTypeDTO :
            criterionTypes.criterionTypes) {

            /* Convert DTO to entity. */
            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO =
                null;
            CriterionType criterionType =
                CriterionConverter.toEntity(criterionTypeDTO);

            /*
             * Check if the criterion type name is used by another criterion
             * type being imported.
             */
            if (criterionType.getName() != null && criterionTypeNames.contains(
                criterionType.getName().toLowerCase())) {

                instanceConstraintViolationsDTO =
                    InstanceConstraintViolationsDTO.create(
                        Util.generateInstanceId(instanceNumber,
                            criterionTypeDTO.name),
                        _("criterion type name is used by another criterion " +
                            "type being imported"));

            } else {

                /* Validate criterion type. */
                try {

                    /*
                     * "validate" is executed before "save", since "save" first
                     * adds the object to the underlying ORM session and then
                     * validates. So, if "validate" method is not called
                     * explicitly before "save", an invalid criterion type
                     * would be added to the underlying ORM session, causing
                     * the invalid criterion type to be added to the database
                     * when the ORM commits the transaction. As a side effect,
                     * validations are executed twice. Note also, that
                     * "CriterionType::checkConstraintUniqueCriterionTypeName"
                     * only checks if a criterion type with the same name
                     * already exists in the *database*, and that the criterion
                     * types being imported are inserted in the database when
                     * the transaction is committed. In consequence, we can only
                     * call "save" if the criterion type is valid according to
                     * "validate" method and its name is not used by another
                     * previously *imported* (not in the database yet) criterion
                     * type.
                     */
                    criterionType.validate();
                    criterionTypeDAO.save(criterionType);

                    if (criterionType.getName() != null) {
                        criterionTypeNames.add(criterionType.getName().
                            toLowerCase());
                    }

                } catch (ValidationException e) {
                    instanceConstraintViolationsDTO =
                        ConstraintViolationConverter.toDTO(
                            Util.generateInstanceId(instanceNumber,
                                criterionTypeDTO.name),
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

}
