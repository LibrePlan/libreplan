package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;

public interface IOrderElementModel {

    public OrderElement getOrderElement();

    public void setCurrent(OrderElement orderElement);

    public List<ICriterionType<?>> getCriterionTypes();

    public ICriterionType<?> getCriterionTypeByName(String name);

    public List<Criterion> getCriterionsFor(ICriterionType<?> type);

}
