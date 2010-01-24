/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.orders.assigntemplates.TemplateFinderPopup;
import org.navalplanner.web.orders.assigntemplates.TemplateFinderPopup.IOnResult;
import org.navalplanner.web.templates.IOrderTemplatesControllerEntryPoints;
import org.navalplanner.web.tree.TreeController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Vbox;

/**
 * Controller for {@link OrderElement} tree view of {@link Order} entities <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementTreeController extends TreeController<OrderElement> {

    private Vbox filter;

    private Vbox orderFilter;

    private BandboxSearch bdFilter;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private BandboxMultipleSearch bdFilters;

    private Checkbox checkIncludeOrderElements;

    private OrderElementTreeitemRenderer renderer = new OrderElementTreeitemRenderer();

    private final IOrderModel orderModel;

    private final OrderElementController orderElementController;

    private transient IPredicate predicate;

    @Resource
    private IOrderTemplatesControllerEntryPoints orderTemplates;

    public List<org.navalplanner.business.labels.entities.Label> getLabels() {
        return orderModel.getLabels();
    }

    @Override
    public OrderElementTreeitemRenderer getRenderer() {
        return renderer;
    }

    public OrderElementTreeController(IOrderModel orderModel,
            OrderElementController orderElementController) {
        super(OrderElement.class);
        this.orderModel = orderModel;
        this.orderElementController = orderElementController;
    }

    @Override
    protected OrderElementTreeModel getModel() {
        return orderModel.getOrderElementTreeModel();
    }

    public void createTemplate() {
        if (tree.getSelectedCount() == 1) {
            createTemplate(getSelectedNode());
        }
    }

    public void createFromTemplate() {
        templateFinderPopup.openForSubElemenetCreation(tree, "after_pointer",
                new IOnResult<OrderElementTemplate>() {
                    @Override
                    public void found(OrderElementTemplate template) {
                        OrderLineGroup parent = (OrderLineGroup) getModel()
                                .getRoot();
                        OrderElement created = orderModel.createFrom(parent,
                                template);
                        getModel().addNewlyAddedChildrenOf(parent);
                        if (!created.getChildren().isEmpty()) {
                            // due to a bug of zk Tree, the children of a newly
                            // added element are not shown. Forcing reload.
                            // See comments at
                            // org.zkoss.ganttz.LeftTasksTree.DeferredFiller
                            Util.reloadBindings(tree);
                        }
                    }
                });
    }

    private void createTemplate(OrderElement selectedNode) {
        if (!selectedNode.isNewObject()) {
            orderTemplates.goToCreateTemplateFrom(selectedNode);
        } else {
            notifyTemplateCantBeCreated();
        }
    }

    private void notifyTemplateCantBeCreated() {
        try {
            Messagebox
                    .show(
                            _("Templates can only be created from already existent order elements.\n"
                                    + "Newly order elements cannot be used."),
                            _("Operation cannot be done"), Messagebox.OK,
                            Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Configuration of the order elements filter
        orderFilter.setVisible(false);
        filter.setVisible(true);
        Component filterComponent = Executions.createComponents(
                "/orders/_orderElementTreeFilter.zul",
                filter, new HashMap<String, String>());
        filterComponent.setVariable("treeController", this, true);
        bdFilter = (BandboxSearch) filterComponent.getFellow("bdFilter");
        templateFinderPopup = (TemplateFinderPopup) comp
                .getFellow("templateFinderPopupAtTree");
    }

    public class OrderElementTreeitemRenderer extends Renderer {

        private Map<OrderElement, Intbox> hoursIntBoxByOrderElement = new HashMap<OrderElement, Intbox>();

        public OrderElementTreeitemRenderer() {
        }

        @Override
        protected void addDescriptionCell(OrderElement element) {
            addTaskNumberCell(element);
        }

        private void addTaskNumberCell(final OrderElement orderElementForThisRow) {
            int[] path = getModel().getPath(orderElementForThisRow);
            String cssClass = "depth_" + path.length;

            Label taskNumber = new Label(pathAsString(path));
            taskNumber.setSclass("tasknumber");
            taskNumber.addEventListener(Events.ON_DOUBLE_CLICK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            IOrderElementModel model = orderModel
                                    .getOrderElementModel(orderElementForThisRow);
                            orderElementController.openWindow(model);
                            // Util.reloadBindings(tree);
                        }

                    });

            // TODO It would be needed to expand the width for the numbers
            // to make it ready for 2 and 3 digit numbers
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

            addCell(cssClass, taskNumber, textBox);
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

            if (orderModel.isCodeAutogenerated()) {
                textBoxCode.setDisabled(true);
            }

            addCell(textBoxCode);
        }

        void addInitDateCell(final OrderElement currentOrderElement) {
            addCell(Util.bind(new Datebox(), new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return currentOrderElement.getInitDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    currentOrderElement.setInitDate(value);
                }
            }));
        }

        void addEndDateCell(final OrderElement currentOrderElement) {
            addCell(Util.bind(new Datebox(), new Util.Getter<Date>() {

                @Override
                public Date get() {
                    return currentOrderElement.getDeadline();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    currentOrderElement.setDeadline(value);
                }
            }));
        }

        void addHoursCell(final OrderElement currentOrderElement) {
            Intbox intboxHours = buildHoursIntboxFor(currentOrderElement);
            hoursIntBoxByOrderElement.put(currentOrderElement, intboxHours);
            addCell(intboxHours);
        }

        private Intbox buildHoursIntboxFor(
                final OrderElement currentOrderElement) {
            Intbox result = new Intbox();
            if (currentOrderElement instanceof OrderLine) {
                OrderLine orderLine = (OrderLine) currentOrderElement;
                Util.bind(result, getHoursGetterFor(currentOrderElement),
                        getHoursSetterFor(orderLine));
                result.setConstraint(getHoursConstraintFor(orderLine));
            } else {
                // If it's a container hours cell is not editable
                Util.bind(result, getHoursGetterFor(currentOrderElement));
            }
            return result;
        }

        private Getter<Integer> getHoursGetterFor(
                final OrderElement currentOrderElement) {
            return new Util.Getter<Integer>() {
                @Override
                public Integer get() {
                    return currentOrderElement.getWorkHours();
                }
            };
        }

        private Constraint getHoursConstraintFor(final OrderLine orderLine) {
            return new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (!orderLine.isTotalHoursValid((Integer) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, taking into account the current list of HoursGroup"));
                    }
                }
            };
        }

        private Setter<Integer> getHoursSetterFor(final OrderLine orderLine) {
            return new Util.Setter<Integer>() {
                @Override
                public void set(Integer value) {
                    orderLine.setWorkHours(value);
                    List<OrderElement> parentNodes = getModel().getParents(
                            orderLine);
                    // Remove the last element because it's an
                    // Order node, not an OrderElement
                    parentNodes.remove(parentNodes.size() - 1);
                    for (OrderElement node : parentNodes) {
                        Intbox intbox = hoursIntBoxByOrderElement.get(node);
                        intbox.setValue(node.getWorkHours());
                    }
                }
            };
        }

        @Override
        protected void addOperationsCell(final Treeitem item,
                final OrderElement currentOrderElement) {
            addCell(createEditButton(currentOrderElement),
                    createTemplateButton(currentOrderElement),
                    createUpButton(item,currentOrderElement),
                    createDownButton(item,currentOrderElement),
                    createUnindentButton(item, currentOrderElement),
                    createIndentButton(item, currentOrderElement),
                    createRemoveButton(currentOrderElement));
        }

        private Button createEditButton(final OrderElement currentOrderElement) {
            Button editbutton = createButton("/common/img/ico_editar1.png",
                    _("Edit"), "/common/img/ico_editar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            IOrderElementModel model = orderModel
                                    .getOrderElementModel(currentOrderElement);
                            orderElementController.openWindow(model);
                        }
                    });
            return editbutton;
        }

        private Component createTemplateButton(
                final OrderElement currentOrderElement) {
            Button templateButton = createButton(
                    "/common/img/ico_derived1.png", _("Create Template"),
                    "/common/img/ico_derived.png",
                    "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            createTemplate(currentOrderElement);
                        }
                    });
            return templateButton;
        }

    }

    @Override
    protected boolean isPredicateApplied() {
        return predicate != null;
    }

    /**
     * Apply filter to order elements in current order
     * @param event
     */
    public void onApplyFilter(Event event) {
            org.navalplanner.business.labels.entities.Label label = getSelectedLabel();
            if (label == null) {
            label = org.navalplanner.business.labels.entities.Label.create("");
            }
            // Create predicate and filter order elements by predicate
            predicate = new LabelOrderElementPredicate(label);
            filterByPredicate();
    }

    private org.navalplanner.business.labels.entities.Label getSelectedLabel() {
        return (org.navalplanner.business.labels.entities.Label) bdFilter
                .getSelectedElement();
    }

    @Override
    protected boolean isNewButtonDisabled() {
        return isPredicateApplied();
    }

    /**
     * Clear {@link BandboxSearch} for Labels, and initializes
     * {@link IPredicate}
     */
    public void clear() {
        selectDefaultTab();
        bdFilter.clear();
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
            tooltipText.append(_(" Labels:"));
            tooltipText.append(StringUtils.join(getLabels(), ","));
            tooltipText.append(".");
        }
        if ((elem.getCriterionRequirements() != null)
                && (!elem.getCriterionRequirements().isEmpty())) {
            ArrayList<String> criterionNames = new ArrayList<String>();
            for(CriterionRequirement each:elem.getCriterionRequirements()) {
                criterionNames.add(each.getCriterion().getName());
            }
            tooltipText.append(_(" Criteria:"));
            tooltipText.append(StringUtils.join(criterionNames, ","));
            tooltipText.append(".");
        }
        // To calculate other unit advances implement
        // getOtherAdvancesPercentage()
        tooltipText.append(_(" Advance:") + elem.getAdvancePercentage());
        tooltipText.append(".");

        // tooltipText.append(elem.getAdvancePercentage());
        return tooltipText.toString();
    }

}
