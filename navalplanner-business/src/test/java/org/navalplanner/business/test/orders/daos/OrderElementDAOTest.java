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

package org.navalplanner.business.test.orders.daos;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.test.calendars.entities.BaseCalendarTest;
import org.navalplanner.business.test.orders.entities.OrderElementTest;
import org.navalplanner.business.test.planner.daos.ResourceAllocationDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/*
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
public class OrderElementDAOTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Autowired
    IQualityFormDAO qualityFormDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IBaseCalendarDAO calendarDAO;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Autowired
    private IScenarioManager scenarioManager;

    @Before
    public void loadRequiredData() {
        scenariosBootstrap.loadRequiredData();
    }

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderElementDAO);
    }

    private OrderLine createValidOrderLine() {
        String unique = UUID.randomUUID().toString();
        return createValidOrderLine(unique, unique);
    }

    private OrderLine createValidOrderLine(String name, String code) {
        Order order = createValidOrder();
        OrderLine orderLine = createStandAloneLine(name, code);
        order.add(orderLine);
        return orderLine;
    }

    private OrderLine createStandAloneLine() {
        String uniqueCode = UUID.randomUUID().toString();
        return createStandAloneLine(uniqueCode, uniqueCode);
    }

    private OrderLine createStandAloneLine(String name, String code) {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName(name);
        orderLine.setCode(code);
        return orderLine;
    }

    private OrderLineGroup createValidOrderLineGroup() {
        String unique = UUID.randomUUID().toString();
        OrderLineGroup result = createValidOrderLineGroup(unique, unique);
        return result;
    }

    private OrderLineGroup createValidOrderLineGroup(String name, String code) {
        Order order = createValidOrder();
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName(name);
        orderLineGroup.setCode(code);
        order.add(orderLineGroup);
        OrderLine line = OrderLine.createOrderLineWithUnfixedPercentage(10);
        orderLineGroup.add(line);
        line.setName(UUID.randomUUID().toString());
        line.setCode(UUID.randomUUID().toString());
        return orderLineGroup;
    }

    private Order createValidOrder() {
        Order order = Order.create();
        order.setName(UUID.randomUUID().toString());
        order.setCode(UUID.randomUUID().toString());
        order.setInitDate(new Date());
        BaseCalendar basicCalendar = BaseCalendarTest.createBasicCalendar();
        calendarDAO.save(basicCalendar);
        order.setCalendar(basicCalendar);
        OrderVersion orderVersion = ResourceAllocationDAOTest
                .setupVersionUsing(scenarioManager, order);
        orderElementDAO.save(order);
        orderElementDAO.flush();
        order.useSchedulingDataFor(orderVersion);
        return order;
    }

    @Test
    public void testSaveOrderLine() {
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        assertTrue(orderElementDAO.exists(orderLine.getId()));
    }

    @Test
    public void testFindUniqueByCode() throws InstanceNotFoundException {
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderElementDAO.save(orderLine);

        OrderLine found = (OrderLine) orderElementDAO
                .findUniqueByCode(orderLine
                .getCode());
        assertTrue(found != null && found.getCode().equals(orderLine.getCode()));
    }

    @Test
    public void testFindUniqueByCodeAndOrderLineGroup()
            throws InstanceNotFoundException {
        // Create OrderLineGroupLine
        OrderLineGroup orderLineGroup = createValidOrderLineGroup();
        orderElementDAO.save(orderLineGroup.getOrder());
        orderElementDAO.flush();

        orderLineGroup.setCode(((Long) orderLineGroup.getId()).toString());
        orderElementDAO.save(orderLineGroup.getOrder());

        // Create OrderLineGroup
        OrderLine orderLine = createStandAloneLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderLineGroup.add(orderLine);

        OrderLine found = (OrderLine) orderElementDAO
                .findUniqueByCodeAndParent(
                orderLineGroup, orderLine.getCode());
        assertTrue(found != null && found.getCode().equals(orderLine.getCode()));
    }

    @Test
    public void testFindByCodeInRoot() throws InstanceNotFoundException {
        // Create OrderLineGroupLine
        Order order = createValidOrder();

        List<OrderElement> list = orderElementDAO.findByCodeAndParent(null,
                order.getCode());
        assertFalse(list.isEmpty());
    }

    @Test
    public void testSaveOrderLineWithAdvanceAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException,
            InstanceNotFoundException {
        OrderLine orderLine = createValidOrderLine();

        OrderElementTest.addAvanceAssignmentWithMeasurement(orderLine,
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400), true);

        orderElementDAO.save(orderLine);
        orderElementDAO.flush();

        assertTrue(orderElementDAO.exists(orderLine.getId()));

        OrderLine found = (OrderLine) orderElementDAO.find(orderLine.getId());
        Set<DirectAdvanceAssignment> directAdvanceAssignments = found.getDirectAdvanceAssignments();
        assertThat(directAdvanceAssignments.size(), equalTo(1));

        SortedSet<AdvanceMeasurement> advanceMeasurements = directAdvanceAssignments
                .iterator().next().getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(1));

        assertThat(advanceMeasurements.iterator().next().getValue(),
                equalTo(new BigDecimal(400)));
    }

    @Test
    public void testSaveOrderLineGroupWithAdvanceAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException,
            InstanceNotFoundException {
        Order order = createValidOrder();
        OrderVersion orderVersion = order.getOrderVersionFor(scenarioManager
                .getCurrent());
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(orderVersion,
                        2000, 3000);
        order.add(orderElement);

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        OrderElementTest.addAvanceAssignmentWithMeasurement(children.get(0),
                advanceType,
                new BigDecimal(1000), new BigDecimal(100), true);

        OrderElementTest.addAvanceAssignmentWithMeasurement(children.get(1),
                advanceType,
                new BigDecimal(1000), new BigDecimal(300), true);

        orderElementDAO.save(orderElement);
        orderElementDAO.flush();

        assertTrue(orderElementDAO.exists(orderElement.getId()));

        OrderLineGroup found = (OrderLineGroup) orderElementDAO
                .find(orderElement.getId());
        assertThat(found.getDirectAdvanceAssignments().size(), equalTo(0));

        assertThat(found.getIndirectAdvanceAssignments().size(), equalTo(2));

        Set<DirectAdvanceAssignment> directAdvanceAssignments = found.getChildren().get(0).getDirectAdvanceAssignments();
        assertThat(directAdvanceAssignments.size(), equalTo(1));

        DirectAdvanceAssignment directAdvanceAssignment = directAdvanceAssignments
                .iterator().next();
        assertThat(directAdvanceAssignment.getMaxValue(),
                equalTo(new BigDecimal(1000)));

        SortedSet<AdvanceMeasurement> advanceMeasurements = directAdvanceAssignment.getAdvanceMeasurements();
        assertThat(advanceMeasurements.size(), equalTo(1));
        assertThat(advanceMeasurements.iterator().next().getValue(),
                equalTo(new BigDecimal(100)));
    }

    @Test
    public void testRemoveOrderLineWithAdvanceAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException,
            InstanceNotFoundException {
        OrderLine orderLine = createValidOrderLine();

        OrderElementTest.addAvanceAssignmentWithMeasurement(orderLine,
                PredefinedAdvancedTypes.UNITS.getType(), new BigDecimal(1000),
                new BigDecimal(400), true);

        orderElementDAO.save(orderLine);
        orderElementDAO.flush();

        Long id = orderLine.getId();
        OrderLine found = (OrderLine) orderElementDAO.find(id);
        assertNotNull(found);

        orderElementDAO.remove(orderLine.getOrder().getId());
        orderElementDAO.flush();

        try {
            orderElementDAO.find(id);
            fail("It should throw an exception");
        } catch (InstanceNotFoundException e) {
            // ok
        }
    }

    @Test
    public void testRemoveOrderLineGroupWithAdvanceAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException,
            InstanceNotFoundException {
        Order order = createValidOrder();
        OrderVersion orderVersion = order.getOrderVersionFor(scenarioManager
                .getCurrent());
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(orderVersion, 2000, 3000);
        order.add(orderElement);
        order.useSchedulingDataFor(order.getOrderVersionFor(scenarioManager
                .getCurrent()));

        List<OrderElement> children = orderElement.getChildren();

        AdvanceType advanceType = PredefinedAdvancedTypes.UNITS.getType();

        OrderElementTest.addAvanceAssignmentWithMeasurement(children.get(0),
                advanceType, new BigDecimal(1000), new BigDecimal(100), true);

        OrderElementTest.addAvanceAssignmentWithMeasurement(children.get(1),
                advanceType, new BigDecimal(1000), new BigDecimal(300), true);

        orderElementDAO.save(orderElement);
        orderElementDAO.flush();

        Long id = orderElement.getId();
        OrderLineGroup found = (OrderLineGroup) orderElementDAO.find(id);
        assertNotNull(found);

        orderElementDAO.remove(id);
        orderElementDAO.flush();

        try {
            found = (OrderLineGroup) orderElementDAO.find(id);
            fail("It should throw an exception");
        } catch (InstanceNotFoundException e) {
            // ok
        }
    }

    @Test
    public void testSaveAndRemoveTaskQualityForm() {
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000, 3000);
        QualityForm qualityForm = QualityForm.create(UUID.randomUUID()
                .toString(), UUID.randomUUID().toString());

        TaskQualityForm taskQualityForm = orderElement
                .addTaskQualityForm(qualityForm);
        assertThat(orderElement.getTaskQualityForms().size(), equalTo(1));

        orderElement.removeTaskQualityForm(taskQualityForm);
        assertThat(orderElement.getTaskQualityForms().size(), equalTo(0));
    }

    @Test
    public void testCheckUniqueQualityForm() {
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000, 3000);
        QualityForm qualityForm = QualityForm.create(UUID.randomUUID()
                .toString(), UUID.randomUUID().toString());
        orderElement.addTaskQualityForm(qualityForm);

        assertThat(orderElement.getTaskQualityForms().size(), equalTo(1));

        try {
            orderElement.addTaskQualityForm(null);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            orderElement.addTaskQualityForm(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // ok
        }
    }

    @Test
    public void testSumChargedHoursRelation() throws InstanceNotFoundException {
        OrderLine orderLine = createValidOrderLine();

        orderLine.getSumChargedEffort().setDirectChargedHours(8);
        orderLine.getSumChargedEffort().setIndirectChargedHours(10);

        orderElementDAO.save(orderLine);

        OrderElement orderLineCopy = orderElementDAO.find(orderLine.getId());

        assertEquals(orderLine.getSumChargedEffort().getId(),
                orderLineCopy.getSumChargedEffort().getId());

        assertTrue(orderLineCopy.getSumChargedEffort().getTotalChargedHours().equals(18));
    }
}
