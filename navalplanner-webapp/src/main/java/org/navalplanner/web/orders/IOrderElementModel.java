package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;

public interface IOrderElementModel {

    public OrderElement getOrderElement();

    public void setCurrent(OrderElement orderElement, OrderModel order);

    public List<CriterionType> getCriterionTypes();

    public CriterionType getCriterionTypeByName(String name);

    public List<Criterion> getCriterionsFor(CriterionType type);

}
