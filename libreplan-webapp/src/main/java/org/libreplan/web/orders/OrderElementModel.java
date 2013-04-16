/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ICostCategoryDAO;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Desktop;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderElementModel implements IOrderElementModel {

    private OrderElement orderElement;

    private OrderModel order;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICostCategoryDAO costCategoryDAO;

    private Map<String, CriterionType> mapCriterionTypes = new HashMap<String, CriterionType>();

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public IOrderModel getOrderModel() {
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public void setCurrent(OrderElement orderElement, OrderModel order) {
        orderElementDAO.reattach(orderElement);

        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            hoursGroup.getCriterionRequirements().size();
            hoursGroup.getValidCriterions().size();
        }

        this.orderElement = orderElement;
        this.order = order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriterionType> getCriterionTypes() {
        List<CriterionType> result = new ArrayList<CriterionType>();

        if (mapCriterionTypes.isEmpty()) {
            loadCriterionTypes();
        }
        result.addAll(mapCriterionTypes.values());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CriterionType getCriterionTypeByName(String name) {
        if (mapCriterionTypes.isEmpty()) {
            loadCriterionTypes();
        }

        return mapCriterionTypes.get(name);
    }

    private void loadCriterionTypes() {
        for (CriterionType criterionType : criterionTypeDAO.getCriterionTypes()) {
            criterionType.getCriterions().size();
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
        return hoursGroup.getValidCriterions();
    }

    @Override
    @Transactional(readOnly = true)
    public CriterionType getCriterionType(Criterion criterion) {
        CriterionType criterionType = criterion.getType();
        criterionTypeDAO.reattach(criterionType);
        criterionType.getName();
        return criterionType;
    }

    @Override
    public void confirmCancel() {

    }

    @Override
    @Transactional
    public void confirmSave() {
        orderElementDAO.save(orderElement);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeAutogenerated() {
        if (order == null) {
            return false;
        }
        return order.isCodeAutogenerated();
    }

    @Override
    @Transactional
    public void moveOrderElement(OrderElement orderElementTobeMoved,
            Order destinationOrder, Desktop desktop) {

        try {
            OrderElement orderElement = orderElementDAO
                    .find(orderElementTobeMoved.getId());

            Order sourceOrder = orderElement.getOrder();

            Order destOrder = orderDAO.find(destinationOrder.getId());

            add(destOrder, orderElement);

            remove(sourceOrder, orderElement);

            // save destination order
            getOrderModel().initEdit(destOrder, desktop);
            getOrderModel().save();


        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add the specified <code>{@link OrderElement}</code> to the given
     * <code>{@link Order}</code>
     *
     * @param order
     *            destination order
     * @param orderElement
     *            order element to be added
     */
    private void add(Order order, OrderElement orderElement) {
        Scenario currentScenario = scenarioManager.getCurrent();
        order.useSchedulingDataFor(order.getOrderVersionFor(currentScenario));
        order.add(orderElement);
    }

    /**
     * Remove the specified <code>{@link OrderElement}</code> from the given
     * <code>{@link Order}</code>
     *
     * @param order
     *            the source order
     * @param orderElement
     *            an order element to be removed
     */
    private void remove(Order order, OrderElement orderElement) {
        Scenario currentScenario = scenarioManager.getCurrent();
        order.useSchedulingDataFor(order.getOrderVersionFor(currentScenario));
        order.remove(orderElement);
    }
}
