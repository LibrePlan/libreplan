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

package org.libreplan.web.test.ws.orders;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.libreplan.web.test.ws.common.Util.mustEnd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.PredefinedCriterionTypes;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.scenarios.bootstrap.IScenariosBootstrap;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.web.orders.OrderElementTreeModelTest;
import org.libreplan.ws.common.api.AdvanceMeasurementDTO;
import org.libreplan.ws.common.api.ConstraintViolationDTO;
import org.libreplan.ws.common.api.CriterionRequirementDTO;
import org.libreplan.ws.common.api.DirectCriterionRequirementDTO;
import org.libreplan.ws.common.api.HoursGroupDTO;
import org.libreplan.ws.common.api.IncompatibleTypeException;
import org.libreplan.ws.common.api.IndirectCriterionRequirementDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.LabelReferenceDTO;
import org.libreplan.ws.common.api.MaterialAssignmentDTO;
import org.libreplan.ws.common.api.OrderDTO;
import org.libreplan.ws.common.api.OrderLineDTO;
import org.libreplan.ws.common.api.OrderLineGroupDTO;
import org.libreplan.ws.common.api.ResourceEnumDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.orders.api.IOrderElementService;
import org.libreplan.ws.orders.api.OrderListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
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
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class OrderElementServiceTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap materialCategoryBootstrap;

    @Resource
    private IDataBootstrap unitTypeBootstrap;

    @Resource
    private IDataBootstrap criterionsBootstrap;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Before
    public void loadRequiredaData() {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                OrderElementTreeModelTest.cleanCriteria(workReportDAO,
                        resourceDAO, criterionTypeDAO);
                return null;
            }
        });
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                configurationBootstrap.loadRequiredData();
                materialCategoryBootstrap.loadRequiredData();
                criterionsBootstrap.loadRequiredData();
                unitTypeBootstrap.loadRequiredData();
                defaultAdvanceTypesBootstrapListener.loadRequiredData();
                scenariosBootstrap.loadRequiredData();
                return null;
            }
        });
    }

    @Autowired
    private IOrderElementService orderElementService;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private Label givenLabelStored() {
        Label label = Label.create("label-code-" + UUID.randomUUID(),
                "labelName " + UUID.randomUUID().toString());

        LabelType labelType = LabelType.create("label-type-"
                + UUID.randomUUID());
        labelType.addLabel(label);

        labelTypeDAO.save(labelType);
        labelTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(labelType);
        sessionFactory.getCurrentSession().evict(label);

        labelType.dontPoseAsTransientObjectAnymore();
        label.dontPoseAsTransientObjectAnymore();
        return label;
    }

    @Test
    public void invalidOrderWithoutCode() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;

        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutAttributes() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: infoComponent.code, infoComponent.name. Check
        // constraints:
        // checkConstraintOrderMustHaveStartDate

        assertThat(constraintViolations.size(), equalTo(2));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutNameAndInitDate() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: name. Check constraints:
        // checkConstraintOrderMustHaveStartDate
        assertThat(constraintViolations.size(), equalTo(2));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutInitDate() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, infoComponentCode. Check constraints:
        // checkConstraintOrderMustHaveStartDate
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidOrderWithoutName() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code,infoComponent.code, infoComponent.name
        assertThat(constraintViolations.size(), equalTo(1));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(mustEnd("code"),
                    mustEnd("name")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrder() {
        String code = "order-code " + UUID.randomUUID().toString();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

    }

    @Test
    public void orderWithInvalidOrderLine() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.code = "order-line-code " + UUID.randomUUID().toString();
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: infoComponent.code, infoComponent.name.
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineWithoutCode() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code,infoComponent.code, infoComponent.name.
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineWithInvalidHoursGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line " + UUID.randomUUID().toString();
        orderLineDTO.code = "order-line-code " + UUID.randomUUID().toString();
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO();
        hoursGroupDTO.resourceType = ResourceEnumDTO.WORKER;
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code
        assertThat(constraintViolations.size(), equalTo(1));

        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(mustEnd("code"),
                    mustEnd("workingHours")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithOrderLine() {
        String code = "order-code " + UUID.randomUUID().toString();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line " + UUID.randomUUID().toString();
        orderLineDTO.code = "order-line-code " + UUID.randomUUID().toString();
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group",
                ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        checkIfExistsByCodeInAnotherTransaction(code);
    }

    @Test
    public void orderWithInvalidOrderLineGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderLineGroupDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.children.add(orderLineGroupDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: infoComponent.code, infoComponenet.name. Check
        // constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(2));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineGroupWithoutCode() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderDTO.children.add(orderLineGroupDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code,infoComponent.code, infoComponenet.name. Check
        // constraints:
        // checkConstraintAtLeastOneHoursGroupForEachOrderElement
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithOrderLineGroupWithoutHoursGroup() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderLineGroupDTO.name = "Order line group "
                + UUID.randomUUID().toString();
        orderLineGroupDTO.code = "order-line-group-code "
                + UUID.randomUUID().toString();
        orderDTO.children.add(orderLineGroupDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
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
        String code = UUID.randomUUID().toString();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        OrderLineGroupDTO orderLineGroupDTO = new OrderLineGroupDTO();
        orderLineGroupDTO.name = "Order line group "
                + UUID.randomUUID().toString();
        orderLineGroupDTO.code = "order-line-group-code "
                + UUID.randomUUID().toString();

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line " + UUID.randomUUID().toString();
        orderLineDTO.code = "order-line-code " + UUID.randomUUID().toString();
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group-"
                + UUID.randomUUID().toString(), ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderLineGroupDTO.children.add(orderLineDTO);

        orderDTO.children.add(orderLineGroupDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        checkIfExistsByCodeInAnotherTransaction(code);
    }

    @Test
    public void orderWithInvalidMaterialAssignment() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: material code
        assertThat(constraintViolations.size(), equalTo(1));
        assertThat(constraintViolations.get(0).fieldName, mustEnd("code"));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void orderWithInvalidMaterialAssignmentWithoutUnitsAndUnitPrice() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        materialAssignmentDTO.materialCode = "material-code "
                + UUID.randomUUID().toString();
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: units, unitPrice
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(
                    mustEnd("units"), mustEnd("unitPrice")));
        }

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validOrderWithMaterialAssignment() {
        String code = "order-code " + UUID.randomUUID().toString();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO();
        materialAssignmentDTO.materialCode = "material-code "
                + UUID.randomUUID().toString();
        materialAssignmentDTO.unitPrice = BigDecimal.TEN;
        materialAssignmentDTO.units = BigDecimal.valueOf(100.0);
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        checkIfExistsByCodeInAnotherTransaction(code);
    }

    @Test
    public void orderWithInvalidLabel() {
        int previous = orderDAO.getOrders().size();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = "order-code " + UUID.randomUUID().toString();
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        LabelReferenceDTO labelReferenceDTO = new LabelReferenceDTO();
        orderDTO.labels.add(labelReferenceDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        assertThat(constraintViolations.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    @NotTransactional
    public void validOrderWithLabel() {
        final String code = "order-code " + UUID.randomUUID().toString();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String labelCode = transactionService
                .runOnTransaction(new IOnTransaction<String>() {
                    @Override
                    public String execute() {
                        return givenLabelStored().getCode();
                    }
                });

        LabelReferenceDTO labelReferenceDTO = new LabelReferenceDTO();
        labelReferenceDTO.code = labelCode;
        orderDTO.labels.add(labelReferenceDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        checkIfExistsByCodeInAnotherTransaction(code);
    }

    private void checkIfExistsByCodeInAnotherTransaction(final String code) {
        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findByCode(code);
                    assertTrue(true);
                } catch (InstanceNotFoundException e) {
                    fail();
                }
                return null;
            }
        });
    }

    @Test
    @NotTransactional
    public void updateLabels() throws InstanceNotFoundException,
            IncompatibleTypeException {
        final String code = "order-code-" + UUID.randomUUID().toString();

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String labelCode = transactionService
                .runOnTransaction(new IOnTransaction<String>() {
                    @Override
                    public String execute() {
                        return givenLabelStored().getCode();
                    }
                });
        LabelReferenceDTO labelReferenceDTO = new LabelReferenceDTO(labelCode);
        orderDTO.labels.add(labelReferenceDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getLabels().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        assertNotNull(orderElement);
        assertThat(orderElement.getLabels().size(), equalTo(1));

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                return null;
            }
        });

        LabelReferenceDTO labelReferenceDTO2 = new LabelReferenceDTO(labelCode);
        orderDTO.labels.add(labelReferenceDTO2);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getLabels().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        // update the same label
        assertThat(orderElement2.getLabels().size(), equalTo(1));
    }

    @Test
    @NotTransactional
    public void updateMaterialAssignment() throws InstanceNotFoundException,
            IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        String materialcode1 = "material-code-1-"
                + UUID.randomUUID().toString();
        String materialcode2 = "material-code-2-"
                + UUID.randomUUID().toString();

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        MaterialAssignmentDTO materialAssignmentDTO = new MaterialAssignmentDTO(
                materialcode1, BigDecimal.valueOf(100.0), BigDecimal.TEN, null);
        orderDTO.materialAssignments.add(materialAssignmentDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getMaterialAssignments().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        assertNotNull(orderElement);
        assertThat(orderElement.getMaterialAssignments().size(), equalTo(1));

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                return null;
            }
        });

        orderDTO.materialAssignments.iterator().next().units = BigDecimal
                .valueOf(150.0);

        MaterialAssignmentDTO materialAssignmentDTO2 = new MaterialAssignmentDTO(
                materialcode2, BigDecimal.valueOf(200.0), BigDecimal.ONE, null);
        orderDTO.materialAssignments.add(materialAssignmentDTO);
        orderDTO.materialAssignments.add(materialAssignmentDTO2);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            for (MaterialAssignment materialAssignment : element
                                    .getMaterialAssignments()) {
                                materialAssignment.getMaterial().getCode();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        assertThat(orderElement2.getMaterialAssignments().size(), equalTo(2));
        for (MaterialAssignment materialAssignment : orderElement2
                .getMaterialAssignments()) {
            assertThat(materialAssignment.getMaterial().getCode(), anyOf(
                    equalTo(materialcode1), equalTo(materialcode2)));
            assertThat(materialAssignment.getUnits(), anyOf(equalTo(BigDecimal
                    .valueOf(150.0).setScale(2)), equalTo(BigDecimal.valueOf(
                    200.0).setScale(2))));
            assertThat(materialAssignment.getUnitPrice(), anyOf(
                    equalTo(BigDecimal.TEN.setScale(2)), equalTo(BigDecimal.ONE
                            .setScale(2))));
        }
    }

    @Test
    @NotTransactional
    public void updateHoursGroup() throws InstanceNotFoundException,
            IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        final String orderLineCode = "order-line-code"
                + UUID.randomUUID().toString();
        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line " + UUID.randomUUID().toString();
        orderLineDTO.code = orderLineCode;
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-groupYY",
                ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            return orderElementDAO.findUniqueByCode(code);
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);

        final OrderLine orderLine = transactionService
                .runOnTransaction(new IOnTransaction<OrderLine>() {
                    @Override
                    public OrderLine execute() {
                        try {
                            OrderLine line = (OrderLine) orderElementDAO
                                    .findUniqueByCode(orderLineCode);
                            line.getHoursGroups().size();
                            return line;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderLine);
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                sessionFactory.getCurrentSession().evict(orderLine);
                return null;
            }
        });

        orderLineDTO.hoursGroups.iterator().next().workingHours = 1500;
        HoursGroupDTO hoursGroupDTO2 = new HoursGroupDTO("hours-groupXX",
                ResourceEnumDTO.WORKER, 2000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO2);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            return orderElementDAO.findUniqueByCode(code);
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement2);

        final OrderLine orderLine2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderLine>() {
                    @Override
                    public OrderLine execute() {
                        try {
                            OrderLine line = (OrderLine) orderElementDAO
                                    .findUniqueByCode(orderLineCode);
                            for (HoursGroup hoursGroup : line.getHoursGroups()) {
                                hoursGroup.getCode();
                            }
                            return line;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderLine2);
        assertThat(orderLine2.getHoursGroups().size(), equalTo(2));

        for (HoursGroup hoursGroup : orderLine2.getHoursGroups()) {
            assertThat(hoursGroup.getCode(), anyOf(equalTo("hours-groupYY"),
                    equalTo("hours-groupXX")));
            assertThat(hoursGroup.getWorkingHours(), anyOf(equalTo(1500),
                    equalTo(2000)));
            assertThat(hoursGroup.getResourceType(),
                    equalTo(ResourceEnum.WORKER));
        }
    }

    @Test
    // FIXME move to subcontractors service when it exists
    public void invalidOrderWithInvalidAdvanceMeasurements()
            throws InstanceNotFoundException {
        String code = "order-code" + UUID.randomUUID().toString();
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO();
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: date
        assertThat(constraintViolations.size(), equalTo(1));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            System.out.println("### constraintViolationDTO.fieldName: " + constraintViolationDTO.fieldName);
            assertThat(constraintViolationDTO.fieldName, anyOf(mustEnd("date")));
        }

        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order shouldn't be stored");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }
    }

    @Test
    @NotTransactional
    // FIXME move to subcontractors service when it exists
    public void validOrderWithAdvanceMeasurements() {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO(
                DateConverter.toXMLGregorianCalendar(new Date()),
                BigDecimal.TEN);
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getDirectAdvanceAssignmentSubcontractor()
                                    .getAdvanceMeasurements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        DirectAdvanceAssignment advanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        assertThat(advanceAssignment.getAdvanceMeasurements().size(),
                equalTo(1));
    }

    @Test
    @NotTransactional
    // FIXME move to subcontractors service when it exists
    public void updateAdvanceMeasurements() throws InstanceNotFoundException,
            IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        LocalDate date = new LocalDate();
        AdvanceMeasurementDTO advanceMeasurementDTO = new AdvanceMeasurementDTO(
                DateConverter.toXMLGregorianCalendar(date), new BigDecimal(15));

        orderDTO.advanceMeasurements.add(advanceMeasurementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getDirectAdvanceAssignmentSubcontractor()
                                    .getAdvanceMeasurements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        DirectAdvanceAssignment advanceAssignment = orderElement
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        assertThat(advanceAssignment.getAdvanceMeasurements().size(),
                equalTo(1));

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                return null;
            }
        });

        AdvanceMeasurementDTO advanceMeasurementDTO2 = new AdvanceMeasurementDTO(
                DateConverter.toXMLGregorianCalendar(date.plusWeeks(1)),
                new BigDecimal(20));
        orderDTO.advanceMeasurements.add(advanceMeasurementDTO2);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            for (AdvanceMeasurement measurement : element
                                    .getDirectAdvanceAssignmentSubcontractor()
                                    .getAdvanceMeasurements()) {
                                measurement.getDate();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement2);
        advanceAssignment = orderElement2
                .getDirectAdvanceAssignmentSubcontractor();
        assertNotNull(advanceAssignment);
        SortedSet<AdvanceMeasurement> advanceMeasurements = advanceAssignment
                .getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(2));
        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            assertThat(advanceMeasurement.getDate(), anyOf(equalTo(date),
                    equalTo(date.plusWeeks(1))));
            assertThat(advanceMeasurement.getValue(), anyOf(
                    equalTo(new BigDecimal(15).setScale(2)),
                    equalTo(new BigDecimal(20).setScale(2))));
        }
    }

    @Test
    public void invalidOrderWithCriterionRequirements()
            throws InstanceNotFoundException {
        String code = "order-code" + UUID.randomUUID().toString();
        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order with code " + code + " already exists");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO();
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        // the criterion format is incorrect because its name and type is empty.
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        try {
            orderElementDAO.findUniqueByCode(code);
            fail("Order shouldn't be stored");
        } catch (InstanceNotFoundException e) {
            // It should throw an exception
        }
    }

    @Test
    @NotTransactional
    public void validOrderWithCriterionRequirements()
            throws InstanceNotFoundException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String name = PredefinedCriterionTypes.LOCATION.getPredefined().get(0);
        String type = PredefinedCriterionTypes.LOCATION.getName();

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO(
                name, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));
    }

    @Test
    @NotTransactional
    public void validOrderWithDirectCriterionRequirementsAndIndidirectCriterionRequirements()
            throws InstanceNotFoundException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String name = PredefinedCriterionTypes.LOCATION.getPredefined().get(0);
        String type = PredefinedCriterionTypes.LOCATION.getName();

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO(
                name, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line " + UUID.randomUUID().toString();
        orderLineDTO.code = "order-line-code-AX";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group"
                + UUID.randomUUID().toString(), ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        IndirectCriterionRequirementDTO indirectCriterionRequirementDTO = new IndirectCriterionRequirementDTO(
                name, type, false);
        orderLineDTO.criterionRequirements.add(indirectCriterionRequirementDTO);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));

        OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode("order-line-code-AX");
                            for (CriterionRequirement requirement : element
                                    .getCriterionRequirements()) {
                                requirement.isValid();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement2);
        assertThat(orderElement2.getCriterionRequirements().size(), equalTo(1));
        assertFalse(((IndirectCriterionRequirement) orderElement2
                .getCriterionRequirements().iterator().next()).isValid());
    }

    @Test
    @NotTransactional
    public void updateCriterionRequirements() throws InstanceNotFoundException,
            IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String name = PredefinedCriterionTypes.LOCATION.getPredefined().get(0);
        String type = PredefinedCriterionTypes.LOCATION.getName();

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO(
                name, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));

        String name2 = PredefinedCriterionTypes.LOCATION.getPredefined().get(1);

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                return null;
            }
        });

        CriterionRequirementDTO criterionRequirementDTO2 = new DirectCriterionRequirementDTO(
                name2, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO2);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            for (CriterionRequirement requirement : element
                                    .getCriterionRequirements()) {
                                requirement.getCriterion().getType().getName();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement2);
        Set<CriterionRequirement> criterionRequirements = orderElement2
                .getCriterionRequirements();
        assertThat(criterionRequirements.size(), equalTo(2));
        for (CriterionRequirement criterionRequirement : criterionRequirements) {
            assertThat(criterionRequirement.getCriterion().getName(), anyOf(
                    equalTo(name), equalTo(name2)));
            assertThat(criterionRequirement.getCriterion().getType().getName(),
                    equalTo(type));
            assertTrue(criterionRequirement instanceof DirectCriterionRequirement);
        }
    }

    @Test
    @NotTransactional
    public void updateDirectCriterionRequirementsAndIndirectCriterionRequirements()
            throws InstanceNotFoundException, IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name " + UUID.randomUUID().toString();
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String name = PredefinedCriterionTypes.LOCATION.getPredefined().get(0);
        String type = PredefinedCriterionTypes.LOCATION.getName();

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO(
                name, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code-RR";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group-RR",
                ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        final OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement);
                return null;
            }
        });

        final OrderElement orderElement2 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode("order-line-code-RR");
                            for (CriterionRequirement requirement : element
                                    .getCriterionRequirements()) {
                                requirement.isValid();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement2);
        assertThat(orderElement2.getCriterionRequirements().size(), equalTo(1));
        assertTrue(((IndirectCriterionRequirement) orderElement2
                .getCriterionRequirements().iterator().next()).isValid());

        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                orderElementDAO.flush();
                sessionFactory.getCurrentSession().evict(orderElement2);
                return null;
            }
        });

        IndirectCriterionRequirementDTO indirectCriterionRequirementDTO = new IndirectCriterionRequirementDTO(
                name, type, false);
        orderLineDTO.criterionRequirements.add(indirectCriterionRequirementDTO);

        orderListDTO = createOrderListDTO(orderDTO);
        instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement3 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement3);
        assertThat(orderElement3.getCriterionRequirements().size(), equalTo(1));

        orderElement3 = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode("order-line-code-RR");
                            for (CriterionRequirement requirement : element
                                    .getCriterionRequirements()) {
                                requirement.isValid();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement3);
        assertThat(orderElement3.getCriterionRequirements().size(), equalTo(1));
        assertFalse(((IndirectCriterionRequirement) orderElement3
                .getCriterionRequirements().iterator().next()).isValid());
    }

    @Test
    @NotTransactional
    public void importDirectCriterionRequirementsAndIndirectCriterionRequirements()
            throws InstanceNotFoundException, IncompatibleTypeException {
        final String code = "order-code" + UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    orderElementDAO.findUniqueByCode(code);
                    fail("Order with code " + code + " already exists");
                } catch (InstanceNotFoundException e) {
                    // It should throw an exception
                }
                return null;
            }
        });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.name = "Order name";
        orderDTO.code = code;
        orderDTO.initDate = DateConverter.toXMLGregorianCalendar(new Date());

        String name = PredefinedCriterionTypes.LOCATION.getPredefined().get(0);
        String type = PredefinedCriterionTypes.LOCATION.getName();

        CriterionRequirementDTO criterionRequirementDTO = new DirectCriterionRequirementDTO(
                name, type);
        orderDTO.criterionRequirements.add(criterionRequirementDTO);

        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Order line";
        orderLineDTO.code = "order-line-code-WW";
        HoursGroupDTO hoursGroupDTO = new HoursGroupDTO("hours-group-WW",
                ResourceEnumDTO.WORKER, 1000,
                new HashSet<CriterionRequirementDTO>());
        orderLineDTO.hoursGroups.add(hoursGroupDTO);

        IndirectCriterionRequirementDTO indirectCriterionRequirementDTO = new IndirectCriterionRequirementDTO(
                name, type, false);
        orderLineDTO.criterionRequirements.add(indirectCriterionRequirementDTO);

        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        OrderElement orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode(code);
                            element.getCriterionRequirements().size();
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));

        orderElement = transactionService
                .runOnTransaction(new IOnTransaction<OrderElement>() {
                    @Override
                    public OrderElement execute() {
                        try {
                            OrderElement element = orderElementDAO
                                    .findUniqueByCode("order-line-code-WW");
                            for (CriterionRequirement requirement : element
                                    .getCriterionRequirements()) {
                                requirement.isValid();
                            }
                            return element;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertNotNull(orderElement);
        assertThat(orderElement.getCriterionRequirements().size(), equalTo(1));
        assertFalse(((IndirectCriterionRequirement) orderElement
                .getCriterionRequirements().iterator().next()).isValid());
    }

    private OrderListDTO createOrderListDTO(OrderDTO... orderDTOs) {

        List<OrderDTO> orderList = new ArrayList<OrderDTO>();

        for (OrderDTO c : orderDTOs) {
            orderList.add(c);
        }

        return new OrderListDTO(orderList);

    }

    @Test
    public void testCannotExistTwoOrderElementsWithTheSameCode() {
        final String repeatedCode = "code1";

        OrderLineDTO orderLineDTO = createOrderLineDTO(repeatedCode);

        OrderDTO orderDTO = createOrderDTO(repeatedCode);
        orderDTO.children.add(orderLineDTO);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    @Test
    public void testCannotExistTwoHoursGroupWithTheSameCode() {
        final String repeatedCode = "code1";

        OrderLineDTO orderLineDTO1 = createOrderLineDTO("orderLineCode1");
        orderLineDTO1.hoursGroups.add(createHoursGroupDTO(repeatedCode));

        OrderLineDTO orderLineDTO2 = createOrderLineDTO("orderLineCode2");
        orderLineDTO2.hoursGroups.add(createHoursGroupDTO(repeatedCode));

        OrderDTO orderDTO = createOrderDTO("orderCode");
        orderDTO.children.add(orderLineDTO1);
        orderDTO.children.add(orderLineDTO2);

        OrderListDTO orderListDTO = createOrderListDTO(orderDTO);
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = orderElementService
                .addOrders(orderListDTO).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    private OrderDTO createOrderDTO(String code) {
        OrderDTO result = new OrderDTO();
        result.initDate = DateConverter.toXMLGregorianCalendar(new Date());
        result.code = code;
        result.name = UUID.randomUUID().toString();
        return result;
    }

    private OrderLineDTO createOrderLineDTO(String code) {
        OrderLineDTO result = new OrderLineDTO();
        result.initDate = DateConverter.toXMLGregorianCalendar(new Date());
        result.code = code;
        result.name = UUID.randomUUID().toString();
        return result;
    }

    private HoursGroupDTO createHoursGroupDTO(String code) {
        HoursGroupDTO result = new HoursGroupDTO();
        result.code = code;
        result.resourceType = ResourceEnumDTO.MACHINE;
        return result;
    }

}
