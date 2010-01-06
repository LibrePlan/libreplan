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
package org.navalplanner.web.templates;

import org.hibernate.validator.ClassValidator;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
import org.navalplanner.web.tree.EntitiesTree;
import org.navalplanner.web.tree.TreeController;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

/**
 * Controller for template element tree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeController extends
        TreeController<OrderElementTemplate> {

    final class TemplatesTreeRenderer extends Renderer {

        private final ClassValidator<OrderElementTemplate> validator = new ClassValidator<OrderElementTemplate>(
                OrderElementTemplate.class);

        void addFirstCell(OrderElementTemplate currentElement) {
            int[] path = getModel().getPath(currentElement);
            String cssClass = "depth_" + path.length;
            Label label = new Label();
            label.setValue("");
            label.setSclass(cssClass);
            addCell(label);
        }

        @Override
        protected void addOperationsCell(Treeitem item,
                OrderElementTemplate currentElement) {
            addCell(createUpButton(item, currentElement),
                    createDownButton(item, currentElement),
                    createUnindentButton(item, currentElement),
                    createIndentButton(item, currentElement),
                    createRemoveButton(currentElement));
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

            addCell(textBox);
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
                }
            });
            addCell(textBoxCode);
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

    }

    private final IOrderTemplatesModel model;

    public TemplatesTreeController(IOrderTemplatesModel model) {
        super(OrderElementTemplate.class);
        this.model = model;
    }

    @Override
    protected void filterByPredicateIfAny() {
    }

    @Override
    protected EntitiesTree<OrderElementTemplate> getModel() {
        return model.getTemplatesTreeModel();
    }

    @Override
    public TreeitemRenderer getRenderer() {
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

}
