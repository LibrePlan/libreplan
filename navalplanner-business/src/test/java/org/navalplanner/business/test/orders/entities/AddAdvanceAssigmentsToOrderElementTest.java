package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.advance.daos.IAdvanceAssigmentDAO;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
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
        Order order = new Order();
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
        AdvanceType advanceType = new AdvanceType(name, value, true, precision,
                true);
        return advanceType;
    }

    private AdvanceAssigment createValidAdvanceAssigment(
            boolean reportGlobalAdvance) {
        BigDecimal value = new BigDecimal(120).setScale(2);
        AdvanceAssigment advanceAssigment = new AdvanceAssigment(
                reportGlobalAdvance, value);
        return advanceAssigment;
    }

    /**
     * An empty {@link OrderElement} without any {@link AdvanceAssigment}.
     * Trying to add a new {@link AdvanceAssigment} to {@link OrderElement} .
     * Expected: Add new {@link AdvanceAssigment} to the list of
     * advanceAssigment of {@link OrderElement}.
     */
    @Test
    public void testSetAdvanceAssigmentEmptyOrderElement() throws Exception{
        Order order = createValidOrder();
        OrderElement orderLine = createValidLeaf("OrderLineA", "1k1k1k1k");

        AdvanceType advanceType = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigment = createValidAdvanceAssigment(true);
        assertTrue(orderLine.getAdvanceAssigments().isEmpty());
        advanceAssigment.setAdvanceType(advanceType);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAvanceAssigment(advanceAssigment);

        order.add(orderLine);
        orderDao.save(order);
        this.sessionFactory.getCurrentSession().flush();
        assertTrue(orderDao.exists(order.getId()));
        assertTrue(orderElementDao.exists(orderLine.getId()));

        assertFalse(orderLine.getAdvanceAssigments().isEmpty());
        assertTrue(advanceAssigmentDao.exists(advanceAssigment.getId()));

    }

    private AdvanceType createAndSaveType(String typeName) {
        AdvanceType advanceType = createValidAdvanceType(typeName);
        advanceTypeDao.save(advanceType);
        return advanceType;
    }

    /**
     * An {@link OrderElement} with an {@link AdvanceAssigment}. Trying to add a
     * new {@link AdvanceAssigment} to {@link OrderElement} . Expected: Add to
     * the list a new {@link AdvanceAssigment} of diferent type.
     */
    @Test
    public void testSetOtherAdvanceAssigmentOrderElement() throws Exception {
        Order order = createValidOrder();
        OrderLine orderLine = createValidLeaf("OrderLineA", "1111111");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeB);
        orderLine.addAvanceAssigment(advanceAssigmentB);
    }

    /**
     * An {@link OrderElement} with an {@link AdvanceAssigment}. Trying to add a
     * new {@link AdvanceAssigment} of the same type.to {@link OrderElement}
     * Expected: It must throw a
     * DuplicateAdvanceAssigmentForOrderElementException Exception.
     */
    @Test
    public void testSetOtherAdvanceAssigmentOrderElementIllegal() throws Exception{
        Order order = createValidOrder();
        OrderLine orderLine = createValidLeaf("OrderLineA", "22222222");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(false);
        advanceAssigmentB.setAdvanceType(advanceTypeA);
        try {
            orderLine.addAvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception");
        } catch (Exception e) {
            // Ok } }
        }
    }

    /**
     * An {@link OrderElement} with an {@link AdvanceAssigment}. Trying to add a
     * new {@link AdvanceAssigment} with the ReportGloblalAdvance value true to
     * {@link OrderElement} Expected: It must throw a
     * DuplicateValueTrueReportGlobalAdvanceException Exception.
     */
    @Test
    public void testSetWithSameReportGloblalAdvance() throws Exception{
        Order order = createValidOrder();
        OrderLine orderLine = createValidLeaf("OrderLineA", "101010101");

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);

        order.add(orderLine);
        orderDao.save(order);

        orderLine.addAvanceAssigment(advanceAssigmentA);

        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(true);
        advanceAssigmentB.setAdvanceType(advanceTypeB);
        try {
            orderLine.addAvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception  ");
        } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
            // Ok
        } catch (Exception e) {
            fail("It should not throw an exception");
        }
    }

    /**
     * Trying define an AdvanceAssigment object when any father of OrderElement
     * with an AdvanceAssigment object that has the other AdvanceType. It must
     * not throw any exception.
     **/
    @Test
    public void testSetAdvanceAssigmentOrdeElementSon() throws Exception{
        final Order order = createValidOrder();
        final OrderElement[] containers = new OrderLineGroup[2];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = OrderLineGroup.create();
            containers[i].setName("bla");
            containers[i].setCode("000000000");
            order.add(containers[i]);
        }
        OrderLineGroup container = (OrderLineGroup) containers[0];
        final OrderElement[] orderElements = new OrderElement[4];
        for (int i = 0; i < orderElements.length; i++) {
            OrderLine leaf = createValidLeaf("bla", "787887");
            orderElements[i] = leaf;
            container.add(leaf);
        }

        for (int i = 1; i < containers.length; i++) {
            OrderLineGroup orderLineGroup = (OrderLineGroup) containers[i];
            OrderLine leaf = createValidLeaf("foo", "156325");
            orderLineGroup.add(leaf);
        }

        AdvanceType advanceTypeA = createAndSaveType("tipoA");
        AdvanceType advanceTypeB = createAndSaveType("tipoB");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(true);
        advanceAssigmentB.setAdvanceType(advanceTypeB);

        orderDao.save(order);

        container.addAvanceAssigment(advanceAssigmentA);

        assertThat(container.getAdvanceAssigments().size(), equalTo(1));
        assertThat(
                container.getChildren().get(0).getAdvanceAssigments().size(),
                equalTo(0));
            ((OrderElement) container.getChildren().get(0))
                    .addAvanceAssigment(advanceAssigmentB);
    }

    /**
     * Trying define an AdvanceAssigment object when any father of OrderElement
     * with an AdvanceAssigment object that has the same AdvanceType Expected:
     * It must throw DuplicateAdvanceAssigmentForOrderElementException
     * Exception.
     **/
    @Test
    public void testSetAdvanceAssigmentOrdeElementSonIllegal() throws Exception{
        final Order order = createValidOrder();
        final OrderElement[] containers = new OrderLineGroup[2];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = OrderLineGroup.create();
            containers[i].setName("bla");
            containers[i].setCode("000000000");
            order.add(containers[i]);
        }
        OrderLineGroup container = (OrderLineGroup) containers[0];

        final OrderElement[] orderElements = new OrderElement[4];
        for (int i = 0; i < orderElements.length; i++) {
            OrderLine leaf = createValidLeaf("bla", "97979");
            orderElements[i] = leaf;
            container.add(leaf);
        }

        for (int i = 1; i < containers.length; i++) {
            OrderLineGroup orderLineGroup = (OrderLineGroup) containers[i];
            OrderLine leaf = createValidLeaf("foo", "797900");
            orderLineGroup.add(leaf);
        }

        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(true);
        advanceAssigmentB.setAdvanceType(advanceTypeA);

        orderDao.save(order);
        container.addAvanceAssigment(advanceAssigmentA);

        assertThat(container.getAdvanceAssigments().size(), equalTo(1));
        assertThat(
                container.getChildren().get(0).getAdvanceAssigments().size(),
                equalTo(0));
        try {
            ((OrderElement) container.getChildren().get(0))
                    .addAvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception  ");
        } catch (Exception e) {
            // Ok
        }
    }

    /**
     * Trying define an AdvanceAssigment object when any child of OrderElement
     * with an AdvanceAssigment object that has the same AdvanceType Expected:
     * It must throw DuplicateAdvanceAssigmentForOrderElementException
     * Exception.
     **/
    @Test
    public void testSetAdvanceAssigmentOrdeElementParentIllegal() throws Exception{
        final Order order = createValidOrder();
        final OrderElement[] containers = new OrderLineGroup[2];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = OrderLineGroup.create();
            containers[i].setName("bla_" + i);
            containers[i].setCode("000000000");
            order.add(containers[i]);
        }
        OrderLineGroup container = (OrderLineGroup) containers[0];
        OrderLineGroup containerSon = (OrderLineGroup) OrderLineGroup.create();
        containerSon.setName("Son");
        containerSon.setCode("11111111");
        container.add(containerSon);
        OrderLine orderLineGranSon = createValidLeaf("GranSon", "75757");
        containerSon.add(orderLineGranSon);
        final OrderElement[] orderElements = new OrderElement[2];
        for (int i = 1; i < orderElements.length; i++) {
            OrderLine leaf = createValidLeaf("bla", "979879");
            orderElements[i] = leaf;
            container.add(leaf);
        }
        for (int i = 1; i < containers.length; i++) {
            OrderLineGroup orderLineGroup = (OrderLineGroup) containers[i];
            OrderLine leaf = createValidLeaf("foo", "79799");
            orderLineGroup.add(leaf);
        }
        AdvanceType advanceTypeA = createAndSaveType("tipoA");

        AdvanceAssigment advanceAssigmentA = createValidAdvanceAssigment(true);
        advanceAssigmentA.setAdvanceType(advanceTypeA);
        AdvanceAssigment advanceAssigmentB = createValidAdvanceAssigment(true);
        advanceAssigmentB.setAdvanceType(advanceTypeA);

        orderDao.save(order);

        orderLineGranSon.addAvanceAssigment(advanceAssigmentA);

        assertThat(orderLineGranSon.getAdvanceAssigments().size(), equalTo(1));

        try {
            container.addAvanceAssigment(advanceAssigmentB);
            fail("It should throw an exception  ");
        } catch (Exception e) {
            // Ok
        }
    }


    @Test(expected = DuplicateAdvanceAssigmentForOrderElementException.class)
    public void addingAnotherAdvanceAssigmentWithAnEquivalentTypeButDifferentInstance()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
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

        line.addAvanceAssigment(assigment);
        line.addAvanceAssigment(assigmentWithSameType);
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
