package org.navalplanner.business.orders.entities;

/**
 * Container of {@link OrderElement}. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderLineGroup {

    public void add(OrderElement orderElement);

    public void remove(OrderElement orderElement);

    public void replace(OrderElement oldOrderElement,
            OrderElement newOrderElement);

    public void up(OrderElement orderElement);

    public void down(OrderElement orderElement);

    public void add(int position, OrderElement orderElement);

}