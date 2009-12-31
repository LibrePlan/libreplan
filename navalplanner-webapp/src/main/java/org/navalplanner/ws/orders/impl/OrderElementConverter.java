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

package org.navalplanner.ws.orders.impl;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.PredefinedMaterialCategories;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.ICriterionRequirable;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.ws.common.api.IncompatibleTypeException;
import org.navalplanner.ws.common.api.ResourceEnumDTO;
import org.navalplanner.ws.common.impl.ResourceEnumConverter;
import org.navalplanner.ws.orders.api.AdvanceMeasurementDTO;
import org.navalplanner.ws.orders.api.CriterionRequirementDTO;
import org.navalplanner.ws.orders.api.DirectCriterionRequirementDTO;
import org.navalplanner.ws.orders.api.HoursGroupDTO;
import org.navalplanner.ws.orders.api.IndirectCriterionRequirementDTO;
import org.navalplanner.ws.orders.api.LabelDTO;
import org.navalplanner.ws.orders.api.MaterialAssignmentDTO;
import org.navalplanner.ws.orders.api.OrderDTO;
import org.navalplanner.ws.orders.api.OrderElementDTO;
import org.navalplanner.ws.orders.api.OrderLineDTO;
import org.navalplanner.ws.orders.api.OrderLineGroupDTO;

/**
 * Converter from/to {@link OrderElement} entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class OrderElementConverter {

    private OrderElementConverter() {
    }

    public final static OrderElementDTO toDTO(OrderElement orderElement,
            ConfigurationOrderElementConverter configuration) {
        String name = orderElement.getName();
        String code = orderElement.getCode();
        Date initDate = orderElement.getInitDate();
        Date deadline = orderElement.getDeadline();
        String description = orderElement.getDescription();

        Set<LabelDTO> labels = new HashSet<LabelDTO>();
        if (configuration.isLabels()) {
            for (Label label : orderElement.getLabels()) {
                labels.add(toDTO(label));
            }
        }

        Set<MaterialAssignmentDTO> materialAssignments = new HashSet<MaterialAssignmentDTO>();
        if (configuration.isMaterialAssignments()) {
            for (MaterialAssignment materialAssignment : orderElement
                    .getMaterialAssignments()) {
                materialAssignments.add(toDTO(materialAssignment));
            }
        }

        Set<AdvanceMeasurementDTO> advanceMeasurements = new HashSet<AdvanceMeasurementDTO>();
        if (configuration.isAdvanceMeasurements()) {
            advanceMeasurements = toDTO(orderElement
                    .getReportGlobalAdvanceAssignment());
        }

        Set<CriterionRequirementDTO> criterionRequirements = new HashSet<CriterionRequirementDTO>();
        if (configuration.isCriterionRequirements()) {
            for (CriterionRequirement criterionRequirement : orderElement
                    .getCriterionRequirements()) {
                criterionRequirements.add(toDTO(criterionRequirement));
            }
        }

        if (orderElement instanceof OrderLine) {
            Set<HoursGroupDTO> hoursGroups = new HashSet<HoursGroupDTO>();
            if (configuration.isHoursGroups()) {
                for (HoursGroup hoursGroup : ((OrderLine) orderElement)
                        .getHoursGroups()) {
                    hoursGroups.add(toDTO(hoursGroup, configuration));
                }
            }

            return new OrderLineDTO(name, code, initDate, deadline,
                    description, labels, materialAssignments,
                    advanceMeasurements, criterionRequirements, hoursGroups);
        } else { // orderElement instanceof OrderLineGroup
            List<OrderElementDTO> children = new ArrayList<OrderElementDTO>();
            for (OrderElement element : orderElement.getChildren()) {
                children.add(toDTO(element, configuration));
            }

            if (orderElement instanceof Order) {
                Boolean dependenciesConstraintsHavePriority = ((Order) orderElement)
                        .getDependenciesConstraintsHavePriority();
                BaseCalendar calendar = ((Order) orderElement).getCalendar();
                String calendarName = null;
                if (calendar != null) {
                    calendarName = calendar.getName();
                }

                return new OrderDTO(name, code, initDate, deadline,
                        description, labels, materialAssignments,
                        advanceMeasurements, criterionRequirements, children,
                        dependenciesConstraintsHavePriority, calendarName);
            } else { // orderElement instanceof OrderLineGroup
                return new OrderLineGroupDTO(name, code, initDate, deadline,
                        description, labels, materialAssignments,
                        advanceMeasurements, criterionRequirements, children);
            }
        }
    }

    public static CriterionRequirementDTO toDTO(
            CriterionRequirement criterionRequirement) {
        String name = criterionRequirement.getCriterion().getName();
        String type = criterionRequirement.getCriterion().getType().getName();

        if (criterionRequirement instanceof IndirectCriterionRequirement) {
            boolean isValid = ((IndirectCriterionRequirement) criterionRequirement)
                    .isIsValid();
            return new IndirectCriterionRequirementDTO(name, type, isValid);
        } else { // criterionRequirement instanceof DirectCriterionRequirement
            return new DirectCriterionRequirementDTO(name, type);
        }
    }

    public final static Set<AdvanceMeasurementDTO> toDTO(
            DirectAdvanceAssignment advanceAssignment) {
        Set<AdvanceMeasurementDTO> advanceMeasurements = new HashSet<AdvanceMeasurementDTO>();

        if (advanceAssignment != null) {
            BigDecimal maxValue = advanceAssignment.getMaxValue();
            for (AdvanceMeasurement advanceMeasurement : advanceAssignment
                    .getAdvanceMeasurements()) {
                advanceMeasurements.add(toDTO(maxValue, advanceAssignment
                        .getAdvanceType().getPercentage(), advanceMeasurement));
            }
        }

        return advanceMeasurements;
    }

    public final static AdvanceMeasurementDTO toDTO(BigDecimal maxValue,
            boolean isPercentage, AdvanceMeasurement advanceMeasurement) {
        BigDecimal value;
        if (isPercentage) {
            value = advanceMeasurement.getValue();
        } else {
            value = advanceMeasurement.getValue().divide(maxValue,
                    RoundingMode.DOWN);
        }
        Date date = advanceMeasurement.getDate().toDateTimeAtStartOfDay()
                .toDate();

        return new AdvanceMeasurementDTO(date, value);
    }

    public final static MaterialAssignmentDTO toDTO(
            MaterialAssignment materialAssignment) {
        return new MaterialAssignmentDTO(materialAssignment.getMaterial()
                .getCode(), materialAssignment.getUnits(), materialAssignment
                .getUnitPrice(), materialAssignment.getEstimatedAvailability());
    }

    public final static LabelDTO toDTO(Label label) {
        return new LabelDTO(label.getName(), label.getType().getName());
    }

    public final static HoursGroupDTO toDTO(HoursGroup hoursGroup,
            ConfigurationOrderElementConverter configuration) {
        ResourceEnumDTO resourceType = ResourceEnumConverter.toDTO(hoursGroup
                .getResourceType());

        Set<CriterionRequirementDTO> criterionRequirements = new HashSet<CriterionRequirementDTO>();
        if (configuration.isCriterionRequirements()) {
            for (CriterionRequirement criterionRequirement : hoursGroup
                    .getCriterionRequirements()) {
                criterionRequirements.add(toDTO(criterionRequirement));
            }
        }

        return new HoursGroupDTO(hoursGroup.getCode(), resourceType, hoursGroup
                .getWorkingHours(), criterionRequirements);
    }

    public final static OrderElement toEntity(OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration) {
        OrderElement orderElement = toEntityExceptCriterionRequirements(
                orderElementDTO, configuration);
        if (configuration.isCriterionRequirements()) {
            addOrCriterionRequirements(orderElement, orderElementDTO);
        }
        return orderElement;
    }

    private static void addOrCriterionRequirements(OrderElement orderElement,
            OrderElementDTO orderElementDTO) {
        addOrCriterionRequirementsEntities(orderElement,
                orderElementDTO.criterionRequirements);

        if (orderElementDTO instanceof OrderLineDTO) {
            for (HoursGroupDTO hoursGroupDTO : ((OrderLineDTO) orderElementDTO).hoursGroups) {
                HoursGroup hoursGroup = ((OrderLine) orderElement)
                        .getHoursGroup(hoursGroupDTO.code);
                if (hoursGroup != null) {
                    addOrCriterionRequirementsEntities(hoursGroup,
                            hoursGroupDTO.criterionRequirements);
                }
            }
        } else { // orderElementDTO instanceof OrderLineGroupDTO
            for (OrderElementDTO childDTO : ((OrderLineGroupDTO) orderElementDTO).children) {
                OrderElement child = ((OrderLineGroup) orderElement)
                        .getOrderElement(childDTO.code);
                addOrCriterionRequirements(child, childDTO);
            }
        }
    }

    private static void addOrCriterionRequirementsEntities(
            ICriterionRequirable criterionRequirable,
            Set<CriterionRequirementDTO> criterionRequirements) {
        for (CriterionRequirementDTO criterionRequirementDTO : criterionRequirements) {
            Criterion criterion = getCriterion(criterionRequirementDTO.name,
                    criterionRequirementDTO.type);
            if (criterion != null) {
                if (criterionRequirementDTO instanceof DirectCriterionRequirementDTO) {
                    DirectCriterionRequirement directCriterionRequirement = getDirectCriterionRequirementByCriterion(
                            criterionRequirable, criterion);
                    if (directCriterionRequirement == null) {
                        criterionRequirable
                                .addCriterionRequirement(DirectCriterionRequirement
                                        .create(criterion));
                    }
                } else { // criterionRequirementDTO instanceof
                    // IndirectCriterionRequirementDTO
                    if (criterion != null) {
                        IndirectCriterionRequirement indirectCriterionRequirement = getIndirectCriterionRequirementByCriterion(
                                criterionRequirable, criterion);
                        if (indirectCriterionRequirement != null) {
                            indirectCriterionRequirement
                                    .setIsValid(((IndirectCriterionRequirementDTO) criterionRequirementDTO).isValid);
                        }
                    }
                }
            }
        }
    }

    private static DirectCriterionRequirement getDirectCriterionRequirementByCriterion(
            ICriterionRequirable criterionRequirable, Criterion criterion) {
        for (CriterionRequirement criterionRequirement : criterionRequirable
                .getCriterionRequirements()) {
            if (criterionRequirement instanceof DirectCriterionRequirement) {
                if (criterionRequirement.getCriterion().isEquivalent(criterion)) {
                    return (DirectCriterionRequirement) criterionRequirement;
                }
            }
        }
        return null;
    }

    private static IndirectCriterionRequirement getIndirectCriterionRequirementByCriterion(
            ICriterionRequirable criterionRequirable, Criterion criterion) {
        for (CriterionRequirement criterionRequirement : criterionRequirable
                .getCriterionRequirements()) {
            if (criterionRequirement instanceof IndirectCriterionRequirement) {
                if (criterionRequirement.getCriterion().isEquivalent(criterion)) {
                    return (IndirectCriterionRequirement) criterionRequirement;
                }
            }
        }
        return null;
    }

    private final static OrderElement toEntityExceptCriterionRequirements(
            OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration) {
        OrderElement orderElement;

        if (orderElementDTO instanceof OrderLineDTO) {
            orderElement = OrderLine.create();

            if (configuration.isHoursGroups()) {
                for (HoursGroupDTO hoursGroupDTO : ((OrderLineDTO) orderElementDTO).hoursGroups) {
                    HoursGroup hoursGroup = toEntity(hoursGroupDTO,
                            configuration);
                    ((OrderLine) orderElement).addHoursGroup(hoursGroup);
                }
            }
        } else { // orderElementDTO instanceof OrderLineGroupDTO
            List<OrderElement> children = new ArrayList<OrderElement>();
            for (OrderElementDTO element : ((OrderLineGroupDTO) orderElementDTO).children) {
                children.add(toEntity(element, configuration));
            }

            if (orderElementDTO instanceof OrderDTO) {
                orderElement = Order.create();

                ((Order) orderElement)
                        .setDependenciesConstraintsHavePriority(((OrderDTO) orderElementDTO).dependenciesConstraintsHavePriority);

                List<BaseCalendar> calendars = Registry.getBaseCalendarDAO()
                        .findByName(((OrderDTO) orderElementDTO).calendarName);
                BaseCalendar calendar;
                if ((calendars != null) && (calendars.size() == 1)) {
                    calendar = calendars.get(0);
                } else {
                    calendar = Registry.getConfigurationDAO()
                            .getConfiguration().getDefaultCalendar();
                }
                ((Order) orderElement).setCalendar(calendar);
            } else { // orderElementDTO instanceof OrderLineGroupDTO
                orderElement = OrderLineGroup.create();
            }

            for (OrderElement child : children) {
                ((OrderLineGroup) orderElement).add(child);
            }
        }

        orderElement.setName(orderElementDTO.name);
        orderElement.setCode(orderElementDTO.code);
        orderElement.setInitDate(orderElementDTO.initDate);
        orderElement.setDeadline(orderElementDTO.deadline);
        orderElement.setDescription(orderElementDTO.description);

        if (configuration.isLabels()) {
            for (LabelDTO labelDTO : orderElementDTO.labels) {
                orderElement.addLabel(toEntity(labelDTO));
            }
        }

        if (configuration.isMaterialAssignments()) {
            for (MaterialAssignmentDTO materialAssignmentDTO : orderElementDTO.materialAssignments) {
                orderElement
                        .addMaterialAssignment(toEntity(materialAssignmentDTO));
            }
        }

        if (configuration.isAdvanceMeasurements()) {
            addAdvanceMeasurements(orderElement, orderElementDTO);
        }

        return orderElement;
    }

    private static Criterion getCriterion(String name, String type) {
        List<Criterion> criterions = Registry.getCriterionDAO()
                .findByNameAndType(name, type);
        if (criterions.size() != 1) {
            return null;
        }
        return criterions.get(0);
    }

    public static DirectCriterionRequirement toEntity(
            DirectCriterionRequirementDTO criterionRequirementDTO) {
        Criterion criterion = getCriterion(criterionRequirementDTO.name,
                criterionRequirementDTO.type);
        if (criterion == null) {
            return null;
        }

        return DirectCriterionRequirement.create(criterion);
    }

    public final static MaterialAssignment toEntity(
            MaterialAssignmentDTO materialAssignmentDTO) {
        Material material = null;

        try {
            material = Registry.getMaterialDAO()
                    .findUniqueByCodeInAnotherTransaction(
                            materialAssignmentDTO.materialCode);
        } catch (InstanceNotFoundException e) {
            material = Material.create(materialAssignmentDTO.materialCode);

            MaterialCategory defaultMaterialCategory = PredefinedMaterialCategories.IMPORTED_MATERIALS_WITHOUT_CATEGORY
                    .getMaterialCategory();
            material.setCategory(defaultMaterialCategory);

            Registry.getMaterialDAO().save(material);
        }

        MaterialAssignment materialAssignment = MaterialAssignment
                .create(material);
        materialAssignment.setUnits(materialAssignmentDTO.units);
        materialAssignment.setUnitPrice(materialAssignmentDTO.unitPrice);
        materialAssignment
                .setEstimatedAvailability(materialAssignmentDTO.estimatedAvailability);
        return materialAssignment;
    }

    public final static Label toEntity(LabelDTO labelDTO) {
        LabelType labelType = null;
        try {
            labelType = Registry.getLabelTypeDAO().findUniqueByName(
                    labelDTO.type);
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        } catch (InstanceNotFoundException e) {
            labelType = LabelType.create(labelDTO.type);
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

    public final static HoursGroup toEntity(HoursGroupDTO hoursGroupDTO,
            ConfigurationOrderElementConverter configuration) {
        ResourceEnum resourceType = ResourceEnumConverter
                .fromDTO(hoursGroupDTO.resourceType);
        HoursGroup hoursGroup = HoursGroup.createUnvalidated(
                hoursGroupDTO.code, resourceType, hoursGroupDTO.workingHours);
        return hoursGroup;
    }

    public final static void update(OrderElement orderElement,
            OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration)
            throws IncompatibleTypeException {
        updateExceptCriterionRequirements(orderElement, orderElementDTO,
                configuration);
        if (configuration.isCriterionRequirements()) {
            addOrCriterionRequirements(orderElement, orderElementDTO);
        }
    }

    private final static void updateExceptCriterionRequirements(
            OrderElement orderElement, OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration)
            throws IncompatibleTypeException {

        if (orderElementDTO instanceof OrderLineDTO) {
            if (!(orderElement instanceof OrderLine)) {
                throw new IncompatibleTypeException(orderElement.getCode(),
                        OrderLine.class, orderElement.getClass());
            }

            if (configuration.isHoursGroups()) {
                for (HoursGroupDTO hoursGroupDTO : ((OrderLineDTO) orderElementDTO).hoursGroups) {
                    if (((OrderLine) orderElement)
                            .containsHoursGroup(hoursGroupDTO.code)) {
                        update(((OrderLine) orderElement)
                                .getHoursGroup(hoursGroupDTO.code),
                                hoursGroupDTO, configuration);
                    } else {
                        ((OrderLine) orderElement)
                                .addHoursGroup(toEntity(
                                hoursGroupDTO, configuration));
                    }
                }
            }
        } else { // orderElementDTO instanceof OrderLineGroupDTO
            if (orderElementDTO instanceof OrderDTO) {
                if (!(orderElement instanceof Order)) {
                    throw new IncompatibleTypeException(orderElement.getCode(),
                            Order.class, orderElement.getClass());
                }

                Boolean dependenciesConstraintsHavePriority = ((OrderDTO) orderElementDTO).dependenciesConstraintsHavePriority;
                if (dependenciesConstraintsHavePriority != null) {
                    ((Order) orderElement)
                            .setDependenciesConstraintsHavePriority(dependenciesConstraintsHavePriority);
                }

                String calendarName = ((OrderDTO) orderElementDTO).calendarName;
                if (calendarName != null) {
                    if (!((Order) orderElement).getCalendar().getName().equals(
                            calendarName)) {
                        List<BaseCalendar> calendars = Registry
                                .getBaseCalendarDAO()
                                .findByName(
                                        ((OrderDTO) orderElementDTO).calendarName);
                        if (calendars.size() == 1) {
                            ((Order) orderElement)
                                    .setCalendar(calendars.get(0));
                        }
                    }
                }
            } else { // orderElementDTO instanceof OrderLineGroupDTO
                if (!(orderElement instanceof OrderLineGroup)) {
                    throw new IncompatibleTypeException(orderElement.getCode(),
                            OrderLineGroup.class, orderElement.getClass());
                }
            }

            for (OrderElementDTO childDTO : ((OrderLineGroupDTO) orderElementDTO).children) {
                if (orderElement.containsOrderElement(childDTO.code)) {
                    update(orderElement.getOrderElement(childDTO.code),
                            childDTO, configuration);
                } else {
                    ((OrderLineGroup) orderElement).add(toEntity(childDTO,
                            configuration));
                }
            }

        }

        if (configuration.isLabels()) {
            for (LabelDTO labelDTO : orderElementDTO.labels) {
                if (!orderElement.containsLabel(labelDTO.name, labelDTO.type)) {
                    orderElement.addLabel(toEntity(labelDTO));
                }
            }
        }

        if (configuration.isMaterialAssignments()) {
            for (MaterialAssignmentDTO materialAssignmentDTO : orderElementDTO.materialAssignments) {
                if (orderElement
                        .containsMaterialAssignment(materialAssignmentDTO.materialCode)) {
                    update(
                            orderElement
                                    .getMaterialAssignment(materialAssignmentDTO.materialCode),
                            materialAssignmentDTO);
                } else {
                    orderElement
                            .addMaterialAssignment(toEntity(materialAssignmentDTO));
                }
            }
        }

        if (configuration.isAdvanceMeasurements()) {
            addAdvanceMeasurements(orderElement, orderElementDTO);
        }

        if (orderElementDTO.name != null) {
            orderElement.setName(orderElementDTO.name);
        }

        if (orderElementDTO.initDate != null) {
            orderElement.setInitDate(orderElementDTO.initDate);
        }

        if (orderElementDTO.deadline != null) {
            orderElement.setDeadline(orderElementDTO.deadline);
        }

        if (orderElementDTO.description != null) {
            orderElement.setDescription(orderElementDTO.description);
        }

    }

    public final static void update(HoursGroup hoursGroup,
            HoursGroupDTO hoursGroupDTO,
            ConfigurationOrderElementConverter configuration) {
        if (!hoursGroup.getCode().equals(hoursGroupDTO.code)) {
            throw new RuntimeException(
                    _("Not the same hours group, impossible to update"));
        }

        if (hoursGroupDTO.workingHours != null) {
            hoursGroup.setWorkingHours(hoursGroupDTO.workingHours);
        }

        if (hoursGroupDTO.resourceType != null) {
            hoursGroup.setResourceType(ResourceEnumConverter
                    .fromDTO(hoursGroupDTO.resourceType));
        }
    }

    public final static void update(MaterialAssignment materialAssignment,
            MaterialAssignmentDTO materialAssignmentDTO) {
        if (!materialAssignment.getMaterial().getCode().equals(
                materialAssignmentDTO.materialCode)) {
            throw new RuntimeException(_("Not the same material, impossible to update"));
        }

        if (materialAssignmentDTO.units != null) {
            materialAssignment.setUnits(materialAssignmentDTO.units);
        }
        if (materialAssignmentDTO.unitPrice != null) {
            materialAssignment.setUnitPrice(materialAssignmentDTO.unitPrice);
        }
        if (materialAssignmentDTO.estimatedAvailability != null) {
            materialAssignment
                    .setEstimatedAvailability(materialAssignmentDTO.estimatedAvailability);
        }
    }

    private static void addAdvanceMeasurements(OrderElement orderElement,
            OrderElementDTO orderElementDTO) {
        if (!orderElementDTO.advanceMeasurements.isEmpty()) {
            DirectAdvanceAssignment directAdvanceAssignment = getDirectAdvanceAssignmentSubcontractor(orderElement);

            for (AdvanceMeasurementDTO advanceMeasurementDTO : orderElementDTO.advanceMeasurements) {
                AdvanceMeasurement advanceMeasurement = null;
                LocalDate date = null;
                if (advanceMeasurementDTO.date != null) {
                    date = new LocalDate(advanceMeasurementDTO.date);
                    advanceMeasurement = directAdvanceAssignment
                            .getAdvanceMeasurementAtExactDate(date);
                }

                if (advanceMeasurement == null) {
                    advanceMeasurement = AdvanceMeasurement.create(date,
                            advanceMeasurementDTO.value);
                    directAdvanceAssignment
                            .addAdvanceMeasurements(advanceMeasurement);
                } else {
                    advanceMeasurement.setValue(advanceMeasurementDTO.value);
                }
            }
        }
    }

    private static DirectAdvanceAssignment getDirectAdvanceAssignmentSubcontractor(
            OrderElement orderElement) {
        DirectAdvanceAssignment directAdvanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        if (directAdvanceAssignment == null) {
            try {
                directAdvanceAssignment = orderElement
                        .addSubcontractorAdvanceAssignment();
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                throw new RuntimeException(e);
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                throw new RuntimeException(e);
            }
        }
        return directAdvanceAssignment;
    }

}