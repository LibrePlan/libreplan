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

package org.navalplanner.web.test.ws.orders;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.annotation.Resource;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.ws.common.api.ConstraintViolationDTO;
import org.navalplanner.ws.common.api.IncompatibleTypeException;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.ResourceEnumDTO;
import org.navalplanner.ws.orders.api.AdvanceMeasurementDTO;
import org.navalplanner.ws.orders.api.HoursGroupDTO;
import org.navalplanner.ws.orders.api.IOrderElementService;
import org.navalplanner.ws.orders.api.LabelDTO;
import org.navalplanner.ws.orders.api.MaterialAssignmentDTO;
import org.navalplanner.ws.orders.api.OrderDTO;
import org.navalplanner.ws.orders.api.OrderLineDTO;
import org.navalplanner.ws.orders.api.OrderLineGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link IOrderElementService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE})
@Transactional
public class OrderElementServiceTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap materialCategoryBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
        materialCategoryBootstrap.loadRequiredData();
    }

    @Autowired
    private IOrderElementService orderElementService;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Test
    public void invalidOrderWithoutAttributes() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, name. Check constraints:
        // checkConstraintOrderMustHaveStartDate
        assertThat(constraintViolations.size(), equalTo(3));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutNameAndInitDate() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.code = "order-code";

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: name. Check constraints:
        // checkConstraintOrderMustHaveStartDate
        assertThat(constraintViolations.size(), equalTo(2));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutCodeAndInitDate() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code. Check constraints:
        // checkConstraintOrderMustHaveStartDate
        assertThat(constraintViolations.size(), equalTo(2));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutCodeAndName() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.initDate = new Date();

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, name
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(
                    equalTo("Order::code"), equalTo("Order::name")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrder() {
        String code = "order-code";
        int previous = orderElementDAO.findByCode(code).size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderElementDAO.findByCode(code).size(),
                equalTo(previous + 1));
    }

    @Test
    public void orderWithInvalidOrderLine() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, name. Check constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(3));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineWithoutHoursGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Check constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineWithInvalidHoursGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO();
        hoursGroupDTO.resourceType = ResourceEnumDTO.WORKER;
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: name, workingHours
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(
                    equalTo("HoursGroup::code"),
                    equalTo("HoursGroup::workingHours")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithOrderLine() {
        String code = "order-code";
        int previous = orderElementDAO.findByCode(code).size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group",
                ResourceEnumDTO.WORKER, 1000);
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderElementDAO.findByCode(code).size(),
                equalTo(previous + 1));
    }

    @Test
    public void orderWithInvalidOrderLineGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderDTO.children.add(orderLineGroupDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, name. Check constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(3));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineGroupWithoutHoursGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderLineGroupDTO.name = "Order line group";
        orderLineGroupDTO.code = "order-line-group-code";
        orderDTO.children.add(orderLineGroupDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Check constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithOrderLineGroup() {
        String code = "order-code";
        int previous = orderElementDAO.findByCode(code).size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderLineGroupDTO.name = "Order line group";
        orderLineGroupDTO.code = "order-line-group-code";

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group",
                ResourceEnumDTO.WORKER, 1000);
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderLineGroupDTO.children.add(orderLineDTO);

        orderDTO.children.add(orderLineGroupDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderElementDAO.findByCode(code).size(),
                equalTo(previous + 1));
    }

    @Ignore
    @Test
    public void orderWithInvalidMaterialAssignment() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: material, units, unitPrice
        assertThat(constraintViolations.size(), equalTo(3));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithInvalidMaterialAssignmentWithoutUnitsAndUnitPrice() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        materialAssignmentDTO.materialCode = "material-code";
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: units, unitPrice
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(
                    equalTo("MaterialAssignment::units"),
                    equalTo("MaterialAssignment::unitPrice")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithMaterialAssignment() {
        String code = "order-code";
        int previous = orderElementDAO.findByCode(code).size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        materialAssignmentDTO.materialCode = "material-code";
        materialAssignmentDTO.unitPrice = BigDecimal.TEN;
        materialAssignmentDTO.units = 100.0;
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderElementDAO.findByCode(code).size(),
                equalTo(previous + 1));
    }

    @Test
    public void orderWithInvalidLabel() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        LabelDTO labelDTO = new LabelDTO();
        orderDTO.labels.add(labelDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: material, units, unitPrice
        assertThat(constraintViolations.size(), equalTo(1));
        assertThat(constraintViolations.get(0).fieldName,
                equalTo("LabelType::name"));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithInvalidLabelWithoutName() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        LabelDTO labelDTO = new LabelDTO();
        labelDTO.type = "Label type";
        orderDTO.labels.add(labelDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: material, units, unitPrice
        assertThat(constraintViolations.size(), equalTo(1));
        assertThat(constraintViolations.get(0).fieldName,
                equalTo("Label::name"));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithLabel() {
        String code = "order-code";
        int previous = orderElementDAO.findByCode(code).size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        LabelDTO labelDTO = new LabelDTO();
        labelDTO.name = "Label name";
        labelDTO.type = "Label type";
        orderDTO.labels.add(labelDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderElementDAO.findByCode(code).size(),
                equalTo(previous + 1));
    }

    @Test
    public void orderWithLabelRepeatedInTheSameBranch() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = "order-code";
        orderDTO.initDate = new Date();

        LabelDTO labelDTO = new LabelDTO();
        labelDTO.name = "Label name";
        labelDTO.type = "Label type";
        orderDTO.labels.add(labelDTO);

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group",
                ResourceEnumDTO.WORKER, 1000);
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderLineDTO.labels.add(labelDTO);
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: checkConstraintLabelNotRepeatedInTheSameBranch
        assertThat(constraintViolations.size(), equalTo(1));
        assertNull(constraintViolations.get(0).fieldName);

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void updateLabels() throws InstanceNotFoundException,
            IncompatibleTypeException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        LabelDTO labelDTO = new LabelDTO("Label name", "Label type");
        orderDTO.labels.add(labelDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);
        assertThat(orderElement.getLabels().size(), equalTo(1));

        LabelDTO labelDTO2 = new LabelDTO("Label name2", "Label type");
        orderDTO.labels.add(labelDTO2);
        instanceConstraintViolationsList = orderElementService
                .updateOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        orderElement = orderElementDAO.findUniqueByCode(code);
        assertThat(orderElement.getLabels().size(), equalTo(2));
        for (Label label : orderElement.getLabels()) {
            assertThat(label.getName(), anyOf(equalTo("Label name"),
                    equalTo("Label name2")));
            assertThat(label.getType().getName(), equalTo("Label type"));
        }
    }

    @Test
    public void updateMaterialAssignment() throws InstanceNotFoundException,
            IncompatibleTypeException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO(
                "material-code", 100.0, BigDecimal.TEN, null);
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);
        assertThat(orderElement.getMaterialAssignments().size(), equalTo(1));

        orderDTO.materialAssignments.iterator().next().units = 150.0;

        MaterialAssignmentDTO materialAssignmentDTO2 = new MaterialAssignmentDTO(
                "material-code2", 200.0, BigDecimal.ONE, null);
        orderDTO.materialAssignments.add(materialAssignmentDTO2);

        instanceConstraintViolationsList = orderElementService
                .updateOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        orderElement = orderElementDAO.findUniqueByCode(code);
        assertThat(orderElement.getMaterialAssignments().size(), equalTo(2));
        for (MaterialAssignment materialAssignment : orderElement
                .getMaterialAssignments()) {
            assertThat(materialAssignment.getMaterial().getCode(), anyOf(
                    equalTo("material-code"), equalTo("material-code2")));
            assertThat(materialAssignment.getUnits(), anyOf(equalTo(150.0),
                    equalTo(200.0)));
            assertThat(materialAssignment.getUnitPrice(), anyOf(
                    equalTo(BigDecimal.TEN), equalTo(BigDecimal.ONE)));
        }
    }

    @Test
    public void updateHoursGroup() throws InstanceNotFoundException,
            IncompatibleTypeException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group",
                ResourceEnumDTO.WORKER, 1000);
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);

        OrderLine orderLine = (OrderLine) orderElementDAO
                .findUniqueByCode("order-line-code");
        assertNotNull(orderLine);
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));

        orderLineDTO.hoursGroups.iterator().next().workingHours = 1500;
        HoursGroupDTO hoursGroupDTO2 = new HoursGroupDTO("hours-group2",
                ResourceEnumDTO.WORKER, 2000);
        orderLineDTO.hoursGroups.add(hoursGroupDTO2);

        instanceConstraintViolationsList = orderElementService
                .updateOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);

        orderLine = (OrderLine) orderElementDAO
                .findUniqueByCode("order-line-code");
        assertNotNull(orderLine);
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));

        for (HoursGroup hoursGroup : orderLine.getHoursGroups()) {
            assertThat(hoursGroup.getCode(), anyOf(equalTo("hours-group"),
                    equalTo("hours-group2")));
            assertThat(hoursGroup.getWorkingHours(), anyOf(
                    equalTo(1500), equalTo(2000)));
            assertThat(hoursGroup.getResourceType(),
                    equalTo(ResourceEnum.WORKER));
        }
    }

    @Test
    public void invalidOrderWithInvalidAdvanceMeasurements()
            throws InstanceNotFoundException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO();
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: date, value
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(
                    equalTo("AdvanceMeasurement::date"),
                    equalTo("AdvanceMeasurement::value")));
        }

        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order shouldn't be stored");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }
    }

    @Test
    public void validOrderWithAdvanceMeasurements()
            throws InstanceNotFoundException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO(
                new Date(), BigDecimal.TEN);
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);
        DirectAdvanceAssignment advanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        assertThat(advanceAssignment.getAdvanceMeasurements().size(),
                equalTo(1));
    }

    @Test
    public void updateAdvanceMeasurements() throws InstanceNotFoundException,
            IncompatibleTypeException {
        String code = "order-code";
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = new Date();

        LocalDate date = new LocalDate();
        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO(
                date.toDateTimeAtStartOfDay().toDate(), new BigDecimal(15));
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);
        DirectAdvanceAssignment advanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        assertThat(advanceAssignment.getAdvanceMeasurements().size(),
                equalTo(1));

        AdvanceMeasurementDTO advanceMeasurementDTO2 = new AdvanceMeasurementDTO(
                date.plusWeeks(1).toDateTimeAtStartOfDay().toDate(),
                new BigDecimal(20));
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO2);
        instanceConstraintViolationsList = orderElementService
                .updateOrder(orderDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        orderElement = orderElementDAO.findUniqueByCode(code);
        assertNotNull(orderElement);
        advanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssignment
                .getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(2));
        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            assertThat(advanceMeasurement.getDate(), anyOf(equalTo(date),
                    equalTo(date.plusWeeks(1))));
            assertThat(advanceMeasurement.getValue(), anyOf(equalTo(new BigDecimal(15)),
                    equalTo(new BigDecimal(20))));
        }
    }

}