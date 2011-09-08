/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

import java.math.BigDecimal;
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
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.orders.entities.TaskSource.IOptionalPersistence;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.IntegrationEntityModel;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.navalplanner.web.orders.labels.LabelsOnConversation;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.zul.Messagebox;

/**
 * Model for UI operations related to {@link Order}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/planner/index.zul;orders_list")
public class OrderModel extends IntegrationEntityModel implements IOrderModel {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private final Map<CriterionType, List<Criterion>> mapCriterions = new HashMap<CriterionType, List<Criterion>>();

    private List<ExternalCompany> externalCompanies = new ArrayList<ExternalCompany>();

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
    private IOrderElementTemplateDAO templateDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    private List<Order> orderList = new ArrayList<Order>();

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Label> getLabels() {
        return getLabelsOnConversation().getLabels();
    }

    private LabelsOnConversation labelsOnConversation;

    private LabelsOnConversation getLabelsOnConversation() {
        if (labelsOnConversation == null) {
            labelsOnConversation = new LabelsOnConversation(labelDAO);
        }
        return labelsOnConversation;
    }

    private QualityFormsOnConversation qualityFormsOnConversation;

    private Scenario currentScenario;

    private List<Scenario> derivedScenarios = new ArrayList<Scenario>();

    private boolean isEditing = false;

    private QualityFormsOnConversation getQualityFormsOnConversation() {
        if (qualityFormsOnConversation == null) {
            qualityFormsOnConversation = new QualityFormsOnConversation(
                    qualityFormDAO);
        }
        return qualityFormsOnConversation;
    }

    @Override
    public List<QualityForm> getQualityForms() {
        return getQualityFormsOnConversation().getQualityForms();
    }

    @Override
    public void addLabel(Label label) {
        getLabelsOnConversation().addLabel(label);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {

        User user;
        try {
            user = userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
            //anyway, if it happenned we return an empty list
            return new ArrayList<Order>();
        }
        getLabelsOnConversation().reattachLabels();
        List<Order> orders = orderDAO.getOrdersByReadAuthorizationByScenario(
                user, scenarioManager.getCurrent());

        initializeOrders(orders);
        return orders;
    }

    private void initializeOrders(List<Order> list) {
        for (Order order : list) {
            orderDAO.reattachUnmodifiedEntity(order);
            if (order.getCustomer() != null) {
                order.getCustomer().getName();
            }
            order.getAdvancePercentage();
            for (Label label : order.getLabels()) {
                label.getName();
            }
            order.getScenarios().size();
        }
        this.orderList = list;
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
        isEditing = true;
        loadNeededDataForConversation();
        this.order = getFromDB(order);
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
        forceLoadAdvanceAssignmentsAndMeasurements(this.order);
        forceLoadCriterionRequirements(this.order);
        forceLoadCalendar(this.getCalendar());
        forceLoadCustomer(this.order.getCustomer());
        forceLoadLabels(this.order);
        forceLoadMaterialAssignments(this.order);
        forceLoadTaskQualityForms(this.order);
        currentScenario = scenarioManager.getCurrent();
        this.order.useSchedulingDataFor(currentScenario);
        loadTasks(this.order);
        initOldCodes();
    }

    private void loadTasks(Order order) {
        TaskSource taskSource = order.getTaskSource();
        if (taskSource == null) {
            return;
        }
        loadTask(taskSource.getTask());
    }

    private void loadTask(TaskElement task) {
        task.getDependenciesWithThisDestination().size();
        task.getDependenciesWithThisOrigin().size();
        if (!task.isLeaf()) {
            for (TaskElement each : task.getChildren()) {
                loadTask(each);
            }
        }
    }

    private void forceLoadLabels(OrderElement orderElement) {
        orderElement.getLabels().size();
        for (OrderElement each : orderElement.getChildren()) {
            forceLoadLabels(each);
        }
    }

    private void forceLoadMaterialAssignments(OrderElement orderElement) {
        orderElement.getMaterialAssignments().size();
        for (OrderElement each : orderElement.getChildren()) {
            forceLoadMaterialAssignments(each);
        }
    }

    private void forceLoadTaskQualityForms(OrderElement orderElement) {
        orderElement.getTaskQualityForms().size();
        for (OrderElement each : orderElement.getChildren()) {
            forceLoadTaskQualityForms(each);
        }
    }

    private void forceLoadCustomer(ExternalCompany customer) {
        if (customer != null) {
            customer.getName();
        }
    }

    private void loadNeededDataForConversation() {
        loadCriterions();
        initializeCacheLabels();
        getQualityFormsOnConversation().initialize();
        loadExternalCompaniesAreClient();
    }

    private void reattachNeededDataForConversation() {
        reattachCriterions();
        reattachLabels();
        getQualityFormsOnConversation().reattach();
    }

    private void initializeCacheLabels() {
        getLabelsOnConversation().initializeLabels();
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
        loadNeededDataForConversation();
        this.order = Order.create();
        initializeOrder();
        initializeCalendar();
        currentScenario = scenarioManager.getCurrent();
        addOrderToCurrentScenario(this.order);
        this.order.useSchedulingDataFor(currentScenario);
    }

    private OrderVersion addOrderToCurrentScenario(Order order) {
        OrderVersion orderVersion = currentScenario.addOrder(order);
        order.setVersionForScenario(currentScenario, orderVersion);
        derivedScenarios = scenarioDAO.getDerivedScenarios(currentScenario);
        for (Scenario scenario : derivedScenarios) {
            scenario.addOrder(order, orderVersion);
        }
        return orderVersion;
    }

    private void initializeOrder() {
        this.orderElementTreeModel = new OrderElementTreeModel(this.order);
        this.order.setInitDate(new Date());
        setDefaultCode();
        this.order.setCodeAutogenerated(true);
    }

    private void initializeCalendar() {
        this.order.setCalendar(getDefaultCalendar());
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareCreationFrom(OrderTemplate template) {
        loadNeededDataForConversation();
        this.order = createOrderFrom((OrderTemplate) templateDAO
                .findExistingEntity(template.getId()));
        forceLoadAdvanceAssignmentsAndMeasurements(this.order);
        initializeOrder();
    }

    private Order createOrderFrom(OrderTemplate template) {
        return template.createOrder(scenarioManager.getCurrent());
    }

    private OrderElement createOrderElementFrom(OrderLineGroup parent,
            OrderElementTemplate template) {
        Validate.notNull(parent);
        return template.createElement(parent);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderElement createFrom(OrderLineGroup parent,
            OrderElementTemplate template) {
        reattachNeededDataForConversation();
        OrderElement result = createOrderElementFrom(parent, templateDAO
                .findExistingEntity(template.getId()));
        if (isCodeAutogenerated()) {
            setAllCodeToNull(result);
        }
        forceLoadAdvanceAssignmentsAndMeasurements(result);
        return result;
    }

    private void setAllCodeToNull(OrderElement result) {
        result.setCode(null);
        for (OrderElement each : result.getChildren()) {
            setAllCodeToNull(each);
        }
    }

    @Override
    public void save() throws ValidationException {
        final boolean newOrderVersionNeeded = isEditing
                && order.hasSchedulingDataBeingModified()
                && !order.isUsingTheOwnerScenario();
        if (!newOrderVersionNeeded || userAcceptsCreateANewOrderVersion()) {
            transactionService.runOnTransaction(new IOnTransaction<Void>() {
                @Override
                public Void execute() {
                    saveOnTransaction(newOrderVersionNeeded);
                    return null;
                }
            });
            dontPoseAsTransientObjectAnymore(order);
        }
    }

    private void dontPoseAsTransientObjectAnymore(Collection<? extends BaseEntity> collection) {
        for(BaseEntity entity : collection) {
            entity.dontPoseAsTransientObjectAnymore();
        }
    }

    private void dontPoseAsTransientObjectAnymore(OrderElement orderElement) {
        orderElement.dontPoseAsTransientObjectAnymore();
        dontPoseAsTransientObjectAnymore(orderElement.getTaskSourcesFromBottomToTop());
        dontPoseAsTransientObjectAnymore(orderElement.getSchedulingDatasForVersionFromBottomToTop());

        dontPoseAsTransientObjectAnymore(orderElement.getDirectAdvanceAssignments());
        dontPoseAsTransientObjectAnymore(getAllMeasurements(orderElement.getDirectAdvanceAssignments()));

        dontPoseAsTransientObjectAnymore(orderElement
                .getIndirectAdvanceAssignments());
        dontPoseAsTransientObjectAnymore(orderElement
                .getCriterionRequirements());
        dontPoseAsTransientObjectAnymore(orderElement.getLabels());
        dontPoseAsTransientObjectAnymore(orderElement.getTaskElements());
        dontPoseAsTransientObjectAnymore(orderElement.getHoursGroups());
        dontPoseAsTransientObjectAnymore(orderElement.getTaskQualityForms());
        dontPoseAsTransientObjectAnymore(orderElement
                .getAllMaterialAssignments());

        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            dontPoseAsTransientObjectAnymore(hoursGroup
                    .getCriterionRequirements());
        }

        for(OrderElement child : orderElement.getAllChildren()) {
            child.dontPoseAsTransientObjectAnymore();
            dontPoseAsTransientObjectAnymore(child);
        }
    }

    private List<AdvanceMeasurement> getAllMeasurements(
            Collection<? extends DirectAdvanceAssignment> assignments) {
        List<AdvanceMeasurement> result = new ArrayList<AdvanceMeasurement>();
        for (DirectAdvanceAssignment each : assignments) {
            result.addAll(each.getAdvanceMeasurements());
        }
        return result;
    }

    private void saveOnTransaction(boolean newOrderVersionNeeded) {
        checkConstraintOrderUniqueCode(order);
        checkConstraintHoursGroupUniqueCode(order);

        reattachCalendar();
        reattachCriterions();
        reattachTasksForTasksSources();

        if (order.isCodeAutogenerated()) {
            generateOrderElementCodes();
        }
        calculateAndSetTotalHours();
        orderDAO.save(order);
        reattachCurrentTaskSources();

        if (newOrderVersionNeeded) {
            OrderVersion newVersion = OrderVersion
                    .createInitialVersion(currentScenario);
            reattachAllTaskSources();
            order.writeSchedulingDataChangesTo(currentScenario, newVersion);
            createAndSaveNewOrderVersion(scenarioManager.getCurrent(),
                    newVersion);
            synchronizeWithSchedule(order,
                    TaskSource.persistButDontRemoveTaskSources(taskSourceDAO));
            order.writeSchedulingDataChanges();
        } else {
            OrderVersion orderVersion = order.getCurrentVersionInfo()
                    .getOrderVersion();
            orderVersion.savingThroughOwner();
            synchronizeWithSchedule(order,
                    TaskSource.persistTaskSources(taskSourceDAO));
            order.writeSchedulingDataChanges();
        }
        saveDerivedScenarios();
        deleteOrderElementWithoutParent();
    }

    private static void checkConstraintOrderUniqueCode(OrderElement order) {
        OrderElement repeatedOrder;

        // Check no code is repeated in this order
        if (order instanceof OrderLineGroup) {
            repeatedOrder = ((OrderLineGroup) order).findRepeatedOrderCode();
            if (repeatedOrder != null) {
                throw new ValidationException(_(
                        "Repeated Project code {0} in Project {1}",
                        repeatedOrder.getCode(), repeatedOrder.getName()));
            }
        }

        // Check no code is repeated within the DB
        repeatedOrder = Registry.getOrderElementDAO()
                .findRepeatedOrderCodeInDB(order);
        if (repeatedOrder != null) {
            throw new ValidationException(_(
                    "Repeated Project code {0} in Project {1}",
                    repeatedOrder.getCode(), repeatedOrder.getName()));
        }
    }

    private static void checkConstraintHoursGroupUniqueCode(Order order) {
        HoursGroup repeatedHoursGroup;

        if (order instanceof OrderLineGroup) {
            repeatedHoursGroup = ((OrderLineGroup) order)
                    .findRepeatedHoursGroupCode();
            if (repeatedHoursGroup != null) {
                throw new ValidationException(_(
                        "Repeated Hours Group code {0} in Project {1}",
                        repeatedHoursGroup.getCode(), repeatedHoursGroup
                                .getParentOrderLine().getName()));
            }
        }

        repeatedHoursGroup = Registry.getHoursGroupDAO()
                .findRepeatedHoursGroupCodeInDB(order.getHoursGroups());
        if (repeatedHoursGroup != null) {
            throw new ValidationException(_(
                    "Repeated Hours Group code {0} in Project {1}",
                    repeatedHoursGroup.getCode(), repeatedHoursGroup
                            .getParentOrderLine().getName()));
        }
    }

    private void createAndSaveNewOrderVersion(Scenario currentScenario,
            OrderVersion newOrderVersion) {
        OrderVersion previousOrderVersion = currentScenario
                .getOrderVersion(order);
        currentScenario.setOrderVersion(order, newOrderVersion);
        scenarioDAO.updateDerivedScenariosWithNewVersion(previousOrderVersion,
                order, currentScenario, newOrderVersion);
    }

    private boolean userAcceptsCreateANewOrderVersion() {
        try {
            int status = Messagebox
                    .show(
                            _("Confirm creating a new project version for this scenario and derived. Are you sure?"),
                            _("New project version"), Messagebox.OK
                                    | Messagebox.CANCEL, Messagebox.QUESTION);
            return (Messagebox.OK == status);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDerivedScenarios() {
        if (derivedScenarios != null) {
            for (Scenario scenario : derivedScenarios) {
                scenarioDAO.save(scenario);
            }
        }
    }

    private void calculateAndSetTotalHours() {
        Integer result = 0;
        for (OrderElement orderElement : order.getChildren()) {
            result = result + orderElement.getWorkHours();
        }
        order.setTotalHours(result);
    }

    private void generateOrderElementCodes() {
        order.generateOrderElementCodes(getNumberOfDigitsCode());
    }

    private void reattachCurrentTaskSources() {
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            taskSourceDAO.reattach(each);
        }
    }

    private void reattachCalendar() {
        if (order.getCalendar() == null) {
            return;
        }
        BaseCalendar calendar = order.getCalendar();
        baseCalendarDAO.reattachUnmodifiedEntity(calendar);
    }

    private void reattachAllTaskSources() {
        // avoid LazyInitializationException for when doing
        // removePredecessorsDayAssignmentsFor
        for (TaskSource each : order
                .getAllScenariosTaskSourcesFromBottomToTop()) {
            taskSourceDAO.reattach(each);
        }
    }

    private void reattachTasksForTasksSources() {
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            each.reattachTask(taskElementDAO);
        }
    }

    private void synchronizeWithSchedule(OrderElement orderElement,
            IOptionalPersistence persistence) {

        List<TaskSourceSynchronization> synchronizationsNeeded = orderElement
                .calculateSynchronizationsNeeded();
        for (TaskSourceSynchronization each : synchronizationsNeeded) {
            each.apply(persistence);
        }
    }

    private void deleteOrderElementWithoutParent() throws ValidationException {
        List<OrderElement> listToBeRemoved = orderElementDAO
                .findWithoutParent();
        for (OrderElement orderElement : listToBeRemoved) {
            if (!(orderElement instanceof Order)) {
                try {
                    // checking no work reports for that orderElement
                    if (!orderElementDAO
                            .isAlreadyInUseThisOrAnyOfItsChildren(orderElement)) {
                        orderElementDAO.remove(orderElement.getId());
                    }
                } catch (InstanceNotFoundException e) {
                    throw new ValidationException(_(""
                            + "It not could remove the task "
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
    public OrderLineGroup getOrder() {
        return order;
    }

    @Override
    @Transactional
    public void remove(Order detachedOrder) {
        Order order = orderDAO.findExistingEntity(detachedOrder.getId());
        removeVersions(order);
        if (order.hasNoVersions()) {
            removeOrderFromDB(order);
        }
    }

    private void removeVersions(Order order) {
        Map<Long, OrderVersion> versionsRemovedById = new HashMap<Long, OrderVersion>();
        List<Scenario> currentAndDerived = currentAndDerivedScenarios();
        for (Scenario each : currentAndDerived) {
            OrderVersion versionRemoved = order.disassociateFrom(each);
            if (versionRemoved != null) {
                versionsRemovedById.put(versionRemoved.getId(), versionRemoved);
            }
        }
        for (OrderVersion each : versionsRemovedById.values()) {
            if (!order.isVersionUsed(each)) {
                removeOrderVersionAt(each, currentAndDerived);
                removeOrderVersionFromDB(each);
            }
        }
    }

    private void removeOrderVersionAt(OrderVersion orderVersion,
            Collection<? extends Scenario> currentAndDerived) {
        for (Scenario each : currentAndDerived) {
            each.removeVersion(orderVersion);
        }
    }

    private List<Scenario> currentAndDerivedScenarios() {
        List<Scenario> scenariosToBeDisassociatedFrom = new ArrayList<Scenario>();
        Scenario currentScenario = scenarioManager.getCurrent();
        scenariosToBeDisassociatedFrom.add(currentScenario);
        scenariosToBeDisassociatedFrom.addAll(scenarioDAO
                .getDerivedScenarios(currentScenario));
        return scenariosToBeDisassociatedFrom;
    }

    private void removeOrderVersionFromDB(OrderVersion currentOrderVersion) {
        try {
            orderVersionDAO.remove(currentOrderVersion.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeOrderFromDB(Order order) {
        try {
            orderDAO.remove(order.getId());
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
        for (OrderElement orderElement : order.getAllOrderElements()) {
            if (!orderElement.isNewObject()) {
                reattachOrderElement(orderElement);
            }

            // Accepts predicate, add it to list of orderElements
            if (predicate.accepts(orderElement)) {
                orderElements.add(orderElement);
            }
        }
        // Return list of filtered elements
        return new OrderElementTreeModel(order, orderElements);
    }

    private void reattachLabels() {
        getLabelsOnConversation().reattachLabels();
    }

    private void reattachOrderElement(OrderElement orderElement) {
        orderElementDAO.reattach(orderElement);
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

    @Override
    public List<ExternalCompany> getExternalCompaniesAreClient() {
        return externalCompanies;
    }

    private void loadExternalCompaniesAreClient() {
        this.externalCompanies = externalCompanyDAO
                .getExternalCompaniesAreClient();
    }

    @Override
    public void setExternalCompany(ExternalCompany externalCompany) {
        if (this.getOrder() != null) {
            Order order = (Order) getOrder();
            order.setCustomer(externalCompany);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String gettooltipText(Order order) {
        orderDAO.reattachUnmodifiedEntity(order);
        StringBuilder result = new StringBuilder();
        result.append(_("Progress") + ": ").append(getEstimatedAdvance(order)).append("% , ");
        result.append(_("Hours invested") + ": ").append(
                getHoursAdvancePercentage(order)).append("% \n");

        if (!getDescription(order).equals("")) {
            result.append(" , " + _("Description") + ": "
                    + getDescription(order)
                    + "\n");
        }

        String labels = buildLabelsText(order);
        if (!labels.equals("")) {
            result.append(" , " + _("Labels") + ": " + labels);
        }
        return result.toString();
    }

    private String getDescription(Order order) {
        if (order.getDescription() != null) {
            return order.getDescription();
        }
        return "";
    }

    private BigDecimal getEstimatedAdvance(Order order) {
        return order.getAdvancePercentage().multiply(new BigDecimal(100));
    }

    private BigDecimal getHoursAdvancePercentage(Order order) {
        BigDecimal result;
        if (order != null) {
            result = orderElementDAO.getHoursAdvancePercentage(order);
        } else {
            result = new BigDecimal(0);
        }
        return result.multiply(new BigDecimal(100));
    }

    private String buildLabelsText(Order order) {
        StringBuilder result = new StringBuilder();
        Set<Label> labels = order.getLabels();
        if (!labels.isEmpty()) {
            for (Label label : labels) {
                result.append(label.getName()).append(",");
            }
            result.delete(result.length() - 1, result.length());
        }
        return result.toString();
    }

    /*
     * Operation to filter the order list
     */

    @Override
    @Transactional(readOnly = true)
    public List<Order> getFilterOrders(OrderPredicate predicate) {
        reattachLabels();
        List<Order> filterOrderList = new ArrayList<Order>();
        for (Order order : orderList) {
            orderDAO.reattach(order);
            if (predicate.accepts(order)) {
                filterOrderList.add(order);
            }
        }
        return filterOrderList;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userCanRead(Order order, String loginName) {
        if (SecurityUtils.isUserInRole(UserRole.ROLE_READ_ALL_ORDERS) ||
                SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return true;
        }
        if (order.isNewObject()
                & SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            return true;
        }
        try {
            User user = userDAO.findByLoginName(loginName);
            for(OrderAuthorization authorization :
                    orderAuthorizationDAO.listByOrderUserAndItsProfiles(order, user)) {
                if(authorization.getAuthorizationType() ==
                        OrderAuthorizationType.READ_AUTHORIZATION ||
                    authorization.getAuthorizationType() ==
                        OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    return true;
                }
            }
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
            //anyway, if it happenned we don't allow the user to pass
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userCanWrite(Order order, String loginName) {
        if (SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return true;
        }
        if (order.isNewObject()
                & SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            return true;
        }
        try {
            User user = userDAO.findByLoginName(loginName);
            for(OrderAuthorization authorization :
                    orderAuthorizationDAO.listByOrderUserAndItsProfiles(order, user)) {
                if(authorization.getAuthorizationType() ==
                        OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    return true;
                }
            }
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
            //anyway, if it happenned we don't allow the user to pass
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyInUse(OrderElement orderElement) {
        return orderElementDAO
                .isAlreadyInUseThisOrAnyOfItsChildren(orderElement);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyInUseAndIsOnlyInCurrentScenario(Order order) {
        return isAlreadyInUse(order) && (order.getScenarios().size() == 1);
    }

    @Override
    @Transactional(readOnly = true)
    public void useSchedulingDataForCurrentScenario(Order order) {
        orderDAO.reattach(order);
        order.useSchedulingDataFor(scenarioManager.getCurrent());
    }

    @Override
    public EntityNameEnum getEntityName() {
        return EntityNameEnum.ORDER;
    }

    @Override
    public Set<IntegrationEntity> getChildren() {
        Set<IntegrationEntity> children = new HashSet<IntegrationEntity>();
        if (order != null) {
            children.addAll(order.getOrderElements());
        }
        return children;
    }

    @Override
    public IntegrationEntity getCurrentEntity() {
        return this.order;
    }

}
