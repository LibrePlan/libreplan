package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
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
public class OrderTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Test
    public void testAddingOrderElement() throws Exception {
        Order order = Order.create();
        OrderLineGroup container = OrderLineGroup.create();
        OrderLine leaf = OrderLine.create();
        container.add(leaf);
        order.add(container);
        assertThat(order.getOrderElements().size(), equalTo(1));
    }

    @Test
    public void testPreservesOrder() throws Exception {
        OrderLineGroup container = OrderLineGroup.create();

        OrderLine[] created = new OrderLine[100];
        for (int i = 0; i < created.length; i++) {
            created[i] = OrderLine.create();
            container.add(created[i]);
        }
        for (int i = 0; i < created.length; i++) {
            assertThat(container.getChildren().get(i),
                    equalTo((OrderElement) created[i]));
        }
    }
}
