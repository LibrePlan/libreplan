package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;

/**
 * Controller for edit {@link Task} popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("editTaskController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskController extends GenericForwardComposer {

    private IEditTaskModel editTaskModel;

    private TaskElement currentTaskElement;

    private Intbox hours;

    private Checkbox fixedDuration;

    private Intbox duration;

    private Datebox endDateBox;

    /**
     * Controller from the Gantt to manage common fields on edit {@link Task}
     * popup.
     */
    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    public void showEditFormFor(Component openRelativeTo,
            org.zkoss.ganttz.data.Task task,
            TaskElement taskElement) {
        this.currentTaskElement = taskElement;
        taskEditFormComposer.showEditFormFor(openRelativeTo, task);
        updateComponentValuesForTask();
    }

    private void updateComponentValuesForTask() {
        if (currentTaskElement instanceof Task) {
            // If it's a Task
            // Show fields
            hours.getFellow("fixedDurationRow").setVisible(true);
            hours.getFellow("durationRow").setVisible(true);

            Task task = (Task) currentTaskElement;

            // Sets the value of fields
            fixedDuration.setChecked(task.isFixedDuration() == null ? false
                    : task.isFixedDuration());
            duration.setValue(editTaskModel.getDuration(task));

            // Disable some fields depending on fixedDuration value
            duration.setDisabled(task.isFixedDuration() == null ? true : !task
                    .isFixedDuration());
            ((Datebox) hours.getFellow("endDateBox"))
                    .setDisabled(task.isFixedDuration() == null ? true : !task
                            .isFixedDuration());
        } else {
            // If it's a TaskGroup
            // Hide fields
            hours.getFellow("fixedDurationRow").setVisible(false);
            hours.getFellow("durationRow").setVisible(false);
        }

        // Sets the values for the common fields
        endDateBox.setValue(currentTaskElement.getEndDate());
        hours.setValue(currentTaskElement.getWorkHours());

        // Update the Task size in the Gantt
        taskEditFormComposer.onChange$endDateBox(null);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        taskEditFormComposer.doAfterCompose(comp);
    }

    public void onCheck$fixedDuration(Event event) {
        if (currentTaskElement instanceof Task) {
            Task task = (Task) currentTaskElement;
            task.setFixedDuration(fixedDuration.isChecked());

            // Disable some fields depending on fixedDuration value
            duration.setDisabled(!fixedDuration.isChecked());
            ((Datebox) duration.getFellow("endDateBox"))
                    .setDisabled(!fixedDuration.isChecked());
        }

        updateComponentValuesForTask();
    }

    public void onChange$duration(Event event) {
        if ((currentTaskElement instanceof Task)
                && isValidDuration(duration.getValue())) {
            Task task = (Task) currentTaskElement;
            task.setDaysDuration(duration.getValue());
        }

        updateComponentValuesForTask();
    }

    private boolean isValidDuration(Integer duration) {
        return duration != null && duration > 0;
    }

    public void onChange$endDateBox(Event event) {
        currentTaskElement.setEndDate(endDateBox.getValue());
        updateComponentValuesForTask();
    }

}
