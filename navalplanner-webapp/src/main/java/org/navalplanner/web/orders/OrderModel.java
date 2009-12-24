/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 * Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IOrderSequenceDAO;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.OrderSequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
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
    private ICriterionTypeDAO criterionTypeDAO;

    private static final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    @Autowired
    private IOrderDAO orderDAO;

    private Order order;

    private OrderElementTreeModel orderElementTreeModel;

    @Autowired
    private IOrderElementModel orderElementModel;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    private Set<Label> cacheLabels = new HashSet<Label>();

    private Set<QualityForm> cacheQualityForms = new HashSet<QualityForm>();

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IOrderSequenceDAO orderSequenceDAO;

    @Override
    public List<Label> getLabels() {
        final List<Label> result = new ArrayList<Label>();
        result.addAll(cacheLabels);
        return result;
    }

    @Override
    public List<QualityForm> getQualityForms() {
        final List<QualityForm> result = new ArrayList<QualityForm>();
        result.addAll(cacheQualityForms);
        return result;
    }

    @Override
    public void addLabel(Label label) {
        cacheLabels.add(label);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderDAO.getOrders();
    }

    private void loadCriterions() {
        mapCriterions.clear();
        List<CriterionType> criterionTypes = criterionTypeDAO
                .getCriterionTypes();
        for (CriterionType criterionType : criterionTypes) {
            List<Criterion> criterions = new ArrayList<Criterion>(criterionDAO
                    .findByType(criterionType));

            mapCriterions.put(criterionType, criterions);
        }
    }

    @Override
    public Map<CriterionType, List<Criterion>> getMapCriterions(){
        final Map<CriterionType, List<Criterion>> result =
                new HashMap<CriterionType, List<Criterion>>();
        result.putAll(mapCriterions);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(Order order) {
        Validate.notNull(order);
        initializeCacheLabels();
        initializeCacheQualityForms();
        loadCriterions();
        this.order = getFromDB(order);
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
        forceLoadAdvanceAssignmentsAndMeasurements(this.order);
        forceLoadCriterionRequirements(this.order);
        forceLoadCalendar(this.getCalendar());
    }

    private void initializeCacheLabels() {
        if (cacheLabels.isEmpty()) {
            cacheLabels = new HashSet<Label>();
            final List<Label> labels = labelDAO.getAll();
            initializeLabels(labels);
            cacheLabels.addAll(labels);
        }
    }

    private void initializeLabels(Collection<Label> labels) {
        for (Label label : labels) {
            initializeLabel(label);
        }
    }

    private void initializeLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    private void initializeCacheQualityForms() {
        if (cacheQualityForms.isEmpty()) {
            cacheQualityForms = new HashSet<QualityForm>();
            final List<QualityForm> qualityForms = qualityFormDAO.getAll();
            initializeQualityForms(qualityForms);
            cacheQualityForms.addAll(qualityForms);
        }
    }

    private void initializeQualityForms(Collection<QualityForm> qualityForms) {
        for (QualityForm qualityForm : qualityForms) {
            initializeQualityForm(qualityForm);
        }
    }

    private void initializeQualityForm(QualityForm qualityForm) {
        qualityForm.getName();
        qualityForm.getQualityFormType();
        for (QualityFormItem qualityFormItem : qualityForm
                .getQualityFormItems()) {
            qualityFormItem.getName();
        }
    }

    private static void forceLoadCriterionRequirements(OrderElement orderElement) {
        orderElement.getHoursGroups().size();
        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            attachDirectCriterionRequirement(hoursGroup
                    .getDirectCriterionRequirement());
        }
        attachDirectCriterionRequirement(orderElement
                .getDirectCriterionRequirement());

        for (OrderElement child : orderElement.getChildren()) {
            forceLoadCriterionRequirements(child);
        }
    }

    private static void attachDirectCriterionRequirement(
            Set<DirectCriterionRequirement> requirements) {
        for (DirectCriterionRequirement requirement : requirements) {
            requirement.getChildren().size();
            requirement.getCriterion().getName();
            requirement.getCriterion().getType().getName();
        }
    }

    private void forceLoadAdvanceAssignmentsAndMeasurements(
            OrderElement orderElement) {
        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement
                .getDirectAdvanceAssignments()) {
            directAdvanceAssignment.getAdvanceType().getUnitName();
            for (AdvanceMeasurement advanceMeasurement : directAdvanceAssignment
                    .getAdvanceMeasurements()) {
                advanceMeasurement.getValue();
            }
        }

        if (orderElement instanceof OrderLineGroup) {
            for (IndirectAdvanceAssignment indirectAdvanceAssignment : ((OrderLineGroup) orderElement)
                    .getIndirectAdvanceAssignments()) {
                indirectAdvanceAssignment.getAdvanceType().getUnitName();
            }

            for (OrderElement child : orderElement.getChildren()) {
                forceLoadAdvanceAssignmentsAndMeasurements(child);
            }
        }
    }

    private Order getFromDB(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate() throws ConcurrentModificationException {
        loadCriterions();
        initializeCacheLabels();
        initializeCacheQualityForms();
        this.order = Order.create();
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
        this.order.setInitDate(new Date());
        this.order.setCalendar(getDefaultCalendar());

        setDefaultOrderCode();
    }

    private void setDefaultOrderCode() throws ConcurrentModificationException {
        String code = orderSequenceDAO.getNextOrderCode();
        if (code == null) {
            throw new ConcurrentModificationException(
                    _("Could not get order code, please try again later"));
        }
        this.order.setCode(code);
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        reattachCriterions();
        reattachTasksForTasksSources();

        if (order.isCodeAutogenerated()) {
            generateOrderElementCodes();
        }

        this.orderDAO.save(order);
        reattachCurrentTaskSources();
        deleteOrderElementWithoutParent();
        synchronizeWithSchedule(order);
    }

    private void generateOrderElementCodes() {
        OrderSequence orderSequence = orderSequenceDAO.getActiveOrderSequence();
        int numberOfDigits = orderSequence.getNumberOfDigits();

        for (OrderElement orderElement : order.getAllOrderElements()) {
            if ((orderElement.getCode() == null)
                    || (orderElement.getCode().isEmpty())) {
                order.incrementLastOrderElementSequenceCode();
                String orderElementCode = OrderSequence
                        .formatValue(numberOfDigits, order
                                .getLastOrderElementSequenceCode());
                orderElement.setCode(order.getCode()
                        + OrderSequence.CODE_SEPARATOR + orderElementCode);
            }
        }
    }

    private void reattachCurrentTaskSources() {
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            taskSourceDAO.reattach(each);
        }
    }

    private void reattachTasksForTasksSources() {
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            each.reloadTask(taskElementDAO);
        }
    }

    private void synchronizeWithSchedule(OrderElement orderElement) {
        for (TaskSourceSynchronization each : order
                .calculateSynchronizationsNeeded()) {
            each.apply(taskSourceDAO);
        }
    }

    private void deleteOrderElementWithoutParent() throws ValidationException {
        List<OrderElement> listToBeRemoved = orderElementDAO
                .findWithoutParent();
        for (OrderElement orderElement : listToBeRemoved) {
            if (!(orderElement instanceof Order)) {
                try {
                    orderElementDAO.remove(orderElement.getId());
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(_(""
                            + "It not could remove the order element "
                            + orderElement.getName()));
                }
            }
        }
    }

    private void reattachCriterions() {
        for (List<Criterion> list : mapCriterions.values()) {
            for (Criterion criterion : list) {
                criterionDAO.reattachUnmodifiedEntity(criterion);
            }
        }
    }

    @Override
    public IOrderLineGroup getOrder() {
        return order;
    }

    @Override
    @Transactional
    public void remove(Order order) {
        try {
            this.orderDAO.remove(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderElementTreeModel getOrderElementTreeModel() {
        return orderElementTreeModel;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderElementTreeModel getOrderElementsFilteredByPredicate(
            IPredicate predicate) {
        // Iterate through orderElements from order
        List<OrderElement> orderElements = new ArrayList<OrderElement>();
        for (OrderElement orderElement : order.getOrderElements()) {
            reattachOrderElement(orderElement);
            reattachLabels();
            initializeLabels(orderElement.getLabels());

            // Accepts predicate, add it to list of orderElements
            if (predicate.accepts(orderElement)) {
                orderElements.add(orderElement);
            }
        }
        // Return list of filtered elements
        return new OrderElementTreeModel(order, orderElements);
    }

    private void reattachLabels() {
        for (Label label : cacheLabels) {
            labelDAO.reattach(label);
        }
    }

    private void reattachQualityForms() {
        for (QualityForm qualityForm : cacheQualityForms) {
            qualityFormDAO.reattach(qualityForm);
        }
    }

    private void reattachOrderElement(OrderElement orderElement) {
        orderElementDAO.save(orderElement);
    }

    @Override
    @Transactional(readOnly = true)
    public IOrderElementModel getOrderElementModel(OrderElement orderElement) {
        reattachCriterions();
        orderElementModel.setCurrent(orderElement, this);
        return orderElementModel;
    }

    @Override
    public List<Criterion> getCriterionsFor(CriterionType criterionType) {
        return mapCriterions.get(criterionType);
    }

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    @Transactional(readOnly = true)
    public BaseCalendar getDefaultCalendar() {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration == null) {
            return null;
        }
        BaseCalendar defaultCalendar = configuration
                .getDefaultCalendar();
        forceLoadCalendar(defaultCalendar);
        return defaultCalendar;
    }

    private void forceLoadCalendar(BaseCalendar calendar) {
        calendar.getName();
    }

    @Override
    public BaseCalendar getCalendar() {
        if (order == null) {
            return null;
        }
        return order.getCalendar();
    }

    @Override
    public void setCalendar(BaseCalendar calendar) {
        if (order != null) {
            order.setCalendar(calendar);
        }
    }

    @Override
    public boolean isCodeAutogenerated() {
        if (order == null) {
            return false;
        }
        return order.isCodeAutogenerated();
    }

}
