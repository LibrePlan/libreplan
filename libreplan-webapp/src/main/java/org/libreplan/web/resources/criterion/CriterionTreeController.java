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

package org.libreplan.web.resources.criterion;

import static org.libreplan.web.I18nHelper._;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.common.exceptions.ValidationException.InvalidValue;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Tree;

public class CriterionTreeController extends GenericForwardComposer {

    private Tree tree;

    private CriterionTreeitemRenderer renderer = new CriterionTreeitemRenderer();

    private TreeViewStateSnapshot snapshotOfOpenedNodes;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private final ICriterionsModel criterionsModel;

    private Textbox criterionName;

    private boolean codeEditionDisabled;

    public CriterionTreeitemRenderer getRenderer() {
        return renderer;
    }

    public CriterionTreeController(ICriterionsModel _criterionsModel) {
        Validate.notNull(_criterionsModel);
        this.criterionsModel = _criterionsModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setAttribute("criterionTreeController", this, true);
        criterionName = (Textbox) self.getFellowIfAny("criterionName");
        clearCriterionName();
    }

    private void clearCriterionName() {
        criterionName.setValue("");
    }

    public TreeModel getCriterionTreeModel() {
        if (getModel() == null) {
            return null;
        }

        return getModel().asTree();
    }

    private ICriterionTreeModel getModel() {
        return criterionsModel.getCriterionTreeModel();
    }

    public class CriterionTreeitemRenderer implements TreeitemRenderer {

        public CriterionTreeitemRenderer() {
        }

        @Override
        public void render(Treeitem item, Object data, int i) {
            final CriterionDTO criterionForThisRow = (CriterionDTO) data;
            item.setValue(data);

            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }

            Treecell cellForName = new Treecell();
            Textbox textboxName= new Textbox();
            textboxName.setWidth("400px");

            cellForName.appendChild(Util.bind(
                    textboxName,
                    () -> {
                        return criterionForThisRow.getName();
                    }, value -> {
                        criterionForThisRow.setName(value);
                    }));

            String message = _("cannot be empty");
            textboxName.setConstraint("no empty:"+message);

            Treecell cellForActive = new Treecell();
            cellForActive.setStyle("center");
            Checkbox checkboxActive = new Checkbox();

            cellForActive.appendChild(Util.bind(
                    checkboxActive,
                    () -> {
                        return criterionForThisRow.isActive();
                    }, value -> {
                        criterionForThisRow.setActive(value);
                    }));

            checkboxActive.addEventListener(Events.ON_CHECK, event -> {
                getModel().updateEnabledCriterions(criterionForThisRow.isActive(),criterionForThisRow);
                reloadTree();
            });

            Treerow tr;
            /*
             * Since only one treerow is allowed, if treerow is not null, append treecells to it.
             * If treerow is null, contruct a new treerow and attach it to item.
             */
            if (item.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(item);
            } else {
                tr = item.getTreerow();
                tr.getChildren().clear();
            }
            // Attach treecells to treerow
            tr.setDraggable("true");
            tr.setDroppable("true");

            // Treecell with the cost category of the Criterion
            Treecell cellForCostCategory = new Treecell();
            criterionForThisRow.getCriterion().getCostCategory();
            cellForCostCategory.appendChild(appendAutocompleteType(item));

            // Treecell with the code of the Criterion
            Treecell cellForCode = new Treecell();
            cellForCode.setStyle("center");
            Textbox codeLabel = new Textbox();
            codeLabel.setDisabled(codeEditionDisabled);

            cellForCode.appendChild(Util.bind(
                    codeLabel,
                    () -> {
                        return criterionForThisRow.getCriterion().getCode();
                    }, value -> {
                        criterionForThisRow.getCriterion().setCode(value);
                    }));

            cellForName.setParent(tr);
            cellForCostCategory.setParent(tr);
            cellForCode.setParent(tr);
            cellForActive.setParent(tr);

            Treecell tcOperations = new Treecell();
            Button upButton = new Button("", "/common/img/ico_bajar1.png");
            upButton.setHoverImage("/common/img/ico_bajar.png");
            upButton.setParent(tcOperations);
            upButton.setSclass("icono");

            upButton.addEventListener(Events.ON_CLICK, event -> {
                getModel().down(criterionForThisRow);
                reloadTree();
            });

            Button downButton = new Button("", "/common/img/ico_subir1.png");
            downButton.setHoverImage("/common/img/ico_subir.png");
            downButton.setParent(tcOperations);
            downButton.setSclass("icono");

            downButton.addEventListener(Events.ON_CLICK, event -> {
                getModel().up(criterionForThisRow);
                reloadTree();
            });

            Button indentButton = createButtonIndent();
            indentButton.setParent(tcOperations);

            if (getModel().getCriterionType().allowHierarchy()) {
                indentButton.addEventListener(Events.ON_CLICK, event -> {
                    getModel().indent(criterionForThisRow);
                    reloadTree();
                });
            }

            Button unindentButton = createButtonUnindent();
            unindentButton.setParent(tcOperations);

            if (getModel().getCriterionType().allowHierarchy()) {
                unindentButton.addEventListener(Events.ON_CLICK, event -> {
                    getModel().unindent(criterionForThisRow);
                    reloadTree();
                });
            }

            Button removeButton = createButtonRemove(criterionForThisRow);
            removeButton.setParent(tcOperations);
            if (criterionsModel.isDeletable(criterionForThisRow.getCriterion())) {

                removeButton.addEventListener(Events.ON_CLICK,  event -> {
                    getModel().removeNode(criterionForThisRow);
                    if (!criterionForThisRow.isNewObject()) {
                        criterionsModel.addForRemoval(criterionForThisRow.getCriterion());
                    }
                    reloadTree();
                });
            }

            tcOperations.setParent(tr);
            tr.addEventListener("onDrop", arg0 -> {
                DropEvent dropEvent = (DropEvent) arg0;
                move(dropEvent.getTarget(), dropEvent.getDragged());
            });
        }
    }

    private CostCategory getCostCategory(Treeitem listitem) {
        return ((CriterionDTO) listitem.getValue()).getCriterion().getCostCategory();
    }

    private Autocomplete appendAutocompleteType(final Treeitem row) {
        final Autocomplete autocomplete = new Autocomplete();
        autocomplete.setAutodrop(true);
        autocomplete.applyProperties();
        autocomplete.setFinder("CostCategoryFinder");

        // Getter, show type selected
        if (getCostCategory(row) != null) {
            autocomplete.setSelectedItem(getCostCategory(row));
        }

        // Setter, set type selected to HourCost.type
        autocomplete.addEventListener("onSelect", event -> {
            final Comboitem comboitem = autocomplete.getSelectedItem();

            if (comboitem != null) {
                // Update resourcesCostCategoryAssignment
                CriterionDTO assignment = row.getValue();
                assignment.getCriterion().setCostCategory(comboitem.getValue());
                row.setValue(assignment);
            }
        });

        autocomplete.addEventListener("onBlur", event -> {
            if (autocomplete.getText().isEmpty()) {
                autocomplete.clear();
                CriterionDTO assignment = row.getValue();
                assignment.getCriterion().setCostCategory(null);
            }
        });

        return autocomplete;
    }

    private Button createButtonUnindent() {
        Button unindentButton;
        if ( this.criterionsModel.getCriterionType().allowHierarchy()) {
            unindentButton = new Button("", "/common/img/ico_izq1.png");
            unindentButton.setHoverImage("/common/img/ico_izq.png");
            unindentButton.setTooltiptext(_("Unindent"));
        } else {
            unindentButton = new Button("", "/common/img/ico_izq_out.png");
            unindentButton.setTooltiptext(_("Not indentable"));
        }
        unindentButton.setSclass("icono");

        return unindentButton;
    }

    private Button createButtonIndent() {
        Button indentButton;
        if ( this.criterionsModel.getCriterionType().allowHierarchy()) {
            indentButton = new Button("", "/common/img/ico_derecha1.png");
            indentButton.setHoverImage("/common/img/ico_derecha.png");
            indentButton.setTooltiptext(_("Indent"));
        } else {
            indentButton = new Button("", "/common/img/ico_derecha_out.png");
            indentButton.setTooltiptext(_("Not indentable"));
        }
        indentButton.setSclass("icono");

        return indentButton;
    }

    private Button createButtonRemove(CriterionDTO criterion) {
        Button removeButton;

        int num = criterionsModel.numberOfRelatedEntities(criterion.getCriterion());

        if (criterionsModel.isDeletable(criterion.getCriterion())) {
            removeButton = new Button("", "/common/img/ico_borrar1.png");
            removeButton.setHoverImage("/common/img/ico_borrar.png");
            removeButton.setTooltiptext(_("Delete"));
        } else {
            removeButton = new Button("", "/common/img/ico_borrar_out.png");

            removeButton.setTooltiptext(criterion.getCriterion().getChildren().isEmpty()
                    ? (num + " " + _("references"))
                    : _("Criterion has subelements"));
        }

        removeButton.setSclass("icono");

        return removeButton;
    }

    public void up() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getModel().up(getSelectedNode());
        }
    }

    public void down() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        if (tree.getSelectedCount() == 1) {
            getModel().down(getSelectedNode());
        }
    }

    public void move(Component droppedIn, Component dragged) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        Treerow from = (Treerow) dragged;
        CriterionDTO fromNode = ((Treeitem) from.getParent()).getValue();
        if ( droppedIn instanceof Tree) {
            getModel().moveToRoot(fromNode,0);
        }
        if ( droppedIn instanceof Treerow) {
            Treerow to = (Treerow) droppedIn;
            CriterionDTO toNode = ((Treeitem) to.getParent()).getValue();
            getModel().move(fromNode, toNode,0);
        }

        reloadTree();
    }

    public void addCriterion() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        try {
            if ((tree.getSelectedCount() == 1) && (this.criterionsModel.getAllowHierarchy())) {
                getModel().addCriterionAt(getSelectedNode(),getName());
            } else {
                getModel().addCriterion(getName());
            }
            clearCriterionName();
            reloadTree();
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messagesForUser.showMessage(Level.ERROR, invalidValue.getMessage());
            }
        }
    }

    public void disabledHierarchy() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().flattenTree();
        reloadTree();
    }

    public void updateEnabledCriterions(boolean isChecked) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().updateEnabledCriterions(isChecked);
        reloadTree();
    }

    public void setCriterionCodeEditionDisabled(boolean disabled) {
        codeEditionDisabled = disabled;
    }

    public void regenerateCodeForUnsavedCriteria(int numberOfDigits) {
        getModel().regenerateCodeForUnsavedCriteria(numberOfDigits);
    }

    public void reloadTree(){
        Util.reloadBindings(tree);
    }

    private String getName() {
        String name = criterionName.getValue();
        getModel().thereIsOtherWithSameNameAndType(name);
        getModel().validateNameNotEmpty(name);
        return name;
    }

    private CriterionDTO getSelectedNode() {
        return (CriterionDTO) tree.getSelectedItem().getValue();
    }

    private static class TreeViewStateSnapshot {

        private final Set<Object> all;

        private final Set<Object> dataOpen;

        private TreeViewStateSnapshot(Set<Object> dataOpen, Set<Object> all) {
            this.dataOpen = dataOpen;
            this.all = all;
        }

        public static TreeViewStateSnapshot snapshotOpened(Tree tree) {
            Set<Object> dataOpen = new HashSet<>();
            Set<Object> all = new HashSet<>();
            if (tree != null && tree.getTreechildren() != null) {

                for (Treeitem treeitem : tree.getTreechildren().getItems()) {
                    Object value = getAssociatedValue(treeitem);

                    if (treeitem.isOpen()) {
                        dataOpen.add(value);
                    }

                    all.add(value);
                }
            }

            return new TreeViewStateSnapshot(dataOpen, all);
        }

        private static Object getAssociatedValue(Treeitem treeitem) {
            return treeitem.getValue();
        }

        public void openIfRequired(Treeitem item) {
            Object value = getAssociatedValue(item);
            item.setOpen(isNewlyCreated(value) || wasOpened(value));
        }

        private boolean wasOpened(Object value) {
            return dataOpen.contains(value);
        }

        private boolean isNewlyCreated(Object value) {
            return !all.contains(value);
        }
    }
}
