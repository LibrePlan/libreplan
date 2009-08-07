package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.ICriterionService;
import org.navalplanner.business.resources.services.ICriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderElementModel implements IOrderElementModel {

    private OrderElement orderElement;

    private OrderModel order;

    @Autowired
    private IOrderElementDAO orderElementDao;

    @Autowired
    private ICriterionTypeDAO criterionTypeDao;

    @Autowired
    private ICriterionTypeService criterionTypeService;

    private Map<String, CriterionType> mapCriterionTypes = new HashMap<String, CriterionType>();

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    @Transactional(readOnly = true)
	public void setCurrent(OrderElement orderElement, OrderModel order) {
        orderElementDao.save(orderElement);

        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            hoursGroup.getCriterions().size();
        }

        this.orderElement = orderElement;
        this.order = order;
    }

    @Override
    public List<CriterionType> getCriterionTypes() {
        List<CriterionType> result = new ArrayList<CriterionType>();

        if (mapCriterionTypes.isEmpty())
            loadCriterionTypes();
        result.addAll(mapCriterionTypes.values());

        return result;
    }

    @Override
    public CriterionType getCriterionTypeByName(String name) {
        if (mapCriterionTypes.isEmpty())
            loadCriterionTypes();

        return mapCriterionTypes.get(name);
    }

    private void loadCriterionTypes() {
        for (CriterionType criterionType : criterionTypeService.getAll()) {
            mapCriterionTypes.put(criterionType.getName(), criterionType);
        }
    }

    @Override
    public List<Criterion> getCriterionsFor(CriterionType type) {
        return (List<Criterion>) order.getCriterionsFor(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Criterion> getCriterionsHoursGroup(HoursGroup hoursGroup) {
        return hoursGroup.getCriterions();
    }

    @Override
    @Transactional(readOnly = true)
    public CriterionType getCriterionType(Criterion criterion) {
        CriterionType criterionType = criterion.getType();
        criterionTypeDao.save(criterionType);
        criterionType.getName();
        return criterionType;
    }
}
