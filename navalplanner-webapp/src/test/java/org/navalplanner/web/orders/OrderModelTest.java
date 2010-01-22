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

package org.navalplanner.web.orders;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.web.resources.criterion.ICriterionsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link OrderModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE })
@Transactional
public class OrderModelTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
    }

    public static Date year(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    @Autowired
    private IOrderModel orderModel;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService adHocTransaction;

    @Autowired
    private ICriterionsModel criterionModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private Criterion criterion;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private Order createValidOrder() {
        Order order = Order.create();
        order.setDescription("description");
        order.setInitDate(year(2000));
        order.setName("name");
        order.setResponsible("responsible");
        order.setCode("code-" + UUID.randomUUID());
        order.setCalendar(configurationDAO.getConfiguration()
                .getDefaultCalendar());
        return order;
    }

    private ExternalCompany createValidExternalCompany() {
        ExternalCompany externalCompany = ExternalCompany.create(UUID
                .randomUUID().toString(), UUID.randomUUID().toString());
        externalCompanyDAO.save(externalCompany);
        return externalCompany;
    }

    @Test
    @Rollback(false)
    public void testNotRollback() {
        // Just to do not make rollback in order to have the default
        // configuration, needed for prepareForCreate in order to autogenerate
        // the order code
    }

    @Test
    public void testCreation() throws ValidationException {
        Order order = createValidOrder();
        order.setCustomer(createValidExternalCompany());
        orderModel.setOrder(order);
        orderModel.save();
        assertTrue(orderDAO.exists(order.getId()));
    }

    @Test
    public void testCreation1() throws ValidationException {
        orderModel.prepareForCreate();
        Order order = (Order) orderModel.getOrder();
        order.setName("name");
        order.setCode("code");
        order.setInitDate(new Date());
        order.setCustomer(createValidExternalCompany());
        orderModel.save();
        assertTrue(orderDAO.exists(order.getId()));
    }

    @Ignore("Test ignored until having the possibility to have a user " +
            "session from tests")
    @Test
    public void testListing() throws Exception {
        List<Order> list = orderModel.getOrders();
        Order order = createValidOrder();
        order.setCustomer(createValidExternalCompany());
        orderModel.setOrder(order);
        orderModel.save();
        assertThat(orderModel.getOrders().size(), equalTo(list.size() + 1));
    }

    @Test
    public void testRemove() throws Exception {
        Order order = createValidOrder();
        orderModel.setOrder(order);
        orderModel.save();
        assertTrue(orderDAO.exists(order.getId()));
        orderModel.remove(order);
        assertFalse(orderDAO.exists(order.getId()));
    }

    @Test(expected = ValidationException.class)
    public void shouldSendValidationExceptionIfEndDateIsBeforeThanStartingDate()
            throws ValidationException {
        Order order = createValidOrder();
        order.setDeadline(year(0));
        orderModel.setOrder(order);
        orderModel.save();
    }

    @Test
    public void testFind() throws Exception {
        Order order = createValidOrder();
        orderModel.setOrder(order);
        orderModel.save();
        assertThat(orderDAO.find(order.getId()), notNullValue());
    }

    @Test
    @NotTransactional
    public void testOrderPreserved() throws ValidationException,
            InstanceNotFoundException {
        final Order order = adHocTransaction.runOnReadOnlyTransaction(new IOnTransaction<Order>() {
            @Override
            public Order execute() {
                return createValidOrder();
            }
        });
        final OrderElement[] containers = new OrderLineGroup[10];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = adHocTransaction
                    .runOnTransaction(new IOnTransaction<OrderLineGroup>() {
                        @Override
                        public OrderLineGroup execute() {
                            return OrderLineGroup.create();
                        }
                    });
            containers[i].setName("bla");
            containers[i].setCode("code-" + UUID.randomUUID());
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

        orderModel.setOrder(order);
        orderModel.save();
        adHocTransaction.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Order reloaded = orderDAO.find(order.getId());
                    List<OrderElement> elements = reloaded.getOrderElements();
                    for (OrderElement orderElement : elements) {
                        assertThat(((OrderLineGroup) orderElement)
                                .getIndirectAdvanceAssignments().size(),
                                equalTo(1));
                    }
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

        orderModel.remove(order);
    }

    private OrderLine createValidLeaf(String parameter) {
        OrderLine result = OrderLine.create();
        result.setName(parameter);
        result.setCode("code-" + UUID.randomUUID());

        HoursGroup hoursGroup = HoursGroup.create(result);
        hoursGroup.setCode("hoursGroupName");
        hoursGroup.setWorkingHours(0);
        result.addHoursGroup(hoursGroup);

        return result;
    }

    @Test
    @NotTransactional
    public void testAddingOrderElement() throws Exception {
        final Order order = adHocTransaction
                .runOnReadOnlyTransaction(new IOnTransaction<Order>() {
                    @Override
                    public Order execute() {
                        return createValidOrder();
                    }
                });
        OrderLineGroup container = adHocTransaction
                .runOnTransaction(new IOnTransaction<OrderLineGroup>() {
                    @Override
                    public OrderLineGroup execute() {
                        return OrderLineGroup.create();
                    }
                });
        container.setName("bla");
        container.setCode("code-" + UUID.randomUUID());
        OrderLine leaf = OrderLine.create();
        leaf.setName("leaf");
        leaf.setCode("code-" + UUID.randomUUID());
        container.add(leaf);
        order.add(container);
        HoursGroup hoursGroup = HoursGroup.create(leaf);
        hoursGroup.setCode("hoursGroupName");
        hoursGroup.setWorkingHours(3);
        leaf.addHoursGroup(hoursGroup);
        orderModel.setOrder(order);
        orderModel.save();
        adHocTransaction.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Order reloaded = orderDAO.find(order.getId());
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
                    orderModel.remove(order);
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
        givenCriterion();
        final Order order = adHocTransaction
                .runOnReadOnlyTransaction(new IOnTransaction<Order>() {
                    @Override
                    public Order execute() {
                        return createValidOrder();
                    }
                });

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("Order element");
        orderLine.setCode("000000000");
        order.add(orderLine);

        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setCode("hoursGroupName");
        hoursGroup.setWorkingHours(10);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setCode("hoursGroupName2");
        hoursGroup2.setWorkingHours(5);

        orderLine.addHoursGroup(hoursGroup);
        //orderLine.addHoursGroup(hoursGroup2);

        CriterionRequirement criterionRequirement =
                DirectCriterionRequirement.create(criterion);

        hoursGroup.addCriterionRequirement(criterionRequirement);
        //hoursGroup2.addCriterionRequirement(criterionRequirement);

        orderModel.setOrder(order);
        orderModel.save();
        adHocTransaction.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    sessionFactory.getCurrentSession().flush();
                    Order reloaded = orderDAO.find(order.getId());
                    List<OrderElement> orderElements = reloaded
                            .getOrderElements();
                    assertThat(orderElements.size(), equalTo(1));

                    List<HoursGroup> hoursGroups = orderElements.get(0)
                            .getHoursGroups();
                    assertThat(hoursGroups.size(), equalTo(1));

                    Set<CriterionRequirement> criterionRequirements = hoursGroups.get(0)
                            .getCriterionRequirements();
                    assertThat(criterionRequirements.size(), equalTo(1));

                    Set<Criterion> criterions = hoursGroups.get(0)
                            .getValidCriterions();
                    assertThat(criterions.size(), equalTo(1));

                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

    }

    private void givenCriterion() throws ValidationException {
        this.criterion = adHocTransaction
                .runOnTransaction(new IOnTransaction<Criterion>() {

                    @Override
                    public Criterion execute() {
                        CriterionType criterionType = CriterionType.create(
                                "test" + UUID.randomUUID(), "");
                        criterionType.setResource(ResourceEnum.WORKER);
                        criterionTypeDAO.save(criterionType);
                        Criterion criterion = Criterion.create("Test"
                                + UUID.randomUUID(), criterionType);
                        try {
                            criterionModel.save(criterion);
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }
                        return criterion;
                    }
                });
        this.criterion.dontPoseAsTransientObjectAnymore();
        this.criterion.getType().dontPoseAsTransientObjectAnymore();
    }

    @Test(expected = ValidationException.class)
    public void testAtLeastOneHoursGroup() throws Exception {
        Order order = createValidOrder();

        OrderLine orderLine = OrderLine.create();
        orderLine.setName("foo");
        orderLine.setCode("000000000");
        order.add(orderLine);

        orderModel.setOrder(order);
        orderModel.save();
    }

}
