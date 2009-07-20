package org.navalplanner.business.test.orders.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.services.IOrderService;
import org.navalplanner.business.planner.services.ITaskElementService;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link Order}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class OrderServiceTest {

    private static Order createValidOrder() {
        Order order = new Order();
        order.setDescription("description");
        order.setCustomer("blabla");
        order.setInitDate(CriterionSatisfactionDAOTest.year(2000));
        order.setName("name");
        order.setResponsible("responsible");
        return order;
    }

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ITaskElementService taskElementService;

    @Autowired
    private CriterionService criterionService;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    public void testCreation() throws ValidationException {
        Order order = createValidOrder();
        orderService.save(order);
        assertTrue(orderService.exists(order));
    }

    @Test
    public void testListing() throws Exception {
        List<Order> list = orderService.getOrders();
        orderService.save(createValidOrder());
        assertThat(orderService.getOrders().size(), equalTo(list.size() + 1));
    }

    @Test
    public void testRemove() throws Exception {
        Order order = createValidOrder();
        orderService.save(order);
        assertTrue(orderService.exists(order));
        orderService.remove(order);
        assertFalse(orderService.exists(order));
    }

    @Test
    public void removingOrderWithAssociatedTasksDeletesThem()
            throws ValidationException, InstanceNotFoundException {
        Order order = createValidOrder();
        OrderLine orderLine = new OrderLine();
        orderLine.setName("bla");
        orderLine.setCode("00000000");
        orderLine.setWorkHours(10);
        order.add(orderLine);
        orderService.save(order);
        taskElementService.convertToScheduleAndSave(order);
        getSession().flush();
        getSession().evict(order);
        Order reloaded = orderService.find(order.getId());
        OrderElement e = reloaded.getOrderElements().iterator().next();
        assertThat(e.getTaskElements().size(), equalTo(1));
        orderService.remove(reloaded);
        assertFalse(orderService.exists(reloaded));
    }

    @Test(expected = ValidationException.class)
    public void shouldSendValidationExceptionIfEndDateIsBeforeThanStartingDate()
            throws ValidationException {
        Order order = createValidOrder();
        order.setEndDate(CriterionSatisfactionDAOTest.year(0));
        orderService.save(order);
    }

    @Test
    public void testFind() throws Exception {
        Order order = createValidOrder();
        orderService.save(order);
        assertThat(orderService.find(order.getId()), notNullValue());
    }

    @Test
    @NotTransactional
    public void testOrderPreserved() throws ValidationException,
            InstanceNotFoundException {
        final Order order = createValidOrder();
        final OrderElement[] containers = new OrderLineGroup[10];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new OrderLineGroup();
            containers[i].setName("bla");
            containers[i].setCode("000000000");
            order.add(containers[i]);
        }
        OrderLineGroup container = (OrderLineGroup) containers[0];

        final OrderElement[] orderElements = new OrderElement[10];
        for (int i = 0; i < orderElements.length; i++) {
            OrderLine leaf = createValidLeaf("bla");
            orderElements[i] = leaf;
            container.add(leaf);
        }

        for (int i = 1; i < containers.length; i++) {
            OrderLineGroup orderLineGroup = (OrderLineGroup) containers[i];
            OrderLine leaf = createValidLeaf("foo");
            orderLineGroup.add(leaf);
        }

        orderService.save(order);
        orderService.onTransaction(new OnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Order reloaded = orderService.find(order.getId());
                    List<OrderElement> elements = reloaded.getOrderElements();
                    for (int i = 0; i < containers.length; i++) {
                        assertThat(elements.get(i).getId(),
                                equalTo(containers[i].getId()));
                    }
                    OrderLineGroup container = (OrderLineGroup) reloaded
                            .getOrderElements().iterator().next();
                    List<OrderElement> children = container.getChildren();
                    for (int i = 0; i < orderElements.length; i++) {
                        assertThat(children.get(i).getId(),
                                equalTo(orderElements[i].getId()));
                    }
                    for (int i = 1; i < containers.length; i++) {
                        OrderLineGroup orderLineGroup = (OrderLineGroup) containers[i];
                        assertThat(orderLineGroup.getChildren().size(),
                                equalTo(1));
                    }
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
        orderService.remove(order);
    }

    private OrderLine createValidLeaf(String parameter) {
        OrderLine result = new OrderLine();
        result.setName(parameter);
        result.setCode("000000000");

        HoursGroup hoursGroup = new HoursGroup(result);
        hoursGroup.setWorkingHours(0);
        result.addHoursGroup(hoursGroup);

        return result;
    }

    @Test
    @NotTransactional
    public void testAddingOrderElement() throws Exception {
        final Order order = createValidOrder();
        OrderLineGroup container = new OrderLineGroup();
        container.setName("bla");
        container.setCode("000000000");
        OrderLine leaf = new OrderLine();
        leaf.setName("leaf");
        leaf.setCode("000000000");
        container.add(leaf);
        order.add(container);
        HoursGroup hoursGroup = new HoursGroup(leaf);
        hoursGroup.setWorkingHours(3);
        leaf.addHoursGroup(hoursGroup);
        orderService.save(order);
        orderService.onTransaction(new OnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Order reloaded = orderService.find(order.getId());
                    assertFalse(order == reloaded);
                    assertThat(reloaded.getOrderElements().size(), equalTo(1));
                    OrderLineGroup containerReloaded = (OrderLineGroup) reloaded
                            .getOrderElements().get(0);
                    assertThat(containerReloaded.getHoursGroups().size(),
                            equalTo(1));
                    assertThat(containerReloaded.getChildren().size(),
                            equalTo(1));
                    OrderElement leaf = containerReloaded.getChildren().get(0);
                    assertThat(leaf.getHoursGroups().size(), equalTo(1));
                    orderService.remove(order);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    @Test
    @NotTransactional
    public void testManyToManyHoursGroupCriterionMapping() throws Exception {
        final Order order = createValidOrder();

        OrderLine orderLine = new OrderLine();
        orderLine.setName("Order element");
        orderLine.setCode("000000000");
        order.add(orderLine);

        HoursGroup hoursGroup = new HoursGroup(orderLine);
        hoursGroup.setWorkingHours(10);
        HoursGroup hoursGroup2 = new HoursGroup(orderLine);
        hoursGroup2.setWorkingHours(5);

        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        CriterionType criterionType = new CriterionType("test");
        Criterion criterion = new Criterion("Test" + UUID.randomUUID(),
                criterionType);
        criterionService.save(criterion);

        hoursGroup.addCriterion(criterion);
        hoursGroup2.addCriterion(criterion);

        orderService.save(order);

        orderService.onTransaction(new OnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Order reloaded = orderService.find(order.getId());

                    List<OrderElement> orderElements = reloaded
                            .getOrderElements();
                    assertThat(orderElements.size(), equalTo(1));

                    List<HoursGroup> hoursGroups = orderElements.get(0)
                            .getHoursGroups();
                    assertThat(hoursGroups.size(), equalTo(2));

                    Set<Criterion> criterions = hoursGroups.get(0)
                            .getCriterions();
                    assertThat(criterions.size(), equalTo(1));

                    Criterion criterion = criterions.iterator().next();

                    assertThat(criterion.getType().getName(), equalTo("test"));
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

    }

    @Test(expected = ValidationException.class)
    public void testAtLeastOneHoursGroup() throws Exception {
        Order order = createValidOrder();

        OrderLine orderLine = new OrderLine();
        orderLine.setName("foo");
        orderLine.setCode("000000000");
        order.add(orderLine);

        orderService.save(order);
    }

}
