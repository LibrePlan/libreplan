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
package org.libreplan.web.templates.budgettemplates;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.ClassValidator;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.templates.entities.BudgetLineTemplate;
import org.libreplan.business.templates.entities.BudgetLineTypeEnum;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.templates.entities.OrderLineTemplate;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.Util.Getter;
import org.libreplan.web.common.Util.Setter;
import org.libreplan.web.orders.DynamicDatebox;
import org.libreplan.web.tree.EntitiesTree;
import org.libreplan.web.tree.TreeController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

/**
 * Controller for template element tree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeController extends
        TreeController<OrderElementTemplate> {

    private final IBudgetTemplatesModel model;

    private final IEditionSubwindowController orderTemplatesController;

    private TemplateElementOperations operationsForOrderTemplate;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        operationsForOrderTemplate.tree(tree);
    }

    final class TemplatesTreeRenderer extends Renderer {

        private final ClassValidator<OrderElementTemplate> validator = new ClassValidator<OrderElementTemplate>(
                OrderElementTemplate.class);

        private Map<OrderElementTemplate, Decimalbox> indemnizationSalaryDecimalboxByElement = new HashMap<OrderElementTemplate, Decimalbox>();

        private Map<OrderElementTemplate, Decimalbox> holidaySalaryDecimalboxByElement = new HashMap<OrderElementTemplate, Decimalbox>();

        @Override
        /**
         * We override parent method to remove the scheduling state
         * toggler from the cell.
         */
        public void addSchedulingStateCell(
                final OrderElementTemplate currentElement) {
            final Treecell cell = addCell();
            cell.addEventListener("onDoubleClick", new EventListener() {
                @Override
                public void onEvent(Event event) {

                    markModifiedTreeitem((Treerow) cell.getParent());
                    onDoubleClickForSchedulingStateCell(currentElement);
                }
            });
            cell.addEventListener(Events.ON_CLICK, new EventListener() {

                private Treeitem item = (Treeitem) getCurrentTreeRow()
                        .getParent();

                @Override
                public void onEvent(Event event) {
                    item.getTree().toggleItemSelection(item);
                }
            });
        }

        @Override
        protected void addOperationsCell(Treeitem item,
                OrderElementTemplate currentElement) {
            addCell(createEditButton(currentElement),
                    createRemoveButton(currentElement));
        }

        private Button createEditButton(
                final OrderElementTemplate currentTemplate) {
            Button result = createButton("/common/img/ico_editar1.png",
                    _("Edit"), "/common/img/ico_editar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) {
                            Treeitem item = getTreeitem(event.getTarget());
                            operationsForOrderTemplate.showEditElement(item);
                        }
                    });
            return result;
        }

        private Treeitem getTreeitem(Component comp) {
            return (Treeitem) comp.getParent().getParent().getParent();
        }

        @Override
        protected void addDescriptionCell(final OrderElementTemplate element) {
            Textbox textBox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return element.getName();
                        }
                    }, new Util.Setter<String>() {

                        @Override
                        public void set(String value) {
                            element.setName(value);
                        }
                    });

            if (readOnly) {
                textBox.setDisabled(true);
            }
            addCell(textBox);
            putNameTextbox(element, textBox);
        }

        @Override
        protected void addCodeCell(final OrderElementTemplate element) {
            Textbox textBoxCode = new Textbox();
            Util.bind(textBoxCode, new Util.Getter<String>() {
                @Override
                public String get() {
                    return element.getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    element.setCode(value);
                    model.notifyUpdate(element);
                }
            });
            textBoxCode.setConstraint(new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if(value == null || value.equals("")) {
                        throw new WrongValueException(
                                comp,
                                _("Code should not be empty"));
                    }
                    if (!model.checkValidCode(element, (String) value)) {
                        throw new WrongValueException(
                                comp,
                                _("Code should be unique inside this template"));
                    }
                }
            });
            if (readOnly) {
                textBoxCode.setDisabled(true);
            }
            addCell(textBoxCode);
            if (textBoxCode.isValid()) {
                model.notifyUpdate(element);
            }
        }

        @Override
        public void addBudgetCell(final OrderElementTemplate currentElement) {
            //create cell normally
            super.addBudgetCell(currentElement);
            //disable cell because its value cannot be changed directly
            Decimalbox box = budgetDecimalboxByElement.get(currentElement);
            box.setClass("budgetline-total");
            box.setDisabled(true);
        }

        @Override
        protected void onDoubleClickForSchedulingStateCell(
                OrderElementTemplate currentElement) {
            // do nothing
        }

        @Override
        protected SchedulingState getSchedulingStateFrom(
                OrderElementTemplate currentElement) {
            return currentElement.getSchedulingState();
        }

        private Decimalbox addDecimalboxFor(OrderElementTemplate element,
                Getter<BigDecimal> getter, Setter<BigDecimal> setter,
                Constraint constraint) {
            Decimalbox result = new DecimalboxDirectValue();
            Util.bind(result, getter, setter);
            result.setConstraint(constraint);
            if (readOnly) {
                result.setDisabled(true);
            }
            addCell(result);
            return result;
        }

        private void addIntboxFor(OrderElementTemplate element,
                Getter<Integer> getter, Setter<Integer> setter,
                Constraint constraint) {
            Intbox result = new Intbox();
            Util.bind(result, getter, setter);
            result.setConstraint(constraint);
            if (readOnly) {
                result.setDisabled(true);
            }
            addCell(result);
        }

        private void addEmptyBox() {
            Intbox intbox = new Intbox(0);
            intbox.setDisabled(true);
            addCell(intbox);
        }

        private void updateTotal(BudgetLineTemplate currentElement) {
            BigDecimal quantity = new BigDecimal(currentElement.getQuantity());
            BigDecimal duration = new BigDecimal(currentElement.getDuration());
            //budget field is used to store the total
            Decimalbox budgetBox = budgetDecimalboxByElement.get(currentElement);
            // Calculate Total as cost * quantiy * duration
            BigDecimal total = currentElement.getCostOrSalary()
                    .multiply(quantity).multiply(duration);
            // Added holidaySalary and indemnizationSalary
            total = total.add(currentElement.getHolidaySalary()).add(
                    currentElement.getIndemnizationSalary());
            budgetBox.setValue(total);
            //fire change event, to update the total in the parents
            Events.sendEvent(budgetBox, new Event(Events.ON_CHANGE));
        }

        @Override
        protected void addCostSalaryCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                addDecimalboxFor(element, new Util.Getter<BigDecimal>() {

                    @Override
                    public BigDecimal get() {
                        return budgetLine.getCostOrSalary();
                    }
                }, new Util.Setter<BigDecimal>() {

                    @Override
                    public void set(BigDecimal value) {
                        budgetLine.setCostOrSalary(value);
                        updateTotal(budgetLine);
                    }
                },
                null);
            }
            else {
                addEmptyBox();
            }
        }

        @Override
        protected void addDurationCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                addIntboxFor(element, new Util.Getter<Integer>() {

                    @Override
                    public Integer get() {
                        return budgetLine.getDuration();
                    }
                }, new Util.Setter<Integer>() {

                    @Override
                    public void set(Integer value) {
                        budgetLine.setDuration(value);
                        updateTotal(budgetLine);
                    }
                },
                null);
            }
            else {
                addEmptyBox();
            }
        }

        @Override
        protected void addQuantityCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                addIntboxFor(element, new Util.Getter<Integer>() {

                    @Override
                    public Integer get() {
                        return budgetLine.getQuantity();
                    }
                }, new Util.Setter<Integer>() {

                    @Override
                    public void set(Integer value) {
                        budgetLine.setQuantity(value);
                        updateTotal(budgetLine);
                    }
                },
                null);
            }
            else {
                addEmptyBox();
            }
        }

        @Override
        protected void addIndemnizationSalaryCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                Decimalbox decimalbox = addDecimalboxFor(element,
                        new Util.Getter<BigDecimal>() {

                    @Override
                    public BigDecimal get() {
                        return budgetLine.getIndemnizationSalary();
                    }
                }, new Util.Setter<BigDecimal>() {

                    @Override
                    public void set(BigDecimal value) {
                        budgetLine.setIndemnizationSalary(value);
                        updateTotal(budgetLine);
                    }
                },
                null);
                if (!readOnly) {
                    decimalbox.setDisabled(!budgetLine.getBudgetLineType()
                            .isRelatedToSalary());
                }
                indemnizationSalaryDecimalboxByElement.put(element, decimalbox);
            }
            else {
                addEmptyBox();
            }
        }

        @Override
        protected void addHolidaySalaryCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                Decimalbox decimalbox = addDecimalboxFor(element,
                        new Util.Getter<BigDecimal>() {

                    @Override
                    public BigDecimal get() {
                        return budgetLine.getHolidaySalary();
                    }
                }, new Util.Setter<BigDecimal>() {

                    @Override
                    public void set(BigDecimal value) {
                        budgetLine.setHolidaySalary(value);
                        updateTotal(budgetLine);
                    }
                },
                null);
                if (!readOnly) {
                    decimalbox.setDisabled(!budgetLine.getBudgetLineType()
                            .isRelatedToSalary());
                }
                holidaySalaryDecimalboxByElement.put(element, decimalbox);
            }
            else {
                addEmptyBox();
            }
        }

        @Override
        public void addBudgetLineTypeCell(final OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                Combobox box = new Combobox();
                for(BudgetLineTypeEnum type: BudgetLineTypeEnum.values()) {
                    Comboitem item = new Comboitem(type.toString());
                    item.setValue(type);
                    box.appendChild(item);
                    if(type.equals(budgetLine.getBudgetLineType())) {
                        box.setSelectedItem(item);
                    }
                }
                box.addEventListener(Events.ON_SELECT, new EventListener() {

                    @Override
                    public void onEvent(Event event) {
                        Combobox box = (Combobox) event.getTarget();
                        if (box.getSelectedItem() == null
                                || box.getSelectedItem().getValue() == null) {
                            throw new WrongValueException(box,
                                    _("please select a type"));
                        }

                        BudgetLineTypeEnum type = (BudgetLineTypeEnum)
                                box.getSelectedItem().getValue();
                        budgetLine.setBudgetLineType(type);

                        Decimalbox indemnizationSalaryDecimalbox = indemnizationSalaryDecimalboxByElement.get(element);
                        indemnizationSalaryDecimalbox.setDisabled(!type
                                .isRelatedToSalary());
                        Decimalbox holidaySalaryDecimalbox = holidaySalaryDecimalboxByElement
                                .get(element);
                        holidaySalaryDecimalbox
                                .setDisabled(!type.isRelatedToSalary());

                        if (!type.isRelatedToSalary()) {
                            BigDecimal zero = BigDecimal.ZERO.setScale(2);
                            budgetLine.setHolidaySalary(zero);
                            indemnizationSalaryDecimalbox.setValue(zero);
                            budgetLine.setIndemnizationSalary(zero);
                            holidaySalaryDecimalbox.setValue(zero);
                            updateTotal(budgetLine);
                        }
                    }
                });
                if (readOnly) {
                    box.setDisabled(true);
                }
                addCell(box);
            }
            else {
                addEmptyBox();
            }
        }

        void addStartDateCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                DynamicDatebox dinamicDatebox = new DynamicDatebox(
                        new DynamicDatebox.Getter<Date>() {

                            @Override
                            public Date get() {
                                if (budgetLine.getStartDate() != null) {
                                    return budgetLine.getStartDate()
                                            .toDateMidnight().toDate();
                                }
                                return null;
                            }
                        }, new DynamicDatebox.Setter<Date>() {

                            @Override
                            public void set(Date value) {
                                budgetLine.setStartDate(LocalDate
                                        .fromDateFields(value));

                            }
                        });
                if (readOnly) {
                    dinamicDatebox.setDisabled(true);
                }
                addDateCell(dinamicDatebox, _("init"));
                putInitDateDynamicDatebox(budgetLine, dinamicDatebox);
            } else {
                addEmptyBox();
            }
        }

        void addEndDateCell(OrderElementTemplate element) {
            if (element.isLeaf()) {
                final BudgetLineTemplate budgetLine = (BudgetLineTemplate) element;
                DynamicDatebox dinamicDatebox = new DynamicDatebox(
                        new DynamicDatebox.Getter<Date>() {

                            @Override
                            public Date get() {
                                if (budgetLine.getEndDate() != null) {
                                    return budgetLine.getEndDate()
                                            .toDateMidnight().toDate();
                                }
                                return null;
                            }
                        }, new DynamicDatebox.Setter<Date>() {

                            @Override
                            public void set(Date value) {
                                budgetLine.setEndDate(LocalDate
                                        .fromDateFields(value));

                            }
                        });
                if (readOnly) {
                    dinamicDatebox.setDisabled(true);
                }
                addDateCell(dinamicDatebox, _("init"));
                putInitDateDynamicDatebox(budgetLine, dinamicDatebox);
            } else {
                addEmptyBox();
            }
        }

    }

    public TemplatesTreeController(IBudgetTemplatesModel model,
            IEditionSubwindowController orderTemplatesController) {
        super(OrderElementTemplate.class);
        this.model = model;
        this.orderTemplatesController = orderTemplatesController;
        initializeOperationsForOrderTemplate();
    }

    /**
     * Initializes operationsForOrderTemplate. A reference to variable tree is
     * needed to be added later in doAfterCompose()
     */
    private void initializeOperationsForOrderTemplate() {
        operationsForOrderTemplate = TemplateElementOperations.build()
            .treeController(this)
            .setIEditionSubwindowController(this.orderTemplatesController);
    }

    @Override
    protected void filterByPredicateIfAny() {
    }

    @Override
    protected EntitiesTree<OrderElementTemplate> getModel() {
        return model.getTemplatesTreeModel();
    }

    @Override
    public TemplatesTreeRenderer getRenderer() {
        return new TemplatesTreeRenderer();
    }

    @Override
    protected boolean isNewButtonDisabled() {
        return readOnly;
    }

    @Override
    protected boolean isPredicateApplied() {
        return false;
    }

    @Override
    protected String createTooltipText(OrderElementTemplate elem) {
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
        // There are no CriterionRequirement or advances in templates
            return tooltipText.toString();
        }

    @Override
    protected IHoursGroupHandler<OrderElementTemplate> getHoursGroupHandler() {
        return new IHoursGroupHandler<OrderElementTemplate>() {

            @Override
            public boolean hasMoreThanOneHoursGroup(OrderElementTemplate element) {
                return element.getHoursGroups().size() > 1;
            }

            @Override
            public boolean isTotalHoursValid(OrderElementTemplate line,
                    Integer value) {
                return ((OrderLineTemplate) line).isTotalHoursValid(value);
            }

            @Override
            public Integer getWorkHoursFor(OrderElementTemplate element) {
                return element.getWorkHours();
            }

            @Override
            public void setWorkHours(OrderElementTemplate element, Integer value) {
                if (element instanceof OrderLineTemplate) {
                    OrderLineTemplate line = (OrderLineTemplate) element;
                    line.setWorkHours(value);
                }
            }
        };
    }

    @Override
    protected IBudgetHandler<OrderElementTemplate> getBudgetHandler() {
        return new IBudgetHandler<OrderElementTemplate>() {

            @Override
            public BigDecimal getBudgetFor(OrderElementTemplate element) {
                return element.getBudget();
            }

            @Override
            public void setBudgetHours(OrderElementTemplate element,
                    BigDecimal budget) {
                if (element instanceof OrderLineTemplate) {
                    OrderLineTemplate line = (OrderLineTemplate) element;
                    line.setBudget(budget);
                }
            }

        };
    }

    public void refreshRow(Treeitem item) {
        try {
            OrderElementTemplate orderElement = (OrderElementTemplate) item
                    .getValue();
            // getRenderer().updateHoursFor(orderElement);
            getRenderer().updateColumnsFor(orderElement);
            getRenderer().render(item, orderElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Operations for a node
     */

    public void editSelectedElement() {
        operationsForOrderTemplate.editSelectedElement();
    }

    public void moveSelectedElementDown() {
        operationsForOrderTemplate.moveSelectedElementDown();
    }

    public void moveSelectedElementUp() {
        operationsForOrderTemplate.moveSelectedElementUp();
    }

    public void unindentSelectedElement() {
        operationsForOrderTemplate.unindentSelectedElement();
    }

    public void indentSelectedElement() {
        operationsForOrderTemplate.indentSelectedElement();
    }

    public void deleteSelectedElement() {
        operationsForOrderTemplate.deleteSelectedElement();
    }



    public void expandAll() {
        Set<Treeitem> childrenSet = new HashSet<Treeitem>();

        Treechildren children = tree.getTreechildren();
        if (children != null) {
            childrenSet.addAll((Collection<Treeitem>) children.getItems());
        }
        for (Treeitem each : childrenSet) {
            this.expandAll(each);
        }
    }

    private void expandAll(Treeitem item) {
        item.setOpen(true);

        Set<Treeitem> childrenSet = new HashSet<Treeitem>();
        Treechildren children = item.getTreechildren();
        if (children != null) {
            childrenSet.addAll((Collection<Treeitem>) children.getItems());
        }

        for (Treeitem each : childrenSet) {
            expandAll(each);
        }
    }

    private void collapseAll() {
        Treechildren children = tree.getTreechildren();
        for(Treeitem each: (Collection<Treeitem>) children.getItems()) {
            each.setOpen(false);
        }
    }

    @Override
    protected INameHandler<OrderElementTemplate> getNameHandler() {
        return new INameHandler<OrderElementTemplate>() {

            @Override
            public String getNameFor(OrderElementTemplate element) {
                return element.getName();
            }

        };
    }

    @Override
    protected ICodeHandler<OrderElementTemplate> getCodeHandler() {
        return new ICodeHandler<OrderElementTemplate>() {

            @Override
            public String getCodeFor(OrderElementTemplate element) {
                // Empty as OrderElementTemplate doesn't have code
                return "";
            }

        };
    }

}