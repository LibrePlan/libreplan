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

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.api.Listhead;

/**
 * Controller for {@link OrderElement} view of {@link Order} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class OrderElementController extends GenericForwardComposer {

    /**
     * {@link IOrderElementModel} with the data needed for this controller
     */
    private IOrderElementModel model;

    /**
     * {@link Window} where {@link OrderElement} edition form is showed
     */
    private Window window;

    /**
     * {@link Listitem} for every {@link HoursGroup}
     */
    private HoursGroupListitemRender renderer = new HoursGroupListitemRender();

    /**
     * {@link Listbox} where {@link HoursGroup} are shown
     */
    private Listbox hoursGroupsListbox;

    /**
     * List of selected {@link CriterionType} just used in the controller
     */
    private Set<CriterionType> selectedCriterionTypes = new LinkedHashSet<CriterionType>();

    private AsignedHoursToOrderElementController asignedHoursController;

    private ManageOrderElementAdvancesController manageOrderElementAdvancesController;

    private AssignedLabelsToOrderElementController assignedLabelsController;

    public OrderElement getOrderElement() {
        if (model == null) {
            return OrderLine.create();
        }

        return model.getOrderElement();
    }

    public HoursGroupListitemRender getRenderer() {
        return renderer;
    }

    /**
     * Returns a {@link List} of {@link HoursGroup}.
     *
     * If the current element is an {@link OrderLine} this method just returns
     * the {@link HoursGroup} of this {@link OrderLine}.
     *
     * Otherwise, this method gets all the {@link HoursGroup} of all the
     * children {@link OrderElement}, and aggregates them if they have the same
     * {@link Criterion}.
     *
     * @return The {@link HoursGroup} list of the current {@link OrderElement}
     */
    public List<HoursGroup> getHoursGroups() {
        if (model == null) {
            return new ArrayList<HoursGroup>();
        }

        // If it's an OrderLine
        if (model.getOrderElement() instanceof OrderLine) {
            return model.getOrderElement().getHoursGroups();
        } else {
            // If it's an OrderLineGroup
            Set<CriterionType> criterionTypes = getSelectedCriterionTypes();

            // If there isn't any CriterionType selected
            if (criterionTypes.isEmpty()) {
                return model.getOrderElement().getHoursGroups();
            }

            // Creates a map in order to join HoursGroup with the same
            // Criterions.
            // Map key will be an String with the Criterions separated by ;
            Map<String, HoursGroup> map = new HashMap<String, HoursGroup>();

            List<HoursGroup> hoursGroups = model.getOrderElement().getHoursGroups();

            for (HoursGroup hoursGroup : hoursGroups) {
                String key = "";
                for (CriterionType criterionType : criterionTypes) {
                    Criterion criterion = hoursGroup
                            .getCriterionByType(criterionType);
                    if (criterion != null) {
                        key += criterion.getName() + ";";
                    } else {
                        key += ";";
                    }
                }

                HoursGroup hoursGroupAggregation = map.get(key);
                if (hoursGroupAggregation == null) {
                    // This is not a real HoursGroup element, it's just an
                    // aggregation that join HoursGroup with the same Criterions
                    hoursGroupAggregation = new HoursGroup();
                    hoursGroupAggregation.setWorkingHours(hoursGroup.getWorkingHours());
                    hoursGroupAggregation.setCriterions(hoursGroup
                            .getCriterions());
                } else {
                    Integer newHours = hoursGroupAggregation.getWorkingHours() + hoursGroup.getWorkingHours();
                    hoursGroupAggregation.setWorkingHours(newHours);
                }

                map.put(key, hoursGroupAggregation);
            }

            return new ArrayList<HoursGroup>(map.values());
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderElementController", this, true);
        window = (Window) comp;
        setupAsignedHoursToOrderElementController(comp);
        setupManageOrderElementAdvancesController(comp);
        setupAssignedLabelsToOrderElementController(comp);
    }


    private void setupAsignedHoursToOrderElementController(Component comp)throws Exception{
        asignedHoursController = new AsignedHoursToOrderElementController();
        asignedHoursController.doAfterCompose(comp);
    }

    private void setupAssignedLabelsToOrderElementController(Component comp)
            throws Exception {
        assignedLabelsController = new AssignedLabelsToOrderElementController();
        assignedLabelsController.doAfterCompose(comp);
    }

    private void setupManageOrderElementAdvancesController(Component comp)
            throws Exception {
        manageOrderElementAdvancesController = new ManageOrderElementAdvancesController();
        manageOrderElementAdvancesController.doAfterCompose(comp);
    }

    /**
     * Open the window to edit a {@link OrderElement}. If it's a
     * {@link OrderLineGroup} less fields will be enabled.
     * @param orderElement
     *            The {@link OrderElement} to be edited
     */
    public void openWindow(IOrderElementModel model){

        this.model = model;

        final OrderElement orderElement = model.getOrderElement();

        asignedHoursController.openWindow(model);
        manageOrderElementAdvancesController.openWindow(model);
        assignedLabelsController.openWindow(model);

        // If is a container
        if (orderElement instanceof OrderLineGroup) {
            // Disable fields just used in the OrderLine
            ((Intbox) window.getFellow("totalHours")).setDisabled(true);

            // Hide not needed buttons
            window.getFellow("manageCriterions").setVisible(false);
            window.getFellow("addHoursGroup").setVisible(false);
            window.getFellow("deleteHoursGroup").setVisible(false);
        } else {
            // Enable fields just used in the OrderLine
            ((Intbox) window.getFellow("totalHours")).setDisabled(false);

            // Show needed buttons
            window.getFellow("manageCriterions").setVisible(true);
            window.getFellow("addHoursGroup").setVisible(true);
            window.getFellow("deleteHoursGroup").setVisible(true);

            // Add EventListener to reload the window when the value change
            window.getFellow("totalHours").addEventListener(Events.ON_CHANGE,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            Util.reloadBindings(window);
                        }
                    });
            ((Intbox) window.getFellow("totalHours"))
                    .setConstraint(new Constraint() {

                        @Override
                        public void validate(Component comp, Object value)
                                throws WrongValueException {
                            if (!((OrderLine) orderElement)
                                    .isTotalHoursValid((Integer) value)) {
                                throw new WrongValueException(comp,
                                        _("Value is not valid, taking into account the current list of HoursGroup"));
                            }
                        }
                    });
        }

        // selectCriterions Vbox is always hidden
        reloadSelectedCriterionTypes();
        window.getFellow("selectCriterions").setVisible(false);

        window.getFellow("hoursGroupsListbox").invalidate();

        Util.reloadBindings(window);
        generateListhead();

        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        reloadSelectedCriterionTypes();
    }

    /**
     * Just close the {@link Window} and refresh parent status. Save actions are
     * managed by "save-when" at .zul file.
     */
    public void back() {
        if (!getOrderElement().checkAtLeastOneHoursGroup()) {
            throw new WrongValueException(window
                    .getFellow("hoursGroupsListbox"),
                    _("At least one HoursGroup is needed"));
        }

        for (CriterionType criterionType : getCriterionTypes()) {
            removeCriterionsFromHoursGroup(criterionType);
        }

        Clients.closeErrorBox(window.getFellow("hoursGroupsListbox"));
        window.setVisible(false);
        Util.reloadBindings(window.getParent());
    }

    /**
     * Adds a new {@link HoursGroup} to the current {@link OrderElement}
     *
     * The {@link OrderElement} should be a {@link OrderLine}
     */
    public void addHoursGroup() {
        OrderLine orderLine = (OrderLine) getOrderElement();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);

        orderLine.addHoursGroup(hoursGroup);

        Util.reloadBindings(window);
    }

    /**
     * Deletes the selected {@link HoursGroup} for the current
     * {@link OrderElement}
     *
     * The {@link OrderElement} should be a {@link OrderLine}
     */
    public void deleteHoursGroups() {
        Set<Listitem> selectedItems = hoursGroupsListbox.getSelectedItems();

        OrderElement orderElement = getOrderElement();

        for (Listitem item : selectedItems) {
            ((OrderLine) orderElement).deleteHoursGroup((HoursGroup) item
                    .getValue());
        }

        Util.reloadBindings(window);
    }

    /**
     * Toggle visibility of the selectCriterions {@link Vbox}
     */
    public void manageCriterions() {
        Component selectCriterions = window.getFellow("selectCriterions");

        if (selectCriterions.isVisible()) {
            selectCriterions.setVisible(false);
        } else {
            reloadSelectedCriterionTypes();
            selectCriterions.setVisible(true);
        }
        Util.reloadBindings(selectCriterions);
    }

    /**
     * Gets the set of possible {@link CriterionType}.
     *
     * @return A {@link Set} of {@link CriterionType}
     */
    public Set<CriterionType> getCriterionTypes() {
        if (model == null) {
            return new HashSet<CriterionType>();
        }

        List<CriterionType> list = model.getCriterionTypes();
        list.removeAll(getSelectedCriterionTypes());
        return new HashSet<CriterionType>(list);
    }

    /**
     * Returns the selected {@link CriterionType}.
     *
     * @return A {@link List} of {@link CriterionType}
     */
    public Set<CriterionType> getSelectedCriterionTypes() {
        return new HashSet<CriterionType>(selectedCriterionTypes);
    }

    public void setSelectedCriterionTypes(
            Set<CriterionType> selectedCriterionTypes) {
        this.selectedCriterionTypes = selectedCriterionTypes;
        Util.reloadBindings(window);
        generateListhead();
    }

    /**
     * Reloads the selected {@link CriterionType}, depending on the
     * {@link Criterion} related with the {@link HoursGroup}
     */
    private void reloadSelectedCriterionTypes() {
        OrderElement orderElement = getOrderElement();

        if (orderElement == null) {
            selectedCriterionTypes = new LinkedHashSet<CriterionType>();
        } else {
            Set<CriterionType> criterionTypes = new LinkedHashSet<CriterionType>();

            for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
                Set<Criterion> criterions = model
                        .getCriterionsHoursGroup(hoursGroup);
                for (Criterion criterion : criterions) {
                    CriterionType type = model.getCriterionType(criterion);
                    CriterionType criterionTypeByName = model
                            .getCriterionTypeByName(type.getName());
                    criterionTypes.add(criterionTypeByName);
                }
            }

            selectedCriterionTypes = criterionTypes;
        }
    }

    /**
     * Removes the {@link Criterion} which matches with this type for all the
     * {@link HoursGroup}
     *
     * @param type
     *            The type of the {@link Criterion} that should be removed
     */
    private void removeCriterionsFromHoursGroup(CriterionType type) {
        OrderElement orderElement = getOrderElement();
        for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
            hoursGroup.removeCriterionByType(type);
        }
    }

    /**
     * Represents every {@link HoursGroup} with an edition form if needed
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    public class HoursGroupListitemRender implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final HoursGroup hoursGroup = (HoursGroup) data;

            hoursGroup.getCriterions();

            item.setValue(hoursGroup);

            Listcell cellWorkingHours = new Listcell();
            cellWorkingHours.setParent(item);
            Listcell cellPercentage = new Listcell();
            cellPercentage.setParent(item);

            final Decimalbox decimalBox = new Decimalbox();
            decimalBox.setScale(2);

            // If is a container
            if (getOrderElement() instanceof OrderLineGroup) {
                // Just getters are needed

                // Working hours
                cellWorkingHours.appendChild(Util.bind(new Intbox(),
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return hoursGroup.getWorkingHours();
                            }
                        }));

                // Percentage
                cellPercentage.appendChild(Util.bind(decimalBox,
                        new Util.Getter<BigDecimal>() {

                            @Override
                            public BigDecimal get() {
                                BigDecimal workingHours = new BigDecimal(hoursGroup
                                        .getWorkingHours()).setScale(2);
                                BigDecimal total = new BigDecimal(model
                                        .getOrderElement()
                                                .getWorkHours()).setScale(2);
                                if (total.equals(new BigDecimal(0).setScale(2))) {
                                    return new BigDecimal(0).setScale(2);
                                }
                                return workingHours.divide(total,
                                        BigDecimal.ROUND_DOWN).scaleByPowerOfTen(2);
                            }
                        }));

                // For each CriterionType selected
                for (CriterionType criterionType : getSelectedCriterionTypes()) {
                    Listcell cellCriterion = new Listcell();
                    cellCriterion.setParent(item);

                    // Add a new Listbox for each CriterionType
                    final Listbox criterionListbox = new Listbox();
                    criterionListbox.setRows(1);
                    criterionListbox.setMold("select");
                    criterionListbox.setDisabled(true);

                    // Add an empty option to remove a Criterion
                    Listitem emptyListitem = new Listitem();
                    emptyListitem.setParent(criterionListbox);

                    // Get the Criterion of the current type in the HoursGroup
                    final Criterion criterionHoursGroup = hoursGroup
                            .getCriterionByType(criterionType);

                    // For each possible Criterion of the current type
                    for (Criterion criterion : model
                            .getCriterionsFor(criterionType)) {
                        // Add the Criterion option
                        Listitem listitem = new Listitem();
                        listitem.setValue(criterion);
                        listitem.setLabel(criterion.getName());
                        listitem.setParent(criterionListbox);

                        // Check if it matches with the HoursGroup criterion
                        if ((criterionHoursGroup != null)
                                && (criterionHoursGroup.getName()
                                        .equals(criterion.getName()))) {
                            // Mark as selected
                            criterionListbox.setSelectedItem(listitem);
                        }
                    }

                    cellCriterion.appendChild(criterionListbox);
                }

            } else { // If is a leaf

                Intbox workingHours = Util.bind(new Intbox(),
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return hoursGroup.getWorkingHours();
                            }
                        }, new Util.Setter<Integer>() {

                            @Override
                            public void set(Integer value) {
                                hoursGroup.setWorkingHours(value);
                            }
                        });

                // Add EventListener to reload the window when the value change
                workingHours.addEventListener(Events.ON_CHANGE,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                ((OrderLine) getOrderElement())
                                        .recalculateHoursGroups();
                                Util.reloadBindings(window);
                            }
                        });

                Decimalbox percentage = Util.bind(decimalBox,
                        new Util.Getter<BigDecimal>() {

                            @Override
                            public BigDecimal get() {
                                return hoursGroup.getPercentage().scaleByPowerOfTen(2);
                            }
                        }, new Util.Setter<BigDecimal>() {

                            @Override
                            public void set(BigDecimal value) {
                                value = value.divide(new BigDecimal(100),BigDecimal.ROUND_DOWN);
                                try {
                                    hoursGroup.setPercentage(value);
                                } catch (IllegalArgumentException e) {
                                    throw new WrongValueException(decimalBox, e.getMessage());
                                }
                            }
                        });

                // Add EventListener to reload the window when the value change
                percentage.addEventListener(Events.ON_CHANGE,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                ((OrderLine) getOrderElement())
                                        .recalculateHoursGroups();
                                Util.reloadBindings(window);
                            }
                        });

                // Fixed percentage
                Listcell cellFixedPercentage = new Listcell();
                cellFixedPercentage.setParent(item);

                Checkbox fixedPercentage = Util.bind(new Checkbox(),
                        new Util.Getter<Boolean>() {

                            @Override
                            public Boolean get() {
                                return hoursGroup.isFixedPercentage();
                            }
                        }, new Util.Setter<Boolean>() {

                            @Override
                            public void set(Boolean value) {
                                hoursGroup.setFixedPercentage(value);
                            }
                        });
                fixedPercentage.addEventListener(Events.ON_CHECK,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                ((OrderLine) getOrderElement())
                                        .recalculateHoursGroups();
                                Util.reloadBindings(window);
                            }
                        });

                // Disable components depending on the policy
                disableComponents(workingHours, percentage,
                        fixedPercentage.isChecked());

                cellWorkingHours.appendChild(workingHours);
                cellPercentage.appendChild(percentage);
                cellFixedPercentage.appendChild(fixedPercentage);

                // For each CriterionType selected
                for (CriterionType criterionType : getSelectedCriterionTypes()) {
                    Listcell cellCriterion = new Listcell();
                    cellCriterion.setParent(item);

                    // Add a new Listbox for each CriterionType
                    final Listbox criterionListbox = new Listbox();
                    criterionListbox.setRows(1);
                    criterionListbox.setMold("select");

                    // Add an empty option to remove a Criterion
                    Listitem emptyListitem = new Listitem();
                    emptyListitem.setParent(criterionListbox);

                    // Get the Criterion of the current type in the HoursGroup
                    final Criterion criterionHoursGroup = hoursGroup
                            .getCriterionByType(criterionType);

                    // For each possible Criterion of the current type
                    for (Criterion criterion : model
                            .getCriterionsFor(criterionType)) {
                        // Add the Criterion option
                        Listitem listitem = new Listitem();
                        listitem.setValue(criterion);
                        listitem.setLabel(criterion.getName());
                        listitem.setParent(criterionListbox);

                        // Check if it matches with the HoursGroup criterion
                        if ((criterionHoursGroup != null)
                                && (criterionHoursGroup.getName()
                                        .equals(criterion.getName()))) {
                            // Mark as selected
                            criterionListbox.setSelectedItem(listitem);
                        }
                    }

                    // Add operation for Criterion selection
                    criterionListbox.addEventListener(Events.ON_SELECT,
                            new EventListener() {

                                @Override
                                public void onEvent(Event event)
                                        throws Exception {
                                    Criterion criterion = (Criterion) criterionListbox
                                            .getSelectedItem().getValue();
                                    if (criterion == null) {
                                        hoursGroup
                                                .removeCriterion(criterionHoursGroup);
                                    } else {
                                        hoursGroup.addCriterion(criterion);
                                    }
                                }
                            });

                    cellCriterion.appendChild(criterionListbox);
                }
            }
        }

        /**
         * Disable workingHours and percentage components depending on the
         * policy selected by the user.
         *
         * @param workingHours
         *            An {@link Intbox} for the workingHours
         * @param percentage
         *            A {@link Decimalbox} for the percentage
         * @param fixedPercentage
         *            If FIXED_PERCENTAGE policy is set or not
         */
        public void disableComponents(Intbox workingHours,
                Decimalbox percentage, Boolean fixedPercentage) {

            if (fixedPercentage) {
                // Working hours not editable
                workingHours.setDisabled(true);
                // Percentage editable
                percentage.setDisabled(false);
            } else {
                // Working hours editable
                workingHours.setDisabled(false);
                // Percentage not editable
                percentage.setDisabled(true);
            }
        }
    }

    /**
     * Generates the {@link Listhead} of the {@link HoursGroup} {@link Listbox}
     * depending on the {@link CriterionType} selected.
     */
    private void generateListhead() {
        Listhead listhead = hoursGroupsListbox.getListheadApi();

        // Remove the current header
        listhead.getChildren().clear();

        // Generate basi headers
        Listheader hours = new Listheader(_("Hours"));
        listhead.appendChild(hours);
        Listheader percentage = new Listheader(_("%"));
        listhead.appendChild(percentage);

        // If it's a leaf add Fixed percentage column
        if (getOrderElement() instanceof OrderLine) {
            Listheader headerFixedPercentage = new Listheader(
                    _("Fixed percentage"));
            listhead.appendChild(headerFixedPercentage);
        }

        // For each CriterionType selected
        for (CriterionType criterionType : getSelectedCriterionTypes()) {
            // Add a new column on the HoursGroup table
            Listheader headerCriterion = new Listheader();
            headerCriterion.setLabel(criterionType.getName());
            listhead.appendChild(headerCriterion);
        }
    }

}
