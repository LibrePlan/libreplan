/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.common.impl;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.materials.bootstrap.PredefinedMaterialCategories;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
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
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.ws.common.api.AdvanceMeasurementDTO;
import org.navalplanner.ws.common.api.CriterionRequirementDTO;
import org.navalplanner.ws.common.api.DirectCriterionRequirementDTO;
import org.navalplanner.ws.common.api.HoursGroupDTO;
import org.navalplanner.ws.common.api.IndirectCriterionRequirementDTO;
import org.navalplanner.ws.common.api.LabelReferenceDTO;
import org.navalplanner.ws.common.api.MaterialAssignmentDTO;
import org.navalplanner.ws.common.api.OrderDTO;
import org.navalplanner.ws.common.api.OrderElementDTO;
import org.navalplanner.ws.common.api.OrderLineDTO;
import org.navalplanner.ws.common.api.OrderLineGroupDTO;
import org.navalplanner.ws.common.api.ResourceEnumDTO;

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
        XMLGregorianCalendar initDate = DateConverter
                .toXMLGregorianCalendar(orderElement.getInitDate());
        XMLGregorianCalendar deadline = DateConverter
                .toXMLGregorianCalendar(orderElement.getDeadline());
        String description = orderElement.getDescription();

        Set<LabelReferenceDTO> labels = new HashSet<LabelReferenceDTO>();
        if (configuration.isLabels()) {
            for (Label label : orderElement.getLabels()) {
                labels.add(LabelReferenceConverter.toDTO(label));
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
                    .isValid();
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
        XMLGregorianCalendar date = DateConverter
                .toXMLGregorianCalendar(advanceMeasurement.getDate());
        return new AdvanceMeasurementDTO(date, value);
    }

    public final static MaterialAssignmentDTO toDTO(
            MaterialAssignment materialAssignment) {

        XMLGregorianCalendar estimatedAvailability = DateConverter
                .toXMLGregorianCalendar(materialAssignment
                        .getEstimatedAvailability());

        return new MaterialAssignmentDTO(materialAssignment.getMaterial()
                .getCode(), materialAssignment.getUnits(), materialAssignment
                .getUnitPrice(), estimatedAvailability);
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
            ConfigurationOrderElementConverter configuration)
            throws ValidationException {
        return toEntity(null, orderElementDTO, configuration);
    }

    public final static OrderElement toEntity(OrderVersion orderVersion,
            OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration) {

        if (orderVersion == null) {
            Scenario current = Registry.getScenarioManager().getCurrent();
            orderVersion = OrderVersion.createInitialVersion(current);
        }
        OrderElement orderElement = toEntityExceptCriterionRequirements(
                orderVersion, orderElementDTO, configuration);

        // FIXME Review why this validation is needed here, it breaks the
        // subcontract service. This was introduced at commit 341145a5
        // Validate OrderElement.code and HoursGroup.code must be unique
        // Order.checkConstraintOrderUniqueCode(orderElement);
        // HoursGroup.checkConstraintHoursGroupUniqueCode(orderElement);

        if (configuration.isCriterionRequirements()) {
            addOrCriterionRequirements(orderElement, orderElementDTO);
        }

        return orderElement;
    }

    private static void checkOrderElementDTOCode(
            OrderElementDTO orderElementDTO,
            String instance) {
        if (orderElementDTO.code == null) {
            throw new ValidationException(instance + _(": code not found"));
        }
    }

    private static void addOrCriterionRequirements(OrderElement orderElement,
            OrderElementDTO orderElementDTO) {
        addOrCriterionRequirementsEntities(orderElement,
                orderElementDTO.criterionRequirements);

        if (orderElement != null) {
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
                        try {
                            criterionRequirable
                                .addCriterionRequirement(DirectCriterionRequirement
                                        .create(criterion));
                        } catch (IllegalStateException e) {
                            throw new ValidationException(e.getMessage());
                        }
                    }
                } else { // criterionRequirementDTO instanceof
                    // IndirectCriterionRequirementDTO
                    IndirectCriterionRequirement indirectCriterionRequirement = getIndirectCriterionRequirementByCriterion(
                            criterionRequirable, criterion);
                    if (indirectCriterionRequirement != null) {
                        indirectCriterionRequirement
                                .setValid(((IndirectCriterionRequirementDTO) criterionRequirementDTO).valid);
                    }
                }
            } else {
                if (criterionRequirementDTO.name == null
                        || criterionRequirementDTO.type == null) {
                    throw new ValidationException(
                            "the criterion format is incorrect");
                } else {
                    throw new ValidationException("the criterion "
                        + criterionRequirementDTO.name + " which type is "
                        + criterionRequirementDTO.type + " not found");
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
            OrderVersion parentOrderVersion,
            OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration)
            throws ValidationException {

        Validate.notNull(parentOrderVersion);
        OrderElement orderElement;

        if (orderElementDTO instanceof OrderLineDTO) {
            checkOrderElementDTOCode(orderElementDTO, "OrderLineDTO");
            if ((configuration.isHoursGroups())
                    && (!((OrderLineDTO) orderElementDTO).hoursGroups.isEmpty())) {
                orderElement = OrderLine
                        .createUnvalidated(orderElementDTO.code);
                for (HoursGroupDTO hoursGroupDTO : ((OrderLineDTO) orderElementDTO).hoursGroups) {
                    HoursGroup hoursGroup = toEntity(hoursGroupDTO,
                            configuration);
                    ((OrderLine) orderElement).addHoursGroup(hoursGroup);
                }
            } else {
                orderElement = OrderLine
                        .createUnvalidatedWithUnfixedPercentage(
                                orderElementDTO.code, 0);
                if (!orderElement.getHoursGroups().isEmpty()) {
                    orderElement.getHoursGroups().get(0).setCode(
                            UUID.randomUUID().toString());
                }
            }
        } else { // orderElementDTO instanceof OrderLineGroupDTO

            if (orderElementDTO instanceof OrderDTO) {
                checkOrderElementDTOCode(orderElementDTO, "OrderDTO");
                orderElement = Order.createUnvalidated(orderElementDTO.code);
                Scenario current = Registry.getScenarioManager().getCurrent();
                ((Order) orderElement).setVersionForScenario(current,
                        parentOrderVersion);
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
                checkOrderElementDTOCode(orderElementDTO, "OrderLineGroupDTO");
                orderElement = OrderLineGroup
                        .createUnvalidated(orderElementDTO.code);
            }
            orderElement.useSchedulingDataFor(parentOrderVersion);
            List<OrderElement> children = new ArrayList<OrderElement>();
            for (OrderElementDTO element : ((OrderLineGroupDTO) orderElementDTO).children) {
                children.add(toEntity(parentOrderVersion, element,
                        configuration));
            }

            for (OrderElement child : children) {
                ((OrderLineGroup) orderElement).add(child);
            }
        }

        orderElement.setName(orderElementDTO.name);
        orderElement.setCode(orderElementDTO.code);
        orderElement
                .setInitDate(DateConverter.toDate(orderElementDTO.initDate));
        orderElement
                .setDeadline(DateConverter.toDate(orderElementDTO.deadline));
        orderElement.setDescription(orderElementDTO.description);

        if (configuration.isLabels()) {
            for (LabelReferenceDTO labelDTO : orderElementDTO.labels) {
                try {
                orderElement.addLabel(LabelReferenceConverter.toEntity(labelDTO));
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException("Label " + labelDTO.code
                            + " not found.");
                }
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

            /*
             * "validate" method avoids that "material" goes to the Hibernate's
             * session if "material" is not valid.
             */
            material.validate();
            Registry.getMaterialDAO().save(material);
            material.dontPoseAsTransientObjectAnymore();
        }

        MaterialAssignment materialAssignment = MaterialAssignment
                .create(material);
        materialAssignment
                .setUnitsWithoutNullCheck(materialAssignmentDTO.units);
        materialAssignment
                .setUnitPriceWithoutNullCheck(materialAssignmentDTO.unitPrice);

        Date estimatedAvailability = DateConverter
                .toDate(materialAssignmentDTO.estimatedAvailability);
        materialAssignment.setEstimatedAvailability(estimatedAvailability);

        return materialAssignment;
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
            throws ValidationException {
        updateExceptCriterionRequirements(orderElement, orderElementDTO,
                configuration);
        if (configuration.isCriterionRequirements()) {
            addOrCriterionRequirements(orderElement, orderElementDTO);
        }
    }

    private final static void updateExceptCriterionRequirements(
            OrderElement orderElement, OrderElementDTO orderElementDTO,
            ConfigurationOrderElementConverter configuration)
            throws ValidationException {

        if (orderElementDTO instanceof OrderLineDTO) {
            if (!(orderElement instanceof OrderLine)) {
                throw new ValidationException(_(
                        "Task {0} : Task group is incompatible type with {1}"
                                + orderElement.getCode(), orderElement
                                .getClass().getName()));
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
                    throw new ValidationException(_(
                            "Task {0} : Project is incompatible type with {1}"
                                    + orderElement.getCode(), orderElement
                                    .getClass().getName()));

                }
                Order order = (Order) orderElement;
                OrderVersion orderVersion = order.getOrderVersionFor(Registry
                        .getScenarioManager()
                        .getCurrent());
                order.useSchedulingDataFor(orderVersion);
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
                    throw new ValidationException(_(
                            "Task {0} : Task group is incompatible type with {1}"
                                    + orderElement.getCode(), orderElement
                                    .getClass().getName()));
                }
            }

            for (OrderElementDTO childDTO : ((OrderLineGroupDTO) orderElementDTO).children) {
                if (orderElement.containsOrderElement(childDTO.code)) {
                    update(orderElement.getOrderElement(childDTO.code),
                            childDTO, configuration);
                } else {
                    if (checkConstraintUniqueOrderCode(orderElementDTO)) {
                        throw new ValidationException(
                                _("Task {0} : Duplicate code in DB"
                                        + orderElementDTO.code));
                    }
                    if (checkConstraintUniqueHoursGroupCode(orderElementDTO)) {
                        throw new ValidationException(
                                _("Hours Group {0} : Duplicate code in DB"
                                        + orderElementDTO.code));
                    }
                    ((OrderLineGroup) orderElement).add(toEntity(childDTO,
                            configuration));
                }
            }

        }

        if (configuration.isLabels()) {
            for (LabelReferenceDTO labelDTO : orderElementDTO.labels) {
                if (!orderElement.containsLabel(labelDTO.code)) {
                    try {
                        orderElement.addLabel(LabelReferenceConverter
                                .toEntity(labelDTO));
                    } catch (InstanceNotFoundException e) {
                        throw new ValidationException("Label " + labelDTO.code
                                + " not found");
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException(e.getMessage());
                    }
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
            orderElement.setInitDate(DateConverter
                    .toDate(orderElementDTO.initDate));
        }

        if (orderElementDTO.deadline != null) {
            orderElement.setDeadline(DateConverter
                    .toDate(orderElementDTO.deadline));
        }

        if (orderElementDTO.description != null) {
            orderElement.setDescription(orderElementDTO.description);
        }

    }

    /**
     * Returns true is there's another {@link OrderElement} in DB with the same code
     *
     * @param orderElement
     * @return
     */
    private static boolean checkConstraintUniqueOrderCode(OrderElementDTO orderElement) {
        try {
            OrderElement existsByCode = Registry.getOrderElementDAO()
                    .findByCode(orderElement.code);
            return existsByCode != null;
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns true if there's another {@link HoursGroup} in DB with the same code
     *
     * @param orderElement
     * @return
     */
    private static boolean checkConstraintUniqueHoursGroupCode(OrderElementDTO orderElement) {
        if (orderElement instanceof OrderLineDTO) {
            return checkConstraintUniqueHoursGroupCode((OrderLineDTO) orderElement);
        }
        return false;
    }

    private static boolean checkConstraintUniqueHoursGroupCode(OrderLineDTO orderLine) {
        try {
            IHoursGroupDAO hoursGroupDAO = Registry.getHoursGroupDAO();
            Set<HoursGroupDTO> hoursGroups = orderLine.hoursGroups;
            for (HoursGroupDTO each: hoursGroups) {
                HoursGroup hoursGroup = hoursGroupDAO.findByCodeAnotherTransaction(each.code);
                if (hoursGroup != null) {
                    return true;
                }
            }
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static void update(HoursGroup hoursGroup,
            HoursGroupDTO hoursGroupDTO,
            ConfigurationOrderElementConverter configuration) {
        if (!hoursGroup.getCode().equals(hoursGroupDTO.code)) {
            throw new ValidationException(
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
            throw new ValidationException(
                    _("Not the same material, impossible to update"));
        }

        if (materialAssignmentDTO.units != null) {
            materialAssignment.setUnits(materialAssignmentDTO.units);
        }
        if (materialAssignmentDTO.unitPrice != null) {
            materialAssignment.setUnitPrice(materialAssignmentDTO.unitPrice);
        }
        if (materialAssignmentDTO.estimatedAvailability != null) {
            Date estimatedAvailability = DateConverter
                    .toDate(materialAssignmentDTO.estimatedAvailability);
            materialAssignment.setEstimatedAvailability(estimatedAvailability);
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
                    date = new LocalDate(DateConverter
                            .toLocalDate(advanceMeasurementDTO.date));
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
                throw new ValidationException(
                        _("Duplicate value true report global progress for task"
                                + orderElement.getCode()));
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                throw new ValidationException(
                        _("Duplicate progress assignment for task "
                                + orderElement.getCode()));
            }
        }
        return directAdvanceAssignment;
    }

    public static AdvanceMeasurement toEntity(
            AdvanceMeasurementDTO advanceMeasurementDTO) {
        LocalDate date = DateConverter.toLocalDate(advanceMeasurementDTO.date);
        AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create(date,
                        advanceMeasurementDTO.value);
        return advanceMeasurement;
    }

    public static AdvanceMeasurementDTO toDTO(
            AdvanceMeasurement advanceMeasurement) {
        XMLGregorianCalendar date = DateConverter
                .toXMLGregorianCalendar(advanceMeasurement.getDate());
        return new AdvanceMeasurementDTO(date, advanceMeasurement
                .getValue());
    }

}
