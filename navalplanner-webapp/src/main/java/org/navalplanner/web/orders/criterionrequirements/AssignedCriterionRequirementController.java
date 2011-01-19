/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.orders.criterionrequirements;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewDataSortableGrid;
import org.navalplanner.web.orders.CriterionRequirementWrapper;
import org.navalplanner.web.orders.HoursGroupWrapper;
import org.navalplanner.web.orders.OrderCRUDController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public abstract class AssignedCriterionRequirementController<T, M> extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Listbox hoursGroupsInOrderLineGroup;

    private List<ResourceEnum> listResourceTypes = new ArrayList<ResourceEnum>();

    private NewDataSortableGrid listingRequirements;

    protected NewDataSortableGrid listHoursGroups;

    protected Intbox orderElementTotalHours;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("assignedCriterionRequirementController", this, true);

        // init the resorcesType
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
        CriterionRequirementWrapper invalidWrapper = validateWrappers(criterionRequirementWrappers());
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

    public abstract List<CriterionRequirementWrapper> criterionRequirementWrappers();

    public abstract List<CriterionWithItsType> getCriterionWithItsTypes();

    public List<CriterionWithItsType> getCriterionWithItsTypesWorker() {
        List<CriterionWithItsType> result = new ArrayList<CriterionWithItsType>();
        for (CriterionWithItsType criterionAndType : getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource()
                    .equals(ResourceEnum.MACHINE)) {
                result.add(criterionAndType);
            }
        }
        return result;
    }

    public List<CriterionWithItsType> getCriterionWithItsTypesMachine() {
        List<CriterionWithItsType> result = new ArrayList<CriterionWithItsType>();
        for (CriterionWithItsType criterionAndType : getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource()
                    .equals(ResourceEnum.WORKER)) {
                result.add(criterionAndType);
            }
        }
        return result;
    }

    public List<ResourceEnum> getResourceTypes() {
        return listResourceTypes;
    }

    public abstract void addCriterionRequirementWrapper();

    public abstract void remove(CriterionRequirementWrapper requirement);

    public abstract void invalidate(CriterionRequirementWrapper requirement);

    public abstract void validate(CriterionRequirementWrapper requirement);

    protected abstract void changeCriterionAndType(
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType newCriterionAndType);

    public void selectCriterionAndType(Listitem item, Bandbox bandbox,
            CriterionRequirementWrapper requirementWrapper) {
        if (item != null) {
            CriterionWithItsType newCriterionAndType = (CriterionWithItsType) item
                    .getValue();
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
        List<CriterionWithItsType> list = new ArrayList<CriterionWithItsType>();
        text = text.trim().toLowerCase();
        for (CriterionWithItsType criterion : this.getCriterionWithItsTypes()) {
            if ((criterion.getNameHierarchy().toLowerCase().contains(text) || criterion
                    .getType().getName().toLowerCase().contains(text))) {
                list.add(criterion);
            }
        }
        return new SimpleListModel(list);
    }

    protected abstract void updateCriterionsWithDiferentResourceType(
            HoursGroupWrapper hoursGroupWrapper);

    public void selectResourceType(Combobox combobox)
            throws InterruptedException {
        HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) ((Row) combobox
                .getParent()).getValue();

        if (combobox.getSelectedItem() != null) {
            try {
                int status = Messagebox
                    .show(
                            _("Are you sure of changing the resource type? You will lose the criterions with different resource type."),
                            "Question", Messagebox.OK | Messagebox.CANCEL,
                            Messagebox.QUESTION);

                if (Messagebox.OK == status) {
                    ResourceEnum resource = (ResourceEnum) combobox
                        .getSelectedItem().getValue();
                    hoursGroupWrapper.assignResourceType(resource);
                    updateCriterionsWithDiferentResourceType(hoursGroupWrapper);
                }
            } catch (InterruptedException e) {
                messagesForUser.showMessage(Level.ERROR, e.getMessage());
                LOG.error(_("Error on showing removing element: ",
                        hoursGroupWrapper.getHoursGroup().getId()), e);
            }
        }
        Util.reloadBindings(listHoursGroups);
    }

    public void reload() {
        Util.reloadBindings(listingRequirements);
        Util.reloadBindings(orderElementTotalHours);
        if (isReadOnly()) {
            Util.reloadBindings(hoursGroupsInOrderLineGroup);
        } else {
            Util.reloadBindings(listHoursGroups);
        }
    }

    protected abstract CriterionRequirementWrapper validateWrappers(
            List<CriterionRequirementWrapper> list);

    protected abstract CriterionRequirementWrapper validateHoursGroupWrappers();

    // Show invalid values inside listhoursGroup.
    private void showInvalidValuesInHoursGroups(
            CriterionRequirementWrapper requirementWrapper) {
        if (listHoursGroups != null) {
            List<Row> listRowsHoursGroup = (List<Row>) ((Rows) listHoursGroups
                    .getRows()).getChildren();
            for (Row row : listRowsHoursGroup) {
                Rows listRequirementRows = getRequirementRows(row);
                Row requirementRow = findRowOfCriterionRequirementWrapper(
                        listRequirementRows, requirementWrapper);
                showInvalidValue(requirementRow, requirementWrapper);
            }
        }
    }

    /**
     * Validates {@link CriterionRequirementWrapper} data constraints
     *
     * @param invalidValue
     */
    private void showInvalidValues(
            CriterionRequirementWrapper requirementWrapper) {
        if (listingRequirements != null) {
            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionRequirementWrapper(listingRequirements
                    .getRows(), requirementWrapper);
            showInvalidValue(row, requirementWrapper);
        }
    }

    private void showInvalidValue(Row row,
            CriterionRequirementWrapper requirementWrapper) {
        if (row != null) {
            Bandbox bandType = getBandType(requirementWrapper, row);
            bandType.setValue(null);
            throw new WrongValueException(bandType,
                    _("The criterion and its type cannot be null"));
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in rows
     *
     * @param Rows
     * @param CriterionRequirementWrapper
     * @return
     */

    private Row findRowOfCriterionRequirementWrapper(Rows rows,
            CriterionRequirementWrapper requirementWrapper) {
        final List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (requirementWrapper.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    /**
     * Locates {@link Bandbox} criterion requirement in {@link row}
     *
     * @param row
     * @return Bandbox
     */
    private Bandbox getBandType(CriterionRequirementWrapper wrapper, Row row) {
        if (wrapper.isNewDirectAndItsHoursGroupIsMachine()) {
            return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren()
                    .get(1);
        }
        if (wrapper.isNewException()) {
            return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren()
                    .get(2);
        }
        return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren().get(0);
    }

    private Rows getRequirementRows(Row row) {
        Panel panel = (Panel) row.getFirstChild().getFirstChild();
        NewDataSortableGrid grid = (NewDataSortableGrid) panel.getFirstChild()
                .getFirstChild();
        return grid.getRows();
    }

    private HoursGroupWrapper getHoursGroupOfRequirementWrapper(
            Row rowRequirement) {
        NewDataSortableGrid grid = (NewDataSortableGrid) rowRequirement
                .getParent().getParent();
        Panel panel = (Panel) grid.getParent().getParent();
        return (HoursGroupWrapper) ((Row) panel.getParent().getParent())
                .getValue();
    }

    /*
     * Operations to manage OrderElement's hoursGroups and to assign criterion
     * requirements to this hoursGroups.
     */

    public boolean isReadOnly() {
        return !isEditableHoursGroup();
    }

    public abstract boolean isEditableHoursGroup();

    public abstract List<HoursGroupWrapper> getHoursGroupWrappers();

    /**
     * Adds a new {@link HoursGroup} to the current {@link OrderElement} The
     * {@link OrderElement} should be a {@link OrderLine}
     */
    public abstract void addHoursGroup();

    /**
     * Deletes the selected {@link HoursGroup} for the current
     * {@link OrderElement} The {@link OrderElement} should be a
     * {@link OrderLine}
     */
    public void deleteHoursGroups(Component self) throws InterruptedException {
        if (getHoursGroupWrappers().size() < 2) {
            Messagebox.show(_("At least one HoursGroup is needed"), _("Error"),
                    Messagebox.OK, Messagebox.ERROR);
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
        return ((HoursGroupWrapper) (((Row) (self.getParent().getParent()))
                .getValue()));
    }

    public void addCriterionToHoursGroup(Component self) {
        final HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);
        if (hoursGroupWrapper != null) {
            addCriterionToHoursGroupWrapper(hoursGroupWrapper);
            repaint(self, hoursGroupWrapper);
        }
    }

    protected abstract void addCriterionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper);

    public void addExceptionToHoursGroups(Component self) {
        final HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);
        if (hoursGroupWrapper != null) {
            addExceptionToHoursGroupWrapper(hoursGroupWrapper);
            repaint(self, hoursGroupWrapper);
        }
    }

    protected abstract CriterionRequirementWrapper addExceptionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper);

    public void removeCriterionToHoursGroup(Component self) {
        try {
            Row row = (Row) self.getParent().getParent();
            CriterionRequirementWrapper requirementWrapper = (CriterionRequirementWrapper) row
                    .getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);
            deleteCriterionToHoursGroup(hoursGroupWrapper, requirementWrapper);
            repaint(self, hoursGroupWrapper);
        } catch (Exception e) {

        }
    }

    public abstract void deleteCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper);

    public void selectCriterionToHoursGroup(Listitem item, Bandbox bandbox,
            CriterionRequirementWrapper requirementWrapper) {

        bandbox.close();
        Listbox listbox = (Listbox) bandbox.getFirstChild().getFirstChild();
        listbox.setModel(new SimpleListModel(getCriterionWithItsTypes()));

        if (item != null) {

            Row row = (Row) bandbox.getParent().getParent();
            CriterionWithItsType criterionAndType = (CriterionWithItsType) item
                    .getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);
            bandbox.setValue(criterionAndType.getNameAndType());

            try {
                selectCriterionToHoursGroup(hoursGroupWrapper,
                        requirementWrapper, criterionAndType);
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
        throw new WrongValueException(bandbox, _(e.getMessage()));
    }

    /*
     * Operations to manage the data hoursGroup, for example validate the
     * percentage and its number of hours or set the fixed percentage
     */

    public void changeTotalHours() {
        recalculateHoursGroup();
        Util.reloadBindings(listHoursGroups);
        Util.reloadBindings(orderElementTotalHours);
    }

    public abstract void recalculateHoursGroup();

    public Constraint validateTotalHours() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (value == null) {
                    value = new Integer(0);
                    orderElementTotalHours.setValue((Integer) value);
                }
                try {
                    if (getElement() instanceof OrderLine) {
                        ((OrderLine) getElement())
                                .setWorkHours((Integer) value);
                    }
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    public Constraint validatePercentage() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) ((Row) comp
                        .getParent()).getValue();
                try {
                    hoursGroupWrapper.setPercentage((BigDecimal) value);
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    /* Operations to return grouped list of hours Group */

    /**
     * Returns a {@link List} of {@link HoursGroup}. If the current element is
     * an {@link OrderLine} this method just returns the {@link HoursGroup} of
     * this {@link OrderLine}. Otherwise, this method gets all the
     * {@link HoursGroup} of all the children {@link OrderElement}, and
     * aggregates them if they have the same {@link Criterion}.
     *
     * @return The {@link HoursGroup} list of the current {@link OrderElement}
     */
    public List<HoursGroup> getHoursGroups() {
        // Creates a map in order to join HoursGroup with the same Criterions
        Map<Map<ResourceEnum, Set<Criterion>>, HoursGroup> map = new HashMap<Map<ResourceEnum, Set<Criterion>>, HoursGroup>();

        List<HoursGroup> hoursGroups = getHoursGroups(getElement());
        for (HoursGroup hoursGroup : hoursGroups) {
            Map<ResourceEnum, Set<Criterion>> key = getKeyFor(hoursGroup);

            HoursGroup hoursGroupAggregation = map.get(key);
            if (hoursGroupAggregation == null) {
                // This is not a real HoursGroup element, it's just an
                // aggregation that join HoursGroup with the same Criterions
                hoursGroupAggregation = new HoursGroup();
                hoursGroupAggregation.setWorkingHours(hoursGroup
                        .getWorkingHours());
                hoursGroupAggregation.setCriterionRequirements(hoursGroup
                        .getCriterionRequirements());
                hoursGroupAggregation.setResourceType(hoursGroup
                        .getResourceType());
            } else {
                Integer newHours = hoursGroupAggregation.getWorkingHours()
                        + hoursGroup.getWorkingHours();
                hoursGroupAggregation.setWorkingHours(newHours);
            }

            map.put(key, hoursGroupAggregation);
        }
        return new ArrayList<HoursGroup>(map.values());
    }

    protected abstract List<HoursGroup> getHoursGroups(T orderElement);

    private Map<ResourceEnum, Set<Criterion>> getKeyFor(HoursGroup hoursGroup) {
        Map<ResourceEnum, Set<Criterion>> keys = new HashMap<ResourceEnum, Set<Criterion>>();
        ResourceEnum resourceType = hoursGroup.getResourceType();
        Set<Criterion> criterions = getKeyCriterionsFor(hoursGroup);
        keys.put(resourceType, criterions);
        return keys;
    }

    private Set<Criterion> getKeyCriterionsFor(HoursGroup hoursGroup) {
        Set<Criterion> key = new HashSet<Criterion>();
        for (Criterion criterion : hoursGroup.getValidCriterions()) {
            if (criterion != null) {
                key.add(criterion);
            }
        }
        return key;
    }

    public abstract boolean isCodeAutogenerated();

    private transient ListitemRenderer renderer = new HoursGroupListitemRender();

    public ListitemRenderer getRenderer() {
        return renderer;
    }

    public class HoursGroupListitemRender implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final HoursGroup hoursGroup = (HoursGroup) data;

            // Criterion Requirements hours Group
            Listcell cellCriterionRequirements = new Listcell();
            cellCriterionRequirements.setParent(item);
            cellCriterionRequirements
                    .appendChild(appendRequirements(hoursGroup));

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
                if (!label.equals("")) {
                    label = label.concat(", ");
                }
                label = label.concat(criterion.getName());
            }
            if (!label.equals("")) {
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
            workingHoursLabel.setValue(String.valueOf(hoursGroup
                    .getWorkingHours()));
            return workingHoursLabel;
        }

    }

    public void onOK(KeyEvent event) {
        Component listitem = event.getReference();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent()
                    .getParent();
            CriterionRequirementWrapper criterionRequirementWrapper = (CriterionRequirementWrapper) ((Row) bandbox
                    .getParent().getParent()).getValue();

            selectCriterionAndType((Listitem) listitem, bandbox,
                    criterionRequirementWrapper);

            bandbox.close();
        }
    }

    public void onClick(MouseEvent event) {
        Component listitem = event.getTarget();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent()
                    .getParent();
            bandbox.close();
        }
    }

    private void repaint(Component self, HoursGroupWrapper hoursGroupWrapper) {
        Grid grid = getHoursGroupDetailsGrid(self);
        if (grid != null) {
            grid.setModel(new SimpleListModel(hoursGroupWrapper
                    .getCriterionRequirementWrappersView().toArray()));
            grid.invalidate();
        } else {
            Util.reloadBindings(listHoursGroups);
        }
    }

    private Grid getHoursGroupDetailsGrid(Component self) {
        try {
            Detail detail = ((Detail) ((Row) self.getParent().getParent())
                    .getFirstChild());
            Panel panel = (Panel) detail.getFirstChild();
            return (Grid) panel.getFirstChild().getFirstChild();
        } catch (Exception e) {
            return null;
        }
    }

}
