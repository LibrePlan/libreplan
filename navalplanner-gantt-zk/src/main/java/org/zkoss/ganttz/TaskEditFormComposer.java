package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class TaskEditFormComposer extends GenericForwardComposer {

    public TaskEditFormComposer() {

    }

    private Window window;
    private TaskBean currentTask;

    private Textbox name;

    private Datebox startDateBox;

    private Datebox endDateBox;

    private Textbox notes;

    private PropertyChangeListener propertyChangeListener;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
        window.setVisible(false);
    }

    public void showEditFormFor(Task task) {
        cleanListener();
        this.currentTask = task.getTaskBean();
        window.doPopup();
        propertyChangeListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (window.isVisible()) {
                    updateComponentValuesForTask(currentTask);
                }
            }
        };
        currentTask.addPropertyChangeListener(propertyChangeListener);
        updateComponentValuesForTask(currentTask);
    }

    private void cleanListener() {
        if (this.currentTask != null) {
            this.currentTask
                    .removePropertyChangeListener(propertyChangeListener);
        }
    }

    private void updateComponentValuesForTask(TaskBean currentTask) {
        window.setTitle(currentTask.getName());
        name.setValue(currentTask.getName());
        startDateBox.setValue(currentTask.getBeginDate());
        endDateBox.setValue(currentTask.getEndDate());
        notes.setValue(currentTask.getNotes());
    }

    public void onChange$name(Event event) {
        currentTask.setName(name.getValue());
    }

    public void onChange$startDateBox(Event event) {
        currentTask.setBeginDate(startDateBox.getValue());
    }

    public void onChange$endDateBox(Event event) {
        currentTask.setEndDate(endDateBox.getValue());
    }

    public void onChange$notes(Event event) {
        currentTask.setNotes(notes.getValue());
    }

    public void onClick$ok(Event event) {
        window.setVisible(false);
        cleanListener();
    }

}
