package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import java.util.Date;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.advance.daos.IAdvanceAssigmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link AdvanceAssigment of OrderElement}. <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class AddAdvanceAssigmentsToOrderElementTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IOrderElementDAO orderElementDao;

    @Autowired
    private IOrderDAO orderDao;

    @Autowired
    private IAdvanceAssigmentDAO advanceAssigmentDao;

    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private static Order createValidOrder() {
        Order order = Order.create();
        order.setDescription("description");
        order.setCustomer("blabla");
        order.setInitDate(CriterionSatisfactionDAOTest.year(2000));
        order.setName("name");
        order.setResponsible("responsible");
        return order;
    }

    private OrderLine createValidLeaf(String name, String code) {
        OrderLine result = OrderLine.create();
        result.setName(name);
        result.setCode(code);
        HoursGroup hoursGroup = HoursGroup.create(result);
        hoursGroup.setWorkingHours(0);
        result.addHoursGroup(hoursGroup);
        return result;
    }

    private AdvanceType createValidAdvanceType(String name) {
        BigDecimal value = new BigDecimal(120).setScale(2);
        BigDecimal precision = new BigDecimal(10).setScale(4);
        AdvanceType advanceType = AdvanceType.create(name, value, true, precision, true);
        return advanceType;
    }

    private AdvanceMeasurement createValidAdvanceMeasurement() {
        AdvanceMeasurement advanceMeasurement = AdvanceMeasurement.create(
                new Date(), new BigDecimal(0),0);
        return advanceMeasurement;
    }

    private AdvanceAssigment createValidAdvanceAssigment(
            boolean reportGlobalAdvance) {
        AdvanceAssigment advanceAssigment = AdvanceAssigment.create(
                reportGlobalAdvance,new BigDecimal(0));
        advanceAssigment.setType(AdvanceAssigment.Type.DIRECT);
        return advanceAssigment;
    }

    @Test
    public void savingTheOrderSavesAlsoTheAddedAssigments() throws Exception {
        Order order = createValidOrder();
        OrderElement orderLine = createValidLeaf("OrderLineA", "1k1k1k1k");

        AdvanceType advanceType = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigment = createValidAdvanceAssigment(true);
        assertTrue(orderLine.getAdvanceAssigments().isEmpty());
        advanceAssigment.setAdvanceType(advanceType);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAdvanceAssigment(advanceAssigment);

        order.add(orderLine);
        orderDao.save(order);
        this.sessionFactory.getCurrentSession().flush();

        assertFalse(orderLine.getAdvanceAssigments().isEmpty());
        assertTrue(advanceAssigmentDao.exists(advanceAssigment.getId()));

    }

    private AdvanceType createAndSaveType(String typeName) {
        AdvanceType advanceType = createValidAdvanceType(typeName);
        advanceTypeDao.save(advanceType);
        return advanceType;
    }

    @Test
    public void addingSeveralAssignmentsOfDifferentTypes() throws Exception {
        Order order = createValidOrder();
        OrderLine orderLine = createValidLeaf("OrderLineA", "1111111");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAdvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeB);
        orderLine.addAdvanceAssigment(advanceAssigmentB);
    }

    @Test
    public void cannotAddDuplicatedAssignment()
            throws Exception {
        OrderLine orderLine = createValidLeaf("OrderLineA", "22222222");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        orderLine.addAdvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeA);

        try {
            orderLine.addAdvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception");
        } catch (DuplicateAdvanceAssigmentForOrderElementException e) {
            // Ok
        }
    }

    @Test
    public void cannotAddTwoAssignmetsWithGlobalReportValue() throws Exception {
        OrderLine orderLine = createValidLeaf("OrderLineA", "101010101");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);


        orderLine.addAdvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(true);
        advanceAssigmentB.setAdvanceType(advanceTypeB);
        try {
            orderLine.addAdvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception  ");
        } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
            // Ok
        }
    }

    @Test
    public void addingAssignmentsOfAnotherTypeToSon() throws Exception {
        OrderLineGroup container = OrderLineGroup.create();
        container.setName("bla");
        container.setCode("000000000");
        OrderLine son = createValidLeaf("bla", "132");
        container.add(son);

        AdvanceMeasurement advanceMeasurement = createValidAdvanceMeasurement();

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        advanceAssigmentA.getAdvanceMeasurements().add(advanceMeasurement);
        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeB);
        advanceAssigmentB.getAdvanceMeasurements().add(advanceMeasurement);


        container.addAdvanceAssigment(advanceAssigmentA);
        son.addAdvanceAssigment(advanceAssigmentB);
    }

    @Test
    public void addingAnAdvanceAssignmentIncreasesTheNumberOfAdvanceAssignments()
            throws Exception {
        final OrderLineGroup container = OrderLineGroup.create();
        container.setName("bla");
        container.setCode("000000000");
        container.add(createValidLeaf("bla", "979"));

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        container.addAdvanceAssigment(advanceAssigmentA);

        assertThat(container.getAdvanceAssigments().size(), equalTo(1));
    }

    @Test
    public void cannotAddDuplicatedAssigmentToSon() throws Exception {
        final OrderLineGroup father = OrderLineGroup.create();
        father.setName("bla");
        father.setCode("000000000");
        father.add(createValidLeaf("bla", "979"));

        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        AdvanceAssigment anotherAssigmentWithSameType = createValidAdvanceAssigment(false);
        anotherAssigmentWithSameType.setAdvanceType(advanceTypeA);

        father.addAdvanceAssigment(advanceAssigmentA);

        try {
            OrderElement child = (OrderElement) father.getChildren().get(0);
            child.addAdvanceAssigment(anotherAssigmentWithSameType);
            fail("It should throw an exception  ");
        } catch (DuplicateAdvanceAssigmentForOrderElementException e) {
            // Ok
        }
    }

    @Test
    public void cannotAddDuplicateAssignmentToGrandParent() throws Exception {
        OrderLineGroup parent = OrderLineGroup.create();
        parent.setName("bla_");
        parent.setCode("000000000");
        OrderLineGroup son = (OrderLineGroup) OrderLineGroup.create();
        son.setName("Son");
        son.setCode("11111111");
        parent.add(son);
        OrderLine grandSon = createValidLeaf("GranSon", "75757");
        son.add(grandSon);

        AdvanceMeasurement advanceMeasurement = createValidAdvanceMeasurement();
        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        advanceAssigmentA.getAdvanceMeasurements().add(advanceMeasurement);
        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeA);
        advanceAssigmentB.getAdvanceMeasurements().add(advanceMeasurement);

        grandSon.addAdvanceAssigment(advanceAssigmentA);

        try {
            parent.addAdvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception  ");
        } catch (DuplicateAdvanceAssigmentForOrderElementException e) {
            // Ok
        }
    }

    @Test(expected = DuplicateAdvanceAssigmentForOrderElementException.class)
    public void addingAnotherAdvanceAssigmentWithAnEquivalentTypeButDifferentInstance()
            throws Exception {
        final Order order = createValidOrder();
        OrderLine line = createValidLeaf("GranSon", "75757");
        order.add(line);
        orderDao.save(order);

        AdvanceType type = createAndSaveType("tipoA");
        getSession().flush();
        getSession().evict(type);

        AdvanceType typeReloaded = reloadType(type);

        AdvanceAssigment assigment = createValidAdvanceAssigment(false);
        assigment.setAdvanceType(type);
        AdvanceAssigment assigmentWithSameType = createValidAdvanceAssigment(false);
        assigmentWithSameType.setAdvanceType(typeReloaded);

        line.addAdvanceAssigment(assigment);
        line.addAdvanceAssigment(assigmentWithSameType);
    }

    private AdvanceType reloadType(AdvanceType type) {
        try {
            // new instance of id is created to avoid both types have the same
            // id object
            Long newLong = new Long(type.getId());
            return advanceTypeDao.find(newLong);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
