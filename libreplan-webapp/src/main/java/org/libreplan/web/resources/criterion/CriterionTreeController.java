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
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.api.Tree;

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
        comp.setVariable("criterionTreeController", this, true);
        criterionName = (Textbox) ((Vbox) self).getFellowIfAny("criterionName");
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
        public void render(Treeitem item, Object data) {
            final CriterionDTO criterionForThisRow = (CriterionDTO) data;
            item.setValue(data);

            if (snapshotOfOpenedNodes != null) {
                snapshotOfOpenedNodes.openIfRequired(item);
            }

            Treecell cellForName = new Treecell();
            Textbox textboxName= new Textbox();
            textboxName.setWidth("400px");
            cellForName.appendChild(Util.bind(textboxName,
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return criterionForThisRow.getName();
                        }
                    }, new Util.Setter<String>() {

                        @Override
                        public void set(String value) {
                            criterionForThisRow.setName(value);
                        }
                    }));
            String message = _("cannot be empty");
            textboxName
                    .setConstraint("no empty:"+message);

            Treecell cellForActive = new Treecell();
            cellForActive.setStyle("center");
            Checkbox checkboxActive = new Checkbox();
            cellForActive.appendChild(Util.bind(checkboxActive,
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return criterionForThisRow.isActive();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            criterionForThisRow.setActive(value);
                        }
                    }));

            checkboxActive.addEventListener(Events.ON_CHECK,new EventListener() {
                @Override
                        public void onEvent(Event event) {
                    getModel().updateEnabledCriterions(criterionForThisRow.isActive(),criterionForThisRow);
                    reloadTree();
                }
            });

            Treerow tr = null;
            /*
             * Since only one treerow is allowed, if treerow is not null, append
             * treecells to it. If treerow is null, contruct a new treerow and
             * attach it to item.
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

            // Treecell with the code of the Criterion
            Treecell cellForCode = new Treecell();
            cellForCode.setStyle("center");
            Textbox codeLabel = new Textbox();
            codeLabel.setDisabled(codeEditionDisabled);
            cellForCode.appendChild(Util.bind(codeLabel,
                    new Util.Getter<String>() {

                @Override
                public String get() {
                    return criterionForThisRow.getCriterion().getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    criterionForThisRow.getCriterion().setCode(value);
                }
            }));

            cellForName.setParent(tr);
            cellForCode.setParent(tr);
            cellForActive.setParent(tr);

            Treecell tcOperations = new Treecell();
            Button upbutton = new Button("", "/common/img/ico_bajar1.png");
            upbutton.setHoverImage("/common/img/ico_bajar.png");
            upbutton.setParent(tcOperations);
            upbutton.setSclass("icono");
            upbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    getModel().down(criterionForThisRow);
                    reloadTree();
                }
            });

            Button downbutton = new Button("", "/common/img/ico_subir1.png");
            downbutton.setHoverImage("/common/img/ico_subir.png");
            downbutton.setParent(tcOperations);
            downbutton.setSclass("icono");
            downbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    getModel().up(criterionForThisRow);
                    reloadTree();
                }
            });

            Button indentbutton = createButtonIndent();
            indentbutton.setParent(tcOperations);
            if(getModel().getCriterionType().allowHierarchy()){
                    indentbutton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                            public void onEvent(Event event) {
                    getModel().indent(criterionForThisRow);
                    reloadTree();
                }
                });
            }

            Button unindentbutton = createButtonUnindent();
            unindentbutton.setParent(tcOperations);
            if(getModel().getCriterionType().allowHierarchy()){
                unindentbutton.addEventListener(Events.ON_CLICK,
                    new EventListener() {
                        @Override
                            public void onEvent(Event event) {
                            getModel().unindent(criterionForThisRow);
                            reloadTree();
                        }
                    });
            }

            Button removebutton = createButtonRemove(criterionForThisRow);
            removebutton.setParent(tcOperations);
            if (criterionsModel.isDeletable(criterionForThisRow.getCriterion())) {
            removebutton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
                            public void onEvent(Event event) {
                getModel().removeNode(criterionForThisRow);
                                if (!criterionForThisRow.isNewObject()) {
                                    criterionsModel
                                            .addForRemoval(criterionForThisRow
                                                    .getCriterion());
                                }
                reloadTree();
                }
            });
        }

            tcOperations.setParent(tr);
            tr.addEventListener("onDrop", new EventListener() {

                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event arg0)
 {
                    DropEvent dropEvent = (DropEvent) arg0;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged());
                }
            });
        }
    }

    private Button createButtonUnindent(){
        Button unindentbutton;
        if( this.criterionsModel.getCriterionType().allowHierarchy()){
            unindentbutton = new Button("", "/common/img/ico_izq1.png");
            unindentbutton.setHoverImage("/common/img/ico_izq.png");
            unindentbutton.setTooltiptext(_("Unindent"));
        }else{
            unindentbutton = new Button("", "/common/img/ico_izq_out.png");
            unindentbutton.setTooltiptext(_("Not indentable"));
        }
        unindentbutton.setSclass("icono");
        return unindentbutton;
    }

    private Button createButtonIndent(){
        Button indentbutton;
        if( this.criterionsModel.getCriterionType().allowHierarchy()){
            indentbutton = new Button("", "/common/img/ico_derecha1.png");
            indentbutton.setHoverImage("/common/img/ico_derecha.png");
            indentbutton.setTooltiptext(_("Indent"));
        }else{
            indentbutton = new Button("", "/common/img/ico_derecha_out.png");
            indentbutton.setTooltiptext(_("Not indentable"));
        }
        indentbutton.setSclass("icono");
        return indentbutton;
    }

    private Button createButtonRemove(CriterionDTO criterion){
        Button removebutton;

        int num = criterionsModel.numberOfRelatedEntities(criterion
                .getCriterion());

        if (criterionsModel.isDeletable(criterion.getCriterion())) {
            removebutton = new Button("", "/common/img/ico_borrar1.png");
            removebutton.setHoverImage("/common/img/ico_borrar.png");
            removebutton.setTooltiptext(_("Delete"));
        } else {
            removebutton = new Button("", "/common/img/ico_borrar_out.png");
            removebutton.setTooltiptext(criterion.getCriterion().getChildren()
                    .isEmpty() ? (num + " " + _("references"))
                    : _("Criterion has subelements"));
        }

        removebutton.setSclass("icono");
        return removebutton;
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

    public void move(Component dropedIn, Component dragged) {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        Treerow from = (Treerow) dragged;
        CriterionDTO fromNode = (CriterionDTO) ((Treeitem) from.getParent())
                .getValue();
        if (dropedIn instanceof Tree) {
            getModel().moveToRoot(fromNode,0);
        }
        if (dropedIn instanceof Treerow) {
            Treerow to = (Treerow) dropedIn;
            CriterionDTO toNode = (CriterionDTO) ((Treeitem) to.getParent())
                    .getValue();
            getModel().move(fromNode, toNode,0);
        }
        reloadTree();
    }

    public void addCriterion() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        try {
            if((tree.getSelectedCount() == 1)
                && (this.criterionsModel.getAllowHierarchy())){
                getModel().addCriterionAt(getSelectedNode(),getName());
            } else {
                getModel().addCriterion(getName());
            }
            clearCriterionName();
            reloadTree();
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messagesForUser.showMessage(Level.ERROR, invalidValue
                        .getMessage());
            }
        }
    }

    public void disabledHierarchy() {
        snapshotOfOpenedNodes = TreeViewStateSnapshot.snapshotOpened(tree);
        getModel().flattenTree();
        reloadTree();
    }

    public void updateEnabledCriterions(boolean isChecked){
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
        return (CriterionDTO) tree.getSelectedItemApi().getValue();
    }

    private static class TreeViewStateSnapshot {
        private final Set<Object> all;
        private final Set<Object> dataOpen;

        private TreeViewStateSnapshot(Set<Object> dataOpen, Set<Object> all) {
            this.dataOpen = dataOpen;
            this.all = all;
        }

        public static TreeViewStateSnapshot snapshotOpened(Tree tree) {
            Set<Object> dataOpen = new HashSet<Object>();
            Set<Object> all = new HashSet<Object>();
            if (tree != null && tree.getTreechildrenApi() != null) {
                Iterator<Treeitem> itemsIterator = tree.getTreechildrenApi()
                        .getItems().iterator();
                while (itemsIterator.hasNext()) {
                    Treeitem treeitem = (Treeitem) itemsIterator.next();
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
