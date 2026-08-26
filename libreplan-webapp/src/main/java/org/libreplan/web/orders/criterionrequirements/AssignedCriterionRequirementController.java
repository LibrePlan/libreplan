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

package org.libreplan.web.orders.criterionrequirements;

import static org.libreplan.web.I18nHelper._t;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.NewDataSortableGrid;
import org.libreplan.web.orders.CriterionRequirementWrapper;
import org.libreplan.web.orders.HoursGroupWrapper;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Separator;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;

import com.libreplan.java.zk.components.customdetailrowcomponent.Detail;

/**
 * Controller for showing OrderElement assigned labels.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public abstract class AssignedCriterionRequirementController<T, M> extends GenericForwardComposer<Component> {

    private Listbox hoursGroupsInOrderLineGroup;

    private List<ResourceEnum> listResourceTypes = new ArrayList<>();

    private NewDataSortableGrid listingRequirements;

    private transient ListitemRenderer renderer = new HoursGroupListitemRender();

    protected NewDataSortableGrid listHoursGroups;

    protected Intbox orderElementTotalHours;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("assignedCriterionRequirementController", this, true);

        // Init the resourcesType
        listResourceTypes.add(ResourceEnum.MACHINE);
        listResourceTypes.add(ResourceEnum.WORKER);
    }

    public abstract T getElement();

    public abstract Set<CriterionType> getCriterionTypes();

    public abstract void setOrderElement(T element);

    public abstract void openWindow(M model);

    public abstract void confirm();

    public boolean close() {
        if (showInvalidValues()) {
            return false;
        }
        confirm();

        return true;
    }

    private boolean showInvalidValues() {
        CriterionRequirementWrapper invalidWrapper = validateWrappers(getCriterionRequirementWrappers());
        if (invalidWrapper != null) {
            showInvalidValues(invalidWrapper);

            return true;
        }

        CriterionRequirementWrapper invalidHoursGroupWrapper = validateHoursGroupWrappers();
        if (invalidHoursGroupWrapper != null) {
            showInvalidValuesInHoursGroups(invalidHoursGroupWrapper);

            return true;
        }

        return false;
    }

    public abstract List<CriterionRequirementWrapper> getCriterionRequirementWrappers();

    /**
     * NewDataSortableGrid.setModel(ListModel) is a strict, non-generic override - unlike plain
     * Grid/Combobox/Listbox, ZK Bind's List-&gt;ListModel auto-wrap does not kick in for it, so the
     * ZUML "model=" binding needs an already-wrapped ListModel explicitly.
     */
    public ListModel<CriterionRequirementWrapper> getCriterionRequirementWrappersModel() {
        return new SimpleListModel<>(getCriterionRequirementWrappers());
    }

    public abstract List<CriterionWithItsType> getCriterionWithItsTypes();

    /**
     * Used in _listHoursGroupCriterionRequirement.zul
     * Should be public!
     */
    public List<CriterionWithItsType> getCriterionWithItsTypesWorker() {
        List<CriterionWithItsType> result = new ArrayList<>();
        for (CriterionWithItsType criterionAndType : getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource().equals(ResourceEnum.MACHINE)) {
                result.add(criterionAndType);
            }
        }

        return result;
    }

    /**
     * Used in _listHoursGroupCriterionRequirement.zul
     * Should be public!
     */
    public List<CriterionWithItsType> getCriterionWithItsTypesMachine() {
        List<CriterionWithItsType> result = new ArrayList<>();
        for (CriterionWithItsType criterionAndType : getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource().equals(ResourceEnum.WORKER)) {
                result.add(criterionAndType);
            }
        }

        return result;
    }

    public List<ResourceEnum> getResourceTypes() {
        return listResourceTypes;
    }

    /**
     * It is used!
     */
    public abstract void addCriterionRequirementWrapper();

    public abstract void remove(CriterionRequirementWrapper requirement);

    public abstract void invalidate(CriterionRequirementWrapper requirement);

    public abstract void validate(CriterionRequirementWrapper requirement);

    protected abstract void changeCriterionAndType(
            CriterionRequirementWrapper requirementWrapper, CriterionWithItsType newCriterionAndType);

    public void selectCriterionAndType(Listitem item,
                                       Bandbox bandbox,
                                       CriterionRequirementWrapper requirementWrapper) {
        if (item != null) {
            CriterionWithItsType newCriterionAndType = item.getValue();
            try {
                bandbox.setValue(newCriterionAndType.getNameAndType());
                changeCriterionAndType(requirementWrapper, newCriterionAndType);
            } catch (IllegalStateException e) {
                showInvalidConstraint(bandbox, e);
                requirementWrapper.setCriterionWithItsType(null);
            }
            Util.reloadBindings(listHoursGroups);
        } else {
            bandbox.setValue("");
        }
    }

    public void onChangingText(Event event) {
        Bandbox bd = (Bandbox) event.getTarget();
        final String inputText = ((InputEvent) event).getValue();
        Listbox listbox = (Listbox) bd.getFirstChild().getFirstChild();
        listbox.setModel(getSubModel(inputText));
        listbox.invalidate();
        bd.open();
    }

    public void onCtrlKey(Event event) {
        Bandbox bd = (Bandbox) event.getTarget();
        Listbox listbox = (Listbox) bd.getFirstChild().getFirstChild();
        List<Listitem> items = listbox.getItems();

        if (!items.isEmpty()) {
            listbox.setSelectedIndex(0);
            items.get(0).setFocus(true);
        }
    }

    private ListModel getSubModel(String text) {
        List<CriterionWithItsType> list = new ArrayList<>();
        String newText = text.trim().toLowerCase();

        for (CriterionWithItsType criterion : this.getCriterionWithItsTypes()) {

            if ( criterion.getNameHierarchy().toLowerCase().contains(newText) ||
                    criterion.getType().getName().toLowerCase().contains(newText) ) {

                list.add(criterion);
            }
        }

        return new SimpleListModel<>(list);
    }

    protected abstract void updateCriterionsWithDifferentResourceType(HoursGroupWrapper hoursGroupWrapper);

    /**
     * Used in _listOrderElementCriterionRequirements.zul
     * Should be public!
     */
    public void selectResourceType(Combobox combobox) throws InterruptedException {
        HoursGroupWrapper hoursGroupWrapper = ((Row) combobox.getParent()).getValue();
        boolean reloadBindings = true;

        if (combobox.getSelectedItem() != null) {
            int status = Messagebox.show(
                    _t("Are you sure of changing the resource type? " +
                            "You will lose the criteria with different resource type."),
                    "Question", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

            if (Messagebox.OK == status) {
                ResourceEnum resource = combobox.getSelectedItem().getValue();
                hoursGroupWrapper.setResourceType(resource.toString());
                updateCriterionsWithDifferentResourceType(hoursGroupWrapper);
            }

            /* Set Type ComboBox to previous value */
            if ( Messagebox.CANCEL == status ) {

                if ( ResourceEnum.WORKER.toString().equals(combobox.getValue()) ) {
                    combobox.setValue(ResourceEnum.MACHINE.toString());
                } else
                    combobox.setValue(ResourceEnum.WORKER.toString());

                reloadBindings = false;
            }
        }

        /* Avoid unnecessary reload of bindings when cancel button pushed */
        if ( reloadBindings ) {
            Util.reloadBindings(listHoursGroups);
        }
    }

    /**
     * listingRequirements' own model="@load(...)" binding gets its first (empty) value pushed
     * eagerly during this page's whole-window tab pre-composition, before openWindow()/init()
     * ever runs - same root cause as AssignedLabelsController's directLabels bug.
     * Util.reloadBindings(listingRequirements) alone never updates the client after that point,
     * so bypass the binder and set the wrapped ListModel directly.
     *
     * rowRenderer="@load(...)" on the same tag is bypassed the same way and for the same reason -
     * setting only the model without also re-asserting the renderer leaves rows falling back to
     * their raw toString() in composition contexts where that binding never fired (see
     * ManageOrderElementAdvancesController's identical editAdvances fix for the confirmed symptom).
     */
    public void reload() {
        listingRequirements.setRowRenderer(getListingRequirementsRowRenderer());
        listingRequirements.setModel(getCriterionRequirementWrappersModel());
        Util.reloadBindings(orderElementTotalHours);
        if (isReadOnly()) {
            Util.reloadBindings(hoursGroupsInOrderLineGroup);
        } else {
            Util.reloadBindings(listHoursGroups);
        }
    }

    protected abstract CriterionRequirementWrapper validateWrappers(List<CriterionRequirementWrapper> list);

    protected abstract CriterionRequirementWrapper validateHoursGroupWrappers();

    /**
     * Show invalid values inside listHoursGroup.
     */
    private void showInvalidValuesInHoursGroups(CriterionRequirementWrapper requirementWrapper) {
        if (listHoursGroups != null) {
            List<Row> listRowsHoursGroup = listHoursGroups.getRows().getChildren();
            for (Row row : listRowsHoursGroup) {
                Rows listRequirementRows = getRequirementRows(row);
                Row requirementRow = findRowOfCriterionRequirementWrapper(listRequirementRows, requirementWrapper);
                showInvalidValue(requirementRow, requirementWrapper);
            }
        }
    }

    /**
     * Validates {@link CriterionRequirementWrapper} data constraints.
     *
     * @param requirementWrapper
     */
    private void showInvalidValues(CriterionRequirementWrapper requirementWrapper) {
        if (listingRequirements != null) {
            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionRequirementWrapper(listingRequirements.getRows(), requirementWrapper);
            showInvalidValue(row, requirementWrapper);
        }
    }

    private void showInvalidValue(Row row, CriterionRequirementWrapper requirementWrapper) {
        if (row != null) {
            Bandbox bandType = getBandType(requirementWrapper, row);
            bandType.setValue(null);
            throw new WrongValueException(bandType, _t("cannot be empty"));
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in rows.
     *
     * @param rows
     * @param requirementWrapper
     * @return {@link Row}
     */

    private Row findRowOfCriterionRequirementWrapper(Rows rows, CriterionRequirementWrapper requirementWrapper) {
        final List<Row> listRows =  rows.getChildren();
        for (Row row : listRows) {
            if (requirementWrapper.equals(row.getValue())) {
                return row;
            }
        }

        return null;
    }

    /**
     * Locates {@link Bandbox} criterion requirement in {@link Row}.
     *
     * @param row
     * @return Bandbox
     */
    private Bandbox getBandType(CriterionRequirementWrapper wrapper, Row row) {
        if (wrapper.isNewDirectAndItsHoursGroupIsMachine()) {
            return (Bandbox) row.getChildren().get(0).getChildren().get(1);
        }

        if (wrapper.isNewException()) {
            return (Bandbox) row.getChildren().get(0).getChildren().get(2);
        }

        return (Bandbox) row.getChildren().get(0).getChildren().get(0);
    }

    private Rows getRequirementRows(Row row) {
        Panel panel = (Panel) row.getFirstChild().getChildren().get(1);
        return ((Grid) panel.getFirstChild().getFirstChild()).getRows();
    }

    private HoursGroupWrapper getHoursGroupOfRequirementWrapper(Row rowRequirement) {
        Grid grid = (Grid) rowRequirement.getParent().getParent();
        Panel panel = (Panel) grid.getParent().getParent();

        return (HoursGroupWrapper) ((Row) panel.getParent().getParent()).getValue();
    }

    /**
     * Operations to manage OrderElement's hoursGroups and to assign criterion requirements to this hoursGroups.
     */

    public boolean isReadOnly() {
        return !isEditableHoursGroup();
    }

    public abstract boolean isEditableHoursGroup();

    public abstract List<HoursGroupWrapper> getHoursGroupWrappers();

    public ListModel<HoursGroupWrapper> getHoursGroupWrappersModel() {
        return new SimpleListModel<>(getHoursGroupWrappers());
    }

    /**
     * Adds a new {@link HoursGroup} to the current {@link OrderElement}.
     * The {@link OrderElement} should be a {@link OrderLine}.
     */
    public abstract void addHoursGroup();

    /**
     * Deletes the selected {@link HoursGroup} for the current {@link OrderElement}.
     * The {@link OrderElement} should be a {@link OrderLine}.
     */
    public void deleteHoursGroups(Component self) throws InterruptedException {
        if (getHoursGroupWrappers().size() < 2) {

            Messagebox.show(
                    _t("At least one HoursGroup is needed"),
                    _t("Error"), Messagebox.OK, Messagebox.ERROR);
            return;
        }

        final HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);

        if (hoursGroupWrapper != null) {
            deleteHoursGroupWrapper(hoursGroupWrapper);
            Util.reloadBindings(listHoursGroups);
        }
    }

    protected abstract void deleteHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper);

    private HoursGroupWrapper getHoursGroupWrapper(Component self) {
        return (HoursGroupWrapper) ((Row) self.getParent().getParent()).getValue();
    }

    public void addCriterionToHoursGroup(Component self) {
        final HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);

        if (hoursGroupWrapper != null) {
            addCriterionToHoursGroupWrapper(hoursGroupWrapper);
            repaint(self, hoursGroupWrapper);
        }
    }

    protected abstract void addCriterionToHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper);

    public void addExceptionToHoursGroups(Component self) {
        final HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);

        if (hoursGroupWrapper != null) {
            addExceptionToHoursGroupWrapper(hoursGroupWrapper);
            repaint(self, hoursGroupWrapper);
        }
    }

    protected abstract CriterionRequirementWrapper addExceptionToHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper);

    public void removeCriterionToHoursGroup(Component self) {
        try {
            Row row = (Row) self.getParent().getParent();
            CriterionRequirementWrapper requirementWrapper = row.getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);
            deleteCriterionToHoursGroup(hoursGroupWrapper, requirementWrapper);
            repaint(self, hoursGroupWrapper);
        } catch (Exception ignored) {}
    }

    public abstract void deleteCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper);

    public void selectCriterionToHoursGroup(Listitem item,
                                            Bandbox bandbox,
                                            CriterionRequirementWrapper requirementWrapper) {

        bandbox.close();
        Listbox listbox = (Listbox) bandbox.getFirstChild().getFirstChild();
        listbox.setModel(new SimpleListModel(getCriterionWithItsTypes()));

        if (item != null) {

            Row row = (Row) bandbox.getParent().getParent();
            CriterionWithItsType criterionAndType = item.getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);
            bandbox.setValue(criterionAndType.getNameAndType());

            try {
                selectCriterionToHoursGroup(hoursGroupWrapper, requirementWrapper, criterionAndType);
            } catch (IllegalStateException e) {
                requirementWrapper.setCriterionWithItsType(null);
                showInvalidConstraint(bandbox, e);
            }
            Util.reloadBindings(listHoursGroups);
        } else {
            bandbox.setValue("");
        }
    }

    protected abstract void selectCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType criterionAndType);

    private void showInvalidConstraint(Bandbox bandbox, IllegalStateException e) {
        bandbox.setValue("");
        throw new WrongValueException(bandbox, _t(e.getMessage()));
    }

    /**
     * Operations to manage the data hoursGroup,
     * for example validate the percentage and its number of hours or set the fixed percentage.
     */

    public void changeTotalHours() {
        recalculateHoursGroup();
        Util.reloadBindings(listHoursGroups);
        Util.reloadBindings(orderElementTotalHours);
    }

    public abstract void recalculateHoursGroup();

    public Constraint getValidateTotalHours() {
        return (comp, value) -> {
            if (value == null) {
                orderElementTotalHours.setValue(0);
            }
            else {
                Validate.isTrue(value instanceof Integer);
                int intValue = (Integer) value;
                try {
                    if (getElement() instanceof OrderLine) {
                        ((OrderLine) getElement()).setWorkHours(intValue);
                    }
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _t(e.getMessage()));
                }
            }
        };
    }

    public Constraint getValidatePercentage() {
        return (comp, value) -> {
            HoursGroupWrapper hoursGroupWrapper = ((Row) comp.getParent()).getValue();
            try {
                hoursGroupWrapper.setPercentage((BigDecimal) value);
            } catch (IllegalArgumentException e) {
                throw new WrongValueException(comp, _t(e.getMessage()));
            }
        };
    }

    /**
     * Operations to return grouped list of hours Group.
     */

    /**
     * Returns a {@link List} of {@link HoursGroup}.
     * If the current element is an {@link OrderLine} this method just returns
     * the {@link HoursGroup} of this {@link OrderLine}.
     * Otherwise, this method gets all the {@link HoursGroup} of all the children {@link OrderElement}, and
     * aggregates them if they have the same {@link Criterion}.
     *
     * @return The {@link HoursGroup} list of the current {@link OrderElement}
     */
    public List<HoursGroup> getHoursGroups() {
        // Creates a map in order to join HoursGroup with the same Criterions
        Map<Map<ResourceEnum, Set<Criterion>>, HoursGroup> map = new HashMap<>();

        List<HoursGroup> hoursGroups = getHoursGroups(getElement());
        for (HoursGroup hoursGroup : hoursGroups) {
            Map<ResourceEnum, Set<Criterion>> key = getKeyFor(hoursGroup);

            HoursGroup hoursGroupAggregation = map.get(key);

            if (hoursGroupAggregation == null) {

                // This is not a real HoursGroup element,
                // it's just an aggregation that join HoursGroup with the same Criterions
                hoursGroupAggregation = new HoursGroup();

                hoursGroupAggregation.setWorkingHours(hoursGroup.getWorkingHours());
                hoursGroupAggregation.setCriterionRequirements(hoursGroup.getCriterionRequirements());
                hoursGroupAggregation.setResourceType(hoursGroup.getResourceType());
            } else {
                Integer newHours = hoursGroupAggregation.getWorkingHours() + hoursGroup.getWorkingHours();
                hoursGroupAggregation.setWorkingHours(newHours);
            }

            map.put(key, hoursGroupAggregation);
        }
        return new ArrayList<>(map.values());
    }

    public ListModel<HoursGroup> getHoursGroupsModel() {
        return new SimpleListModel<>(getHoursGroups());
    }

    protected abstract List<HoursGroup> getHoursGroups(T orderElement);

    private Map<ResourceEnum, Set<Criterion>> getKeyFor(HoursGroup hoursGroup) {
        Map<ResourceEnum, Set<Criterion>> keys = new HashMap<>();
        ResourceEnum resourceType = hoursGroup.getResourceType();
        Set<Criterion> criterions = getKeyCriterionsFor(hoursGroup);
        keys.put(resourceType, criterions);

        return keys;
    }

    private Set<Criterion> getKeyCriterionsFor(HoursGroup hoursGroup) {
        Set<Criterion> key = new HashSet<>();
        for (Criterion criterion : hoursGroup.getValidCriterions()) {
            if (criterion != null) {
                key.add(criterion);
            }
        }
        return key;
    }

    public abstract boolean isCodeAutogenerated();

    public ListitemRenderer getRenderer() {
        return renderer;
    }

    public class HoursGroupListitemRender implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data, int i) {
            final HoursGroup hoursGroup = (HoursGroup) data;

            // Criterion Requirements hours Group
            Listcell cellCriterionRequirements = new Listcell();
            cellCriterionRequirements.setParent(item);
            cellCriterionRequirements.appendChild(appendRequirements(hoursGroup));

            // Type hours Group
            Listcell cellType = new Listcell();
            cellType.setParent(item);
            cellType.appendChild(appendType(hoursGroup));

            // Working hours
            Listcell cellWorkingHours = new Listcell();
            cellWorkingHours.setParent(item);
            cellWorkingHours.appendChild(appendWorkingHours(hoursGroup));
        }

        private Label appendRequirements(final HoursGroup hoursGroup) {
            Label requirementsLabel = new Label();
            requirementsLabel.setMultiline(true);
            requirementsLabel.setValue(getLabelRequirements(hoursGroup));

            return requirementsLabel;
        }

        private String getLabelRequirements(HoursGroup hoursGroup) {
            String label = "";
            for (Criterion criterion : hoursGroup.getValidCriterions()) {
                if ( !"".equals(label) ) {
                    label = label.concat(", ");
                }
                label = label.concat(criterion.getName());
            }
            if ( !"".equals(label) ) {
                label = label.concat(".");
            }
            return label;
        }

        private Label appendType(final HoursGroup hoursGroup) {
            Label type = new Label();
            type.setValue(hoursGroup.getResourceType().toString());

            return type;
        }

        private Label appendWorkingHours(final HoursGroup hoursGroup) {
            Label workingHoursLabel = new Label();
            workingHoursLabel.setValue(String.valueOf(hoursGroup.getWorkingHours()));

            return workingHoursLabel;
        }

    }

    public void onOK(KeyEvent event) {
        Component listitem = event.getReference();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent().getParent();

            CriterionRequirementWrapper criterionRequirementWrapper =
                    ((Row) bandbox.getParent().getParent()).getValue();

            selectCriterionAndType((Listitem) listitem, bandbox, criterionRequirementWrapper);

            bandbox.close();
        }
    }

    public void onClick(MouseEvent event) {
        Component listitem = event.getTarget();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent().getParent();
            bandbox.close();
        }
    }

    private void repaint(Component self, HoursGroupWrapper hoursGroupWrapper) {
        Grid grid = getHoursGroupDetailsGrid(self);
        if (grid != null) {
            grid.setModel(new SimpleListModel<>(hoursGroupWrapper.getCriterionRequirementWrappersView().toArray()));
            grid.invalidate();
        } else {
            Util.reloadBindings(listHoursGroups);
        }
    }

    private Grid getHoursGroupDetailsGrid(Component self) {
        try {
            Detail detail = (Detail) self.getParent().getParent().getFirstChild();

            /* Because first element is Separator */
            Panel panel = (Panel) detail.getChildren().get(1);

            return (Grid) panel.getFirstChild().getFirstChild();
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * The row/listitem templates below (self="@{each=...}") never registered under this app's
     * AnnotateBinder shim (see project_zk_annotate_binder_bug memory) - replaced with equivalent
     * RowRenderer/imperative Java construction, preserving the exact child-index structure that
     * getBandType()/getRequirementRows()/getHoursGroupOfRequirementWrapper()/
     * getHoursGroupDetailsGrid() above rely on (row.getChildren().get(0) is always the hbox
     * holding the bandbox(es), detailrow is always the row's first child with
     * [separator, panel, separator] as its own children).
     */

    private final transient ListitemRenderer criterionWithItsTypeRenderer = (item, data, index) -> {
        CriterionWithItsType criterionWithItsType = (CriterionWithItsType) data;
        item.setValue(criterionWithItsType);
        new Listcell(criterionWithItsType.getType().getName()).setParent(item);
        new Listcell(criterionWithItsType.getNameHierarchy()).setParent(item);
        item.addEventListener(Events.ON_CLICK, event -> onClick((MouseEvent) event));
    };

    public ListitemRenderer getCriterionWithItsTypeRenderer() {
        return criterionWithItsTypeRenderer;
    }

    private Listbox buildCriterionWithItsTypeListbox(List<CriterionWithItsType> items) {
        Listbox listbox = new Listbox();
        listbox.setWidth("500px");
        listbox.setHeight("150px");
        listbox.setModel(new SimpleListModel<>(items));
        listbox.setItemRenderer(getCriterionWithItsTypeRenderer());

        Listhead listhead = new Listhead();
        listhead.setParent(listbox);
        new Listheader("Type").setParent(listhead);
        new Listheader("Criterion").setParent(listhead);

        return listbox;
    }

    public RowRenderer getListingRequirementsRowRenderer() {
        return (row, data, index) -> {
            final CriterionRequirementWrapper wrapper = (CriterionRequirementWrapper) data;
            row.setValue(wrapper);

            Hbox hbox = new Hbox();
            hbox.setParent(row);

            final Bandbox bandbox = new Bandbox();
            bandbox.setWidth("400px");
            bandbox.setVisible(wrapper.isUpdatable());
            bandbox.setValue(wrapper.getCriterionAndType());
            bandbox.setCtrlKeys("#down");
            bandbox.addEventListener(Events.ON_CHANGING, this::onChangingText);
            bandbox.addEventListener(Events.ON_CTRL_KEY, this::onCtrlKey);
            bandbox.addEventListener(Events.ON_OK, event -> onOK((KeyEvent) event));
            bandbox.setParent(hbox);

            Bandpopup bandpopup = new Bandpopup();
            bandpopup.setParent(bandbox);

            Listbox listbox = buildCriterionWithItsTypeListbox(getCriterionWithItsTypes());
            listbox.addEventListener(Events.ON_SELECT,
                    event -> selectCriterionAndType(listbox.getSelectedItem(), bandbox, wrapper));
            listbox.setParent(bandpopup);

            Label assignedLabel = new Label(wrapper.getCriterionAndType());
            assignedLabel.setVisible(wrapper.isUnmodifiable());
            assignedLabel.setParent(hbox);

            new Label(wrapper.getType()).setParent(row);

            Hbox operations = new Hbox();
            operations.setParent(row);

            Button delete = Util.createRemoveButton(event -> remove(wrapper));
            delete.setVisible(wrapper.isDirect());
            delete.setParent(operations);

            Button validateButton = new Button();
            validateButton.setLabel(_t("Validate"));
            validateButton.setVisible(wrapper.isIndirectInvalid());
            validateButton.addEventListener(Events.ON_CLICK, event -> validate(wrapper));
            validateButton.setParent(operations);

            Button invalidateButton = new Button();
            invalidateButton.setLabel(_t("Invalidate"));
            invalidateButton.setVisible(wrapper.isIndirectValid());
            invalidateButton.addEventListener(Events.ON_CLICK, event -> invalidate(wrapper));
            invalidateButton.setParent(operations);
        };
    }

    private Bandbox buildHoursGroupBandbox(final CriterionRequirementWrapper wrapper, boolean visible,
                                            List<CriterionWithItsType> items) {
        final Bandbox bandbox = new Bandbox();
        bandbox.setWidth("480px");
        bandbox.setVisible(visible);
        bandbox.setValue(wrapper.getCriterionAndType());
        bandbox.addEventListener(Events.ON_CHANGING, this::onChangingText);

        Bandpopup bandpopup = new Bandpopup();
        bandpopup.setParent(bandbox);

        Listbox listbox = buildCriterionWithItsTypeListbox(items);
        listbox.addEventListener(Events.ON_SELECT,
                event -> selectCriterionToHoursGroup(listbox.getSelectedItem(), bandbox, wrapper));
        listbox.setParent(bandpopup);

        return bandbox;
    }

    private RowRenderer getCriterionRequirementInHoursGroupRowRenderer(final HoursGroupWrapper hoursGroupWrapper) {
        return (row, data, index) -> {
            final CriterionRequirementWrapper wrapper = (CriterionRequirementWrapper) data;
            row.setValue(wrapper);

            Hbox hbox = new Hbox();
            hbox.setParent(row);

            buildHoursGroupBandbox(wrapper, wrapper.isNewDirectAndItsHoursGroupIsWorker(),
                    getCriterionWithItsTypesWorker()).setParent(hbox);

            buildHoursGroupBandbox(wrapper, wrapper.isNewDirectAndItsHoursGroupIsMachine(),
                    getCriterionWithItsTypesMachine()).setParent(hbox);

            buildHoursGroupBandbox(wrapper, wrapper.isNewException(),
                    hoursGroupWrapper.getValidCriterions()).setParent(hbox);

            Label assignedLabel = new Label(wrapper.getCriterionAndType());
            assignedLabel.setVisible(wrapper.isOldDirectOrException());
            assignedLabel.setParent(hbox);

            new Label(wrapper.getTypeToHoursGroup()).setParent(row);

            Hbox operations = new Hbox();
            operations.setParent(row);

            final Button delete = new Button();
            delete.setVisible(isEditableHoursGroup());
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setTooltiptext(_t("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> removeCriterionToHoursGroup(delete));
            delete.setParent(operations);

            Button disabledDelete = new Button();
            disabledDelete.setVisible(isReadOnly());
            disabledDelete.setSclass("icono");
            disabledDelete.setImage("/common/img/ico_borrar_out.png");
            disabledDelete.setTooltiptext(_t("Disable Delete"));
            disabledDelete.setParent(operations);
        };
    }

    private Panel buildCriterionRequirementsPanel(final HoursGroupWrapper hoursGroupWrapper) {
        Panel panel = new Panel();
        panel.setTitle(_t("Criteria Requirement"));
        panel.setBorder("normal");

        Panelchildren panelchildren = new Panelchildren();
        panelchildren.setParent(panel);

        Grid grid = new Grid();
        grid.setMold("paging");
        grid.setPageSize(5);
        grid.setEmptyMessage(_t("No criterions"));
        grid.setParent(panelchildren);

        Columns columns = new Columns();
        columns.setParent(grid);

        Column nameColumn = new Column();
        nameColumn.setLabel(_t("Criterion name"));
        nameColumn.setParent(columns);

        Column typeColumn = new Column();
        typeColumn.setLabel(_t("Type"));
        typeColumn.setWidth("120px");
        typeColumn.setAlign("center");
        typeColumn.setParent(columns);

        Column operationsColumn = new Column();
        operationsColumn.setLabel(_t("Operations"));
        operationsColumn.setWidth("90px");
        operationsColumn.setAlign("center");
        operationsColumn.setParent(columns);

        grid.setModel(new SimpleListModel<>(hoursGroupWrapper.getCriterionRequirementWrappersView()));
        grid.setRowRenderer(getCriterionRequirementInHoursGroupRowRenderer(hoursGroupWrapper));

        return panel;
    }

    public RowRenderer getHoursGroupsRowRenderer() {
        return (row, data, index) -> {
            final HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) data;
            row.setValue(hoursGroupWrapper);

            Detail detail = new Detail();
            detail.setOpen(true);
            detail.setParent(row);

            Separator topSeparator = new Separator();
            topSeparator.setBar(false);
            topSeparator.setSpacing("40px");
            topSeparator.setOrient("vertical");
            topSeparator.setParent(detail);

            buildCriterionRequirementsPanel(hoursGroupWrapper).setParent(detail);

            Separator bottomSeparator = new Separator();
            bottomSeparator.setBar(false);
            bottomSeparator.setSpacing("40px");
            bottomSeparator.setOrient("vertical");
            bottomSeparator.setParent(detail);

            final Textbox code = new Textbox(hoursGroupWrapper.getCode());
            code.setReadonly(isReadOnly());
            code.setDisabled(isCodeAutogenerated());
            code.addEventListener(Events.ON_CHANGE, event -> hoursGroupWrapper.setCode(code.getValue()));
            code.setParent(row);

            final Combobox comboboxType = new Combobox();
            comboboxType.setWidth("90px");
            comboboxType.setDisabled(isReadOnly());
            Comboitem selectedItem = null;
            for (ResourceEnum resourceEnum : getResourceTypes()) {
                Comboitem comboitem = new Comboitem(resourceEnum.toString());
                comboitem.setValue(resourceEnum);
                comboitem.setParent(comboboxType);
                if (resourceEnum.equals(hoursGroupWrapper.getResourceType())) {
                    selectedItem = comboitem;
                }
            }
            if (selectedItem != null) {
                comboboxType.setSelectedItem(selectedItem);
            }
            comboboxType.addEventListener(Events.ON_CHANGE, event -> {
                try {
                    selectResourceType(comboboxType);
                } catch (InterruptedException ignored) {}
            });
            comboboxType.setParent(row);

            final Intbox workingHours = new Intbox();
            workingHours.setValue(hoursGroupWrapper.getWorkingHours());
            workingHours.setReadonly(hoursGroupWrapper.isWorkingHoursReadOnly());
            workingHours.addEventListener(Events.ON_CHANGE, event -> {
                hoursGroupWrapper.setWorkingHours(workingHours.getValue());
                recalculateHoursGroup();
            });
            workingHours.setParent(row);

            final Decimalbox percentage = new Decimalbox();
            percentage.setScale(2);
            percentage.setValue(hoursGroupWrapper.getPercentage());
            percentage.setConstraint(getValidatePercentage());
            percentage.setReadonly(hoursGroupWrapper.isPercentageReadOnly());
            percentage.addEventListener(Events.ON_CHANGE, event -> recalculateHoursGroup());
            percentage.setParent(row);

            final Checkbox fixedPercentage = new Checkbox();
            fixedPercentage.setChecked(Boolean.TRUE.equals(hoursGroupWrapper.getFixedPercentage()));
            fixedPercentage.addEventListener(Events.ON_CHECK, event -> {
                hoursGroupWrapper.setFixedPercentage(fixedPercentage.isChecked());
                recalculateHoursGroup();
            });
            fixedPercentage.setParent(row);

            Hbox operations = new Hbox();
            operations.setStyle("margin-left: 35px");
            operations.setParent(row);

            final Button deleteHoursGroup = new Button();
            deleteHoursGroup.setSclass("icono");
            deleteHoursGroup.setImage("/common/img/ico_borrar1.png");
            deleteHoursGroup.setHoverImage("/common/img/ico_borrar.png");
            deleteHoursGroup.setTooltiptext(_t("Delete"));
            deleteHoursGroup.addEventListener(Events.ON_CLICK, event -> {
                try {
                    deleteHoursGroups(deleteHoursGroup);
                } catch (InterruptedException ignored) {}
            });
            deleteHoursGroup.setParent(operations);

            final Button addCriterion = new Button();
            addCriterion.setLabel(_t("Add Criterion"));
            addCriterion.setSclass("add-button");
            addCriterion.addEventListener(Events.ON_CLICK, event -> addCriterionToHoursGroup(addCriterion));
            addCriterion.setParent(operations);

            final Button addException = new Button();
            addException.setLabel(_t("Add Exception"));
            addException.setSclass("add-button");
            addException.setDisabled(hoursGroupWrapper.isDontExistValidCriterions());
            addException.addEventListener(Events.ON_CLICK, event -> addExceptionToHoursGroups(addException));
            addException.setParent(operations);
        };
    }

}
