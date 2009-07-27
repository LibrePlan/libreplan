package org.navalplanner.web.orders;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;

public interface IOrderElementModel {

    OrderElement getOrderElement();

    void setCurrent(OrderElement orderElement, OrderModel order);

    List<CriterionType> getCriterionTypes();

    CriterionType getCriterionTypeByName(String name);

    List<Criterion> getCriterionsFor(CriterionType type);

    Set<Criterion> getCriterionsHoursGroup(HoursGroup hoursGroup);

    CriterionType getCriterionType(Criterion criterion);
}
