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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.Configuration;
import org.libreplan.business.common.Registry;
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
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.ConfirmCloseUtil;
import org.libreplan.web.common.FilterUtils;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.Util.ReloadStrategy;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.common.components.finders.OrderFilterEnum;
import org.libreplan.web.common.components.finders.TaskGroupFilterEnum;
import org.libreplan.web.orders.criterionrequirements.AssignedCriterionRequirementToOrderElementController;
import org.libreplan.web.orders.criterionrequirements.OrderElementCriterionRequirementComponent;
import org.libreplan.web.orders.files.OrderFilesController;
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
import org.zkoss.zul.Window;
import org.zkoss.zul.Listbox;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for CRUD actions.
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCRUDController extends GenericForwardComposer {

    private static final String DEFAULT_TAB = "tabOrderElements";

    private final String DELETE = "Delete";

    private final String ON_CLICK_EVENT = "onClick";

    private final String ICONO_CLASS = "icono";

    private final String TAB_ADVANCES = "tabAdvances";

    private final String INFORMATION = "Information";

    @Autowired
    private IOrderModel orderModel;

    @Autowired
    private IOrderDAO orderDAO;

    @Resource
    private IOrderTemplatesControllerEntryPoints orderTemplates;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Window editWindow;

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

    private BandboxSearch bdExternalCompanies;

    private OnlyOneVisible cachedOnlyOneVisible;

    private IOrderPlanningGate planningControllerEntryPoints;

    private BaseCalendarsComboitemRenderer baseCalendarsComboitemRenderer = new BaseCalendarsComboitemRenderer();

    private OrdersRowRenderer ordersRowRenderer = new OrdersRowRenderer();

    private OrderElementTreeController orderElementTreeController;

    private ProjectDetailsController projectDetailsController;

    private JiraSynchronizationController jiraSynchronizationController;

    private TimSynchronizationController timSynchronizationController;

    private AssignedLabelsToOrderElementController assignedLabelsController;

    private AssignedHoursToOrderElementController assignedHoursController;

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private AssignedCriterionRequirementToOrderElementController assignedCriterionRequirementController;

    private AssignedMaterialsToOrderElementController assignedMaterialsController;

    private AssignedTaskQualityFormsToOrderElementController assignedTaskQualityFormController;

    private OrderAuthorizationController orderAuthorizationController;

    private OrderFilesController orderFilesController;

    private Grid gridAskedEndDates;

    private EndDatesRenderer endDatesRenderer = new EndDatesRenderer();

    private Textbox filterProjectName;

    private Checkbox filterExcludeFinishedProject;

    private Runnable onUp;

    private boolean readOnly = true;

    public void showCreateFormFromTemplate(OrderTemplate template) {
        showOrderElementFilter();
        showCreateButtons(false);
        orderModel.prepareCreationFrom(template, getDesktop());
        prepareEditWindow();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setAttribute("controller", this, true);

        // Configuration of the order filter
        Component filterComponent =
                Executions.createComponents("/orders/_orderFilter.zul", orderFilter, new HashMap<String, String>());

        filterComponent.setAttribute("orderFilterController", this, true);
        filterStartDate = (Datebox) filterComponent.getFellow("filterStartDate");
        filterFinishDate = (Datebox) filterComponent.getFellow("filterFinishDate");
        bdFilters = (BandboxMultipleSearch) filterComponent.getFellow("bdFilters");

        filterProjectName = (Textbox) filterComponent.getFellow("filterProjectName");

        filterExcludeFinishedProject = (Checkbox) filterComponent.getFellow("filterExcludeFinishedProject");

        checkCreationPermissions();
        setupGlobalButtons();
        initializeFilter();
    }

    private void initializeFilter() {
        Date startDate = FilterUtils.readProjectsStartDate();
        Date endDate = FilterUtils.readProjectsEndDate();

        boolean calculateStartDate = startDate == null;
        boolean calculateEndDate = endDate == null;

        // Filter predicate needs to be calculated based on the projects dates
        if ( (calculateStartDate) || (calculateEndDate) ) {

            User user = orderModel.getUser();

            // Calculate filter based on user preferences
            if ( user != null ) {
                if ( (startDate == null ) && !FilterUtils.hasProjectsStartDateChanged() &&
                        (user.getProjectsFilterPeriodSince() != null) ) {

                    startDate = new LocalDate()
                            .minusMonths(user.getProjectsFilterPeriodSince())
                            .toDateTimeAtStartOfDay()
                            .toDate();
                }
                if ( (endDate == null ) &&
                        !FilterUtils.hasProjectsEndDateChanged() &&
                        (user.getProjectsFilterPeriodTo() != null) ) {

                    endDate = new LocalDate()
                            .plusMonths(user.getProjectsFilterPeriodTo())
                            .toDateTimeAtStartOfDay()
                            .toDate();
                }
            }
        }
        filterStartDate.setValue(startDate);
        filterFinishDate.setValue(endDate);

        filterProjectName.setValue(FilterUtils.readProjectsName());

        filterExcludeFinishedProject.setValue(FilterUtils.readExcludeFinishedProjects());

        loadLabels();
        FilterUtils.writeProjectPlanningFilterChanged(false);

        createDeleteAllProjectsButton();
    }

    /**
     * This method is needed to create "Delete all projects" button,
     * that is visible only for developers on orders list page.
     */
    private void createDeleteAllProjectsButton() {
        if (!isDeleteAllProjectsButtonDisabled()) {
            Button deleteAllProjectButton = new Button();
            deleteAllProjectButton.setLabel("Delete all projects");
            deleteAllProjectButton.setDisabled(isDeleteAllProjectsButtonDisabled());
            deleteAllProjectButton.addEventListener(Events.ON_CLICK, event -> deleteAllProjects());
            orderFilter.appendChild(deleteAllProjectButton);
        }
    }

    private void loadLabels() {
        List<FilterPair> sessionFilters = FilterUtils.readProjectsParameters();
        // Allow labels when list is empty
        if ( sessionFilters != null ) {
            bdFilters.addSelectedElements(toOrderFilterEnum(sessionFilters));
            return;
        }

        User user = orderModel.getUser();

        // Calculate filter based on user preferences
        if ( (user != null) && (user.getProjectsFilterLabel() != null) ) {

            bdFilters.addSelectedElement(new FilterPair(
                    OrderFilterEnum.Label,
                    user.getProjectsFilterLabel().getFinderPattern(),
                    user.getProjectsFilterLabel()));
        }
    }

    private List<FilterPair> toOrderFilterEnum(List<FilterPair> filterPairs) {
        List<FilterPair> result = new ArrayList<>();
        for (FilterPair filterPair : filterPairs) {
            TaskGroupFilterEnum type = (TaskGroupFilterEnum) filterPair.getType();

            switch (type) {

                case Label:
                    result.add(new FilterPair(OrderFilterEnum.Label, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case Criterion:
                    result.add(new FilterPair(
                            OrderFilterEnum.Criterion, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case ExternalCompany:
                    result.add(new FilterPair(
                            OrderFilterEnum.ExternalCompany, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case State:
                    result.add(new FilterPair(OrderFilterEnum.State, filterPair.getPattern(), filterPair.getValue()));
                    break;

                default:
                    break;
            }
        }
        return result;
    }

    private void setupGlobalButtons() {

        saveOrderAndContinueButton.addEventListener(Events.ON_CLICK, event -> saveAndContinue());

        cancelEditionButton.addEventListener(Events.ON_CLICK, event -> Messagebox.show(
                _("Unsaved changes will be lost. Are you sure?"), _("Confirm exit dialog"),
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                evt -> {
                    if ( "onOK".equals(evt.getName()) ) {
                        ConfirmCloseUtil.resetConfirmClose();
                        Executions.sendRedirect("/planner/index.zul;company_scheduling");
                    }
                }));

    }

    private void initEditOrderElementWindow() {
        final Component parent = listWindow.getParent();

        Map<String, Object> editOrderElementArgs = new HashMap<>();
        editOrderElementArgs.put("top_id", "editOrderElement");

        editOrderElementWindow =
                (Window) Executions.createComponents("/orders/_editOrderElement.zul", parent, editOrderElementArgs);

        Util.createBindingsFor(editOrderElementWindow);
        Util.reloadBindings(editOrderElementWindow);
    }

    private void addEditWindowIfNecessary() {
        if ( editWindow != null ) {
            return;
        }

        listWindow.setVisible(false);
        cachedOnlyOneVisible = null;

        Map<String, Object> editWindowArgs = new HashMap<>();
        editWindowArgs.put("top_id", "editWindow");
        Component parent = listWindow.getParent();
        editWindow = (Window) Executions.createComponents("/orders/_edition.zul", parent, editWindowArgs);

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
            if ( options != null && options.isEmpty() ) {

                schedulingMode.appendChild(createCombo(
                        SchedulingMode.FORWARD, _("Forward"), _("Schedule from start to deadline")));

                schedulingMode.appendChild(createCombo(
                        SchedulingMode.BACKWARDS, _("Backwards"), _("Schedule from deadline to start")));
            }
        }

        void chooseCurrentSchedulingMode() {
            @SuppressWarnings("unchecked")
            List<Comboitem> items = schedulingMode.getItems();

            SchedulingMode currentMode = getOrder().getSchedulingMode();

            for (Comboitem each : items) {
                if ( each.getValue().equals(currentMode) ) {
                    schedulingMode.setSelectedItem(each);
                    setConstraintsFor(currentMode);

                    return;
                }
            }
        }

        private void listenToChangeOfMode() {
            schedulingMode.addEventListener(Events.ON_SELECT, event -> {
                SchedulingMode chosen =  schedulingMode.getSelectedItem().getValue();
                if (chosen != null) {
                    getOrder().setSchedulingMode(chosen);
                    setConstraintsFor(chosen);
                    changeFocusAccordingTo(chosen);
                }
            });
        }

        private Comboitem createCombo(SchedulingMode value, String label, String description) {
            Comboitem result = new Comboitem();
            result.setValue(value);
            result.setLabel(label);
            result.setDescription(description);

            return result;
        }

        private void setConstraintsFor(final SchedulingMode mode) {
            initDate.setConstraint((comp, value) -> {
                if (value == null) {

                    if (mode == SchedulingMode.FORWARD) {
                        throw new WrongValueException(comp, _("Starting date cannot be empty in forward mode"));
                    }

                    if ( orderModel.isAnyTaskWithConstraint(PositionConstraintType.AS_SOON_AS_POSSIBLE) ) {
                        throw new WrongValueException(
                                comp,
                                _("Starting date cannot be empty because there is a task with constraint " +
                                        "\"as soon as possible\""));
                    }
                }
            });

            deadline.setConstraint((comp, value) -> {
                if (value == null) {
                    if (mode == SchedulingMode.BACKWARDS) {
                        throw new WrongValueException(comp, _("Deadline cannot be empty in backwards mode"));
                    }

                    if (orderModel.isAnyTaskWithConstraint(PositionConstraintType.AS_LATE_AS_POSSIBLE)) {
                        throw new WrongValueException(
                                comp,
                                _("Deadline cannot be empty because there is a task with constraint " +
                                        "\"as late as possible\""));
                    }
                }
            });
        }

        private void changeFocusAccordingTo(SchedulingMode chosen) {
            initDate.setFocus(SchedulingMode.FORWARD == chosen);
            deadline.setFocus(SchedulingMode.BACKWARDS == chosen);
        }

        public Constraint getCheckConstraintFinishDate() {
            return (comp, value) -> {
                Date finishDate = (Date) value;

                if ( (finishDate != null) && (initDate.getValue() != null) &&
                        (finishDate.compareTo(initDate.getValue()) < 0) ) {

                    deadline.setValue(null);
                    getOrder().setDeadline(null);
                    throw new WrongValueException(comp, _("must be after start date"));
                }
            };
        }

        public Constraint checkConstraintStartDate() {
            return (comp, value) -> {
                Date startDate = (Date) value;

                if ( (startDate != null) && (deadline.getValue() != null) &&
                        (startDate.compareTo(deadline.getValue()) > 0) ) {

                    initDate.setValue(null);
                    getOrder().setInitDate(null);
                    throw new WrongValueException(comp, _("must be lower than end date"));
                }
            };
        }

    }

    private void bindListOrderStatusSelectToOnStatusChange() {
        Listbox listOrderStatus = (Listbox) editWindow.getFellow("listOrderStatus");
        listOrderStatus.addEventListener(Events.ON_SELECT, event -> updateDisabilitiesOnInterface());
    }

    public void setupOrderElementTreeController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        if ( orderElementTreeController == null ) {
            // Create order element edit window
            OrderElementController orderElementController = new OrderElementController();

            if ( editOrderElementWindow == null ) {
                initEditOrderElementWindow();
            }

            try {
                orderElementController.doAfterCompose(self.getFellow("editOrderElement"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Prepare tree, attach edit window to tree
            orderElementTreeController =
                    new OrderElementTreeController(orderModel, orderElementController, messagesForUser);

            TreeComponent orderElementsTree = (TreeComponent) editWindow.getFellow("orderElementTree");
            orderElementTreeController.setTreeComponent(orderElementsTree);
            orderElementsTree.useController(orderElementTreeController);
            orderElementTreeController.setReadOnly(readOnly);

            Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
            tree.setModel(null);
            tree.setItemRenderer(orderElementTreeController.getRenderer());

            reloadTree(orderElementsTree);
        }
    }

    private void reloadTree(TreeComponent orderElementsTree) {
        final Tree tree = (Tree) orderElementsTree.getFellowIfAny("tree");
        tree.setModel(orderElementTreeController.getFilteredTreeModel());
        tree.addEventListener(Events.ON_SELECT, event -> tree.clearSelection());
    }

    /**
     * Operations to do before to change the selected tab.
     */
    private boolean confirmLastTab() {
        if ( getCurrentTab() != null ) {

            // Confirm advances tab
            if ( TAB_ADVANCES.equals(getCurrentTab().getId()) ) {
                if ( manageOrderElementAdvancesController != null && !manageOrderElementAdvancesController.save() ) {
                    resetSelectedTab();
                    selectTab(TAB_ADVANCES);

                    return false;
                }
            }
        }

        return true;
    }

    private IOrderElementModel getOrderElementModel() {
        return orderModel.getOrderElementModel(orderModel.getOrder());
    }

    public void setupAssignedHoursToOrderElementController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        Component orderElementHours = editWindow.getFellowIfAny("orderElementHours");

        if (assignedHoursController == null) {

            assignedHoursController = (AssignedHoursToOrderElementController)
                    orderElementHours.getAttribute("assignedHoursToOrderElementController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedHoursController.openWindow(orderElementModel);
        } else {
            Util.createBindingsFor(orderElementHours);
            Util.reloadBindings(orderElementHours);
            assignedHoursController.paintProgressBars();
        }
    }

    public void setupManageOrderElementAdvancesController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        Component orderElementAdvances = editWindow.getFellowIfAny("orderElementAdvances");

        if ( manageOrderElementAdvancesController == null ) {
            final IOrderElementModel orderElementModel = getOrderElementModel();

            manageOrderElementAdvancesController = (ManageOrderElementAdvancesController)
                    orderElementAdvances.getAttribute("manageOrderElementAdvancesController", true);

            manageOrderElementAdvancesController.openWindow(orderElementModel);

        } else {
            manageOrderElementAdvancesController.refreshChangesFromOrderElement();
            manageOrderElementAdvancesController.createAndLoadBindings();
            manageOrderElementAdvancesController.refreshSelectedAdvance();
        }
    }

    public void setupAssignedLabelsToOrderElementController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        if ( assignedLabelsController == null ) {

            LabelsAssignmentToOrderElementComponent labelsAssignment =
                    (LabelsAssignmentToOrderElementComponent) editWindow.getFellow("orderElementLabels");

            assignedLabelsController = labelsAssignment.getController();

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedLabelsController.openWindow(orderElementModel);
        }
    }

    public void setupAssignedCriterionRequirementsToOrderElementController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        if ( assignedCriterionRequirementController == null ) {
            Component orderElementCriterionRequirements =
                    editWindow.getFellowIfAny("orderElementCriterionRequirements");

            assignedCriterionRequirementController =
                    ((OrderElementCriterionRequirementComponent) orderElementCriterionRequirements).getController();

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedCriterionRequirementController.openWindow(orderElementModel);
        } else {
            reloadHoursGroupOrder();
        }
    }

    public void setupAssignedMaterialsToOrderElementController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        if ( assignedMaterialsController == null ) {

            OrderElementMaterialAssignmentsComponent assignmentsComponent =
                    (OrderElementMaterialAssignmentsComponent) editWindow.getFellowIfAny("orderElementMaterials");

            assignedMaterialsController = assignmentsComponent.getController();

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedMaterialsController.openWindow(orderElementModel.getOrderElement());
        }
    }

    public void setupAssignedTaskQualityFormsToOrderElementController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        Component orderElementTaskQualityForms = editWindow.getFellowIfAny("orderElementTaskQualityForms");
        if ( assignedTaskQualityFormController == null ) {

            assignedTaskQualityFormController = (AssignedTaskQualityFormsToOrderElementController)
                    orderElementTaskQualityForms.getAttribute("assignedTaskQualityFormsController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();
            assignedTaskQualityFormController.openWindow(orderElementModel);

        } else {
            Util.createBindingsFor(orderElementTaskQualityForms);
            Util.reloadBindings(orderElementTaskQualityForms);
        }
    }

    public void setupOrderFilesController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        Component orderFiles = editWindow.getFellowIfAny("orderElementFiles");

        if ( orderFilesController == null ){
            orderFilesController = (OrderFilesController) orderFiles.getAttribute("orderFilesController", true);

            final IOrderElementModel orderElementModel = getOrderElementModel();

            orderFilesController.openWindow(orderElementModel);

        }
    }

    public void setupOrderAuthorizationController() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();

        Component orderElementAuthorizations = editWindow.getFellowIfAny("orderElementAuthorizations");

        if (orderAuthorizationController == null) {

            orderAuthorizationController = (OrderAuthorizationController)
                    orderElementAuthorizations.getAttribute("orderAuthorizationController", true);

            orderAuthorizationController.setMessagesForUserComponent(messagesForUser);
            initOrderAuthorizations();

        } else {
            Util.createBindingsFor(orderElementAuthorizations);
            Util.reloadBindings(orderElementAuthorizations);
        }
    }

    private void initOrderAuthorizations() {
        Component orderElementAuthorizations = editWindow.getFellowIfAny("orderElementAuthorizations");
        final Order order = orderModel.getOrder();

        if ( order.isNewObject() ) {
            orderAuthorizationController.initCreate(orderModel.getPlanningState());
        } else {
            orderAuthorizationController.initEdit(orderModel.getPlanningState());
        }

        Util.createBindingsFor(orderElementAuthorizations);
        Util.reloadBindings(orderElementAuthorizations);
    }

    public List<Order> getOrders() {
        return getOrdersFiltered();
    }

    private List<Order> getOrdersFiltered() {
        List<org.libreplan.business.labels.entities.Label> labels = new ArrayList<>();
        List<Criterion> criteria = new ArrayList<>();
        ExternalCompany customer = null;
        OrderStatusEnum state = null;
        //Boolean excludeFinishedProject = false;

        for (FilterPair filterPair : (List<FilterPair>) bdFilters.getSelectedElements()) {
            OrderFilterEnum type = (OrderFilterEnum) filterPair.getType();
            switch (type) {

                case Label:
                    labels.add((org.libreplan.business.labels.entities.Label) filterPair.getValue());
                    break;

                case Criterion:
                    criteria.add((Criterion) filterPair.getValue());
                    break;

                case ExternalCompany:
                    if ( customer != null ) {
                        // It's impossible to have an Order associated to more than 1 customer
                        return Collections.emptyList();
                    }
                    customer = (ExternalCompany) filterPair.getValue();
                    break;

                case State:
                    if ( state != null ) {
                        // It's impossible to have an Order associated with more than 1 state
                        return Collections.emptyList();
                    }
                    state = (OrderStatusEnum) filterPair.getValue();
                    break;

                default:
                    break;
            }
        }

        return orderModel.getOrders(
                filterStartDate.getValue(), filterFinishDate.getValue(), labels, criteria, customer, state, filterExcludeFinishedProject.isChecked());
    }

    private OnlyOneVisible getVisibility() {
        if (cachedOnlyOneVisible == null) {
            cachedOnlyOneVisible = new OnlyOneVisible(listWindow);
        }

        return cachedOnlyOneVisible;
    }

    public Order getOrder() {
        return orderModel.getOrder();
    }

    public void saveAndContinue() {
        saveAndContinue(true);
    }

    protected void saveAndContinue(boolean showSaveMessage) {

        Order order = orderModel.getOrder();
        final boolean isNewObject = order.isNewObject();
        setCurrentTab();
        Tab previousTab = getCurrentTab();
        save(showSaveMessage);

        if ( orderModel.userCanRead(order, SecurityUtils.getSessionUserLoginName()) ) {
            refreshOrderWindow();

            // Come back to the current tab after initialize all tabs
            resetSelectedTab();
            selectTab(previousTab.getId());
            Events.sendEvent(new SelectEvent<>(Events.ON_SELECT, previousTab, null));

            if ( isNewObject ) {
                this.planningControllerEntryPoints.goToOrderDetails(order);
            }
        } else {
            Messagebox.show(
                    _("You don't have read access to this project"), _(INFORMATION),
                    Messagebox.OK, Messagebox.INFORMATION);

            goToList();
        }
    }

    private void refreshOrderWindow() {
        if ( orderElementTreeController != null ) {
            orderElementTreeController.resetCellsMarkedAsModified();
        }
        updateDisabilitiesOnInterface();
        refreshCodeTextboxesOnly();
        getVisibility().showOnly(editWindow);
    }

    private void refreshCodeTextboxesOnly() {
        if ( orderElementTreeController != null ) {

            Map<OrderElement, Textbox> orderElementCodeTextBoxes =
                    orderElementTreeController.getOrderElementCodeTextboxes();

            for (OrderElement element : orderElementCodeTextBoxes.keySet()) {
                if ( element.getId() != null ) {
                    orderElementCodeTextBoxes.get(element).setValue(element.getCode());
                }
            }
        }
    }

    private void save(boolean showSaveMessage) {
        if ( manageOrderElementAdvancesController != null ) {
            selectTab(TAB_ADVANCES);
            if ( !manageOrderElementAdvancesController.save() ) {
                setCurrentTab();

                return;
            }
        }

        if ( assignedCriterionRequirementController != null ) {
            selectTab("tabRequirements");
            if ( !assignedCriterionRequirementController.close() ) {
                setCurrentTab();

                return;
            }
        }

        if ( assignedTaskQualityFormController != null ) {
            selectTab("tabTaskQualityForm");
            if (!assignedTaskQualityFormController.confirm()) {
                setCurrentTab();

                return;
            }
        }

        // Come back to the default tab
        if ( getCurrentTab() != null ) {
            selectTab(getCurrentTab().getId());
        }

        orderModel.save(showSaveMessage);
    }

    private void selectDefaultTab() {
        selectTab(DEFAULT_TAB);
    }

    private void resetSelectedTab() {
        selectedTab = null;
    }

    private void setCurrentTab() {
        Tabbox tabboxOrder = (Tabbox) editWindow.getFellowIfAny("tabboxOrder");
        if ( tabboxOrder != null ) {
            selectedTab = tabboxOrder.getSelectedTab();
        }
    }

    Tab getCurrentTab() {
        return selectedTab;
    }

    void selectTab(String str) {
        Tab tab = (Tab) editWindow.getFellowIfAny(str);
        if ( tab != null ) {
            tab.setSelected(true);
        }
    }

    public void goToList() {
        loadComponents();
        showWindow(listWindow);
    }

    private void loadComponents() {
        // Load the components of the order list
        listing = (Grid) listWindow.getFellow("listing");
        showOrderFilter();
        showCreateButtons(true);
    }

    private void showWindow(Window window) {
        getVisibility().showOnly(window);
        Util.reloadBindings(ReloadStrategy.ONE_PER_REQUEST, window);
    }

    public void reloadHoursGroupOrder() {
        if ("tabRequirements".equals(getCurrentTab().getId())) {
            assignedCriterionRequirementController.reload();
        }
    }

    public void cancel() {
        goToList();
    }

    public void up() {
        if ( onUp == null ) {
            throw new IllegalStateException("in order to call up onUp action should have been set");
        }
        onUp.run();
    }

    public void confirmRemove(Order order) {
        if ( orderModel.userCanWrite(order) ) {

            int status = Messagebox.show(
                    _("Confirm deleting {0}. Are you sure?", order.getName()), DELETE,
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

            if ( Messagebox.OK == status ) {
                remove(order);
            }
        }
        else {
            Messagebox.show(
                    _("Not enough permissions to edit this project"), _(INFORMATION),
                    Messagebox.OK, Messagebox.INFORMATION);
        }
    }

    private void remove(Order order) {
        boolean hasImputedExpenseSheets = orderModel.hasImputedExpenseSheetsThisOrAnyOfItsChildren(order);

        if ( hasImputedExpenseSheets ) {

            messagesForUser.showMessage(
                    Level.ERROR,
                    _("You can not remove the project \"{0}\" because this one has imputed expense sheets.",
                            order.getName()));
            return;
        }

        boolean alreadyInUse = orderModel.isAlreadyInUseAndIsOnlyInCurrentScenario(order);
        if ( alreadyInUse ) {

            messagesForUser.showMessage(
                    Level.ERROR,
                    _("You can not remove the project \"{0}\" because it has time tracked at some of its tasks",
                            order.getName()));
        } else {
            if ( !StringUtils.isBlank(order.getExternalCode()) ) {

                if ( Messagebox.show(
                        _("This project is a subcontracted project. If you delete it, " +
                                "you won't be able to report progress anymore. Are you sure?"),
                        _("Confirm"),
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.CANCEL ) {
                    return;
                }
            }

            orderModel.remove(order);
            Util.reloadBindings(self);

            messagesForUser.clearMessages();
            messagesForUser.showMessage(Level.INFO, _("Removed {0}", order.getName()));
        }
    }

    public void schedule(Order order) {
        orderModel.useSchedulingDataForCurrentScenario(order);
        if ( orderModel.userCanRead(order, SecurityUtils.getSessionUserLoginName()) ) {

            if ( order.isScheduled() ) {
                planningControllerEntryPoints.goToScheduleOf(order);
                showCreateButtons(false);

            } else {
                Messagebox.show(
                        _("The project has no scheduled elements"), _(INFORMATION),
                        Messagebox.OK, Messagebox.INFORMATION);
            }
        } else {
            Messagebox.show(
                    _("You don't have read access to this project"), _(INFORMATION),
                    Messagebox.OK, Messagebox.INFORMATION);
        }
    }

    private void createTemplate(Order order) {
        orderTemplates.goToCreateTemplateFrom(order);
    }

    public void createFromTemplate(OrderTemplate template) {
        orderModel.prepareCreationFrom(template, getDesktop());
    }

    public void goToEditForm(Order order) {
        showOrderElementFilter();
        showCreateButtons(false);
        planningControllerEntryPoints.goToOrderDetails(order);
    }

    public void initEdit(Order order) {
        checkUserCanRead(order);
        orderModel.initEdit(order, getDesktop());
        prepareEditWindow();
    }

    public void checkUserCanRead(Order order) {
        if ( !orderModel.userCanRead(order, SecurityUtils.getSessionUserLoginName()) ) {
            Messagebox.show(
                    _("Sorry, you do not have permissions to access this project"), _(INFORMATION),
                    Messagebox.OK, Messagebox.INFORMATION);
        }
    }

    public IOrderModel getOrderModel() {
        return orderModel;
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
        resetTabControllers();
        addEditWindowIfNecessary();
        updateDisabilitiesOnInterface();
        setupOrderElementTreeController();
        selectDefaultTab();
    }

    private void initializeCustomerComponent() {
        bdExternalCompanies = (BandboxSearch) editWindow.getFellow("bdExternalCompanies");

        bdExternalCompanies.setListboxEventListener(Events.ON_SELECT, event ->  {
            final Object object = bdExternalCompanies.getSelectedElement();
            orderModel.setExternalCompany((ExternalCompany) object);
        });

        bdExternalCompanies.setListboxEventListener(Events.ON_OK, event ->  {
            final Object object = bdExternalCompanies.getSelectedElement();
            orderModel.setExternalCompany((ExternalCompany) object);
            bdExternalCompanies.close();
        });

        gridAskedEndDates = (Grid) editWindow.getFellow("gridAskedEndDates");

    }

    public void setupOrderDetails() {
        if ( !confirmLastTab() ) {
            return;
        }
        setCurrentTab();
        OrderDatesHandler orderDatesHandler = new OrderDatesHandler(editWindow);
        bindListOrderStatusSelectToOnStatusChange();
        initializeCustomerComponent();
        reloadOrderDetailsTab();
        orderDatesHandler.chooseCurrentSchedulingMode();
        setupJiraSynchronizationController();
        setupTimSynchronizationController();
    }

    private void reloadOrderDetailsTab() {
        Tabpanel tabPanel = (Tabpanel) editWindow.getFellow("tabPanelGeneralData");
        Util.createBindingsFor(tabPanel);
        Util.reloadBindings(tabPanel);
    }

    public void goToCreateForm() {
        prepareForCreate(getDesktop());
        getCreationPopup().showWindow(this, null);
    }

    public void prepareForCreate(Desktop desktop) {
        orderModel.prepareForCreate(desktop);
    }

    void editNewCreatedOrder(Window detailsWindow) {
        showOrderElementFilter();
        hideCreateButtons();
        prepareEditWindow();
        detailsWindow.setVisible(false);
        setupOrderAuthorizationController();
        detailsWindow.getAttributes();
    }

    public ProjectDetailsController getCreationPopup() {
        if ( projectDetailsController == null ) {
            projectDetailsController = new ProjectDetailsController();
        }

        return projectDetailsController;
    }

    private void hideCreateButtons() {
        showCreateButtons(false);
    }

    public void setPlanningControllerEntryPoints(IOrderPlanningGate planningControllerEntryPoints) {
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
        public void render(Comboitem comboitem, Object o, int i) throws Exception {
            BaseCalendar calendar = (BaseCalendar) o;
            comboitem.setLabel(calendar.getName());
            comboitem.setValue(calendar);

            BaseCalendar current = orderModel.getCalendar();
            if ( (current != null) && calendar.getId().equals(current.getId()) ) {
                Combobox combobox = (Combobox) comboitem.getParent();
                combobox.setSelectedItem(comboitem);
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
            if ( orderElementTreeController != null ) {
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
        public void render(Row row, Object o, int i) throws Exception {
            final Order order = (Order) o;
            row.setValue(order);

            appendLabel(row, order.getName());
            appendLabel(row, order.getCode());
            appendDate(row, order.getInitDate());
            appendDate(row, order.getDeadline());
            appendCustomer(row, order.getCustomer());
            appendObject(row, Util.addCurrencySymbol(order.getTotalManualBudget()));
            appendObject(row, Util.addCurrencySymbol(order.getTotalBudget()));
            appendObject(row, order.getTotalHours());
            appendObject(row, _(order.getState().toString()));
            appendOperations(row, order);

            row.setTooltiptext(getTooltipText(order));
            row.addEventListener(ON_CLICK_EVENT, event -> goToEditForm(order));
        }

        private void appendObject(final Row row, Serializable object) {
            String text = "";
            if ( object != null ) {
                text = object.toString();
            }
            appendLabel(row, text);
        }

        private void appendCustomer(final Row row, ExternalCompany externalCompany) {
            String customerName = "";
            if ( externalCompany != null ) {
                customerName = externalCompany.getName();
            }
            appendLabel(row, customerName);
        }

        private void appendDate(final Row row, Date date) {
            String labelDate = "";
            if ( date != null ) {
                labelDate = Util.formatDate(date);
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

        private void appendLabel(final Row row, String value) {
            Label label = new Label(value);
            row.appendChild(label);
        }

        private void appendButtonEdit(final Hbox hbox, final Order order) {
            Button buttonEdit = new Button();
            buttonEdit.setSclass(ICONO_CLASS);
            buttonEdit.setImage("/common/img/ico_editar1.png");
            buttonEdit.setHoverImage("/common/img/ico_editar.png");
            buttonEdit.setTooltiptext(_("Edit"));
            buttonEdit.addEventListener(ON_CLICK_EVENT, event -> goToEditForm(order));
            hbox.appendChild(buttonEdit);
        }

        private void appendButtonDelete(final Hbox hbox, final Order order) {
            if ( orderModel.userCanWrite(order) ) {
                Button buttonDelete = new Button();
                buttonDelete.setSclass(ICONO_CLASS);
                buttonDelete.setImage("/common/img/ico_borrar1.png");
                buttonDelete.setHoverImage("/common/img/ico_borrar.png");
                buttonDelete.setTooltiptext(_(DELETE));
                buttonDelete.addEventListener(ON_CLICK_EVENT, event -> confirmRemove(order));
                hbox.appendChild(buttonDelete);
            }
        }

        private void appendButtonPlan(final Hbox hbox, final Order order) {
            Button buttonPlan = new Button();
            buttonPlan.setSclass(ICONO_CLASS);
            buttonPlan.setImage("/common/img/ico_planificador1.png");
            buttonPlan.setHoverImage("/common/img/ico_planificador.png");
            buttonPlan.setTooltiptext(_("See scheduling"));
            buttonPlan.addEventListener(ON_CLICK_EVENT, event -> schedule(order));
            hbox.appendChild(buttonPlan);
        }

        private void appendButtonDerived(final Hbox hbox, final Order order) {
            Button buttonDerived = new Button();
            buttonDerived.setSclass(ICONO_CLASS);
            buttonDerived.setImage("/common/img/ico_derived1.png");
            buttonDerived.setHoverImage("/common/img/ico_derived.png");
            buttonDerived.setTooltiptext(_("Create Template"));
            buttonDerived.addEventListener(ON_CLICK_EVENT, event -> createTemplate(order));

            if ( !SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TEMPLATES) ) {
                buttonDerived.setDisabled(true);
                buttonDerived.setTooltiptext(_("Not enough permissions to create templates"));
            }

            hbox.appendChild(buttonDerived);
        }
    }

    public String getTooltipText(final Order order) {
        return orderModel.gettooltipText(order);
    }

    public void reloadTotalBudget(Label labelTotalBudget) {
        Util.reloadBindings(labelTotalBudget);
    }

    /**
     * Operations to filter the orders by multiple filters.
     */

    public Constraint checkConstraintFinishDate() {
        return (comp, value) -> {
            Date finishDate = (Date) value;

            if ( (finishDate != null) && (filterStartDate.getRawValue() != null) &&
                    (finishDate.compareTo((Date) filterStartDate.getRawValue()) < 0) ) {

                throw new WrongValueException(comp, _("must be after start date"));
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return (comp, value) -> {
            Date startDate = (Date) value;

            if ( (startDate != null) && (filterFinishDate.getRawValue() != null) &&
                    (startDate.compareTo((Date) filterFinishDate.getRawValue()) > 0) ) {

                throw new WrongValueException(comp, _("must be lower than end date"));
            }
        };
    }

    public void onApplyFilter() {
        OrderPredicate predicate = createPredicate();
        storeSessionVariables();
        FilterUtils.writeProjectFilterChanged(true);

        if ( predicate != null ) {
            // Force reload conversation state in oderModel
            getOrders();
            filterByPredicate(predicate);
        } else {
            showAllOrders();
        }

    }

    private void storeSessionVariables() {
        FilterUtils.writeProjectsFilter(
                filterStartDate.getValue(),
                filterFinishDate.getValue(),
                getSelectedBandboxAsTaskGroupFilters(),
                filterProjectName.getValue(),
                filterExcludeFinishedProject.getValue());
    }

    private List<FilterPair> getSelectedBandboxAsTaskGroupFilters() {
        List<FilterPair> result = new ArrayList<>();

        for (FilterPair filterPair : (List<FilterPair>) bdFilters.getSelectedElements()) {
            OrderFilterEnum type = (OrderFilterEnum) filterPair.getType();
            switch (type) {
                case Label:
                    result.add(new FilterPair(
                            TaskGroupFilterEnum.Label, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case Criterion:
                    result.add(new FilterPair(
                            TaskGroupFilterEnum.Criterion, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case ExternalCompany:
                    result.add(new FilterPair(
                            TaskGroupFilterEnum.ExternalCompany, filterPair.getPattern(), filterPair.getValue()));
                    break;

                case State:
                    result.add(new FilterPair(
                            TaskGroupFilterEnum.State, filterPair.getPattern(), filterPair.getValue()));
                    break;

                default:
                    result.add(new FilterPair(
                            OrderFilterEnum.Label, filterPair.getPattern(), filterPair.getValue()));
                    break;
            }
        }
        return result;
    }

    private OrderPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters.getSelectedElements();

        Date startDate = filterStartDate.getValue();
        Date finishDate = filterFinishDate.getValue();
        String name = filterProjectName.getValue();

        return listFilters.isEmpty() && startDate == null && finishDate == null && name == null
                ? null
                : new OrderPredicate(listFilters, startDate, finishDate, name);
    }

    private void filterByPredicate(OrderPredicate predicate) {
        List<Order> filterOrders = orderModel.getFilterOrders(predicate);
        listing.setModel(new SimpleListModel<>(filterOrders.toArray()));
        listing.invalidate();
    }

    private void showAllOrders() {
        listing.setModel(new SimpleListModel<>(getOrders().toArray()));
        listing.invalidate();
    }

    private void showOrderFilter() {
        orderFilter.setVisible(true);
        orderElementFilter.setVisible(false);
    }

    public void showOrderElementFilter() {
        if ( orderFilter != null ) {
            orderFilter.setVisible(false);
        }

        if ( orderElementFilter != null ) {
            orderElementFilter.setVisible(true);
        }
    }

    public void showCreateButtons(boolean showCreate) {
        if ( !showCreate ) {
            Hbox perspectiveButtonsInsertionPoint = (Hbox) page.getFellow("perspectiveButtonsInsertionPoint");
            perspectiveButtonsInsertionPoint.getChildren().clear();
            saveOrderAndContinueButton.setParent(perspectiveButtonsInsertionPoint);
            cancelEditionButton.setParent(perspectiveButtonsInsertionPoint);
        }

        if ( createOrderButton != null ) {
            createOrderButton.setVisible(showCreate);
        }

        if ( saveOrderAndContinueButton != null ) {
            saveOrderAndContinueButton.setVisible(!showCreate);
        }

        if ( cancelEditionButton != null ) {
            cancelEditionButton.setVisible(!showCreate);
        }

    }

    public void highLight(final OrderElement orderElement) {
        final Tab tab = (Tab) editWindow.getFellowIfAny("tabOrderElements");

        LongOperationFeedback.executeLater(tab, () -> {
            if ( tab != null ) {
                tab.setSelected(true);
                Events.sendEvent(new SelectEvent<>(Events.ON_SELECT, tab, null));
            }

            if ( !(orderElement instanceof Order) && orderElementTreeController != null ) {
                final Treeitem item = orderElementTreeController.getTreeitemByOrderElement(orderElement);

                if ( item != null)  {
                    orderElementTreeController.showEditionOrderElement(item);
                }
            }
        });
    }

    /**
     * Checks the creation permissions of the current user and enables/disables the create buttons accordingly.
     */
    private void checkCreationPermissions() {
        if ( !SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_CREATE_PROJECTS) && createOrderButton != null ) {
            createOrderButton.setDisabled(true);
        }
    }

    private void updateDisabilitiesOnInterface() {
        Order order = orderModel.getOrder();

        boolean permissionForWriting = orderModel.userCanWrite(order);
        boolean isInStoredState = order.getState() == OrderStatusEnum.STORED;
        boolean isInitiallyStored = orderModel.getPlanningState().getSavedOrderState() == OrderStatusEnum.STORED;

        readOnly = !permissionForWriting || isInStoredState;

        if ( orderElementTreeController != null ) {
            orderElementTreeController.setReadOnly(readOnly);
        }
        saveOrderAndContinueButton.setDisabled(!permissionForWriting || (isInitiallyStored && isInStoredState));
    }

    public void sortOrders() {
        Column columnDateStart = (Column) listWindow.getFellow("columnDateStart");
        if (columnDateStart != null) {
            if ( "ascending".equals(columnDateStart.getSortDirection()) ) {
                columnDateStart.sort(false, false);
                columnDateStart.setSortDirection("ascending");
            } else if ( "descending".equals(columnDateStart.getSortDirection()) ) {
                columnDateStart.sort(true, false);
                columnDateStart.setSortDirection("descending");
            }
        }
    }

    public SortedSet<DeadlineCommunication> getDeliverDates() {
        return getOrder() != null ? getOrder().getDeliveringDates() : new TreeSet<>(new DeliverDateComparator());
    }

    public Constraint checkValidProjectName() {
        return (comp, value) -> {

            if ( StringUtils.isBlank((String) value) ) {
                throw new WrongValueException(comp, _("cannot be empty"));
            }

            try {
                Order found = orderDAO.findByNameAnotherTransaction((String) value);
                if ( !found.getId().equals(getOrder().getId()) ) {
                    throw new WrongValueException(comp, _("project name already being used"));
                }
            } catch (InstanceNotFoundException ignored) {}
        };
    }

    public Constraint checkValidProjectCode() {
        return (comp, value) -> {

            if ( StringUtils.isBlank((String) value) ) {
                throw new WrongValueException(comp, _("cannot be empty"));
            }

            try {
                Order found = orderDAO.findByCodeAnotherTransaction((String) value);
                if ( !found.getId().equals(getOrder().getId()) ) {
                    throw new WrongValueException(comp, _("project code already being used"));
                }
            } catch (InstanceNotFoundException ignored) {}
        };
    }

    public boolean isSubcontractedProject() {
        return (getOrder() != null) && (getOrder().getExternalCode() != null);
    }

    public String getProjectType() {
        return isSubcontractedProject() ? _("Subcontracted by client") : _("Regular project");
    }

    public void setCurrentDeliveryDate(Grid listDeliveryDates) {
        if ( getOrder() != null &&
                getOrder().getDeliveringDates() != null &&
                !getOrder().getDeliveringDates().isEmpty() ) {

            DeadlineCommunication lastDeliveryDate = getOrder().getDeliveringDates().first();
            if ( listDeliveryDates != null ) {

                listDeliveryDates.renderAll();
                final Rows rows = listDeliveryDates.getRows();

                for (Component component : rows.getChildren()) {
                    final Row row = (Row) component;
                    final DeadlineCommunication deliveryDate = row.getValue();

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
        if ( newEndDate == null || newEndDate.getValue() == null ) {
            messagesForUser.showMessage(Level.ERROR, _("You must select a valid date. "));

            return;
        }

        if ( thereIsSomeCommunicationDateEmpty() ) {
            messagesForUser.showMessage(
                    Level.ERROR,
                    _("It will only be possible to add an end date if all the exiting ones in the table " +
                            "have already been sent to the customer."));
            return;
        }

        if ( orderModel.alreadyExistsRepeatedEndDate(newEndDate.getValue()) ) {
            messagesForUser.showMessage(Level.ERROR, _("It already exists a end date with the same date. "));

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
            if ( endDate.getCommunicationDate() == null ) {
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
        public void render(Row row, Object o, int i) throws Exception {
            EndDateCommunication endDate = (EndDateCommunication) o;
            row.setValue(endDate);

            appendLabel(row, Util.formatDateTime(endDate.getSaveDate()));
            appendLabel(row, Util.formatDate(endDate.getEndDate()));
            appendLabel(row, Util.formatDateTime(endDate.getCommunicationDate()));
            appendOperations(row, endDate);
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
            deleteButton.setSclass(ICONO_CLASS);
            deleteButton.setImage("/common/img/ico_borrar1.png");
            deleteButton.setHoverImage("/common/img/ico_borrar.png");
            deleteButton.setTooltiptext(_(DELETE));
            deleteButton.addEventListener(Events.ON_CLICK, event -> removeAskedEndDate(endDate));

            return deleteButton;
        }

        private boolean isNotUpdate(final EndDateCommunication endDate) {
            EndDateCommunication lastAskedEndDate = getOrder().getEndDateCommunicationToCustomer().first();

            return !((lastAskedEndDate != null) &&
                    (lastAskedEndDate.equals(endDate))) ||
                    (lastAskedEndDate.getCommunicationDate() != null);

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

    public void readSessionFilterDates() {
        filterStartDate.setValue(FilterUtils.readProjectsStartDate());
        filterFinishDate.setValue(FilterUtils.readProjectsEndDate());
        filterProjectName.setValue(FilterUtils.readProjectsName());
        filterExcludeFinishedProject.setValue(FilterUtils.readExcludeFinishedProjects());

        loadLabels();
    }

    /**
     * Setup the connector, JiraSynchronization controller.
     */
    public void setupJiraSynchronizationController() {
        if ( jiraSynchronizationController == null ) {
            jiraSynchronizationController = new JiraSynchronizationController();
            jiraSynchronizationController.setOrderController(this);
        }

        try {
            jiraSynchronizationController.doAfterCompose(editWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Setup the connector, TimSynchronization controller.
     */
    public void setupTimSynchronizationController() {
        if ( timSynchronizationController == null ) {
            timSynchronizationController = new TimSynchronizationController();
            timSynchronizationController.setOrderController(this);
        }

        try {
            timSynchronizationController.doAfterCompose(editWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BigDecimal getResourcesBudget() {
        return Registry
                .getTransactionService()
                .runOnReadOnlyTransaction(() -> getOrderElementModel().getOrderElement().getResourcesBudget());
    }

    public BigDecimal getTotalBudget() {
        return getOrder().getBudget().add(getResourcesBudget());
    }

    private Boolean isDeleteAllProjectsButtonDisabled() {
        return Configuration.getInstance().isDeleteAllProjectsButtonDisabled();
    }

    /**
     * Should be public!
     * Used in orders/_orderFilter.zul
     */
    public void deleteAllProjects() {
        boolean canNotDelete = false;
        for (Order order : orderModel.getOrders()) {
            try {
                orderModel.remove(order);
            } catch (Exception ignored) {
                canNotDelete = true;
                continue;
            }
        }
        if (canNotDelete) {
            messagesForUser.showMessage(Level.ERROR, "Not all projects were removed") ;
        }
        listing.setModel(new SimpleListModel<>(orderModel.getOrders()));
        listing.invalidate();
    }

}
