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

package org.navalplanner.ws.resources.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.exceptions.CreateUnvalidatedException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.MultipleInstancesException;
import org.navalplanner.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.ws.common.impl.DateConverter;
import org.navalplanner.ws.resources.api.CriterionSatisfactionDTO;
import org.navalplanner.ws.resources.api.MachineDTO;
import org.navalplanner.ws.resources.api.ResourceDTO;
import org.navalplanner.ws.resources.api.ResourcesCostCategoryAssignmentDTO;
import org.navalplanner.ws.resources.api.WorkerDTO;

/**
 * Converter from/to resource-related entities to/from DTOs.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ResourceConverter {

    private ResourceConverter() {}

    public final static Resource toEntity(ResourceDTO resourceDTO)
        throws CreateUnvalidatedException {

        Resource resource;

        if (resourceDTO instanceof MachineDTO) {
            resource = createResourceWithBasicData((MachineDTO) resourceDTO);
        } else if (resourceDTO instanceof WorkerDTO) {
            resource = createResourceWithBasicData((WorkerDTO) resourceDTO);
        } else {
            throw new RuntimeException(
                _("Service does not manage resource of type: {0}",
                    resourceDTO.getClass().getName()));
        }

        addCriterionSatisfactions(resource,
            resourceDTO.criterionSatisfactions);
        setResourceCalendar(resource, resourceDTO.calendarName);
        addResourcesCostCategoryAssignments(resource,
            resourceDTO.resourcesCostCategoryAssignments);

        return resource;

    }

    private final static Machine createResourceWithBasicData(
        MachineDTO machineDTO) {

        return Machine.createUnvalidated
            (StringUtils.trim(machineDTO.code),
            StringUtils.trim(machineDTO.name),
            StringUtils.trim(machineDTO.description));

    }

    private final static Worker createResourceWithBasicData(
        WorkerDTO workerDTO) {

        return Worker.createUnvalidated(
            StringUtils.trim(workerDTO.code),
            StringUtils.trim(workerDTO.firstName),
            StringUtils.trim(workerDTO.surname),
            StringUtils.trim(workerDTO.nif));

    }

    private static void addCriterionSatisfactions(Resource resource,
        List<CriterionSatisfactionDTO> criterionSatisfactions)
        throws CreateUnvalidatedException {

        for (CriterionSatisfactionDTO criterionSatisfactionDTO :
            criterionSatisfactions) {

            CriterionSatisfaction criterionSatisfaction =
                toEntity(criterionSatisfactionDTO, resource);

            resource.addUnvalidatedSatisfaction(criterionSatisfaction);

        }

    }

    private static CriterionSatisfaction toEntity(
        CriterionSatisfactionDTO criterionSatisfactionDTO, Resource resource)
        throws CreateUnvalidatedException {

        return CriterionSatisfaction.createUnvalidated(
            StringUtils.trim(criterionSatisfactionDTO.code),
            StringUtils.trim(criterionSatisfactionDTO.criterionTypeName),
            StringUtils.trim(criterionSatisfactionDTO.criterionName),
            resource,
            DateConverter.toDate(criterionSatisfactionDTO.startDate),
            DateConverter.toDate(criterionSatisfactionDTO.endDate));

    }

    private static void setResourceCalendar(Resource resource,
        String calendarName) throws CreateUnvalidatedException {

        try {
            resource.setResourceCalendar(calendarName);
        } catch (InstanceNotFoundException e) {
             throw new CreateUnvalidatedException(
                _("{0}: calendar not found", calendarName));
        } catch (MultipleInstancesException e) {
            throw new CreateUnvalidatedException(
                _("there exist multiple calendars with name {0}",
                    calendarName));
        }

    }

    private static void addResourcesCostCategoryAssignments(
        Resource resource, List<ResourcesCostCategoryAssignmentDTO>
        resourcesCostCategoryAssignments)
        throws CreateUnvalidatedException {

        for (ResourcesCostCategoryAssignmentDTO assignmentDTO :
            resourcesCostCategoryAssignments) {

            ResourcesCostCategoryAssignment assignment = toEntity(assignmentDTO,
                resource);
            resource.addResourcesCostCategoryAssignment(assignment);

        }

    }

    private static ResourcesCostCategoryAssignment toEntity(
        ResourcesCostCategoryAssignmentDTO assignmentDTO, Resource resource)
        throws CreateUnvalidatedException {

        return ResourcesCostCategoryAssignment.createUnvalidated(
            assignmentDTO.code, assignmentDTO.costCategoryName, resource,
            DateConverter.toLocalDate(assignmentDTO.startDate),
            DateConverter.toLocalDate(assignmentDTO.endDate));

    }

}
