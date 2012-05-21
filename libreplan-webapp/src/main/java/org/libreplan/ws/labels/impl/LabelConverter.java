/*
 * This file is part of LibrePlan
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

package org.libreplan.ws.labels.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.ws.labels.api.LabelDTO;
import org.libreplan.ws.labels.api.LabelTypeDTO;
import org.libreplan.ws.labels.api.LabelTypeListDTO;

/**
 * Converter from/to {@link Label} related entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class LabelConverter {

    private LabelConverter() {
    }

    public final static LabelTypeListDTO toDTO(Collection<LabelType> labelTypes) {
        List<LabelTypeDTO> labelTypeDTOs = new ArrayList<LabelTypeDTO>();

        for (LabelType label : labelTypes) {
            labelTypeDTOs.add(toDTO(label));
        }

        return new LabelTypeListDTO(labelTypeDTOs);
    }

    public final static LabelTypeDTO toDTO(LabelType labelType) {
        List<LabelDTO> labelDTOs = new ArrayList<LabelDTO>();

        for (Label label : labelType.getLabels()) {
            labelDTOs.add(toDTO(label));
        }

        return new LabelTypeDTO(labelType.getCode(), labelType.getName(),
                labelDTOs);
    }

    public final static LabelDTO toDTO(Label label) {
        return new LabelDTO(label.getCode(), label.getName());
    }

    public final static LabelType toEntity(LabelTypeDTO labelTypeDTO) {
        LabelType labelType = LabelType.create(labelTypeDTO.code,
                labelTypeDTO.name);

        for (LabelDTO labelDTO : labelTypeDTO.labels) {
            labelType.addLabel(toEntity(labelDTO));
        }

        return labelType;

    }

    private static Label toEntity(LabelDTO labelDTO) {
        Label label = Label.create(labelDTO.code, labelDTO.name);

        return label;
    }

    public final static void updateLabelType(LabelType labelType,
            LabelTypeDTO labelTypeDTO) throws ValidationException {

        /*
         * 1: Update basic properties in existing label or add new label.
         */
        List<LabelDTO> labelDTOs = labelTypeDTO.labels;
        for (LabelDTO labelDTO : labelDTOs) {

            /* Step 1.1: requires each label DTO to have a code. */
            if (StringUtils.isBlank(labelDTO.code)) {
                throw new ValidationException("missing code in a label");
            }

            try {
                Label label = labelType.getLabelByCode(labelDTO.code);
                label.updateUnvalidated(StringUtils.trim(labelDTO.name));
            } catch (InstanceNotFoundException e) {
                labelType.addLabel(toEntity(labelDTO));
            }
        }

        /* 2: Update label type basic properties. */
        labelType.updateUnvalidated(StringUtils.trim(labelTypeDTO.name));

    }

}
