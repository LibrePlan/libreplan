package org.navalplanner.business.test.orders.entities;

import org.junit.Test;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.OrderLine;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Order}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OrderTest {

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
