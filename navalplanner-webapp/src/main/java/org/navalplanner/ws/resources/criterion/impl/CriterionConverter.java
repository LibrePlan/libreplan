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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.ws.resources.criterion.api.CriterionDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeDTO;
import org.navalplanner.ws.resources.criterion.api.CriterionTypeListDTO;
import org.navalplanner.ws.resources.criterion.api.ResourceEnumDTO;

/**
 * Converter from/to criterion-related entities to/from DTOs.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public final class CriterionConverter {

    private final static Map<ResourceEnum, ResourceEnumDTO>
        resourceEnumToDTO =
            new HashMap<ResourceEnum, ResourceEnumDTO>();

    private final static Map<ResourceEnumDTO, ResourceEnum>
        resourceEnumFromDTO =
            new HashMap<ResourceEnumDTO, ResourceEnum>();

    static {

        resourceEnumToDTO.put(ResourceEnum.RESOURCE,
                ResourceEnumDTO.RESOURCE);
        resourceEnumFromDTO.put(ResourceEnumDTO.RESOURCE,
                ResourceEnum.RESOURCE);

        resourceEnumToDTO.put(ResourceEnum.WORKER,
            ResourceEnumDTO.WORKER);
        resourceEnumFromDTO.put(ResourceEnumDTO.WORKER,
                ResourceEnum.WORKER);

    }

    private CriterionConverter() {}

    public final static CriterionTypeListDTO toDTO(
        Collection<CriterionType> criterionTypes) {

        List<CriterionTypeDTO> criterionTypeDTOs =
            new ArrayList<CriterionTypeDTO>();

        for (CriterionType c : criterionTypes) {
            criterionTypeDTOs.add(toDTO(c));
        }

        return new CriterionTypeListDTO(criterionTypeDTOs);

    }

    public final static CriterionTypeDTO toDTO(CriterionType criterionType) {

        List<CriterionDTO> criterionDTOs = new ArrayList<CriterionDTO>();

        for (Criterion c : criterionType.getCriterions()) {
            if (c.getParent() == null) {
                criterionDTOs.add(toDTO(c));
            }
        }

        if (criterionDTOs.isEmpty()) {
            criterionDTOs = null;
        }

        return new CriterionTypeDTO(
            criterionType.getName(),
            criterionType.getDescription(),
            criterionType.allowHierarchy(),
            criterionType.isAllowSimultaneousCriterionsPerResource(),
            criterionType.isEnabled(),
            toDTO(criterionType.resource()),
            criterionDTOs);

    }

    public final static CriterionDTO toDTO(Criterion criterion) {

        List<CriterionDTO> childrenDTOs = new ArrayList<CriterionDTO>();

        for (Criterion c : criterion.getChildren()) {
            childrenDTOs.add(toDTO(c));
        }

        if (childrenDTOs.isEmpty()) {
            childrenDTOs = null;
        }

        return new CriterionDTO(criterion.getName(), criterion.isActive(),
            childrenDTOs);

    }


    public final static ResourceEnumDTO toDTO(ResourceEnum resource) {

        ResourceEnumDTO value = resourceEnumToDTO.get(resource);

        if (value == null) {
            throw new RuntimeException("Unable to convert '" +
                resource.toString() + "' value to ResourceEnumDTO");
        } else {
            return value;
        }

    }

    public final static CriterionType toEntity(
        CriterionTypeDTO criterionTypeDTO) {

        CriterionType criterionType = CriterionType.create(
            criterionTypeDTO.name,
            criterionTypeDTO.description,
            criterionTypeDTO.allowHierarchy,
            criterionTypeDTO.allowSimultaneousCriterionsPerResource,
            criterionTypeDTO.enabled,
            CriterionConverter.fromDTO(criterionTypeDTO.resource));

        for (CriterionDTO criterionDTO : criterionTypeDTO.criterions) {
             addCriterion(criterionType, criterionDTO, null);
        }

        return criterionType;

    }

    private static ResourceEnum fromDTO(ResourceEnumDTO resource) {

        ResourceEnum value = resourceEnumFromDTO.get(resource);

        if (value == null) {
            throw new RuntimeException("Unable to convert '" +
                resource.toString() + "' value to ResourceEnum");
        } else {
            return value;
        }

    }

    private static Criterion addCriterion(CriterionType criterionType,
        CriterionDTO criterionDTO, Criterion criterionParent) {

        Criterion criterion = toEntityWithoutChildren(criterionDTO,
            criterionType, criterionParent);
        criterionType.getCriterions().add(criterion);

        for (CriterionDTO childDTO : criterionDTO.children) {
            Criterion child = addCriterion(criterionType, childDTO, criterion);
            criterion.getChildren().add(child);
        }

        return criterion;

    }

    private static Criterion toEntityWithoutChildren(
        CriterionDTO childDTO, CriterionType criterionType,
        Criterion criterionParent) {

        Criterion criterion = Criterion.create(childDTO.name, criterionType);

        criterion.setActive(childDTO.active);
        criterion.setParent(criterionParent);

        return criterion;

    }

}
