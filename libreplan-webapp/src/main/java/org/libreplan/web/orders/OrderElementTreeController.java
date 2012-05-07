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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.orders.assigntemplates.TemplateFinderPopup;
import org.libreplan.web.orders.assigntemplates.TemplateFinderPopup.IOnResult;
import org.libreplan.web.templates.IOrderTemplatesControllerEntryPoints;
import org.libreplan.web.tree.TreeController;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.api.Treecell;
import org.zkoss.zul.api.Treerow;
import org.zkoss.zul.impl.api.InputElement;

/**
 * Controller for {@link OrderElement} tree view of {@link Order} entities <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class OrderElementTreeController extends TreeController<OrderElement> {

    private Vbox orderElementFilter;

    private BandboxMultipleSearch bdFiltersOrderElement;

    private Datebox filterStartDateOrderElement;

    private Datebox filterFinishDateOrderElement;

    private Checkbox labelsWithoutInheritance;

    private Textbox filterNameOrderElement;

    private OrderElementTreeitemRenderer renderer = new OrderElementTreeitemRenderer();

    private final IOrderModel orderModel;

    private final OrderElementController orderElementController;

    private transient IPredicate predicate;

    @Resource
    private IOrderTemplatesControllerEntryPoints orderTemplates;

    private OrderElementOperations operationsForOrderElement;

    private final IMessagesForUser messagesForUser;

    private Popup filterOptionsPopup;

    public List<org.libreplan.business.labels.entities.Label> getLabels() {
        return orderModel.getLabels();
    }

    @Override
    public OrderElementTreeitemRenderer getRenderer() {
        return renderer;
    }

    public OrderElementTreeController(IOrderModel orderModel,
            OrderElementController orderElementController,
            IMessagesForUser messagesForUser) {
        super(OrderElement.class);
        this.orderModel = orderModel;
        this.orderElementController = orderElementController;
        this.messagesForUser = messagesForUser;
        initializeOperationsForOrderElement();
    }

    /**
     * Initializes operationsForOrderTemplate. A reference to variables tree and
     * orderTemplates will be set later in doAfterCompose()
     */
    private void initializeOperationsForOrderElement() {
        operationsForOrderElement = OrderElementOperations.build()
            .treeController(this)
            .orderModel(this.orderModel)
            .orderElementController(this.orderElementController);
    }

    public OrderElementController getOrderElementController() {
        return orderElementController;
    }

    @Override
    protected OrderElementTreeModel getModel() {
        return orderModel.getOrderElementTreeModel();
    }

    /**
     * Operations for each node
     */

    public void editSelectedElement() {
        operationsForOrderElement.editSelectedElement();
    }

    public void createTemplateFromSelectedElement() {
        operationsForOrderElement.createTemplateFromSelectedElement();
    }

    public void moveSelectedElementUp() {
        operationsForOrderElement.moveSelectedElementUp();
    }

    public void moveSelectedElementDown() {
        operationsForOrderElement.moveSelectedElementDown();
    }

    public void indentSelectedElement() {
        operationsForOrderElement.indentSelectedElement();
    }

    public void unindentSelectedElement() {
        operationsForOrderElement.unindentSelectedElement();
    }

    public void deleteSelectedElement() {
        operationsForOrderElement.deleteSelectedElement();
    }

    public void createFromTemplate() {
        templateFinderPopup.openForSubElemenetCreation(tree, "after_pointer",
                new IOnResult<OrderElementTemplate>() {
                    @Override
                    public void found(OrderElementTemplate template) {
                        OrderLineGroup parent = (OrderLineGroup) getModel()
                                .getRoot();
                        orderModel.createFrom(parent, template);
                        getModel().addNewlyAddedChildrenOf(parent);
                    }
                });
    }

    protected void filterByPredicateIfAny() {
        if (predicate != null) {
            filterByPredicate();
        }
    }

    private void filterByPredicate() {
        OrderElementTreeModel orderElementTreeModel = orderModel
                .getOrderElementsFilteredByPredicate(predicate);
        tree.setModel(orderElementTreeModel.asTree());
        tree.invalidate();
    }

    void doEditFor(Order order) {
        Util.reloadBindings(tree);
    }

    public void disabledCodeBoxes(boolean disabled) {
        Set<Treeitem> childrenSet = new HashSet<Treeitem>();
        Treechildren treeChildren = tree.getTreechildren();
        if (treeChildren != null) {
            childrenSet.addAll((Collection<Treeitem>) treeChildren.getItems());
        }
        for (Treeitem each : childrenSet) {
            disableCodeBoxes(each, disabled);
        }
    }

    private void disableCodeBoxes(Treeitem item, boolean disabled) {
        Treerow row = item.getTreerow();
        InputElement codeBox = (InputElement) ((Treecell) row.getChildren()
                .get(1)).getChildren().get(0);
        codeBox.setDisabled(disabled);
        codeBox.invalidate();

        Set<Treeitem> childrenSet = new HashSet<Treeitem>();
        Treechildren children = item.getTreechildren();
        if (children != null) {
            childrenSet.addAll((Collection<Treeitem>) children.getItems());
        }

        for (Treeitem each : childrenSet) {
            disableCodeBoxes(each, disabled);
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        orderElementFilter.getChildren().clear();
        appendExpandCollapseButton();

        // Configuration of the order elements filter
        Component filterComponent = Executions.createComponents(
                "/orders/_orderElementTreeFilter.zul", orderElementFilter,
                new HashMap<String, String>());
        filterComponent.setVariable("treeController", this, true);
        bdFiltersOrderElement = (BandboxMultipleSearch) filterComponent
                .getFellow("bdFiltersOrderElement");
        filterOptionsPopup = (Popup) filterComponent
                .getFellow("filterOptionsPopup");
        filterStartDateOrderElement = (Datebox) filterOptionsPopup
                .getFellow("filterStartDateOrderElement");
        filterFinishDateOrderElement = (Datebox) filterOptionsPopup
                .getFellow("filterFinishDateOrderElement");
        labelsWithoutInheritance = (Checkbox) filterOptionsPopup
                .getFellow("labelsWithoutInheritance");
        filterNameOrderElement = (Textbox) filterComponent
                .getFellow("filterNameOrderElement");
        labelsWithoutInheritance = (Checkbox) filterComponent
                .getFellow("labelsWithoutInheritance");
        templateFinderPopup = (TemplateFinderPopup) comp
                .getFellow("templateFinderPopupAtTree");
        operationsForOrderElement.tree(tree)
                .orderTemplates(this.orderTemplates);
    }

    private void appendExpandCollapseButton() {
        List<Component> children = orderElementFilter.getParent().getChildren();

        // Is already added?
        Button button = (Button) ComponentsFinder.findById("expandAllButton", children);
        if (button != null) {
            if (button.getSclass().equals("planner-command clicked")) {
                button.setSclass("planner-command");
                button.invalidate();
            }
            return;
        }

        // Append expand/collapse button
        final Button expandAllButton = new Button();
        expandAllButton.setId("expandAllButton");
        expandAllButton.setClass("planner-command");
        expandAllButton.setTooltiptext(_("Expand/Collapse all"));
        expandAllButton.setImage("/common/img/ico_expand.png");
        expandAllButton.addEventListener("onClick", new EventListener() {
            @Override
            public void onEvent(Event event) {
                if (expandAllButton.getSclass().equals("planner-command")) {
                    expandAll();
                    expandAllButton.setSclass("planner-command clicked");
                } else {
                    collapseAll();
                    expandAllButton.setSclass("planner-command");
                }
            }
        });
        children.add(expandAllButton);
    }

    public void expandAll() {
        Set<Treeitem> childrenSet = new HashSet<Treeitem>();
        Treechildren children = tree.getTreechildren();
        if(children != null) {
            childrenSet.addAll((Collection<Treeitem>) children.getItems());
        }
        for(Treeitem each: childrenSet) {
            expandAll(each);
        }
    }

    private void expandAll(Treeitem item) {
        item.setOpen(true);

        Set<Treeitem> childrenSet = new HashSet<Treeitem>();
        Treechildren children = item.getTreechildren();
        if(children != null) {
            childrenSet.addAll((Collection<Treeitem>) children.getItems());
        }

        for(Treeitem each: childrenSet) {
            expandAll(each);
        }
    }

    public void collapseAll() {
        Treechildren children = tree.getTreechildren();
        for(Treeitem each: (Collection<Treeitem>) children.getItems()) {
            each.setOpen(false);
        }
    }

    private Map<OrderElement, Textbox> orderElementCodeTextboxes = new HashMap<OrderElement, Textbox>();

    public Map<OrderElement, Textbox> getOrderElementCodeTextboxes() {
        return orderElementCodeTextboxes;
    }

    public class OrderElementTreeitemRenderer extends Renderer {

        public OrderElementTreeitemRenderer() {
        }

        @Override
        protected void addDescriptionCell(OrderElement element) {
            addTaskNameCell(element);
        }

        private void addTaskNameCell(final OrderElement orderElementForThisRow) {
            int[] path = getModel().getPath(orderElementForThisRow);
            String cssClass = "depth_" + path.length;

            Textbox textBox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return orderElementForThisRow.getName();
                        }
                    }, new Util.Setter<String>() {

                        @Override
                        public void set(String value) {
                            orderElementForThisRow.setName(value);
                        }
                    });
            if (readOnly) {
                textBox.setDisabled(true);
            }
            textBox.setConstraint("no empty:" + _("cannot be empty"));
            addCell(cssClass, textBox);
            putNameTextbox(orderElementForThisRow, textBox);
        }

        @Override
        protected SchedulingState getSchedulingStateFrom(
                OrderElement currentElement) {
            return currentElement.getSchedulingState();
        }

        @Override
        protected void onDoubleClickForSchedulingStateCell(
                final OrderElement currentOrderElement) {
            IOrderElementModel model = orderModel
                    .getOrderElementModel(currentOrderElement);
            orderElementController.openWindow(model);
            updateNameFor(currentOrderElement);
            updateHoursFor(currentOrderElement);
            updateBudgetFor(currentOrderElement);
        }

        protected void addCodeCell(final OrderElement orderElement) {
            Textbox textBoxCode = new Textbox();
            Util.bind(textBoxCode, new Util.Getter<String>() {
                @Override
                public String get() {
                    return orderElement.getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    orderElement.setCode(value);
                }
            });
            textBoxCode.setConstraint(new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!orderElement.isFormatCodeValid((String) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid.\n Code cannot contain chars like '_' \n and should not be empty"));
                    }
                }
            });

            if (orderModel.isCodeAutogenerated() || readOnly) {
                textBoxCode.setDisabled(true);
            }

            addCell(textBoxCode);
            orderElementCodeTextboxes.put(orderElement, textBoxCode);
        }

        void addInitDateCell(final OrderElement currentOrderElement) {
            DynamicDatebox dinamicDatebox = new DynamicDatebox(
                    new DynamicDatebox.Getter<Date>() {

                        @Override
                        public Date get() {
                            return currentOrderElement.getInitDate();
                        }
                    }, new DynamicDatebox.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            currentOrderElement.setInitDate(value);

                        }
                    });
            if (readOnly) {
                dinamicDatebox.setDisabled(true);
            }
            addDateCell(dinamicDatebox, _("init"));
        }

        void addEndDateCell(final OrderElement currentOrderElement) {
            DynamicDatebox dinamicDatebox = new DynamicDatebox(
                    new DynamicDatebox.Getter<Date>() {

                        @Override
                        public Date get() {
                            return currentOrderElement.getDeadline();
                        }
                    }, new DynamicDatebox.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            currentOrderElement.setDeadline(value);
                        }
                    });
            if (readOnly) {
                dinamicDatebox.setDisabled(true);
            }
            addDateCell(dinamicDatebox, _("end"));
        }

        @Override
        protected void addOperationsCell(final Treeitem item,
                final OrderElement currentOrderElement) {
            addCell(createEditButton(currentOrderElement, item),
                    createRemoveButton(currentOrderElement));
        }

        private Button createEditButton(final OrderElement currentOrderElement,
                final Treeitem item) {
            Button editbutton = createButton("/common/img/ico_editar1.png",
                    _("Edit"), "/common/img/ico_editar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            showEditionOrderElement(item);
                        }
                    });
            return editbutton;
        }

    }

    @Override
    protected boolean isPredicateApplied() {
        return predicate != null;
    }

    /**
     * Apply filter to order elements in current order
     */
    public void onApplyFilter() {
        OrderElementPredicate predicate = createPredicate();
        this.predicate = predicate;

        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllOrderElements();
        }
    }

    private OrderElementPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFiltersOrderElement
                .getSelectedElements();
        Date startDate = filterStartDateOrderElement.getValue();
        Date finishDate = filterFinishDateOrderElement.getValue();
        boolean ignoreLabelsInheritance = Boolean
                .valueOf(labelsWithoutInheritance.isChecked());
        String name = filterNameOrderElement.getValue();

        if (listFilters.isEmpty() && startDate == null && finishDate == null
                && name == null) {
            return null;
        }
        return new OrderElementPredicate(listFilters, startDate, finishDate,
                name, ignoreLabelsInheritance);
    }

    private void filterByPredicate(OrderElementPredicate predicate) {
        OrderElementTreeModel orderElementTreeModel = orderModel
                .getOrderElementsFilteredByPredicate(predicate);
        tree.setModel(orderElementTreeModel.asTree());
        tree.invalidate();
    }

    public void showAllOrderElements() {
        this.predicate = null;
        tree.setModel(orderModel.getOrderElementTreeModel().asTree());
        tree.invalidate();
    }

    @Override
    protected boolean isNewButtonDisabled() {
        if(readOnly) {
            return true;
        }
        return isPredicateApplied();
    }

    /**
     * Clear {@link BandboxSearch} for Labels, and initializes
     * {@link IPredicate}
     */
    public void clear() {
        selectDefaultTab();
        bdFiltersOrderElement.clear();
        predicate = null;
    }

    Tab tabGeneralData;

    private TemplateFinderPopup templateFinderPopup;

    private void selectDefaultTab() {
        tabGeneralData.setSelected(true);
    }

    @Override
    protected String createTooltipText(OrderElement elem) {
        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(elem.getName() + ". ");
        if ((elem.getDescription() != null)
                && (!elem.getDescription().equals(""))) {
            tooltipText.append(elem.getDescription());
            tooltipText.append(". ");
        }
        if ((elem.getLabels() != null) && (!elem.getLabels().isEmpty())) {
            tooltipText.append(" " + _("Labels") + ":");
            tooltipText.append(StringUtils.join(elem.getLabels(), ","));
            tooltipText.append(".");
        }
        if ((elem.getCriterionRequirements() != null)
                && (!elem.getCriterionRequirements().isEmpty())) {
            ArrayList<String> criterionNames = new ArrayList<String>();
            for(CriterionRequirement each:elem.getCriterionRequirements()) {
                if (each.isValid()) {
                    criterionNames.add(each.getCriterion().getName());
                }
            }
            if (!criterionNames.isEmpty()) {
                tooltipText.append(" " + _("Criteria") + ":");
                tooltipText.append(StringUtils.join(criterionNames, ","));
                tooltipText.append(".");
            }
        }
        // To calculate other unit advances implement
        // getOtherAdvancesPercentage()
        tooltipText.append(" " + _("Progress") + ":" + elem.getAdvancePercentage());
        tooltipText.append(".");

        // tooltipText.append(elem.getAdvancePercentage());
        return tooltipText.toString();
    }

    public void showEditionOrderElement(final Treeitem item) {
        OrderElement currentOrderElement = (OrderElement) item.getValue();
        markModifiedTreeitem(item.getTreerow());
        IOrderElementModel model = orderModel
                .getOrderElementModel(currentOrderElement);
        orderElementController.openWindow(model);
        refreshRow(item);
    }

    public void refreshRow(Treeitem item) {
        try {
            getRenderer().updateNameFor((OrderElement) item.getValue());
            getRenderer().updateHoursFor((OrderElement) item.getValue());
            getRenderer().updateBudgetFor((OrderElement) item.getValue());
            getRenderer().render(item, item.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Treeitem getTreeitemByOrderElement(OrderElement element) {
        List<Treeitem> listItems = new ArrayList<Treeitem>(this.tree.getItems());
        for (Treeitem item : listItems) {
            OrderElement orderElement = (OrderElement) item.getValue();
            if (orderElement.getId().equals(element.getId())) {
                return item;
            }
        }
        return null;
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
                        && (filterStartDateOrderElement.getValue() != null)
                        && (finishDate.compareTo(filterStartDateOrderElement
                                .getValue()) < 0)) {
                    filterFinishDateOrderElement.setValue(null);
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
                        && (filterFinishDateOrderElement.getValue() != null)
                        && (startDate.compareTo(filterFinishDateOrderElement
                                .getValue()) > 0)) {
                    filterStartDateOrderElement.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    @Override
    public void remove(OrderElement element) {
        boolean hasImputedExpenseSheets = orderModel.hasImputedExpenseSheets(element);
        if (hasImputedExpenseSheets) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("You can not remove the project \"{0}\" because this one has imputed expense sheets.",
                                    element.getName()));
            return;
        }

        boolean alreadyInUse = orderModel.isAlreadyInUse(element);
        if (alreadyInUse) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("You can not remove the task \"{0}\" because of this or any of its children are already in use in some work reports",
                                    element.getName()));
        } else {
            super.remove(element);
            orderElementCodeTextboxes.remove(element);
        }
    }

    @Override
    protected IHoursGroupHandler<OrderElement> getHoursGroupHandler() {
        return new IHoursGroupHandler<OrderElement>() {

            @Override
            public boolean hasMoreThanOneHoursGroup(OrderElement element) {
                return element.getHoursGroups().size() > 1;
            }

            @Override
            public boolean isTotalHoursValid(OrderElement line, Integer value) {
                return ((OrderLine) line).isTotalHoursValid(value);
            }

            @Override
            public Integer getWorkHoursFor(OrderElement element) {
                return element.getWorkHours();
            }

            @Override
            public void setWorkHours(OrderElement element, Integer value) {
                if (element instanceof OrderLine) {
                    OrderLine line = (OrderLine) element;
                    line.setWorkHours(value);
                }
            }
        };
    }

    @Override
    protected IBudgetHandler<OrderElement> getBudgetHandler() {
        return new IBudgetHandler<OrderElement>() {

            @Override
            public BigDecimal getBudgetFor(OrderElement element) {
                return element.getBudget();
            }

            @Override
            public void setBudgetHours(OrderElement element,
                    BigDecimal budget) {
                if (element instanceof OrderLine) {
                    OrderLine line = (OrderLine) element;
                    line.setBudget(budget);
                }
            }

        };
    }

    @Override
    protected INameHandler<OrderElement> getNameHandler() {
        return new INameHandler<OrderElement>() {

            @Override
            public String getNameFor(OrderElement element) {
                return element.getName();
            }

        };
    }

}
