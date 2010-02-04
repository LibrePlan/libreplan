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

package org.navalplanner.ws.labels.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.ws.labels.api.LabelDTO;
import org.navalplanner.ws.labels.api.LabelTypeDTO;
import org.navalplanner.ws.labels.api.LabelTypeListDTO;

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

}
