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

package org.navalplanner.ws.labels.impl;

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
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.labels.api.ILabelService;
import org.navalplanner.ws.labels.api.LabelTypeDTO;
import org.navalplanner.ws.labels.api.LabelTypeListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link ILabelService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/labels/")
@Produces("application/xml")
@Service("labelServiceREST")
public class LabelServiceREST implements ILabelService {

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public LabelTypeListDTO getLabelTypes() {
        return LabelConverter.toDTO(labelTypeDAO.getAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addLabelTypes(
            LabelTypeListDTO labelTypes) {
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();
        Long numItem = new Long(1);
        Set<String> labelTypeNames = new HashSet<String>();

        for (LabelTypeDTO labelTypeDTO : labelTypes.labelTypes) {
            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;

            LabelType labelType = LabelConverter.toEntity(labelTypeDTO);

            if (labelType.getName() != null
                    && labelTypeNames.contains(labelType.getName()
                            .toLowerCase())) {

                instanceConstraintViolationsDTO = InstanceConstraintViolationsDTO
                        .create(Util.generateInstanceConstraintViolationsDTOId(
                                numItem, labelTypeDTO),
                                _("label type name is used by another label "
                                        + "type being imported"));
            } else {
                try {
                    labelType.validate();
                    labelTypeDAO.save(labelType);

                    if (labelType.getName() != null) {
                        labelTypeNames.add(labelType.getName().toLowerCase());
                    }
                } catch (ValidationException e) {
                    instanceConstraintViolationsDTO = ConstraintViolationConverter
                            .toDTO(Util
                                    .generateInstanceConstraintViolationsDTOId(
                                            numItem, labelTypeDTO), e
                                    .getInvalidValues());
                }
            }

            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList
                        .add(instanceConstraintViolationsDTO);
            }

            numItem++;
        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);
    }

}
