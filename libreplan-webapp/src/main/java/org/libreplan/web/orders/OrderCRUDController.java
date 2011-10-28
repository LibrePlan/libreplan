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

import static org.libreplan.web.I18nHelper._;

import java.text.SimpleDateFormat;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.Order.SchedulingMode;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.orders.assigntemplates.TemplateFinderPopup;
import org.libreplan.web.orders.assigntemplates.TemplateFinderPopup.IOnResult;
import org.libreplan.web.orders.criterionrequirements.AssignedCriterionRequirementToOrderElementController;
import org.libreplan.web.orders.labels.AssignedLabelsToOrderElementController;
import org.libreplan.web.orders.labels.LabelsAssignmentToOrderElementComponent;
import org.libreplan.web.orders.materials.AssignedMaterialsToOrderElementController;
import org.libreplan.web.orders.materials.OrderElementMaterialAssignmentsComponent;
import org.libreplan.web.planner.order.IOrderPlanningGate;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.templates.IOrderTemplatesControllerEntryPoints;
import org.libreplan.web.tree.TreeComponent;
import org.libreplan.web.users.OrderAuthorizationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.api.Listbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCRUDController extends GenericForwardComposer {

    private static final String DEFAULT_TAB = "tabOrderElements";

    private static final class LabelCreatorForInvalidValues implements
            IMessagesForUser.ICustomLabelCreator {

        @Override
        public Component createLabelFor(
                InvalidValue invalidValue) {
            if (invalidValue.getBean() instanceof OrderElement) {
                Label result = new Label();

                String orderElementName;
                if (invalidValue.getBean() instanceof Order) {
                    orderElementName = _("Project");
                } else {
                    orderElementName = ((OrderElement) invalidValue
                            .getBean()).getName();
                }

                result.setValue(orderElementName + " "
                        + invalidValue.getPropertyName() + ": "
                        + invalidValue.getMessage());
                return result;
            } else if (invalidValue.getBean() instanceof HoursGroup) {
                Label result = new Label();
                HoursGroup hoursGroup = (HoursGroup) invalidValue.getBean();
                result.setValue(_("Hours Group at ")
                        + getParentName(hoursGroup) + ". "
                        + invalidValue.getPropertyName() + ": "
                        + invalidValue.getMessage());
                return result;
            }else {
                return MessagesForUser.createLabelFor(invalidValue);
            }
        }

        private String getParentName(HoursGroup hoursGroup) {
            return (hoursGroup.getParentOrderLine() != null) ? hoursGroup
                    .getParentOrderLine().getName() : hoursGroup
                    .getOrderLineTemplate().getName();
        }
    }

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    @Autowired
    private IOrderModel orderModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private TemplateFinderPopup templateFinderPopup;

    public void createOrderFromTemplate() {
        templateFinderPopup.openForOrderCreation(createOrderFromTemplateButton,
                "after_start", new IOnResult<OrderTemplate>() {

                    @Override
                    public void found(OrderTemplate template) {
                        showCreateFormFromTemplate(template);
                    }
                });
    }

    public void showCreateFormFromTemplate(OrderTemplate template) {
        showOrderElementFilter();
        showCreateButtons(false);
        orderModel.prepareCreationFrom(template, getDesktop());
        prepareEditWindow();
        showEditWindow(_("Create project from Template"));
    }

    @Resource
    private IOrderTemplatesControllerEntryPoints orderTemplates;

    private Window editWindow;

    private OrderDatesHandler orderDatesHandler;

    private Window editOrderElementWindow;

    private Window listWindow;

    private Tab selectedTab;

    private Grid listing;

    private Hbox orderFilter;

    private Vbox orderElementFilter;

    private Button createOrderButton;
    private Button createOrderFromTemplateButton;
    private Button saveOrderAndContinueButton;
    private Button cancelEditionButton;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private BandboxMultipleSearch bdFilters;

    private Checkbox checkIncludeOrderElements;

    private BandboxSearch bdExternalCompanies;

    private OnlyOneVisible cachedOnlyOneVisible;

    private IOrderPlanningGate planningControllerEntryPoints;

    private BaseCalendarsComboitemRenderer baseCalendarsComboitemRenderer = new BaseCalendarsComboitemRenderer();

    private OrdersRowRenderer ordersRowRenderer = new OrdersRowRenderer();

    private OrderElementTreeController orderElementTreeController;

    private ProjectDetailsController projectDetailsController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);

        // Configuration of the order filter
        Component filterComponent = Executions.createComponents(
                "/orders/_orderFilter.zul", orderFilter,
                new HashMap<String, String>());
        filterComponent.setVariable("orderFilterController", this, true);
        filterStartDate = (Datebox) filterComponent
                .getFellow("filterStartDate");
        filterFinishDate = (Datebox) filterComponent
                .getFellow("filterFinishDate");
        bdFilters = (BandboxMultipleSearch) filterComponent
                .getFellow("bdFilters");
        checkIncludeOrderElements = (Checkbox) filterComponent
                .getFellow("checkIncludeOrderElements");

        checkCreationPermissions();
        setupGlobalButtons();

    }

    private void setupGlobalButtons() {

        Hbox perspectiveButtonsInsertionPoint = (Hbox) page
                .getFellow("perspectiveButtonsInsertionPoint");

        List<Component> children = perspectiveButtonsInsertionPoint
                .getChildren();
        perspectiveButtonsInsertionPoint.getChildren().removeAll(children);

        createOrderButton.setParent(perspectiveButtonsInsertionPoint);
        createOrderButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
            @Override
                    public void onEvent(Event event) throws Exception {
                goToCreateForm();
                    }
                });

        createOrderFromTemplateButton
                .setParent(perspectiveButtonsInsertionPoint);
        createOrderFromTemplateButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        createOrderFromTemplate();
            }
        });

        saveOrderAndContinueButton.setParent(perspectiveButtonsInsertionPoint);
        saveOrderAndContinueButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        saveAndContinue();
                    }
                });

        cancelEditionButton.setParent(perspectiveButtonsInsertionPoint);
        cancelEditionButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        try {
                            Messagebox
                                    .show(_("Unsaved changes will be lost. Are you sure?"),
                                            _("Confirm exit dialog"),
                                            Messagebox.OK | Messagebox.CANCEL,
                                            Messagebox.QUESTION,
                                            new org.zkoss.zk.ui.event.EventListener() {
                                                public void onEvent(Event evt)
                                                        throws InterruptedException {
                                                    if (evt.getName().equals(
                                                            "onOK")) {
                                                        Executions
                                                                .sendRedirect("/planner/index.zul;company_scheduling");
                                                    }
                                                }
                                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void initEditOrderElementWindow() {
        final Component parent = listWindow.getParent();

        Map<String, Object> editOrderElementArgs = new HashMap<String, Object>();
        editOrderElementArgs.put("top_id", "editOrderElement");
        editOrderElementWindow = (Window) Executions.createComponents(
                "/orders/_editOrderElement.zul", parent, editOrderElementArgs);

        Util.createBindingsFor(editOrderElementWindow);
        Util.reloadBindings(editOrderElementWindow);
    }

    private void addEditWindowIfNecessary() {
        if (editWindow != null) {
            return;
        }

        listWindow.setVisible(false);
        cachedOnlyOneVisible = null;

        Map<String, Object> editWindowArgs = new HashMap<String, Object>();
        editWindowArgs.put("top_id", "editWindow");
        Component parent = listWindow.getParent();
        editWindow = (Window) Executions.createComponents(
                "/orders/_edition.zul", parent, editWindowArgs);

        Util.createBindingsFor(editWindow);
        Util.reloadBindings(editWindow);
    }

    private class OrderDatesHandler {

        private final Combobox schedulingMode;

        private final Datebox initDate;

        private final Datebox deadline;

        public OrderDatesHandler(Window editWindow) {
            schedulingMode = Util.findComponentAt(editWindow, "schedulingMode");
            initDate = Util.findComponentAt(editWindow, "initDate");
            deadline = Util.findComponentAt(editWindow, "deadline");
            initializeSchedulingModeCombobox();
        }

        private void initializeSchedulingModeCombobox() {
            fillSchedulingModes();
            listenToChangeOfMode();
        }

        private void fillSchedulingModes() {
            List options = schedulingMode.getChildren();
            if (options != null && options.isEmpty()) {
                schedulingMode.appendChild(createCombo(SchedulingMode.FORWARD,
                        _("Forward"), _("Schedule from start to deadline")));
                schedulingMode.appendChild(createCombo(
                        SchedulingMode.BACKWARDS, _("Backwards"),
                        _("Schedule from the deadline to start")));
            }
        }

        void chooseCurrentSchedulingMode() {
            @SuppressWarnings("unchecked")
            List<Comboitem> items = schedulingMode.getItems();
            SchedulingMode currentMode = getOrder().getSchedulingMode();
            for (Comboitem each : items) {
                if (each.getValue().equals(currentMode)) {
                    schedulingMode.setSelectedItem(each);
                    setConstraintsFor(currentMode);
                    return;
                }
            }
        }

        private void listenToChangeOfMode() {
            schedulingMode.addEventListener(Events.ON_SELECT,
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            SchedulingMode chosen = (SchedulingMode) schedulingMode
                                    .getSelectedItem().getValue();
                            if (chosen != null) {
                                getOrder().setSchedulingMode(chosen);
                                setConstraintsFor(chosen);
                                changeFocusAccordingTo(chosen);
                            }
                        }

                    });
        }

        private Comboitem createCombo(Object value, String label,
                String description) {
            Comboitem result = new Comboitem();
            result.setValue(value);
            result.setLabel(label);
            result.setDescription(description);
            return result;
        }

        private void setConstraintsFor(SchedulingMode mode) {
            initDate.setConstraint(mode == SchedulingMode.FORWARD ? "no empty"
                    : null);
            deadline.setConstraint(mode == SchedulingMode.BACKWARDS ? "no empty"
                    : null);
        }

        private void changeFocusAccordingTo(SchedulingMode chosen) {
            initDate.setFocus(SchedulingMode.FORWARD == chosen);
            deadline.setFocus(SchedulingMode.BACKWARDS == chosen);
        }
    }

    private void bindListOrderStatusSelectToOnStatusChange() {
        Listbox listOrderStatus = (Listbox) editWindow
                .getFellow("listOrderStatus");
        listOrderStatus.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                updateDisabilitiesOnInterface();
            }
        });
    }

    public void setupOrderElementTreeController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        if (orderElementTreeController == null) {
            // Create order element edit window
            OrderElementController orderElementController = new OrderElementController();
            if (editOrderElementWindow == null) {
                initEditOrderElementWindow();
            }
            try {
                orderElementController.doAfterCompose(self
                        .getFellow("editOrderElement"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Prepare tree, attach edit window to tree
            orderElementTreeController = new OrderElementTreeController(
                    orderModel, orderElementController, messagesForUser);
            TreeComponent orderElementsTree = (TreeComponent) editWindow
                    .getFellow("orderElementTree");
            orderElementTreeController.setTreeComponent(orderElementsTree);
            orderElementsTree.useController(orderElementTreeController);
            orderElementTreeController.setReadOnly(readOnly);

            Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
            tree.setModel(null);
            tree.setTreeitemRenderer(orderElementTreeController.getRenderer());

            reloadTree(orderElementsTree);
        }
    }

    private void reloadTree(TreeComponent orderElementsTree) {
        final Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
        tree.setModel(orderElementTreeController.getTreeModel());
        tree.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                //undo the work done by this event
                //to be able to control it from the ON_CLICK event
                tree.clearSelection();
            }
        });
    }

    /*
     * Operations to do before to change the selected tab
     */
    private boolean confirmLastTab() {
        if (getCurrentTab() != null) {
            // Confirm advances tab.
            if (getCurrentTab().getId().equals("tabAdvances")) {
                if (manageOrderElementAdvancesController != null
                        && !manageOrderElementAdvancesController.save()) {
                    resetSelectedTab();
                    selectTab("tabAdvances");
                    return false;
                }
            }
        }
        return true;
    }

    private IOrderElementModel getOrderElementModel() {
        final Order order = (Order) orderModel.getOrder();
        return orderModel.getOrderElementModel(order);
    }

    private AssignedHoursToOrderElementController assignedHoursController;

    public void setupAssignedHoursToOrderElementController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        if (assignedHoursController == null) {
            Component orderElementHours = editWindow
                    .getFellowIfAny("orderElementHours");
            assignedHoursController = (AssignedHoursToOrderElementController) orderElementHours
                    .getVariable("assignedHoursToOrderElementController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedHoursController.openWindow(orderElementModel);
        }
    }

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    public void setupManageOrderElementAdvancesController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        Component orderElementAdvances = editWindow
                .getFellowIfAny("orderElementAdvances");
        if (manageOrderElementAdvancesController == null) {
            final IOrderElementModel orderElementModel = getOrderElementModel();
            manageOrderElementAdvancesController = (ManageOrderElementAdvancesController) orderElementAdvances
                    .getVariable("manageOrderElementAdvancesController", true);
            manageOrderElementAdvancesController.openWindow(orderElementModel);
        } else {
            manageOrderElementAdvancesController.refreshChangesFromOrderElement();
            manageOrderElementAdvancesController.createAndLoadBindings();
            manageOrderElementAdvancesController.refreshSelectedAdvance();
        }
    }

    private AssignedLabelsToOrderElementController assignedLabelsController;

    public void setupAssignedLabelsToOrderElementController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        if (assignedLabelsController == null) {
            LabelsAssignmentToOrderElementComponent labelsAssignment = (LabelsAssignmentToOrderElementComponent) editWindow
                .getFellow("orderElementLabels");
            assignedLabelsController = labelsAssignment.getController();

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedLabelsController.openWindow(orderElementModel);
        }
    }

    private AssignedCriterionRequirementToOrderElementController assignedCriterionRequirementController;

    public void setupAssignedCriterionRequirementsToOrderElementController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        if (assignedCriterionRequirementController == null) {
            Component orderElementCriterionRequirements = editWindow
                .getFellowIfAny("orderElementCriterionRequirements");
            assignedCriterionRequirementController = (AssignedCriterionRequirementToOrderElementController) orderElementCriterionRequirements
                .getVariable("assignedCriterionRequirementController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedCriterionRequirementController
                    .openWindow(orderElementModel);
        } else {
            reloadHoursGroupOrder();
        }
    }

    private AssignedMaterialsToOrderElementController assignedMaterialsController;

    public void setupAssignedMaterialsToOrderElementController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        if (assignedMaterialsController == null) {
            OrderElementMaterialAssignmentsComponent assignmentsComponent = (OrderElementMaterialAssignmentsComponent) editWindow
                .getFellowIfAny("orderElementMaterials");
            assignedMaterialsController = assignmentsComponent.getController();

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedMaterialsController.openWindow(orderElementModel
                    .getOrderElement());
        }
    }

    private AssignedTaskQualityFormsToOrderElementController assignedTaskQualityFormController;

    public void setupAssignedTaskQualityFormsToOrderElementController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        Component orderElementTaskQualityForms = editWindow
                .getFellowIfAny("orderElementTaskQualityForms");
        if (assignedTaskQualityFormController == null) {
            assignedTaskQualityFormController = (AssignedTaskQualityFormsToOrderElementController) orderElementTaskQualityForms
                .getVariable("assignedTaskQualityFormsController", true);
            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedTaskQualityFormController.openWindow(orderElementModel);
        } else {
            Util.createBindingsFor(orderElementTaskQualityForms);
            Util.reloadBindings(orderElementTaskQualityForms);
        }
    }

    private OrderAuthorizationController orderAuthorizationController;

    public void setupOrderAuthorizationController() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();

        Component orderElementAuthorizations = editWindow
                .getFellowIfAny("orderElementAuthorizations");
        if (orderAuthorizationController == null) {
            orderAuthorizationController = (OrderAuthorizationController) orderElementAuthorizations
                    .getVariable("orderAuthorizationController", true);
            orderAuthorizationController
                    .setMessagesForUserComponent(messagesForUser);
            initOrderAuthorizations();
        } else {
            Util.createBindingsFor(orderElementAuthorizations);
            Util.reloadBindings(orderElementAuthorizations);
        }
    }

    private void initOrderAuthorizations() {
        Component orderElementAuthorizations = editWindow
                .getFellowIfAny("orderElementAuthorizations");
        final Order order = (Order) orderModel.getOrder();
        if (order.isNewObject()) {
            orderAuthorizationController.initCreate(orderModel
                    .getPlanningState());
        } else {
            orderAuthorizationController
                    .initEdit(orderModel.getPlanningState());
        }
        Util.createBindingsFor(orderElementAuthorizations);
        Util.reloadBindings(orderElementAuthorizations);
    }

    private void saveOrderAuthorizations() {
        setupOrderAuthorizationController();
        orderAuthorizationController.save();
    }

    public List<Order> getOrders() {
        return orderModel.getOrders();
    }

    private OnlyOneVisible getVisibility() {
        if (cachedOnlyOneVisible == null) {
            cachedOnlyOneVisible = new OnlyOneVisible(listWindow);
        }
        return cachedOnlyOneVisible;
    }

    public Order getOrder() {
        return (Order) orderModel.getOrder();
    }

    public void saveAndContinue() {
        saveAndContinue(true);
    }

    private void saveAndContinue(boolean showSaveMessage) {

        Order order = (Order) orderModel.getOrder();
        final boolean isNewObject = order.isNewObject();
        setCurrentTab();
        Tab previousTab = getCurrentTab();
        save(showSaveMessage);

        if (orderModel.userCanRead(order,
                SecurityUtils.getSessionUserLoginName())) {
            refreshOrderWindow();

            // come back to the current tab after initialize all tabs.
            resetSelectedTab();
            selectTab(previousTab.getId());
            Events.sendEvent(new SelectEvent(Events.ON_SELECT, previousTab,
                    null));

            if (isNewObject) {
                this.planningControllerEntryPoints.goToOrderDetails(order);
            }
        } else {
            try {
                Messagebox
                        .show(_("You don't have read access to this project"),
                                _("Information"), Messagebox.OK,
                                Messagebox.INFORMATION);
                goToList();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void refreshOrderWindow() {
        if (orderElementTreeController != null) {
            orderElementTreeController.resetCellsMarkedAsModified();
        }
        updateDisabilitiesOnInterface();
        refreshCodeTextboxesOnly();
        getVisibility().showOnly(editWindow);
    }

    private void refreshCodeTextboxesOnly() {
        if (orderElementTreeController != null) {
            Map<OrderElement, Textbox> orderElementCodeTextBoxes =
                orderElementTreeController.getOrderElementCodeTextboxes();

            for (OrderElement element : orderElementCodeTextBoxes.keySet()) {
                if (element.getId() != null) {
                    orderElementCodeTextBoxes.get(element).setValue(
                            element.getCode());
                }
            }
        }
    }

    private void save() {
        save(true);
    }

    private void save(boolean showSaveMessage) {
        if (manageOrderElementAdvancesController != null) {
            selectTab("tabAdvances");
            if (!manageOrderElementAdvancesController.save()) {
                setCurrentTab();
                return;
            }
        }
        if (assignedCriterionRequirementController != null) {
            selectTab("tabRequirements");
            if (!assignedCriterionRequirementController.close()) {
                setCurrentTab();
                return;
            }
        }
        if (assignedTaskQualityFormController != null) {
            selectTab("tabTaskQualityForm");
            if (!assignedTaskQualityFormController.confirm()) {
                setCurrentTab();
                return;
            }
        }

        // come back to the default tab.
        if (getCurrentTab() != null) {
            selectTab(getCurrentTab().getId());
        }

        orderModel.save(showSaveMessage);
        try {
            saveOrderAuthorizations();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e, new LabelCreatorForInvalidValues());
        }
    }

    Tab tabGeneralData;

    private void selectDefaultTab() {
        selectTab(DEFAULT_TAB);
    }

    private void resetSelectedTab() {
        selectedTab = null;
    }

    private void setCurrentTab() {
        Tabbox tabboxOrder = (Tabbox) editWindow.getFellowIfAny("tabboxOrder");
        if (tabboxOrder != null) {
            selectedTab = tabboxOrder.getSelectedTab();
        }
    }

    private Tab getCurrentTab() {
        return selectedTab;
    }

    private void selectTab(String str) {
        Tab tab = (Tab) editWindow.getFellowIfAny(str);
        if (tab != null) {
            tab.setSelected(true);
        }
    }

    public void goToList() {
        loadComponents();
        showWindow(listWindow);
    }

    private void loadComponents() {
        // load the components of the order list
        listing = (Grid) listWindow.getFellow("listing");
        showOrderFilter();
        showCreateButtons(true);
        clearFilterDates();
    }

    private void showWindow(Window window) {
        getVisibility().showOnly(window);
        Util.reloadBindings(window);
    }

    public void reloadHoursGroupOrder() {
        if (getCurrentTab().getId().equals("tabRequirements")) {
            assignedCriterionRequirementController.reload();
        }
    }

    public void cancel() {
        goToList();
    }

    public void up() {
        if (onUp == null) {
            throw new IllegalStateException(
                    "in order to call up onUp action should have been set");
        }
        onUp.run();
    }

    public void confirmRemove(Order order) {
        if(orderModel.userCanWrite(order, SecurityUtils.getSessionUserLoginName())) {
            try {
                int status = Messagebox.show(_("Confirm deleting {0}. Are you sure?", order.getName()),
                        "Delete", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
                if (Messagebox.OK == status) {
                    remove(order);
                }
            } catch (InterruptedException e) {
                messagesForUser.showMessage(
                        Level.ERROR, e.getMessage());
                LOG.error(_("Error on showing removing element: ", order.getId()), e);
            }
        }
        else {
            try {
                Messagebox.show(_("You don't have permissions to edit this project"),
                        _("Information"), Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void remove(Order order) {
        boolean alreadyInUse = orderModel.isAlreadyInUseAndIsOnlyInCurrentScenario(order);
        if (alreadyInUse) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _(
                                    "You can not remove the project \"{0}\" because of any of its tasks are already in use in some work reports and the project just exists in the current scenario",
                                    order.getName()));
        } else {
            if (!StringUtils.isBlank(order.getExternalCode())) {
                try {
                    if (Messagebox
                            .show(
                                    _("Deleting this subcontracted project, you are going to lose the relation to report progress. Are you sure?"),
                                    _("Confirm"), Messagebox.OK
                                            | Messagebox.CANCEL,
                                    Messagebox.QUESTION) == Messagebox.CANCEL) {
                        return;
                    }
                } catch (InterruptedException e) {
                    messagesForUser.showMessage(Level.ERROR, e.getMessage());
                    LOG.error(_("Error on showing removing element: ", order
                            .getId()), e);
                }
            }

            orderModel.remove(order);
            Util.reloadBindings(self);
            messagesForUser.showMessage(Level.INFO, _("Removed {0}", order
                    .getName()));
        }
    }

    public void schedule(Order order) {
        orderModel.useSchedulingDataForCurrentScenario(order);
        if(orderModel.userCanRead(order, SecurityUtils.getSessionUserLoginName())) {
            if (order.isScheduled()) {
                planningControllerEntryPoints.goToScheduleOf(order);
                showCreateButtons(false);
            } else {
                try {
                    Messagebox.show(_("The project has no scheduled elements"),
                            _("Information"), Messagebox.OK, Messagebox.INFORMATION);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            try {
                Messagebox.show(_("You don't have read access to this project"),
                        _("Information"), Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createTemplate(Order order) {
        orderTemplates.goToCreateTemplateFrom(order);
    }

    private Runnable onUp;

    public void goToEditForm(Order order) {
        showOrderElementFilter();
        showCreateButtons(false);
        planningControllerEntryPoints.goToOrderDetails(order);
    }

    public void initEdit(Order order) {
        if (!orderModel.userCanRead(order, SecurityUtils.getSessionUserLoginName())) {
            try {
                Messagebox.show(_("Sorry, you do not have permissions to access this project"),
                        _("Information"), Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        orderModel.initEdit(order, getDesktop());
        if (editWindow != null) {
            resetTabControllers();
            setupOrderElementTreeController();
            selectDefaultTab();
            return;
        }

        prepareEditWindow();
        showEditWindow(_("Edit project"));
    }

    private Desktop getDesktop() {
        return listWindow.getDesktop();
    }

    private void resetTabControllers() {
        orderElementTreeController = null;
        assignedHoursController = null;
        manageOrderElementAdvancesController = null;
        assignedLabelsController = null;
        assignedCriterionRequirementController = null;
        assignedMaterialsController = null;
        assignedTaskQualityFormController = null;
        orderAuthorizationController = null;
    }

    private void prepareEditWindow() {
        addEditWindowIfNecessary();
        updateDisabilitiesOnInterface();
        setupOrderElementTreeController();
        selectDefaultTab();
    }

    private void showEditWindow(String title) {
        initializeTabs();
        editWindow.setTitle(title);
        getVisibility().showOnly(editWindow);
    }

    private void initializeCustomerComponent() {
        bdExternalCompanies = (BandboxSearch) editWindow
                .getFellow("bdExternalCompanies");
        bdExternalCompanies.setListboxEventListener(
                Events.ON_SELECT, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        final Object object = bdExternalCompanies
                                .getSelectedElement();
                        orderModel.setExternalCompany((ExternalCompany) object);
                    }
                });
        bdExternalCompanies.setListboxEventListener(Events.ON_OK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        final Object object = bdExternalCompanies
                                .getSelectedElement();
                        orderModel.setExternalCompany((ExternalCompany) object);
                        bdExternalCompanies.close();
                    }
                });

    }

    public void setupOrderDetails() {
        if (!confirmLastTab()) {
            return;
        }
        setCurrentTab();
        orderDatesHandler = new OrderDatesHandler(editWindow);
        bindListOrderStatusSelectToOnStatusChange();
        initializeCustomerComponent();
        reloadOrderDetailsTab();
        orderDatesHandler.chooseCurrentSchedulingMode();
    }

    public void reloadOrderDetailsTab() {
        Tabpanel tabPanel = (Tabpanel) editWindow
                .getFellow("tabPanelGeneralData");
        Util.createBindingsFor(tabPanel);
        Util.reloadBindings(tabPanel);
    }

    private void initializeTabs() {
        final IOrderElementModel orderElementModel = getOrderElementModel();

        if (orderElementTreeController != null){
            TreeComponent orderElementsTree = (TreeComponent) editWindow
                    .getFellow("orderElementTree");
            reloadTree(orderElementsTree);
        }
        if (assignedHoursController != null) {
            assignedHoursController.openWindow(orderElementModel);
        }
        if (manageOrderElementAdvancesController != null) {
            manageOrderElementAdvancesController.openWindow(orderElementModel);
        }
        if (assignedLabelsController != null) {
            assignedLabelsController.openWindow(orderElementModel);
        }
        if (assignedCriterionRequirementController != null) {
            assignedCriterionRequirementController
                    .openWindow(orderElementModel);
        }
        if (assignedMaterialsController != null) {
            assignedMaterialsController.openWindow(orderElementModel
                    .getOrderElement());
        }
        if (assignedTaskQualityFormController != null) {
            assignedTaskQualityFormController.openWindow(orderElementModel);
        }
        if (orderAuthorizationController != null) {
            initOrderAuthorizations();
        }
    }

    public void goToCreateForm() {
        prepareForCreate(getDesktop());
        getCreationPopup().showWindow(this, null);
    }

    public void prepareForCreate(Desktop desktop) {
        orderModel.prepareForCreate(desktop);
    }

    private void editNewCreatedOrder() {
        showOrderElementFilter();
        hideCreateButtons();
        prepareEditWindow();
        showEditWindow(_("Create project"));
    }

    public void editNewCreatedOrder(Window detailsWindow) {
        editNewCreatedOrder();
        // close project details window
        detailsWindow.setVisible(false);
        saveAndContinue(false);
    }

    public ProjectDetailsController getCreationPopup() {
        if (projectDetailsController == null) {
            projectDetailsController = new ProjectDetailsController();
        }
        return projectDetailsController;
    }

    private void hideCreateButtons() {
        showCreateButtons(false);
    }

    public void setPlanningControllerEntryPoints(
            IOrderPlanningGate planningControllerEntryPoints) {
        this.planningControllerEntryPoints = planningControllerEntryPoints;
    }

    public IOrderPlanningGate getPlanningControllerEntryPoints() {
        return this.planningControllerEntryPoints;
    }

    public void setActionOnUp(Runnable onUp) {
        this.onUp = onUp;
    }

    public List<BaseCalendar> getBaseCalendars() {
        return orderModel.getBaseCalendars();
    }

    public BaseCalendarsComboitemRenderer getBaseCalendarsComboitemRenderer() {
        return baseCalendarsComboitemRenderer;
    }

    private class BaseCalendarsComboitemRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem item, Object data) {
            BaseCalendar calendar = (BaseCalendar) data;
            item.setLabel(calendar.getName());
            item.setValue(calendar);

            BaseCalendar current = orderModel.getCalendar();
            if ((current != null) && calendar.getId().equals(current.getId())) {
                Combobox combobox = (Combobox) item.getParent();
                combobox.setSelectedItem(item);
            }
        }

    }

    public void setBaseCalendar(BaseCalendar calendar) {
        orderModel.setCalendar(calendar);
    }

    public boolean isCodeAutogenerated() {
        return orderModel.isCodeAutogenerated();
    }

    public void setCodeAutogenerated(boolean codeAutogenerated) {
        try {
            orderModel.setCodeAutogenerated(codeAutogenerated);
            if (orderElementTreeController != null) {
                orderElementTreeController.disabledCodeBoxes(codeAutogenerated);
            }
        } catch (ConcurrentModificationException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
        Util.reloadBindings(editWindow);
    }

    public void setCodeAutogeneratedInModel(boolean codeAutogenerated) {
        orderModel.setCodeAutogenerated(codeAutogenerated);
    }

    public OrderStatusEnum[] getOrderStatus() {
        return OrderStatusEnum.values();
    }

    public List<ExternalCompany> getExternalCompaniesAreClient() {
        return orderModel.getExternalCompaniesAreClient();
    }

    public OrdersRowRenderer getOrdersRowRender() {
        return ordersRowRenderer;
    }

    public class OrdersRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {

            final Order order = (Order) data;
            row.setValue(order);

            appendLabel(row, order.getName());
            appendLabel(row, order.getCode());
            appendDate(row, order.getInitDate());
            appendDate(row, order.getDeadline());
            appendCustomer(row, order.getCustomer());
            appendObject(row, order.getTotalBudget());
            appendObject(row, order.getTotalHours());
            appendObject(row, _(order.getState().toString()));
            appendOperations(row, order);

            row.setTooltiptext(getTooltipText(order));
            row.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event event) {
                    goToEditForm(order);
                }
            });
        }
    }

    private void appendObject(final Row row, Object object) {
        String text = new String("");
        if (object != null) {
            text = object.toString();
        }
        appendLabel(row, text);
    }

    private void appendCustomer(final Row row, ExternalCompany externalCompany) {
        String customerName = new String("");
        if (externalCompany != null) {
            customerName = externalCompany.getName();
        }
        appendLabel(row, customerName);
    }

    private void appendLabel(final Row row, String value) {
        Label label = new Label(value);
        row.appendChild(label);
    }

    private void appendDate(final Row row, Date date) {
        String labelDate = new String("");
        if (date != null) {
            labelDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        appendLabel(row, labelDate);
    }

    private void appendOperations(final Row row,final Order order){
        Hbox hbox = new Hbox();
        appendButtonEdit(hbox,order);
        appendButtonDelete(hbox, order);
        appendButtonPlan(hbox, order);
        appendButtonDerived(hbox, order);
        row.appendChild(hbox);
    }

    private void appendButtonEdit(final Hbox hbox, final Order order) {
        Button buttonEdit = new Button();
        buttonEdit.setSclass("icono");
        buttonEdit.setImage("/common/img/ico_editar1.png");
        buttonEdit.setHoverImage("/common/img/ico_editar.png");
        buttonEdit.setTooltiptext(_("Edit"));
        buttonEdit.addEventListener("onClick",new EventListener() {
            @Override
            public void onEvent(Event event) {
                goToEditForm(order);
            }
        });
        hbox.appendChild(buttonEdit);
    }

    private void appendButtonDelete(final Hbox hbox, final Order order) {
        if(orderModel.userCanWrite(order, SecurityUtils.getSessionUserLoginName())) {
            Button buttonDelete = new Button();
            buttonDelete.setSclass("icono");
            buttonDelete.setImage("/common/img/ico_borrar1.png");
            buttonDelete.setHoverImage("/common/img/ico_borrar.png");
            buttonDelete.setTooltiptext(_("Delete"));
            buttonDelete.addEventListener("onClick",new EventListener() {
                @Override
                public void onEvent(Event event) {
                    confirmRemove(order);
                }
            });
            hbox.appendChild(buttonDelete);
        }
    }

    private void appendButtonPlan(final Hbox hbox, final Order order) {
        Button buttonPlan = new Button();
        buttonPlan.setSclass("icono");
        buttonPlan.setImage("/common/img/ico_planificador1.png");
        buttonPlan.setHoverImage("/common/img/ico_planificador.png");
        buttonPlan.setTooltiptext(_("See scheduling"));
        buttonPlan.addEventListener("onClick",new EventListener() {
            @Override
            public void onEvent(Event event) {
                schedule(order);
            }
        });
        hbox.appendChild(buttonPlan);
    }

    private void appendButtonDerived(final Hbox hbox, final Order order) {
        Button buttonDerived = new Button();
        buttonDerived.setSclass("icono");
        buttonDerived.setImage("/common/img/ico_derived1.png");
        buttonDerived.setHoverImage("/common/img/ico_derived.png");
        buttonDerived.setTooltiptext(_("Create Template"));
        buttonDerived.addEventListener("onClick", new EventListener() {
            @Override
            public void onEvent(Event event) {
                createTemplate(order);
            }
        });
        hbox.appendChild(buttonDerived);
    }

    public String getTooltipText(final Order order) {
        return orderModel.gettooltipText(order);
    }

    public void reloadTotalBudget(Label txtTotalBudget) {
        Util.reloadBindings(txtTotalBudget);
    }

    /**
     * Operations to filter the orders by multiple filters
     */

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDate.getValue() != null)
                        && (finishDate.compareTo(filterStartDate.getValue()) < 0)) {
                    filterFinishDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be greater than start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDate.getValue() != null)
                        && (startDate.compareTo(filterFinishDate.getValue()) > 0)) {
                    filterStartDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public void onApplyFilter() {
        OrderPredicate predicate = createPredicate();
        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllOrders();
        }
    }

    private OrderPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();
        Date startDate = filterStartDate.getValue();
        Date finishDate = filterFinishDate.getValue();
        Boolean includeOrderElements = checkIncludeOrderElements.isChecked();

        if (listFilters.isEmpty() && startDate == null && finishDate == null) {
            return null;
        }
        return new OrderPredicate(listFilters, startDate, finishDate,
                includeOrderElements);
    }

    private void filterByPredicate(OrderPredicate predicate) {
        List<Order> filterOrders = orderModel.getFilterOrders(predicate);
        listing.setModel(new SimpleListModel(filterOrders.toArray()));
        listing.invalidate();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    public void showAllOrders() {
        listing.setModel(new SimpleListModel(orderModel.getOrders().toArray()));
        listing.invalidate();
    }

    private void showOrderFilter() {
        orderFilter.setVisible(true);
        orderElementFilter.setVisible(false);
    }

    public void showOrderElementFilter() {
        if (orderFilter != null) {
            orderFilter.setVisible(false);
        }
        if (orderElementFilter != null) {
            orderElementFilter.setVisible(true);
        }
    }

    public void showCreateButtons(boolean showCreate) {
        createOrderButton.setVisible(showCreate);
        createOrderFromTemplateButton.setVisible(showCreate);
        saveOrderAndContinueButton.setVisible(!showCreate);
        cancelEditionButton.setVisible(!showCreate);
    }

    public void highLight(final OrderElement orderElement) {
        final Tab tab = (Tab) editWindow.getFellowIfAny("tabOrderElements");
        LongOperationFeedback.executeLater(tab, new Runnable() {

            @Override
            public void run() {
                if (tab != null) {
                    tab.setSelected(true);
                    Events.sendEvent(new SelectEvent(Events.ON_SELECT, tab,
                            null));
                }

                if (!(orderElement instanceof Order)
                        && orderElementTreeController != null) {
                    final Treeitem item = orderElementTreeController
                    .getTreeitemByOrderElement(orderElement);

                    if (item != null) {
                        orderElementTreeController
                                .showEditionOrderElement(item);
                    }
                }
            }
        });
    }

    /**
     * Checks the creation permissions of the current user and enables/disables
     * the create buttons accordingly.
     */
    private void checkCreationPermissions() {
        if (!SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            createOrderButton.setDisabled(true);
            createOrderFromTemplateButton.setDisabled(true);
        }
    }

    private boolean readOnly = true;

    private OrderStatusEnum initialStatus;

    private void updateDisabilitiesOnInterface() {
        Order order = (Order) orderModel.getOrder();

        initialStatus = order.getState();

        boolean permissionForWriting = orderModel.userCanWrite(order,
                SecurityUtils.getSessionUserLoginName());
        boolean isInStoredState = order.getState() == OrderStatusEnum.STORED;
        boolean isInitiallyStored = initialStatus == OrderStatusEnum.STORED;

        readOnly = !permissionForWriting || isInStoredState;

        if (orderElementTreeController != null) {
            orderElementTreeController.setReadOnly(readOnly);
        }
        saveOrderAndContinueButton.setDisabled(!permissionForWriting
                || (isInitiallyStored && isInStoredState));
    }

    public void sortOrders() {
        Column columnDateStart = (Column) listWindow
                .getFellow("columnDateStart");
        if (columnDateStart != null) {
            if (columnDateStart.getSortDirection().equals("ascending")) {
                columnDateStart.sort(false, false);
                columnDateStart.setSortDirection("ascending");
            } else if (columnDateStart.getSortDirection().equals("descending")) {
                columnDateStart.sort(true, false);
                columnDateStart.setSortDirection("descending");
            }
        }
    }

}
