package org.navalplanner.web.workorders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.workorders.entities.ActivityWork;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWork;
import org.navalplanner.business.workorders.entities.TaskWorkContainer;
import org.navalplanner.business.workorders.entities.TaskWorkLeaf;
import org.navalplanner.business.workorders.entities.ActivityWork.HoursPolicies;
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
 * Controller for {@link TaskWork} view of {@link ProjectWork} entities <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class TaskWorkController extends GenericForwardComposer {

    /**
     * {@link TaskWork} that is managed
     */
    private TaskWork taskWork;

    /**
     * {@link Popup} where {@link TaskWork} edition form is showed
     */
    private Popup popup;

    /**
     * Model of the {@link ActivityWork} list
     */
    private List<ActivityWork> activityWorksModel;

    /**
     * {@link Listitem} for every {@link ActivityWork}
     */
    private AcitivyWorkListitemRender renderer = new AcitivyWorkListitemRender();

    /**
     * {@link Listbox} where {@link ActivityWork} are shown
     */
    private Listbox activityWorksListbox;


    public TaskWork getTaskWork() {
        return taskWork;
    }

    public List<ActivityWork> getActivityWorksModel() {
        return activityWorksModel;
    }

    public AcitivyWorkListitemRender getRenderer() {
        return renderer;
    }


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("taskWorkController", this, true);

        popup = (Popup) comp;
    }

    /**
     * Open the popup to edit a {@link TaskWork}. If it's a
     * {@link TaskWorkContainer} less fields will be enabled.
     *
     * @param taskWork
     *            The {@link TaskWork} to be edited
     */
    public void openPopup(TaskWork taskWork) {
        this.taskWork = taskWork;

        this.activityWorksModel = taskWork.getActivities();

        // If is a container
        if (taskWork instanceof TaskWorkContainer) {
            // Disable fields just used in the TaskWorkLeaf
            ((Textbox) popup.getFellow("totalHours")).setDisabled(true);

            // Hide not needed buttons
            popup.getFellow("manageCriterions").setVisible(false);
            popup.getFellow("addActivityWork").setVisible(false);
            popup.getFellow("deleteActivityWork").setVisible(false);
        } else {
            // Enable fields just used in the TaskWorkLeaf
            ((Textbox) popup.getFellow("totalHours")).setDisabled(false);

            // Show needed buttons
            popup.getFellow("manageCriterions").setVisible(true);
            popup.getFellow("addActivityWork").setVisible(true);
            popup.getFellow("deleteActivityWork").setVisible(true);
        }

        fillFixedHoursCheckbox(taskWork);

        Util.reloadBindings(popup);

        popup.open(popup.getParent(), "start-after");
    }

    /**
     * Private method that just fills the Div with id "fixedHoursCheckbox" in
     * the .zul.
     *
     * If the parameter is a {@link TaskWorkLeaf} the method adds the needed
     * checkbox.
     *
     * @param taskWork
     *            {@link TaskWork} that is been rendered
     */
    private void fillFixedHoursCheckbox(final TaskWork taskWork) {

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
        if (taskWork instanceof TaskWorkLeaf) {
            // Add specific fields
            fixedHoursCheckbox.appendChild(Util.bind(new Checkbox(),
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return ((TaskWorkLeaf) taskWork).isFixedHours();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            ((TaskWorkLeaf) taskWork).setFixedHours(value);
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
     * Adds a new {@link ActivityWork} to the current {@link TaskWork}
     *
     * The {@link TaskWork} should be a {@link TaskWorkLeaf}
     */
    public void addActivityWork() {
        ActivityWork activity = new ActivityWork();

        ((TaskWorkLeaf) taskWork).addActivity(activity);

        this.activityWorksModel = taskWork.getActivities();
        Util.reloadBindings(popup);
    }

    /**
     * Deletes the selected {@link ActivityWork} for the current
     * {@link TaskWork}
     *
     * The {@link TaskWork} should be a {@link TaskWorkLeaf}
     */
    public void deleteActivityWorks() {
        Set<Listitem> selectedItems = activityWorksListbox.getSelectedItems();
        for (Listitem item : selectedItems) {
            ((TaskWorkLeaf) taskWork).deleteActivity((ActivityWork) item
                    .getValue());
        }

        this.activityWorksModel = taskWork.getActivities();
        Util.reloadBindings(popup);
    }

    /**
     * Represents every {@link AcitivyWork} with an edition form if needed
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    public class AcitivyWorkListitemRender implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final ActivityWork activity = (ActivityWork) data;

            item.setValue(activity);

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
            if (taskWork instanceof TaskWorkContainer) {
                // Just getters are needed

                // Working hours
                cellWorkingHours.appendChild(Util.bind(new Intbox(),
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return activity.getWorkingHours();
                            }
                        }));

                // Percentage
                cellPercentage.appendChild(Util.bind(new Decimalbox(),
                        new Util.Getter<BigDecimal>() {

                            @Override
                            public BigDecimal get() {
                                return activity.getPercentage();
                            }
                        }));

                // Hours policy
                hoursPolicyListBox.setSelectedIndex(activity.getHoursPolicy()
                        .ordinal());
                hoursPolicyListBox.setDisabled(true);
                cellHoursPolicy.appendChild(hoursPolicyListBox);

            } else { // If is a leaf

                final Intbox workingHours = Util.bind(new Intbox(),
                        new Util.Getter<Integer>() {

                            @Override
                            public Integer get() {
                                return activity.getWorkingHours();
                            }
                        }, new Util.Setter<Integer>() {

                            @Override
                            public void set(Integer value) {
                                activity.setWorkingHours(value);
                            }
                        });

                final Decimalbox percentage = Util.bind(new Decimalbox(),
                        new Util.Getter<BigDecimal>() {

                            @Override
                            public BigDecimal get() {
                                return activity.getPercentage();
                            }
                        }, new Util.Setter<BigDecimal>() {

                            @Override
                            public void set(BigDecimal value) {
                                activity.setPercentage(value);
                            }
                        });

                // Hours policy
                hoursPolicyListBox.setSelectedIndex(activity.getHoursPolicy()
                        .ordinal());
                hoursPolicyListBox.addEventListener(Events.ON_SELECT,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                HoursPolicies policy = (HoursPolicies) hoursPolicyListBox
                                        .getSelectedItem().getValue();
                                activity.setHoursPolicy(policy);

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
