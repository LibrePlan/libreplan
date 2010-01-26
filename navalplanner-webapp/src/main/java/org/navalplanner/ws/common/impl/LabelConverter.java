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

package org.navalplanner.ws.common.impl;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.ws.common.api.LabelDTO;

/**
 * Converter from/to {@link Label} entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class LabelConverter {

    private LabelConverter() {
    }

    public final static LabelDTO toDTO(Label label) {
        return new LabelDTO(label.getName(), label.getType().getName());
    }

    public final static Label forceToEntity(LabelDTO labelDTO) {
        LabelType labelType = null;
        try {
            labelType = Registry.getLabelTypeDAO().findUniqueByName(
                    labelDTO.type);
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        } catch (InstanceNotFoundException e) {
            labelType = LabelType.create(labelDTO.type);
            /*
             * "validate" method avoids that "labelType" goes to the Hibernate's
             * session if "labelType" is not valid.
             */
            labelType.validate();
            Registry.getLabelTypeDAO().save(labelType);
        }

        Label label = Registry.getLabelDAO().findByNameAndType(labelDTO.name,
                labelType);
        if (label == null) {
            label = Label.create(labelDTO.name);
            label.setType(labelType);
        }

        return label;
    }

    public static Set<Label> toEntity(Set<LabelDTO> labels)
            throws InstanceNotFoundException {
        Set<Label> result = new HashSet<Label>();
        for (LabelDTO labelDTO : labels) {
            result.add(toEntity(labelDTO));
        }
        return result;
    }

    public final static Label toEntity(LabelDTO labelDTO)
            throws InstanceNotFoundException {
        LabelType labelType = null;
        try {
            labelType = Registry.getLabelTypeDAO().findUniqueByName(
                    labelDTO.type);
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        }

        Label label = Registry.getLabelDAO().findByNameAndType(labelDTO.name,
                labelType);
        if (label == null) {
            throw new InstanceNotFoundException(labelDTO.name, Label.class
                    .getName());
        }

        return label;
    }

}