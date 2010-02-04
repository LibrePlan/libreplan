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

package org.navalplanner.ws.resources.criterion.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.ws.common.api.DuplicateCodeBeingImportedException;
import org.navalplanner.ws.common.api.DuplicateNaturalKeyBeingImportedException;
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
        Long numItem = new Long(1);
        Set<String> existingKeys = new HashSet<String>();

        /* Process criterion types. */
        for (CriterionTypeDTO criterionTypeDTO :
            criterionTypes.criterionTypes) {

            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO =
                null;
            CriterionType criterionType = null;

            try {

                /*
                 * We must detect if there exists another instance being
                 * imported with the same code or natural key, since
                 * "IntegrationEntity::checkConstraintUniqueCode" and
                 * the natural key unique constraint rule
                 * (@AssertTrue/@AssertFalse method) in the concrete entity only
                 * can check this condition with respect to the entities already
                 * existing in database (such methods use DAO
                 * "xxxAnotherTransaction" methods to avoid Hibernate to launch
                 * INSERT statements for new objects when launching queries in
                 * conversational use cases).
                 */
                criterionTypeDTO.checkDuplicateCode(existingKeys);
                criterionTypeDTO.checkDuplicateNaturalKey(existingKeys);

                /*
                 * Convert DTO to entity. Note that the entity, if exists in
                 * the database, must be retrieved in another transaction.
                 * Otherwise (if retrieved as part of the current
                 * transaction), if the implementation of "updateEntity" makes
                 * modifications to the entity passed as a parameter and
                 * then throws an exception (because something make impossible
                 * to continue updating), the entity would be considered as
                 * dirty because it would be contained in the underlying ORM
                 * session, and in consequence, the ORM would try to update it
                 * when committing the transaction. Furthermore, the entity
                 * must be initialized so that "updateEntity" can access
                 * related entities.
                 */
                try {
                    criterionType =
                        findByCodeAnotherTransactionInitialized(
                            criterionTypeDTO.code);
                    udpateEntity(criterionType, criterionTypeDTO);
                } catch (InstanceNotFoundException e) {
                    criterionType = toEntity(criterionTypeDTO);
                }

                /*
                 * Save the entity (insert or update).
                 *
                 * "validate" is executed before "save", since "save" first
                 * adds the object to the underlying ORM session and then
                 * validates. So, if "validate" method is not called explicitly
                 * before "save", an invalid entity would be added to the
                 * underlying ORM session, causing the invalid entity to be
                 * added to the database when the ORM commits the transaction.
                 * As a side effect, validations are executed twice.
                 */
                criterionType.validate();
                criterionTypeDAO.save(criterionType);

            } catch (DuplicateCodeBeingImportedException e) {
                instanceConstraintViolationsDTO =
                    InstanceConstraintViolationsDTO.create(
                        Util.generateInstanceConstraintViolationsDTOId(numItem,
                            criterionTypeDTO),
                        _("code: {0} is used by another instance of type {1} " +
                            "being imported", e.getCode(), e.getEntityType()));
            } catch (DuplicateNaturalKeyBeingImportedException e) {
                instanceConstraintViolationsDTO =
                    InstanceConstraintViolationsDTO.create(
                        Util.generateInstanceConstraintViolationsDTOId(numItem,
                            criterionTypeDTO),
                        _("values: {0} are used by another instance of type " +
                            "{1} being imported",
                            Arrays.toString(e.getNaturalKeyValues()),
                            e.getEntityType()));
            } catch (ValidationException e) {
                instanceConstraintViolationsDTO =
                    ConstraintViolationConverter.toDTO(
                        Util.generateInstanceConstraintViolationsDTOId(
                            numItem, criterionTypeDTO), e);
            }


            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList.add(
                    instanceConstraintViolationsDTO);
            }

            numItem++;

        }

        return new InstanceConstraintViolationsListDTO(
            instanceConstraintViolationsList);

    }

    private CriterionType findByCodeAnotherTransactionInitialized(
        String code) throws InstanceNotFoundException {

        return criterionTypeDAO.findByCodeAnotherTransactionInitialized(
            code);

    }

    private CriterionType toEntity(CriterionTypeDTO criterionTypeDTO) {
        return CriterionConverter.toEntity(criterionTypeDTO);
    }

    private void udpateEntity(CriterionType criterionType,
        CriterionTypeDTO criterionTypeDTO) throws ValidationException {

        CriterionConverter.updateCriterionType(criterionType, criterionTypeDTO);

    }

}
