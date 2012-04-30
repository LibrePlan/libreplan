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
package org.libreplan.web.templates;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.ClassValidator;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.templates.entities.OrderLineTemplate;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.Util.Getter;
import org.libreplan.web.common.Util.Setter;
import org.libreplan.web.tree.EntitiesTree;
import org.libreplan.web.tree.TreeController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treeitem;

/**
 * Controller for template element tree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeController extends
        TreeController<OrderElementTemplate> {

    private final IOrderTemplatesModel model;

    private final OrderTemplatesController orderTemplatesController;

    private TemplateElementOperations operationsForOrderTemplate;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        operationsForOrderTemplate.tree(tree);
    }

    final class TemplatesTreeRenderer extends Renderer {

        private final ClassValidator<OrderElementTemplate> validator = new ClassValidator<OrderElementTemplate>(
                OrderElementTemplate.class);

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
            textBox.setConstraint("no empty:" + _("cannot be null or empty"));
            addCell(textBox);
            putNameTextbox(element, textBox);
        }

        @Override
        protected void addCodeCell(final OrderElementTemplate element) {
            //empty because templates don't have code attribute
        }

        void addInitCell(final OrderElementTemplate currentElement) {
            final Intbox intbox = new Intbox();
            Util.bind(intbox, new Getter<Integer>() {

                @Override
                public Integer get() {
                    return currentElement.getStartAsDaysFromBeginning();
                }
            }, new Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    checkInvalidValues(validator, "startAsDaysFromBeginning",
                            value, intbox);
                    currentElement.setStartAsDaysFromBeginning(value);
                }
            });
            addCell(intbox);
        }

        void addEndCell(final OrderElementTemplate currentElement) {
            final Intbox intbox = new Intbox();
            Util.bind(intbox, new Getter<Integer>() {

                @Override
                public Integer get() {
                    return currentElement.getDeadlineAsDaysFromBeginning();
                }
            }, new Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    checkInvalidValues(validator,
                            "deadlineAsDaysFromBeginning", value, intbox);
                    currentElement.setDeadlineAsDaysFromBeginning(value);
                }
            });
            addCell(intbox);
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

        @Override
        protected void addCostSalaryCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

        @Override
        protected void addDurationCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

        @Override
        protected void addQuantityCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

        @Override
        protected void addIndemnizationSalaryCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

        @Override
        protected void addHolidaySalaryCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

        @Override
        public void addBudgetLineTypeCell(OrderElementTemplate element) {
            // added to prevent compilation errors, but it will be never used
        }

    }

    public TemplatesTreeController(IOrderTemplatesModel model,
            OrderTemplatesController orderTemplatesController) {
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
            .orderTemplatesController(this.orderTemplatesController);
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
        return false;
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
            getRenderer().updateHoursFor(orderElement);
            getRenderer().updateBudgetFor(orderElement);
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

}