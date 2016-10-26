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

package org.libreplan.web.resources.search;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.resources.daos.IResourceLoadRatiosCalculator.ILoadRatiosDataType;
import org.libreplan.business.resources.daos.IResourcesSearcher.IResourcesQuery;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceType;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.NewAllocationSelector.AllocationType;
import org.libreplan.web.common.components.ResourceAllocationBehaviour;
import org.libreplan.web.planner.allocation.INewAllocationsAdder;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.TreeitemRenderer;

/**
 * Controller for searching for {@link Resource}.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public class NewAllocationSelectorController extends AllocationSelectorController {

    private static final BigDecimal AVAILABILITY_GOOD_VALUE = new BigDecimal(0.50);

    private static final BigDecimal AVAILABILITY_INTERMEDIUM_VALUE = new BigDecimal(0.25);

    private ResourceListRenderer resourceListRenderer = new ResourceListRenderer();

    private Radiogroup allocationTypeSelector;

    private Tree criterionsTree;

    private Listbox listBoxResources;

    private Label allocationSelectedItems;

    private Datebox startDateLoadRatiosDatebox, endDateLoadRatiosDatebox;

    private CriterionRenderer criterionRenderer = new CriterionRenderer();

    private AllocationType currentAllocationType;

    private ResourceAllocationBehaviour behaviour;

    public NewAllocationSelectorController(ResourceAllocationBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("selectorController", this, true);
        initializeComponents();
    }

    private void initializeComponents() {
        initializeCriteriaTree();
        initializeListboxResources();
        initializeAllocationTypeSelector();
        initializeFilteringDatesConstraints();
    }

    private void initializeFilteringDatesConstraints() {
        startDateLoadRatiosDatebox.setConstraint(checkConstraintFilteringDate());
        endDateLoadRatiosDatebox.setConstraint(checkConstraintFilteringDate());
    }

    private void initializeCriteriaTree() {
        // Initialize criteria tree
        if ( criterionsTree != null ) {
            criterionsTree.addEventListener("onSelect", event -> {
                searchResources("", getSelectedCriterions());
                showSelectedAllocations();
            });
        }

        criterionsTree.setItemRenderer(criterionRenderer);
    }

    private void initializeListboxResources() {
        // Initialize found resources box
        listBoxResources.addEventListener(Events.ON_SELECT,
                event -> {
                    if (isGenericType()) {
                        returnToSpecificDueToResourceSelection();
                    }
                    showSelectedAllocations();
                });

        listBoxResources.setMultiple(behaviour.allowMultipleSelection());
        listBoxResources.setItemRenderer(getListitemRenderer());
    }

    private void initializeAllocationTypeSelector() {
        // Add onCheck listener
        listBoxResources.setItemRenderer(getListitemRenderer());

        // Initialize radio group of selector types
        allocationTypeSelector.addEventListener(Events.ON_CHECK,
                event -> {
                    Radio radio = (Radio) event.getTarget();

                    if (radio == null) {
                        return;
                    }

                    onType(AllocationType.valueOf(radio.getValue()));
                    showSelectedAllocations();
                });

        // Feed with values
        for (AllocationType each : behaviour.allocationTypes()) {
            allocationTypeSelector.appendChild(radio(each));
        }
        doInitialSelection();
    }

    private Radio radio(AllocationType allocationType) {
        Radio result = new Radio(allocationType.getName());
        result.setValue(allocationType.toString());
        return result;
    }

    private void onType(AllocationType type) {
        currentAllocationType = type;
        Util.reloadBindings(criterionsTree);
        refreshListBoxResources();
    }

    private void doInitialSelection() {
        Radio item = allocationTypeSelector.getItemAtIndex(0);
        currentAllocationType = AllocationType.valueOf(item.getValue());
        showSelectedAllocations();
        item.setSelected(true);
    }

    private void showSelectedAllocations() {
        allocationSelectedItems.setValue(buildSelectedAllocationsString());
    }

    private String buildSelectedAllocationsString() {
        if ( currentAllocationType == AllocationType.SPECIFIC ) {
            List<Resource> resources = getSelectedResources();

            return Resource.getCaptionFor(resources);
        } else {
            List<Criterion> criteria = getSelectedCriterions();

            return currentAllocationType.asCaption(criteria);
        }
    }

    private List<ResourceWithItsLoadRatios> getAllResources() {
        return addLoadRatiosCalculations(query().byResourceType(getType()).execute());
    }

    private List<ResourceWithItsLoadRatios> addLoadRatiosCalculations(List<? extends Resource> listResources) {

        List<ResourceWithItsLoadRatios> result = new ArrayList<>();

        for (Resource each : listResources) {
            ILoadRatiosDataType t = resourceLoadRatiosCalculator.calculateLoadRatios(each,
                    LocalDate.fromDateFields(startDateLoadRatiosDatebox.getValue()),
                    LocalDate.fromDateFields(endDateLoadRatiosDatebox.getValue()),
                    scenarioManager.getCurrent());

            result.add(new ResourceWithItsLoadRatios(each, t));
        }

        return result;
    }

    private ResourceType getType() {
        return behaviour.getType();
    }

    private IResourcesQuery<?> query() {
        return currentAllocationType.doQueryOn(resourcesSearcher);
    }

    private void refreshListBoxResources() {
        refreshListBoxResources(getAllResources());
    }

    private void refreshListBoxResources(List<ResourceWithItsLoadRatios> resources) {
        listBoxResources.setModel(new SimpleListModel<>(resources));
        triggerSortListBoxResources();
    }

    private void triggerSortListBoxResources() {
        for (Object child : listBoxResources.getListhead().getChildren()) {
            final Listheader hd = (Listheader) child;
            if ( !"natural".equals(hd.getSortDirection()) ) {
                hd.sort("ascending".equals(hd.getSortDirection()), true);
            }
        }
    }

    private void returnToSpecificDueToResourceSelection() {
        currentAllocationType = AllocationType.SPECIFIC;
        List<Criterion> criteria = getSelectedCriterions();
        List<Resource> selectedWorkers = getSelectedWorkers();
        refreshListBoxResources(addLoadRatiosCalculations(query().byCriteria(criteria).byResourceType(getType()).execute()));

        listBoxResources.renderAll();
        // Force render so list items has the value property so the resources can be selected

        selectWorkers(selectedWorkers);
        currentAllocationType.doTheSelectionOn(allocationTypeSelector);
    }

    private void selectWorkers(Collection<? extends Resource> selectedWorkers) {
        for (Resource each : selectedWorkers) {
            Listitem listItem = findListItemFor(each);

            if ( listItem != null ) {
                listItem.setSelected(true);
            }
        }
    }

    private Listitem findListItemFor(Resource resource) {
        @SuppressWarnings("unchecked")
        Collection<Listitem> items = listBoxResources.getItems();
        for (Listitem item : items) {
            Resource itemResource = ((ResourceWithItsLoadRatios) item.getValue()).getResource();

            if ( itemResource != null && itemResource.getId().equals(resource.getId()) ) {
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
        Set<Listitem> selectedItems = new HashSet<>(listBox.getSelectedItems());
        for (Listitem each : selectedItems) {
            listBox.removeItemFromSelection(each);
        }
    }

    @SuppressWarnings("unchecked")
    private void clearSelection(Tree tree) {
        Set<Treeitem> selectedItems = new HashSet<>(tree.getSelectedItems());
        for (Treeitem each : selectedItems) {
            tree.removeItemFromSelection(each);
        }
    }

    /**
     * Get input text, and search for workers.
     *
     * @param event
     */
    public void searchWorkers(InputEvent event) {
        searchResources(event.getValue(), getSelectedCriterions());
    }

    /**
     * Does the actual search for workers, and refresh results.
     *
     * @param name
     * @param criterions
     */
    private void searchResources(String name, List<Criterion> criterions) {

        final List<? extends Resource> resources =
                query().byName(name).byCriteria(criterions).byResourceType(getType()).execute();

        refreshListBoxResources(addLoadRatiosCalculations(resources));
    }

    /**
     * Returns list of selected {@link Criterion}, selects only those which are leaf nodes.
     *
     * @return {@link List<Criterion>}
     */
    @Override
    public List<Criterion> getSelectedCriterions() {
        List<Criterion> result = new ArrayList<>();

        Set<Treeitem> selectedItems = criterionsTree.getSelectedItems();
        for (Treeitem item : selectedItems) {
            CriterionTreeNode node = item.getValue();

            if ( node.getData() instanceof Criterion ) {
                result.add((Criterion) node.getData());
            }
        }

        return result;
    }

    public ResourceListRenderer getListitemRenderer() {
        return resourceListRenderer;
    }

    @Override
    public void onClose() {
        clearAll();
    }

    @Override
    public void clearAll() {
        /* Do nothing */
    }

    public void open(final LocalDate start, final LocalDate end) {
        setStartFilteringDate(start);
        setEndFilteringDate(end);

        clearSelection(listBoxResources);
        clearSelection(criterionsTree);


        adHocTransactionService.runOnReadOnlyTransaction((IOnTransaction<Void>) () -> {
            refreshListBoxResources();
            criterionsTree.setModel(getCriterions());

            return null;
        });

        doInitialSelection();
    }

    public List<Resource> getSelectedWorkers() {
        if (isGenericType()) {
            return allResourcesShown();
        } else {
            return getSelectedResources();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Resource> allResourcesShown() {
        List<Resource> result = new ArrayList<>();
        List<Listitem> selectedItems = listBoxResources.getItems();
        for (Listitem item : selectedItems) {
            result.add(((ResourceWithItsLoadRatios) item.getValue()).getResource());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Resource> getSelectedResources() {
        List<Resource> result = new ArrayList();
        Set<Listitem> selectedItems = listBoxResources.getSelectedItems();

        for (Listitem item : selectedItems) {

            result.add(((ResourceWithItsLoadRatios) item.getValue()).getResource());
        }

        return result;
    }

    /**
     * Encapsulates {@link DefaultTreeNode}.
     *
     * @author Diego Pino García <dpino@igalia.com>
     */
    private static class CriterionTreeNode extends DefaultTreeNode {

        public CriterionTreeNode(Object data, List<CriterionTreeNode> children) {
            super(data, children);
        }

        /**
         * Returns {@link CriterionTreeNode} name depending if node contains a {@link Criterion} or a {@link CriterionType}.
         */
        @Override
        public String toString() {
            if ( getData() instanceof CriterionType ) {
                return ((CriterionType) getData()).getName();
            }
            if ( getData() instanceof Criterion ) {
                return ((Criterion) getData()).getName();
            }

            return "";
        }
    }

    /**
     * Gets all {@link Criterion} and returns a {@link TreeModel} out of it.
     * This {@link TreeModel} is used to feed criterionsTree widget.
     *
     * @return {@link TreeModel}
     */
    public TreeModel getCriterions() {
        Map<CriterionType, Set<Criterion>> criterions = query().getCriteria();

        List<CriterionTreeNode> rootList = new ArrayList<>();
        for (Entry<CriterionType, Set<Criterion>> entry : criterions.entrySet()) {
            rootList.add(asNode(entry.getKey(), entry.getValue()));
        }
        CriterionTreeNode root = new CriterionTreeNode("Root", rootList);

        return new DefaultTreeModel(root);
    }

    public List<AllocationType> getAllocationTypes() {
        return Arrays.asList(AllocationType.values());
    }

    /**
     * Converts {@link CriterionType} to {@link CriterionTreeNode}.
     *
     * @param criterionType
     * @param criterions
     * @return {@link CriterionTreeNode}
     */
    private CriterionTreeNode asNode(CriterionType criterionType, Set<Criterion> criterions) {
        return new CriterionTreeNode(criterionType, toNodeList(withoutParents(criterions)));
    }

    private List<Criterion> withoutParents(Collection<? extends Criterion> criterions) {
        List<Criterion> result = new ArrayList<>();
        for (Criterion each : criterions) {
            if (each.getParent() == null) {
                result.add(each);
            }
        }

        return result;
    }

    private List<CriterionTreeNode> toNodeList(Collection<? extends Criterion> criterions) {
        ArrayList<CriterionTreeNode> result = new ArrayList<>();
        for (Criterion criterion : sortedByName(criterions)) {
            result.add(asNode(criterion));
        }

        return result;
    }

    private List<Criterion> sortedByName(Collection<? extends Criterion> criterions) {
        List<Criterion> result = new ArrayList<>(criterions);
        Collections.sort(result, Criterion.byName);

        return result;
    }

    /**
     * Converts {@link Criterion} to {@link CriterionTreeNode}.
     *
     * @param criterion
     * @return {@link CriterionTreeNode}
     */
    private CriterionTreeNode asNode(Criterion criterion) {
        return new CriterionTreeNode(criterion, toNodeList(criterion.getChildren()));
    }

    private static class ResourceWithItsLoadRatios implements Comparable<ResourceWithItsLoadRatios> {

        private Resource resource;

        private ILoadRatiosDataType ratios;

        public ResourceWithItsLoadRatios(Resource resource, ILoadRatiosDataType ratios) {
            Validate.notNull(resource);
            Validate.notNull(ratios);
            this.resource = resource;
            this.ratios = ratios;
        }

        public Resource getResource() {
            return this.resource;
        }

        public ILoadRatiosDataType getRatios() {
            return this.ratios;
        }

        @Override
        public int compareTo(ResourceWithItsLoadRatios o) {
            return this.resource.compareTo(o.getResource());
        }
    }

    private static class ResourceListRenderer implements ListitemRenderer {

        /**
         * Render for listBoxResources.
         *
         * @author Diego Pino García <dpino@igalia.com>
         */
        @Override
        public void render(Listitem listitem, Object o, int i) throws Exception {
            listitem.setValue(o);
            appendLabelResource(listitem);
        }



        private void appendLabelResource(Listitem item) {
            ResourceWithItsLoadRatios dataToRender = item.getValue();

            Listcell cellName = new Listcell();
            Resource resource = dataToRender.getResource();
            cellName.appendChild(new Label(resource.getShortDescription()));
            item.appendChild(cellName);

            Listcell cellAvailability = new Listcell();
            BigDecimal availability = dataToRender.getRatios().getAvailiabilityRatio();
            Div totalDiv = new Div();
            totalDiv.setStyle("width:50px;height:12px;border: solid 1px black");
            Div containedDiv = new Div();

            String styleValue = "width:" + availability.movePointRight(2) + "%;height:12px;background-color:"
                    + calculateRgba(availability) + ";float:left;left:0";
            containedDiv.setStyle(styleValue);

            Label l = new Label(availability.movePointRight(2).toString() + "%");
            l.setStyle("width:50px;margin-left: 12px");
            containedDiv.appendChild(l);
            totalDiv.appendChild(containedDiv);
            cellAvailability.appendChild(totalDiv);
            item.appendChild(cellAvailability);

            Listcell cellOvertime = new Listcell();
            BigDecimal overtime = dataToRender.getRatios().getOvertimeRatio();
            Label overtimeLabel = new Label(overtime.toString());
            cellOvertime.appendChild(overtimeLabel);
            if ( !overtime.equals(BigDecimal.ZERO.setScale(2)) ) {
                overtimeLabel.setStyle("position: relative; top: -12px");
                Image img = new Image("/dashboard/img/value-meaning-negative.png");
                img.setStyle("width: 25px; position: relative; top: -5px");
                cellOvertime.appendChild(img);
            }

            item.appendChild(cellOvertime);
        }

        private String calculateRgba(BigDecimal availability) {
            String result;
            if ( availability.compareTo(AVAILABILITY_INTERMEDIUM_VALUE) < 0) {
                result = "rgba(150,0,0,0.3)";
            } else if ( availability.compareTo(AVAILABILITY_GOOD_VALUE) < 0) {
                result = "rgba(255,255,0,0.5)";
            } else {
                result = "rgba(102,204,0,0.3)";
            }

            return result;
        }
    }

    public CriterionRenderer getCriterionRenderer() {
        return criterionRenderer;
    }

    /**
     * Render for criterionsTree.
     *
     * I had to implement a renderer for the Tree.
     * Every item in the tree should be set as opened at first.
     * I tried to do this by iterating through the list of items after setting the model at doAfterCompose,
     * but I got a {@link java.util.ConcurrentModificationException}.
     * It seems that at that point some other component was using the list of items, so it was not possible to modify it.
     * There's not other point where to initialize components but doAfterCompose.
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     */
    private static class CriterionRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree
         */
        @Override
        public void render(Treeitem treeitem, Object o, int i) throws Exception {
            Treecell tc = new Treecell(Objects.toString(o));
            Treerow tr;
            treeitem.setValue(o);
            if (treeitem.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(treeitem);
                treeitem.setOpen(true); // Expand node
            } else {
                tr = treeitem.getTreerow();
                tr.getChildren().clear();
            }
            tc.setParent(tr);
        }
    }

    @Override
    public void addTo(INewAllocationsAdder allocationsAdder) {
        currentAllocationType.addTo(this, allocationsAdder);
    }

    public void allowSelectMultipleResources(boolean multiple) {
        listBoxResources.setMultiple(multiple);
    }

    public boolean isAllowSelectMultipleResources() {
        return listBoxResources.isMultiple();
    }

    public void setEndFilteringDate(LocalDate d) {
        endDateLoadRatiosDatebox.setValue(asDate(d));
    }

    public void setStartFilteringDate(LocalDate date) {
        startDateLoadRatiosDatebox.setValue(asDate(date));
    }

    private static Date asDate(LocalDate date) {
        if ( date == null ) {
            return null;
        }

        return date.toDateTimeAtStartOfDay().toDate();
    }

    public void updateLoadRatios() {
        searchResources("", getSelectedCriterions());
    }

    public Constraint  checkConstraintFilteringDate() {
        return (comp, value) -> {
            if ( value == null ) {
                if ( comp.getId().equals("startDateLoadRatiosDatebox") ) {
                    throw new WrongValueException(comp, _("Start filtering date cannot be empty"));
                } else if ( comp.getId().equals("endDateLoadRatiosDatebox") ) {
                    throw new WrongValueException(comp, _("End filtering date cannot be empty"));
                }
            }

            Date startDate;

            if ( comp.getId().equals("startDateLoadRatiosDatebox") ) {
                startDate = (Date) value;
            } else {
                startDate = (Date) startDateLoadRatiosDatebox.getRawValue();
            }

            Date endDate;
            if ( comp.getId().equals("endDateLoadRatiosDatebox") ) {
                endDate = (Date) value;
            } else {
                endDate = (Date) endDateLoadRatiosDatebox.getRawValue();
            }

            if ( (startDate != null) && (endDate != null) ) {
                if ( (startDate.after(endDate)) || (startDate.equals(endDate)) ) {
                    throw new WrongValueException(comp, _("Start filtering date must be before than end filtering date"));
                }
            }
        };
    }

}
