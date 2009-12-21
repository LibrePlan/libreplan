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

package org.navalplanner.business.test.orders.daos;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
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
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.test.orders.entities.OrderElementTest;
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

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderElementDAO);
    }

    private OrderLine createValidOrderLine() {
        String unique = UUID.randomUUID().toString();
        return createValidOrderLine(unique, unique);
    }

    private OrderLine createValidOrderLine(String name, String code) {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName(name);
        orderLine.setCode(code);
        return orderLine;
    }

    private OrderLineGroup createValidOrderLineGroup() {
        String unique = UUID.randomUUID().toString();
        return createValidOrderLineGroup(unique, unique);
    }

    private OrderLineGroup createValidOrderLineGroup(String name, String code) {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.setName(name);
        orderLineGroup.setCode(code);
        return orderLineGroup;
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
        orderElementDAO.save(orderLineGroup);
        orderLineGroup.setCode(((Long) orderLineGroup.getId()).toString());
        orderElementDAO.save(orderLineGroup);

        // Create OrderLineGroup
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderLineGroup.add(orderLine);
        orderElementDAO.save(orderLine);

        OrderLine found = (OrderLine) orderElementDAO
                .findUniqueByCodeAndParent(
                orderLineGroup, orderLine.getCode());
        assertTrue(found != null && found.getCode().equals(orderLine.getCode()));
    }

    @Test
    public void testFindByCodeInRoot() throws InstanceNotFoundException {
        // Create OrderLineGroupLine
        OrderLineGroup orderLineGroup = createValidOrderLineGroup();
        orderElementDAO.save(orderLineGroup);
        orderLineGroup.setCode(((Long) orderLineGroup.getId()).toString());
        orderElementDAO.save(orderLineGroup);

        List<OrderElement> list = orderElementDAO.findByCodeAndParent(null,
                orderLineGroup.getCode());
        assertFalse(list.isEmpty());
    }

    @Test
    public void testFindDistinguishedCode() {
        // Create OrderLineGroupLine
        OrderLineGroup orderLineGroup = createValidOrderLineGroup();
        orderElementDAO.save(orderLineGroup);
        orderLineGroup.setCode(((Long) orderLineGroup.getId()).toString());
        orderElementDAO.save(orderLineGroup);

        // Create OrderLineGroup
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderLineGroup.add(orderLine);
        orderElementDAO.save(orderLine);

        try {
            String distinguishedCode = orderElementDAO
                    .getDistinguishedCode(orderLine);
            String code = orderLine.getCode();
            OrderElement orderElement = orderLine;
            while (orderElement.getParent() != null) {
                code = orderLine.getParent().getCode() + "-" + code;
                orderElement = orderElement.getParent();
            }
            assertTrue(distinguishedCode.equals(code));
        } catch (InstanceNotFoundException e) {

        }
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
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000,
                3000);

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

        orderElementDAO.remove(id);
        orderElementDAO.flush();

        try {
            found = (OrderLine) orderElementDAO.find(id);
            fail("It should throw an exception");
        } catch (InstanceNotFoundException e) {
            found = null;
        }
        assertNull(found);
    }

    @Test
    public void testRemoveOrderLineGroupWithAdvanceAssignments()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException,
            InstanceNotFoundException {
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000, 3000);

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
            found = null;
        }
        assertNull(found);
    }

    @Test
    public void testSaveAndRemoveTaskQualityForm() {
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000, 3000);
        QualityForm qualityForm = QualityForm.create(UUID.randomUUID()
                .toString(), UUID.randomUUID().toString());
        qualityFormDAO.save(qualityForm);
        TaskQualityForm taskQualityForm = orderElement
                .addTaskQualityForm(qualityForm);

        orderElementDAO.save(orderElement);
        orderElementDAO.flush();
        assertThat(orderElement.getTaskQualityForms().size(), equalTo(1));

        orderElement.remove(taskQualityForm);

        orderElementDAO.save(orderElement);
        orderElementDAO.flush();
        assertThat(orderElement.getTaskQualityForms().size(), equalTo(0));
    }

    @Test
    public void testCheckUniqueQualityForm() {
        OrderElement orderElement = OrderElementTest
                .givenOrderLineGroupWithTwoOrderLines(2000, 3000);
        QualityForm qualityForm = QualityForm.create(UUID.randomUUID()
                .toString(), UUID.randomUUID().toString());
        qualityFormDAO.save(qualityForm);
        orderElement.addTaskQualityForm(qualityForm);

        orderElementDAO.save(orderElement);
        orderElementDAO.flush();
        assertThat(orderElement.getTaskQualityForms().size(), equalTo(1));

        try {
            orderElement.addTaskQualityForm(null);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            //
        }

        try {
            orderElement.addTaskQualityForm(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            //
        }
    }

}
