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

package org.navalplanner.web.resources.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * Controller for searching for {@link Worker}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class WorkerSearchController extends GenericForwardComposer {

    @Autowired
    private IWorkerSearchModel workerSearchModel;

    private WorkerListRenderer workerListRenderer = new WorkerListRenderer();

    private Textbox txtName;

    private Tree criterionsTree;

    private Listbox listBoxWorkers;

    CriterionRenderer criterionRenderer = new CriterionRenderer();

    public WorkerSearchController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        initController();
    }

    /**
     * Initializes ZUL components
     */
    private void initController() {
        // Add event listener onSelect to criterionsTree widget
        Tree tree = (Tree) self.getFellowIfAny("criterionsTree");
        if (tree != null) {
            tree.addEventListener("onSelect", new EventListener() {

                // Whenever an element of the tree is selected, a search query
                // is executed, refreshing the results into the workers listbox
                @Override
                public void onEvent(Event event) throws Exception {
                    searchWorkers(txtName.getValue(), getSelectedCriterions());
                }
            });
        }

        // Initialize components
        criterionsTree.setTreeitemRenderer(criterionRenderer);
        listBoxWorkers.setItemRenderer(getListitemRenderer());

        // Show all workers
        refreshListBoxWorkers(workerSearchModel.getAllWorkers());
    }

    /**
     * Get input text, and search for workers
     *
     * @param event
     */
    public void searchWorkers(InputEvent event) {
        searchWorkers(event.getValue(), getSelectedCriterions());
    }

    /**
     * Does the actual search for workers, and refresh results
     *
     * @param name
     * @param criterions
     */
    private void searchWorkers(String name, List<Criterion> criterions) {
        // No text, and no criterions selected
        if (criterions.isEmpty()
                && (name == null || name.isEmpty())) {
            refreshListBoxWorkers(workerSearchModel.getAllWorkers());
            return;
        }

        final List<Worker> listWorkers = workerSearchModel.findWorkers(name,
                criterions);
        refreshListBoxWorkers(listWorkers);
    }

    /**
     * Returns list of selected {@link Criterion}, selects only those which are
     * leaf nodes
     *
     * @return
     */
    private List<Criterion> getSelectedCriterions() {
        List<Criterion> result = new ArrayList<Criterion>();

        Set<Treeitem> selectedItems = criterionsTree.getSelectedItems();
        for (Treeitem item : selectedItems) {
            CriterionTreeNode node = (CriterionTreeNode) item.getValue();

            if (node.getData() instanceof Criterion) {
                result.add((Criterion) node.getData());
            }
        }

        return result;
    }

    /**
     * Refresh listBoxWorkers with new {@link Worker} results
     *
     * @param workers
     *            array of {@link Worker}
     */
    private void refreshListBoxWorkers(Worker[] workers) {
        listBoxWorkers.setModel(new SimpleListModel(workers));
    }

    private void refreshListBoxWorkers(List<Worker> workers) {
        listBoxWorkers.setModel(new SimpleListModel(workers));
    }

    public WorkerListRenderer getListitemRenderer() {
        return workerListRenderer;
    }

    public void onClose() {
        clearAll();
    }

    public void clearAll() {
        txtName.setValue("");
        refreshListBoxWorkers(workerSearchModel.getAllWorkers());
        criterionsTree.setModel(getCriterions());
    }

    public List<Worker> getSelectedWorkers() {
        List<Worker> result = new ArrayList<Worker>();

        Set<Listitem> selectedItems = listBoxWorkers.getSelectedItems();
        for (Listitem item : selectedItems) {
            Worker worker = (Worker) item.getValue();
            result.add(worker);
        }
        return result;
    }

    /**
     * Encapsulates {@link SimpleTreeNode}
     *
     * @author Diego Pino García <dpino@igalia.com>
     *
     */
    private class CriterionTreeNode extends SimpleTreeNode {

        public CriterionTreeNode(Object data, List children) {
            super(data, children);
        }

        /**
         * Returns {@link CriterionTreeNode} name depending if node contains a
         * {@link Criterion} or a {@link CriterionType}
         */
        @Override
        public String toString() {
            if (getData() instanceof CriterionType) {
                return ((CriterionType) getData()).getName();
            }
            if (getData() instanceof Criterion) {
                return ((Criterion) getData()).getName();
            }

            return "";
        }
    }

    /**
     * Gets all {@link Criterion} and returns a {@link TreeModel} out of it
     *
     * This {@link TreeModel} is used to feed criterionsTree widget
     *
     * @return
     */
    public TreeModel getCriterions() {
        HashMap<CriterionType, Set<Criterion>> criterions = workerSearchModel
                .getCriterions();

        List<CriterionTreeNode> rootList = new ArrayList<CriterionTreeNode>();
        for (CriterionType key : criterions.keySet()) {
            rootList.add(asNode(key, criterions.get(key)));
        }
        CriterionTreeNode root = new CriterionTreeNode("Root", rootList);
        return new SimpleTreeModel(root);
    }

    /**
     * Converts {@link CriterionType} to {@link CriterionTreeNode}
     *
     * @param criterionType
     * @param criterions
     * @return
     */
    private CriterionTreeNode asNode(CriterionType criterionType,
            Set<Criterion> criterions) {
        return new CriterionTreeNode(criterionType, toNodeList(criterions));
    }

    /**
     * Converts a {@link Set} of {@link Criterion} to an {@link ArrayList} of
     * {@link CriterionTreeNode}
     *
     * @param criterions
     * @return
     */
    private ArrayList<CriterionTreeNode> toNodeList(Set<Criterion> criterions) {
        ArrayList<CriterionTreeNode> result = new ArrayList<CriterionTreeNode>();
        for (Criterion criterion : criterions) {
            result.add(asNode(criterion));
        }
        return result;
    }

    /**
     * Converts {@link Criterion} to {@link CriterionTreeNode}
     *
     * @param criterion
     * @return
     */
    private CriterionTreeNode asNode(Criterion criterion) {
        return new CriterionTreeNode(criterion, new ArrayList());
    }

    /**
     * Render for listBoxWorkers
     *
     * @author Diego Pino García <dpino@igalia.com>
     *
     */
    private class WorkerListRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            item.setValue((Worker) data);

            appendLabelWorker(item);
        }

        private void appendLabelWorker(Listitem item) {
            Worker worker = (Worker) item.getValue();

            Listcell listWorker = new Listcell();
            listWorker.appendChild(new Label(worker.getName()));
            item.appendChild(listWorker);

            Listcell listName = new Listcell();
            listName.appendChild(new Label(worker.getNif()));
            item.appendChild(listName);
        }
    }

    public CriterionRenderer getCriterionRenderer() {
        return criterionRenderer;
    }

    /**
     * Render for criterionsTree
     *
     * I had to implement a renderer for the Tree, for settin open to tree for
     * each treeitem while being rendered.
     *
     * I tried to do this by iterating through the list of items after setting
     * model in doAfterCompose, but I got a ConcurrentModificationException. It
     * seems that at that point some other component was using the list of item,
     * so it was not possible to modify it. There's not other point where to
     * initialize components but doAfterCompose.
     *
     * Finally, I tried this solution and it works
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    private class CriterionRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) throws Exception {
            Treecell tc = new Treecell(Objects.toString(node));
            Treerow tr = null;
            ti.setValue(node);
            if (ti.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(ti);
                ti.setOpen(true); // Expand node
            } else {
                tr = ti.getTreerow();
                tr.getChildren().clear();
            }
            tc.setParent(tr);
        }

    }

}
