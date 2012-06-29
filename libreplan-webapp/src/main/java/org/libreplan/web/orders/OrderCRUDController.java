/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.entities.DeadlineCommunication;
import org.libreplan.business.externalcompanies.entities.DeliverDateComparator;
import org.libreplan.business.externalcompanies.entities.EndDateCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.Order.SchedulingMode;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.planner.entities.PositionConstraintType;
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
import org.zkoss.util.Locales;
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
import org.zkoss.zul.Rows;
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
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCRUDController extends GenericForwardComposer {

    private static final String DEFAULT_TAB = "tabOrderElements";

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    @Autowired
    private IOrderModel orderModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

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

    @Autowired
    private IOrderDAO orderDAO;

    private Grid gridAskedEndDates;

    private EndDatesRenderer endDatesRenderer = new EndDatesRenderer();

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

        saveOrderAndContinueButton.addEventListener(Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        saveAndContinue();
                    }
                });

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

        private void setConstraintsFor(final SchedulingMode mode) {
            initDate.setConstraint(new Constraint() {

                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (value == null) {
                        if (mode == SchedulingMode.FORWARD) {
                            throw new WrongValueException(
                                    comp,
                                    _("Starting date cannot be empty in forward mode"));
                        }
                        if (orderModel
                                .isAnyTaskWithConstraint(PositionConstraintType.AS_SOON_AS_POSSIBLE)) {
                            throw new WrongValueException(comp,
                                    _("Starting date cannot be empty because there is a task with constraint \"as soon as possible\""));
                        }
                    }
                }
            });
            deadline.setConstraint(new Constraint() {

                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (value == null) {
                        if (mode == SchedulingMode.BACKWARDS) {
                            throw new WrongValueException(
                                    comp,
                                    _("Deadline cannot be empty in backwards mode"));
                        }
                        if (orderModel
                                .isAnyTaskWithConstraint(PositionConstraintType.AS_LATE_AS_POSSIBLE)) {
                            throw new WrongValueException(comp,
                                    _("Deadline cannot be empty because there is a task with constraint \"as late as possible\""));
                        }
                    }
                }
            });
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

        Component orderElementHours = editWindow
                .getFellowIfAny("orderElementHours");
        if (assignedHoursController == null) {
            assignedHoursController = (AssignedHoursToOrderElementController) orderElementHours
                    .getVariable("assignedHoursToOrderElementController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedHoursController.openWindow(orderElementModel);
        } else {
            Util.createBindingsFor(orderElementHours);
            Util.reloadBindings(orderElementHours);
            assignedHoursController.paintProgressBars();
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
                throw new RuntimeException(e);
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
        boolean hasImputedExpenseSheets = orderModel.hasImputedExpenseSheets(order);
        if (hasImputedExpenseSheets) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("You can not remove the project \"{0}\" because this one has imputed expense sheets.",
                                    order.getName()));
            return;
        }

        boolean alreadyInUse = orderModel.isAlreadyInUseAndIsOnlyInCurrentScenario(order);
        if (alreadyInUse) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _(
                                    "You can not remove the project \"{0}\" because it has work reported on it or any of its tasks",
                                    order.getName()));
        } else {
            if (!StringUtils.isBlank(order.getExternalCode())) {
                try {
                    if (Messagebox
                            .show(
                                    _("This is a subcontracted project, if you delete it you can not report progress anymore. Are you sure?"),
                                    _("Confirm"), Messagebox.OK
                                            | Messagebox.CANCEL,
                                    Messagebox.QUESTION) == Messagebox.CANCEL) {
                        return;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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

    public void createFromTemplate(OrderTemplate template) {
        orderModel.prepareCreationFrom(template, getDesktop());
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

        gridAskedEndDates = (Grid) editWindow.getFellow("gridAskedEndDates");

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

    public void editNewCreatedOrder(Window detailsWindow) {
        showOrderElementFilter();
        hideCreateButtons();
        prepareEditWindow();
        showEditWindow(_("Create project"));
        detailsWindow.setVisible(false);
        setupOrderAuthorizationController();
        detailsWindow.getAttributes();
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
            appendObject(row, Util.addCurrencySymbol(order.getTotalBudget()));
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
        if (!SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TEMPLATES)) {
            buttonDerived.setDisabled(true);
            buttonDerived
                    .setTooltiptext(_("You do not have permissions to create templates"));
        }
        hbox.appendChild(buttonDerived);
    }

    public String getTooltipText(final Order order) {
        return orderModel.gettooltipText(order);
    }

    public void reloadTotalBudget(Label labelTotalBudget) {
        Util.reloadBindings(labelTotalBudget);
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
                            _("must be after start date"));
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
        if (!showCreate) {
            Hbox perspectiveButtonsInsertionPoint = (Hbox) page
                    .getFellow("perspectiveButtonsInsertionPoint");
            perspectiveButtonsInsertionPoint.getChildren().clear();
            saveOrderAndContinueButton
                    .setParent(perspectiveButtonsInsertionPoint);
            cancelEditionButton.setParent(perspectiveButtonsInsertionPoint);
        }
        if (createOrderButton != null) {
            createOrderButton.setVisible(showCreate);
        }
        if (saveOrderAndContinueButton != null) {
            saveOrderAndContinueButton.setVisible(!showCreate);
        }
        if (cancelEditionButton != null) {
            cancelEditionButton.setVisible(!showCreate);
        }

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
        if (!SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_PROJECTS)) {
            if (createOrderButton != null) {
                createOrderButton.setDisabled(true);
            }
        }
    }

    private boolean readOnly = true;

    private void updateDisabilitiesOnInterface() {
        Order order = (Order) orderModel.getOrder();

        boolean permissionForWriting = orderModel.userCanWrite(order,
                SecurityUtils.getSessionUserLoginName());
        boolean isInStoredState = order.getState() == OrderStatusEnum.STORED;
        boolean isInitiallyStored = orderModel.getPlanningState()
                .getSavedOrderState() == OrderStatusEnum.STORED;

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

    public SortedSet<DeadlineCommunication> getDeliverDates() {
        if(getOrder() != null){
               return getOrder().getDeliveringDates();
        }
        return new TreeSet<DeadlineCommunication>(new DeliverDateComparator());
    }

    public Constraint chekValidProjectName() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                if (StringUtils.isBlank((String) value)) {
                    throw new WrongValueException(comp,
                            _("cannot be empty"));
                }
                try {
                    Order found = orderDAO
                            .findByNameAnotherTransaction((String) value);
                    if (!found.getId().equals(getOrder().getId())) {
                        throw new WrongValueException(comp,
                                _("project name already being used"));
                    }
                } catch (InstanceNotFoundException e) {
                    return;
                }
            }
        };
    }

    public Constraint chekValidProjectCode() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                if (StringUtils.isBlank((String) value)) {
                    throw new WrongValueException(comp,
                            _("cannot be empty"));
                }
                try {
                    Order found = orderDAO
                            .findByCodeAnotherTransaction((String) value);
                    if (!found.getId().equals(getOrder().getId())) {
                        throw new WrongValueException(comp,
                                _("project code already being used"));
                    }
                } catch (InstanceNotFoundException e) {
                    return;
                }
            }
        };
    }

    public boolean isSubcontractedProject() {
        return (getOrder() != null) ? (getOrder().getExternalCode() != null)
                : false;
    }

    public String getProjectType() {
        return (isSubcontractedProject()) ? "Subcontracted by client"
                : "Regular project";
    }

    public void setCurrentDeliveryDate(Grid listDeliveryDates) {
        if (getOrder() != null && getOrder().getDeliveringDates() != null
                && !getOrder().getDeliveringDates().isEmpty()) {
            DeadlineCommunication lastDeliveryDate = getOrder()
                    .getDeliveringDates().first();
            if (listDeliveryDates != null) {
                listDeliveryDates.renderAll();
                final Rows rows = listDeliveryDates.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext();) {
                    final Row row = (Row) i.next();
                    final DeadlineCommunication deliveryDate = (DeadlineCommunication) row
                            .getValue();
                    if (deliveryDate.equals(lastDeliveryDate)) {
                        row.setSclass("current-delivery-date");
                        return;
                    }
                }
            }
        }
    }

    public SortedSet<EndDateCommunication> getEndDates() {
        return orderModel.getEndDates();
    }

    public void addAskedEndDate(Datebox newEndDate) {
        if (newEndDate == null || newEndDate.getValue() == null) {
            messagesForUser.showMessage(Level.ERROR, _("You must select a valid date. "));
            return;
        }
        if (thereIsSomeCommunicationDateEmpty()) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("It will only be possible to add a end date if all the exiting ones in the table have already been sent to customer.."));
            return;
        }
        if (orderModel.alreadyExistsRepeatedEndDate(newEndDate.getValue())) {
            messagesForUser.showMessage(Level.ERROR,
                    _("It already exists a end date with the same date. "));
            return;
        }
        orderModel.addAskedEndDate(newEndDate.getValue());
        reloadGridAskedEndDates();
    }

    private void reloadGridAskedEndDates() {
        Util.reloadBindings(gridAskedEndDates);
    }

    private boolean thereIsSomeCommunicationDateEmpty() {
        for (EndDateCommunication endDate : orderModel.getEndDates()) {
            if (endDate.getCommunicationDate() == null) {
                return true;
            }
        }
        return false;
    }

    public EndDatesRenderer getEndDatesRenderer() {
        return this.endDatesRenderer;
    }

    private class EndDatesRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            EndDateCommunication endDate = (EndDateCommunication) data;
            row.setValue(endDate);

            appendLabel(row, toString(endDate.getSaveDate(), "dd/MM/yyyy HH:mm"));
            appendLabel(row, toString(endDate.getEndDate(), "dd/MM/yyyy"));
            appendLabel(row, toString(endDate.getCommunicationDate(), "dd/MM/yyyy HH:mm"));
            appendOperations(row, endDate);
        }

        private String toString(Date date, String precision) {
            if (date == null) {
                return "";
            }
            return new SimpleDateFormat(precision, Locales.getCurrent()).format(date);
        }

        private void appendLabel(Row row, String label) {
            row.appendChild(new Label(label));
        }

        private void appendOperations(Row row, EndDateCommunication endDate) {
            Hbox hbox = new Hbox();
            hbox.appendChild(getDeleteButton(endDate));
            row.appendChild(hbox);
        }

        private Button getDeleteButton(final EndDateCommunication endDate) {

            Button deleteButton = new Button();
            deleteButton.setDisabled(isNotUpdate(endDate));
            deleteButton.setSclass("icono");
            deleteButton.setImage("/common/img/ico_borrar1.png");
            deleteButton.setHoverImage("/common/img/ico_borrar.png");
            deleteButton.setTooltiptext(_("Delete"));
            deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    removeAskedEndDate(endDate);
                }
            });

            return deleteButton;
        }

        private boolean isNotUpdate(final EndDateCommunication endDate) {
            EndDateCommunication lastAskedEndDate = getOrder()
                    .getEndDateCommunicationToCustomer().first();
            if ((lastAskedEndDate != null) && (lastAskedEndDate.equals(endDate))) {
                return (lastAskedEndDate.getCommunicationDate() != null);
            }
            return true;
        }
    }

    public void removeAskedEndDate(EndDateCommunication endDate) {
        orderModel.removeAskedEndDate(endDate);
        reloadGridAskedEndDates();
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

    public String getCurrencySymbol() {
        return Util.getCurrencySymbol();
    }
}
