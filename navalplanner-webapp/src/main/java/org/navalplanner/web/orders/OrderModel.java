package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.services.IOrderService;
import org.navalplanner.business.planner.services.ITaskElementService;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.ICriterionService;
import org.navalplanner.business.resources.services.ICriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link Order}. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderModel implements IOrderModel {

    @Autowired
    ICriterionService criterionService;

    @Autowired
    ICriterionTypeService criterionTypeService;

    private static final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    private final IOrderService orderService;

    private Order order;

    private ClassValidator<Order> orderValidator = new ClassValidator<Order>(
            Order.class);

    private OrderElementTreeModel orderElementTreeModel;

    @Autowired
    private IOrderElementModel orderElementModel;

    private final ITaskElementService taskElementService;

    @Autowired
    public OrderModel(IOrderService orderService,
            ITaskElementService taskElementService) {
        Validate.notNull(orderService);
        Validate.notNull(taskElementService);
        this.orderService = orderService;
        this.taskElementService = taskElementService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderService.getOrders();
    }

    private void loadCriterions() {
        mapCriterions.clear();
        List<CriterionType> criterionTypes = criterionTypeService.getAll();
        for (CriterionType criterionType : criterionTypes) {
            List<Criterion> criterions = new ArrayList<Criterion>(
                    criterionService.getCriterionsFor(criterionType));

            mapCriterions.put(criterionType, criterions);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(Order order) {
        Validate.notNull(order);
        loadCriterions();

        this.order = getFromDB(order);
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
    }

    private Order getFromDB(Order order) {
        try {
            return orderService.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForCreate() {
        loadCriterions();

        this.order = new Order();
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
        this.order.setInitDate(new Date());
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = orderValidator.getInvalidValues(order);
        if (invalidValues.length > 0)
            throw new ValidationException(invalidValues);

        this.orderService.save(order);
    }

    @Override
    public IOrderLineGroup getOrder() {
        return order;
    }

    @Override
    public void remove(Order order) {
        try {
            this.orderService.remove(order);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForRemove(Order order) {
        this.order = order;
    }

    @Override
    public OrderElementTreeModel getOrderElementTreeModel() {
        return orderElementTreeModel;
    }

    @Override
    public IOrderElementModel getOrderElementModel(OrderElement orderElement) {
        orderElementModel.setCurrent(orderElement, this);
        return orderElementModel;
    }

    @Override
    public void prepareForSchedule(Order order) {
        this.order = order;
    }

    @Override
    @Transactional
    public void schedule() {
        taskElementService.convertToScheduleAndSave(getFromDB(order));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyScheduled(Order order) {
        return getFromDB(order).isSomeTaskElementScheduled();
    }

    public List<Criterion> getCriterionsFor(CriterionType criterionType) {
        return mapCriterions.get(criterionType);
    }
}
