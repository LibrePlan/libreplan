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

package org.libreplan.ws.resources.criterion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.ws.common.impl.ResourceEnumConverter;
import org.libreplan.ws.resources.criterion.api.CriterionDTO;
import org.libreplan.ws.resources.criterion.api.CriterionTypeDTO;

/**
 * Converter from/to criterion-related entities to/from DTOs.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public final class CriterionConverter {

    private CriterionConverter() {}

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
            criterionType.getCode(),
            criterionType.getName(),
            criterionType.getDescription(),
            criterionType.allowHierarchy(),
            criterionType.isAllowSimultaneousCriterionsPerResource(),
            criterionType.isEnabled(),
            ResourceEnumConverter
                        .toDTO(criterionType.getResource()),
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

        return new CriterionDTO(criterion.getCode(), criterion.getName(),
            criterion.isActive(), childrenDTOs);

    }

    public final static CriterionType toEntity(
        CriterionTypeDTO criterionTypeDTO) {

        CriterionType criterionType = CriterionType.createUnvalidated(
            StringUtils.trim(criterionTypeDTO.code),
            StringUtils.trim(criterionTypeDTO.name),
            StringUtils.trim(criterionTypeDTO.description),
            criterionTypeDTO.allowHierarchy,
            criterionTypeDTO.allowSimultaneousCriterionsPerResource,
            criterionTypeDTO.enabled,
            ResourceEnumConverter.fromDTO(criterionTypeDTO.resource));

        for (CriterionDTO criterionDTO : criterionTypeDTO.criterions) {
             addCriterion(criterionType, criterionDTO, null);
        }

        return criterionType;

    }

    public final static void updateCriterionType(CriterionType criterionType,
        CriterionTypeDTO criterionTypeDTO) throws ValidationException {

        /* 1: Get criterion wrappers with parent code. */
        Set<CriterionDTOWithParentCode> criterionWrappers =
            getCriterionWrappers(criterionTypeDTO.criterions, null);


        /*
         * 2: Update basic properties in existing criteria and add new
         * criteria.
         */
        for (CriterionDTOWithParentCode criterionWrapper : criterionWrappers) {

            /* Step 3 requires each criterion DTO to have a code. */
            if (StringUtils.isBlank(criterionWrapper.dto.code)) {
                throw new ValidationException("missing code in a criterion");
            }

            try {
                Criterion criterion = criterionType.getCriterionByCode(
                    criterionWrapper.dto.code);
                criterion.updateUnvalidated(
                    StringUtils.trim(criterionWrapper.dto.name),
                    criterionWrapper.dto.active);
            } catch (InstanceNotFoundException e) {
                criterionType.getCriterions().add(toEntityWithoutChildren(
                    criterionWrapper.dto, criterionType, null));
            }

        }

        /* 3: Update relationships. */
        for (CriterionDTOWithParentCode criterionWrapper : criterionWrappers) {

            Criterion criterion = criterionType.getExistingCriterionByCode(
                criterionWrapper.dto.code);
            Criterion newCriterionParent = null;

            if (criterionWrapper.parentCode != null) {
                newCriterionParent = criterionType.getExistingCriterionByCode(
                    criterionWrapper.parentCode);
            }

            criterion.moveTo(newCriterionParent);

        }


        /* 4: Update criterion type basic properties. */
        criterionType.updateUnvalidated(
            StringUtils.trim(criterionTypeDTO.name),
            StringUtils.trim(criterionTypeDTO.description),
            criterionTypeDTO.allowHierarchy,
            criterionTypeDTO.allowSimultaneousCriterionsPerResource,
            criterionTypeDTO.enabled,
            ResourceEnumConverter.fromDTO(criterionTypeDTO.resource));

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

        Criterion criterion = Criterion.createUnvalidated(
            StringUtils.trim(childDTO.code),
            StringUtils.trim(childDTO.name),
            criterionType, criterionParent, childDTO.active);

        return criterion;

    }

    private static Set<CriterionDTOWithParentCode> getCriterionWrappers(
        Collection<CriterionDTO> criterionTypeDTOs, String parentCode) {

        Set<CriterionDTOWithParentCode> wrappers =
            new HashSet<CriterionDTOWithParentCode>();

        for (CriterionDTO criterionDTO : criterionTypeDTOs) {
            wrappers.add(new CriterionDTOWithParentCode(criterionDTO,
                parentCode));
            wrappers.addAll(getCriterionWrappers(criterionDTO.children,
                criterionDTO.code));
        }

        return wrappers;

    }

}
