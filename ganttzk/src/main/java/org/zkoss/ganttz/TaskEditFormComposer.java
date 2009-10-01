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

package org.zkoss.ganttz;

import java.util.Date;

import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.Task;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Popup;

public class TaskEditFormComposer extends GenericForwardComposer {

    public TaskEditFormComposer() {

    }

    private Popup popUp;

    private Task currentTask;

    private Textbox name;

    private Datebox startDateBox;

    private Datebox endDateBox;

    private Textbox notes;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        popUp = (Popup) comp;
    }

    public void showEditFormFor(Component openRelativeTo, Task task) {
        this.currentTask = task;
        popUp.open(openRelativeTo, "after_start");
        updateComponentValuesForTask(currentTask);
    }

    private void updateComponentValuesForTask(
            ITaskFundamentalProperties currentTask) {
        // popUp.setTitle(currentTask.getName());
        name.setValue(currentTask.getName());
        startDateBox.setValue(currentTask.getBeginDate());
        endDateBox.setValue(new Date(currentTask.getBeginDate().getTime()
                + currentTask.getLengthMilliseconds()));
        notes.setValue(currentTask.getNotes());
    }

    public void onChange$name(Event event) {
        currentTask.setName(name.getValue());
    }

    public void onChange$startDateBox(Event event) {
        currentTask.setBeginDate(startDateBox.getValue());
    }

    public void onChange$endDateBox(Event event) {
        currentTask.setLengthMilliseconds(endDateBox.getValue().getTime()
                - currentTask.getBeginDate().getTime());
    }

    public void onChange$notes(Event event) {
        currentTask.setNotes(notes.getValue());
    }

    public void onClick$ok(Event event) {
        popUp.close();
    }

}
