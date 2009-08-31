package org.navalplanner.web.resources.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.zkoss.zul.Treeitem;

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

        // Feed criterionsTree with criterions
        if (criterionsTree.getModel() == null) {
            criterionsTree.setModel(getCriterions());
        }

        // Set renderer for listboxWorkers
        if (listBoxWorkers.getItemRenderer() == null) {
            listBoxWorkers.setItemRenderer(getListitemRenderer());
        }
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
        if (name == null || name.length() == 0) {
            refreshListBoxWorkers(new Worker[0]);
            return;
        }

        List<Worker> listWorkers = workerSearchModel.findWorkers(name,
                getSelectedCriterions());
        refreshListBoxWorkers(listWorkers
                .toArray(new Worker[listWorkers
                .size()]));
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

    public WorkerListRenderer getListitemRenderer() {
        return workerListRenderer;
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

            Listcell listCell = new Listcell();
            listCell.appendChild(new Label(worker.getName()));
            item.appendChild(listCell);
        }
    }
}
