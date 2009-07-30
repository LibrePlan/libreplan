package org.navalplanner.business.test.orders.daos;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderElementDao;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
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

    @Autowired
    private IOrderElementDao orderElementDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(orderElementDAO);
    }

    private OrderLine createValidOrderLine() {
        String unique = UUID.randomUUID().toString();
        return createValidOrderLine(unique, unique);
    }

    private OrderLine createValidOrderLine(String name, String code) {
        OrderLine orderLine = new OrderLine();
        orderLine.setName(name);
        orderLine.setCode(code);
        return orderLine;
    }

    private OrderLineGroup createValidOrderLineGroup() {
        String unique = UUID.randomUUID().toString();
        return createValidOrderLineGroup(unique, unique);
    }

    private OrderLineGroup createValidOrderLineGroup(String name, String code) {
        OrderLineGroup orderLineGroup = new OrderLineGroup();
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
    public void testFindByCode() {
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderElementDAO.save(orderLine);

        OrderLine found = (OrderLine) orderElementDAO.findByCode(orderLine
                .getCode());
        assertTrue(found != null && found.getCode().equals(orderLine.getCode()));
    }

    @Test
    public void testFindByCodeAndOrderLineGroup() {
        // Create OrderLineGroupLine
        OrderLineGroup orderLineGroup = createValidOrderLineGroup();
        orderElementDAO.save(orderLineGroup);
        orderLineGroup.setCode(((Long) orderLineGroup.getId()).toString());
        orderElementDAO.save(orderLineGroup);

        // Create OrderLineGroup
        OrderLine orderLine = createValidOrderLine();
        orderElementDAO.save(orderLine);
        orderLine.setCode(((Long) orderLine.getId()).toString());
        orderLine.setParent(orderLineGroup);
        orderElementDAO.save(orderLine);

        OrderLine found = (OrderLine) orderElementDAO.findByCode(
                orderLineGroup, orderLine.getCode());
        assertTrue(found != null && found.getCode().equals(orderLine.getCode()));
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
        orderLine.setParent(orderLineGroup);
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
}
