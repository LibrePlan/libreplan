/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewAllocationSelector.AllocationType;
import org.navalplanner.web.planner.allocation.INewAllocationsAdder;
import org.navalplanner.web.resources.search.IResourceSearchModel.IResourcesQuery;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * Controller for searching for {@link Resource}
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class NewAllocationSelectorController extends
        AllocationSelectorController {

    private ResourceListRenderer resourceListRenderer = new ResourceListRenderer();

    private Radiogroup allocationTypeSelector;

    private Tree criterionsTree;

    private Listbox listBoxResources;

    private Label allocationSelectedItems;

    private CriterionRenderer criterionRenderer = new CriterionRenderer();

    private AllocationType currentAllocationType;

    public NewAllocationSelectorController() {

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
        doInitialSelection();
        // Add event listener onSelect to criterionsTree widget
        if (criterionsTree != null) {
            criterionsTree.addEventListener("onSelect", new EventListener() {

                // Whenever an element of the tree is selected, a search query
                // is executed, refreshing the results into the workers listbox
                @Override
                public void onEvent(Event event) throws Exception {
                    searchResources("", getSelectedCriterions());
                }
            });
        }

        // Initialize components
        criterionsTree.setTreeitemRenderer(criterionRenderer);
        listBoxResources.setItemRenderer(getListitemRenderer());

        refreshListBoxResources();
        listBoxResources.addEventListener(Events.ON_SELECT,
                new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (isGenericType()) {
                    returnToSpecificDueToResourceSelection();
                }
            }
        });
        allocationTypeSelector.addEventListener(Events.ON_CHECK,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        AllocationType type = AllocationType
                                .getSelected(allocationTypeSelector);
                        onType(type);
                        showSelectedAllocations();
                    }
                });
    }

    private List<? extends Resource> getAllResources() {
        return query().byResourceType(type).execute();
    }

    private IResourcesQuery<?> query() {
        return currentAllocationType.doQueryOn(resourceSearchModel);
    }

    private void doInitialSelection() {
        currentAllocationType = AllocationType.GENERIC_WORKERS;
        AllocationType.GENERIC_WORKERS.doTheSelectionOn(allocationTypeSelector);
        onType(currentAllocationType);
    }

    private void onType(AllocationType type) {
        currentAllocationType = type;
        Util.reloadBindings(criterionsTree);
        refreshListBoxResources();
    }

    private void returnToSpecificDueToResourceSelection() {
        currentAllocationType = AllocationType.SPECIFIC;
        List<Criterion> criteria = getSelectedCriterions();
        List<Resource> selectedWorkers = getSelectedWorkers();
        refreshListBoxResources(query().byCriteria(criteria)
                .byResourceType(type).execute());
        listBoxResources.renderAll(); // force render so list items has the
                                      // value property so the resources can be
                                      // selected

        selectWorkers(selectedWorkers);
        currentAllocationType.doTheSelectionOn(allocationTypeSelector);
    }

    private void selectWorkers(Collection<? extends Resource> selectedWorkers) {
        for (Resource each : selectedWorkers) {
            Listitem listItem = findListItemFor(each);
            if (listItem != null) {
                listItem.setSelected(true);
            }
        }
    }

    private Listitem findListItemFor(Resource resource) {
        @SuppressWarnings("unchecked")
        Collection<Listitem> items = listBoxResources.getItems();
        for (Listitem item : items) {
            Resource itemResource = (Resource) item.getValue();
            if (itemResource != null
                    && itemResource.getId().equals(resource.getId())) {
                return item;
            }
        }
        return null;
    }

    private static final EnumSet<AllocationType> genericTypes = EnumSet.of(
            AllocationType.GENERIC_MACHINES, AllocationType.GENERIC_WORKERS);

    private boolean isGenericType() {
        return genericTypes.contains(currentAllocationType);
    }

    @SuppressWarnings("unchecked")
    private void clearSelection(Listbox listBox) {
        Set<Listitem> selectedItems = new HashSet<Listitem>(listBox
                .getSelectedItems());
        for (Listitem each : selectedItems) {
            listBox.removeItemFromSelection(each);
        }
    }

    @SuppressWarnings("unchecked")
    private void clearSelection(Tree tree) {
        Set<Treeitem> selectedItems = new HashSet<Treeitem>(tree.getSelectedItems());
        for (Treeitem each : selectedItems) {
            tree.removeItemFromSelection(each);
        }
    }

    /**
     * Get input text, and search for workers
     *
     * @param event
     */
    public void searchWorkers(InputEvent event) {
        searchResources(event.getValue(), getSelectedCriterions());
    }

    /**
     * Does the actual search for workers, and refresh results
     *
     * @param name
     * @param criterions
     */
    private void searchResources(String name, List<Criterion> criterions) {
        final List<? extends Resource> resources = query().byName(name)
                .byCriteria(criterions)
                .byResourceType(type)
                .execute();
        refreshListBoxResources(resources);
    }

    /**
     * Returns list of selected {@link Criterion}, selects only those which are
     * leaf nodes
     *
     * @return
     */
    public List<Criterion> getSelectedCriterions() {
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

    private void refreshListBoxResources(List<? extends Resource> resources) {
        listBoxResources.setModel(new SimpleListModel(resources));
    }

    public ResourceListRenderer getListitemRenderer() {
        return resourceListRenderer;
    }

    public void onClose() {
        clearAll();
    }

    public void clearAll() {
        refreshListBoxResources();
        criterionsTree.setModel(getCriterions());
        clearSelection(listBoxResources);
        clearSelection(criterionsTree);
        doInitialSelection();
    }

    private void refreshListBoxResources() {
        refreshListBoxResources(getAllResources());
    }

    public List<Resource> getSelectedWorkers() {
        if (isGenericType()) {
            return allResourcesShown();
        } else {
            return getSelectedResourcesOnListbox();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Resource> allResourcesShown() {
        List<Resource> result = new ArrayList<Resource>();
        List<Listitem> selectedItems = listBoxResources.getItems();
        for (Listitem item : selectedItems) {
            result.add((Resource) item.getValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Resource> getSelectedResourcesOnListbox() {
        List<Resource> result = new ArrayList<Resource>();
        Set<Listitem> selectedItems = listBoxResources.getSelectedItems();
        for (Listitem item : selectedItems) {
            result.add((Resource) item.getValue());
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

        public CriterionTreeNode(Object data, List<CriterionTreeNode> children) {
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
        Map<CriterionType, Set<Criterion>> criterions = query().getCriteria();

        List<CriterionTreeNode> rootList = new ArrayList<CriterionTreeNode>();
        for (Entry<CriterionType, Set<Criterion>> entry : criterions.entrySet()) {
            rootList.add(asNode(entry.getKey(), entry.getValue()));
        }
        CriterionTreeNode root = new CriterionTreeNode("Root", rootList);
        return new SimpleTreeModel(root);
    }

    public List<AllocationType> getAllocationTypes() {
        return Arrays.asList(AllocationType.values());
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
        return new CriterionTreeNode(criterion, new ArrayList<CriterionTreeNode>());
    }

    /**
     * Render for listBoxResources
     * @author Diego Pino García <dpino@igalia.com>
     */
    private class ResourceListRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            item.setValue((Resource) data);

            appendLabelResource(item);
        }

        private void appendLabelResource(Listitem item) {
            Resource resource = (Resource) item.getValue();

            Listcell cell = new Listcell();
            cell.appendChild(new Label(resource.getShortDescription()));
            item.appendChild(cell);
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

    public void addTo(INewAllocationsAdder allocationsAdder) {
        currentAllocationType.addTo(this, allocationsAdder);
    }

    public void allowSelectMultipleResources(boolean multiple) {
        listBoxResources.setMultiple(multiple);
    }

    public void showSelectedAllocations() {
        allocationSelectedItems.setValue(buildSelectedAllocationsString());
    }

    private String buildSelectedAllocationsString() {

        if (currentAllocationType == AllocationType.SPECIFIC) {
            List<String> result = new ArrayList<String>();
            for (Resource each : getSelectedResourcesOnListbox()) {
                result.add(each.getShortDescription());
            }
            return StringUtils.join(result, ",");
        } else {
            List<Criterion> criterions = getSelectedCriterions();
            return currentAllocationType.asCaption(criterions);
        }
    }

}
