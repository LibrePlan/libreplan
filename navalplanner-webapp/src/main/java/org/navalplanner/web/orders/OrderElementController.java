package org.navalplanner.web.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.HoursGroup.HoursPolicies;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;

/**
 * Controller for {@link OrderElement} view of {@link Order} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementController extends GenericForwardComposer {

    /**
     * {@link OrderElement} that is managed
     */
    private OrderElement orderElement;

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


    public OrderElement getOrderElement() {
        return orderElement;
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
    public void openPopup(OrderElement orderElement) {
        this.orderElement = orderElement;

        this.hoursGroupsModel = orderElement.getHoursGroups();

        // If is a container
        if (orderElement instanceof OrderLineGroup) {
            // Disable fields just used in the OrderLine
            ((Textbox) popup.getFellow("totalHours")).setDisabled(true);

            // Hide not needed buttons
            popup.getFellow("manageCriterions").setVisible(false);
            popup.getFellow("addHoursGroup").setVisible(false);
            popup.getFellow("deleteHoursGroup").setVisible(false);
        } else {
            // Enable fields just used in the OrderLine
            ((Textbox) popup.getFellow("totalHours")).setDisabled(false);

            // Show needed buttons
            popup.getFellow("manageCriterions").setVisible(true);
            popup.getFellow("addHoursGroup").setVisible(true);
            popup.getFellow("deleteHoursGroup").setVisible(true);
        }

        fillFixedHoursCheckbox(orderElement);

        Util.reloadBindings(popup);

        popup.open(popup.getParent(), "start-after");
    }

    /**
     * Private method that just fills the Div with id "fixedHoursCheckbox" in
     * the .zul.
     *
     * If the parameter is a {@link OrderLine} the method adds the needed
     * checkbox.
     *
     * @param orderElement
     *            {@link OrderElement} that is been rendered
     */
    private void fillFixedHoursCheckbox(final OrderElement orderElement) {

        // Get the Div with id "fixedHoursCheckbox"
        Component fixedHoursCheckbox = popup.getFellow("fixedHoursCheckbox");

        // Empty the content of the Div
        // Making a copy to avoid a ConcurrentModificationException
        List<Component> children = new ArrayList<Component>(fixedHoursCheckbox
                .getChildren());
        for (Component component : children) {
            fixedHoursCheckbox.removeChild(component);
        }

        // If is a leaf
        if (orderElement instanceof OrderLine) {
            // Add specific fields
            fixedHoursCheckbox.appendChild(Util.bind(new Checkbox(),
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return ((OrderLine) orderElement).isFixedHours();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            ((OrderLine) orderElement).setFixedHours(value);
                        }
                    }));
            fixedHoursCheckbox.appendChild(new Label("Fixed hours"));
        }
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
        for (Listitem item : selectedItems) {
            ((OrderLine) orderElement).deleteHoursGroup((HoursGroup) item
                    .getValue());
        }

        this.hoursGroupsModel = orderElement.getHoursGroups();
        Util.reloadBindings(popup);
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

            // If is a container
            if (orderElement instanceof OrderLineGroup) {
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
                cellPercentage.appendChild(Util.bind(new Decimalbox(),
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

                final Decimalbox percentage = Util.bind(new Decimalbox(),
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
