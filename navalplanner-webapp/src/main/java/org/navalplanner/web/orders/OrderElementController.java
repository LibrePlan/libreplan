package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.HoursGroup.HoursPolicies;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Popup;
import org.zkoss.zul.api.Listhead;

/**
 * Controller for {@link OrderElement} view of {@link Order} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementController extends GenericForwardComposer {

    private IOrderElementModel model;

    /**
     * {@link Popup} where {@link OrderElement} edition form is showed
     */
    private Popup popup;

    /**
     * Model of the {@link HoursGroup} list
     */
    private List<HoursGroup> hoursGroupsModel;

    /**
     * {@link Listitem} for every {@link HoursGroup}
     */
    private HoursGroupListitemRender renderer = new HoursGroupListitemRender();

    /**
     * {@link Listbox} where {@link HoursGroup} are shown
     */
    private Listbox hoursGroupsListbox;

    private Set<ICriterionType<?>> selectedCriterionTypes = new HashSet<ICriterionType<?>>();

    public OrderElement getOrderElement() {
        if (model == null) {
            return new OrderLine();
        }

        return model.getOrderElement();
    }

    public List<HoursGroup> getHoursGroupsModel() {
        return hoursGroupsModel;
    }

    public HoursGroupListitemRender getRenderer() {
        return renderer;
    }


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderElementController", this, true);

        popup = (Popup) comp;
    }

    /**
     * Open the popup to edit a {@link OrderElement}. If it's a
     * {@link OrderLineGroup} less fields will be enabled.
     *
     * @param orderElement
     *            The {@link OrderElement} to be edited
     */
    public void openPopup(IOrderElementModel model) {

        this.model = model;

        final OrderElement orderElement = model.getOrderElement();

        this.hoursGroupsModel = orderElement.getHoursGroups();

        // If is a container
        if (orderElement instanceof OrderLineGroup) {
            // Disable fields just used in the OrderLine
            ((Intbox) popup.getFellow("totalHours")).setDisabled(true);

            // Hide not needed buttons
            popup.getFellow("manageCriterions").setVisible(false);
            popup.getFellow("addHoursGroup").setVisible(false);
            popup.getFellow("deleteHoursGroup").setVisible(false);
        } else {
            // Enable fields just used in the OrderLine
            ((Intbox) popup.getFellow("totalHours")).setDisabled(false);

            // Show needed buttons
            popup.getFellow("manageCriterions").setVisible(true);
            popup.getFellow("addHoursGroup").setVisible(true);
            popup.getFellow("deleteHoursGroup").setVisible(true);

            // Add EventListener to reload the popup when the value change
            popup.getFellow("totalHours").addEventListener(Events.ON_CHANGE,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            Util.reloadBindings(popup);
                        }
                    });
            ((Intbox) popup.getFellow("totalHours"))
                    .setConstraint(new Constraint() {

                        @Override
                        public void validate(Component comp, Object value)
                                throws WrongValueException {
                            if (!((OrderLine) orderElement)
                                    .isTotalHoursValid((Integer) value)) {
                                throw new WrongValueException(comp,
                                        "Value is not valid, taking into account "
                                                + "the current list of HoursGroup");
                            }
                        }
                    });
        }

        Util.reloadBindings(popup);

        popup.open(popup.getParent(), "start-after");

        reloadSelectedCriterionTypes();
    }

    /**
     * Just close the {@link Popup}
     */
    public void cancel() {
        popup.close();
    }

    /**
     * Just close the {@link Popup} and refresh parent status. Save actions are
     * managed by "save-when" at .zul file.
     */
    public void save() {
        popup.close();
        Util.reloadBindings(popup.getParent());
    }

    /**
     * Adds a new {@link HoursGroup} to the current {@link OrderElement}
     *
     * The {@link OrderElement} should be a {@link OrderLine}
     */
    public void addHoursGroup() {
        HoursGroup hoursGroup = new HoursGroup();

        OrderElement orderElement = getOrderElement();

        ((OrderLine) orderElement).addHoursGroup(hoursGroup);

        this.hoursGroupsModel = orderElement.getHoursGroups();
        Util.reloadBindings(popup);
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

        this.hoursGroupsModel = orderElement.getHoursGroups();
        Util.reloadBindings(popup);
    }

    public void manageCriterions() {
        Component selectCriterions = popup.getFellow("selectCriterions");

        if (selectCriterions.isVisible()) {
            selectCriterions.setVisible(false);
        } else {
            reloadSelectedCriterionTypes();
            selectCriterions.setVisible(true);
        }
        Util.reloadBindings(selectCriterions);
    }

    public List<ICriterionType<?>> getCriterionTypes() {
        if (model == null) {
            return new ArrayList<ICriterionType<?>>();
        }

        List<ICriterionType<?>> list = model.getCriterionTypes();
        list.removeAll(getSelectedCriterionTypes());
        return list;
    }

    public Set<ICriterionType<?>> getSelectedCriterionTypes() {
        return selectedCriterionTypes;
    }

    public void reloadSelectedCriterionTypes() {
        OrderElement orderElement = getOrderElement();

        if (orderElement == null) {
            selectedCriterionTypes = new HashSet<ICriterionType<?>>();
        } else {
            Set<ICriterionType<?>> criterionTypes = new HashSet<ICriterionType<?>>();

            for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
                Set<Criterion> criterions = hoursGroup.getCriterions();
                for (Criterion criterion : criterions) {
                    String type = criterion.getType();
                    criterionTypes.add(model.getCriterionTypeByName(type));
                }
            }

            selectedCriterionTypes = criterionTypes;
        }
    }

    public void assignCriterions(Set<Listitem> selectedItems) {
        for (Listitem listitem : selectedItems) {
            ICriterionType<?> value = (ICriterionType<?>) listitem.getValue();
            selectedCriterionTypes.add(value);
        }
        Util.reloadBindings(popup);
    }

    public void unassignCriterions(Set<Listitem> selectedItems) {
        for (Listitem listitem : selectedItems) {
            ICriterionType<?> value = (ICriterionType<?>) listitem.getValue();
            selectedCriterionTypes.remove(value);
            removeCriterionsFromHoursGroup(value);
        }
        Util.reloadBindings(popup);
    }

    private void removeCriterionsFromHoursGroup(ICriterionType<?> type) {
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
            Listcell cellHoursPolicy = new Listcell();
            cellHoursPolicy.setParent(item);

            // Generate hours policy Listbox
            final Listbox hoursPolicyListBox = new Listbox();
            hoursPolicyListBox.setRows(1);
            hoursPolicyListBox.setMold("select");

            for (HoursPolicies hourPolicy : HoursPolicies.values()) {
                Listitem listitem = new Listitem();
                listitem.setValue(hourPolicy);
                listitem.setLabel(hourPolicy.toString());
                listitem.setParent(hoursPolicyListBox);
            }

            Decimalbox decimalBox = new Decimalbox();
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
                                return hoursGroup.getPercentage();
                            }
                        }));

                // Hours policy
                hoursPolicyListBox.setSelectedIndex(hoursGroup.getHoursPolicy()
                        .ordinal());
                hoursPolicyListBox.setDisabled(true);
                cellHoursPolicy.appendChild(hoursPolicyListBox);

            } else { // If is a leaf

                final Intbox workingHours = Util.bind(new Intbox(),
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

                // Add EventListener to reload the popup when the value change
                workingHours.addEventListener(Events.ON_CHANGE,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                Util.reloadBindings(popup);
                                ((OrderLine) getOrderElement())
                                        .recalculatePercentages();
                            }
                        });

                final Decimalbox percentage = Util.bind(decimalBox,
                        new Util.Getter<BigDecimal>() {

                            @Override
                            public BigDecimal get() {
                                return hoursGroup.getPercentage();
                            }
                        }, new Util.Setter<BigDecimal>() {

                            @Override
                            public void set(BigDecimal value) {
                                hoursGroup.setPercentage(value);
                            }
                        });

                // Add EventListener to reload the popup when the value change
                percentage.addEventListener(Events.ON_CHANGE,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                Util.reloadBindings(popup);
                                ((OrderLine) getOrderElement())
                                        .recalculatePercentages();
                            }
                        });

                // Hours policy
                hoursPolicyListBox.setSelectedIndex(hoursGroup.getHoursPolicy()
                        .ordinal());
                hoursPolicyListBox.addEventListener(Events.ON_SELECT,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                HoursPolicies policy = (HoursPolicies) hoursPolicyListBox
                                        .getSelectedItem().getValue();
                                hoursGroup.setHoursPolicy(policy);

                                // Disable components depending on the policy
                                disableComponents(workingHours, percentage,
                                        policy);
                            }
                        });

                // Disable components depending on the policy
                disableComponents(workingHours, percentage,
                        (HoursPolicies) hoursPolicyListBox.getSelectedItem()
                                .getValue());

                cellWorkingHours.appendChild(workingHours);
                cellPercentage.appendChild(percentage);
                cellHoursPolicy.appendChild(hoursPolicyListBox);

                // For each ICriterionType selected
                for (ICriterionType<?> criterionType : getSelectedCriterionTypes()) {
                    Listcell cellCriterion = new Listcell();
                    cellCriterion.setParent(item);

                    // Add a new column on the HoursGroup table
                    Listhead header = ((Listbox) item.getParent())
                            .getListheadApi();
                    Listheader headerCriterion = new Listheader();
                    headerCriterion.setLabel(criterionType.getName());
                    headerCriterion.setParent(header);

                    // Add a new Listbox for each ICriterionType
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
         * @param policy
         *            A {@link HoursPolicies} value
         */
        public void disableComponents(Intbox workingHours,
                Decimalbox percentage, HoursPolicies policy) {

            switch (policy) {
            case FIXED_PERCENTAGE:
                // Working hours not editable
                workingHours.setDisabled(true);
                // Percentage editable
                percentage.setDisabled(false);
                break;

            case NO_FIXED:
            case FIXED_HOURS:
            default:
                // Working hours editable
                workingHours.setDisabled(false);
                // Percentage not editable
                percentage.setDisabled(true);
                break;
            }
        }
    }

}
