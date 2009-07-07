package org.navalplanner.web.orders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.orders.daos.IOrderElementDao;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.CriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderElementModel implements IOrderElementModel {

    private OrderElement orderElement;

    @Autowired
    private IOrderElementDao orderElementDao;

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Autowired
    private CriterionService criterionService;

    @Autowired
    private CriterionTypeService criterionTypeService;

    private Map<String, CriterionType> mapCriterionTypes = new HashMap<String, CriterionType>();

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public void setCurrent(OrderElement orderElement) {
        // FIXME Review reattachment
        boolean wasTransient = orderElement.isTransient();
        Set<HoursGroup> transientHoursGroups = orderElement
                .getTransientHoursGroups();
        orderElementDao.save(orderElement);
        orderElement.forceLoadHourGroupsCriterions();
        if (wasTransient) {
            orderElement.makeTransientAgain();
        }
        for (HoursGroup hoursGroup : transientHoursGroups) {
            hoursGroup.makeTransientAgain();
        }
        this.orderElement = orderElement;
    }

    @Override
    public List<CriterionType> getCriterionTypes() {
        List<CriterionType> criterionTypes = criterionTypeService.getAll();

        if (mapCriterionTypes.isEmpty()) {
            for (CriterionType criterionType : criterionTypes) {
                mapCriterionTypes.put(criterionType.getName(), criterionType);
            }
        }

        return criterionTypes;
    }

    @Override
    public CriterionType getCriterionTypeByName(String name) {
        if (mapCriterionTypes.isEmpty()) {
            for (CriterionType criterionType : criterionTypeService
                    .getAll()) {
                mapCriterionTypes.put(criterionType.getName(), criterionType);
            }
        }

        return mapCriterionTypes.get(name);
    }

    @Override
    public List<Criterion> getCriterionsFor(CriterionType type) {
        return (List<Criterion>) criterionService.getCriterionsFor(type);
    }

}
