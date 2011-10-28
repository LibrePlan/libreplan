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

package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.api.Label;
import org.zkoss.zul.api.Treerow;

public class LeftTasksTreeRow extends GenericForwardComposer {

    public interface ILeftTasksTreeNavigator {
        LeftTasksTreeRow getBelowRow();

        LeftTasksTreeRow getAboveRow();
    }

    private static final Log LOG = LogFactory.getLog(LeftTasksTreeRow.class);

    private final Task task;

    private Label nameLabel;

    private Textbox nameBox;

    private Label startDateLabel;

    private Textbox startDateTextBox;

    private Label endDateLabel;

    private Textbox endDateTextBox;

    private Datebox startDateBox;

    private Datebox endDateBox;

    private DateFormat dateFormat;

    private final ILeftTasksTreeNavigator leftTasksTreeNavigator;

    private final IDisabilityConfiguration disabilityConfiguration;

    public static LeftTasksTreeRow create(
            IDisabilityConfiguration disabilityConfiguration, Task bean,
            ILeftTasksTreeNavigator taskDetailnavigator) {
        return new LeftTasksTreeRow(disabilityConfiguration, bean,
                taskDetailnavigator);
    }

    private LeftTasksTreeRow(IDisabilityConfiguration disabilityConfiguration,
            Task task,
            ILeftTasksTreeNavigator leftTasksTreeNavigator) {
        this.disabilityConfiguration = disabilityConfiguration;
        this.task = task;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locales
                .getCurrent());
        this.leftTasksTreeNavigator = leftTasksTreeNavigator;
    }

    public Task getTask() {
        return task;
    }

    public Textbox getNameBox() {
        return nameBox;
    }

    public void setNameBox(Textbox nameBox) {
        this.nameBox = nameBox;
    }

    public Datebox getStartDateBox() {
        return startDateBox;
    }

    public void setStartDateBox(Datebox startDateBox) {
        this.startDateBox = startDateBox;
        this.startDateBox.setCompact(true);
        this.startDateBox.setFormat("dd/MM/yyyy");
    }

    public Datebox getEndDateBox() {
        return endDateBox;
    }

    public Task getData() {
        return task;
    }

    /**
     * When a text box associated to a datebox is requested to show the datebox,
     * the corresponding datebox is shown
     * @param component
     *            the component that has received focus
     */
    public void userWantsDateBox(Component component) {
        if (component == startDateTextBox) {
            if (canChangeStartDate()) {
                showDateBox(startDateBox, startDateTextBox);
            }
        }
        if (component == endDateTextBox) {
            if (canChangeEndDate()) {
                showDateBox(endDateBox, endDateTextBox);
            }
        }
    }

    private void showDateBox(Datebox dateBox, Textbox associatedTextBox) {
        associatedTextBox.setVisible(false);
        dateBox.setVisible(true);
        dateBox.setFocus(true);
        dateBox.setOpen(true);
    }

    private enum Navigation {
        LEFT, UP, RIGHT, DOWN;
        public static Navigation getIntentFrom(KeyEvent keyEvent) {
            return values()[keyEvent.getKeyCode() - 37];
        }
    }

    public void focusGoUp(int position) {
        LeftTasksTreeRow aboveDetail = leftTasksTreeNavigator.getAboveRow();
        if (aboveDetail != null) {
            aboveDetail.receiveFocus(position);
        }
    }

    public void receiveFocus() {
        receiveFocus(0);
    }

    public void receiveFocus(int position) {
        this.getTextBoxes().get(position).focus();
    }

    public void focusGoDown(int position) {
        LeftTasksTreeRow belowDetail = leftTasksTreeNavigator.getBelowRow();
        if (belowDetail != null) {
            belowDetail.receiveFocus(position);
        } else {
            getListDetails().getGoingDownInLastArrowCommand().doAction();
        }
    }

    private LeftTasksTree getListDetails() {
        Component current = nameBox;
        while (!(current instanceof LeftTasksTree)) {
            current = current.getParent();
        }
        return (LeftTasksTree) current;
    }

    public void userWantsToMove(Textbox textbox, KeyEvent keyEvent) {
        Navigation navigation = Navigation.getIntentFrom(keyEvent);
        List<Textbox> textBoxes = getTextBoxes();
        int position = textBoxes.indexOf(textbox);
        switch (navigation) {
        case UP:
            focusGoUp(position);
            break;
        case DOWN:
            focusGoDown(position);
            break;
        case LEFT:
            if (position == 0) {
                focusGoUp(getTextBoxes().size() - 1);
            } else {
                textBoxes.get(position - 1).focus();
            }
            break;
        case RIGHT:
            if (position < textBoxes.size() - 1) {
                textBoxes.get(position + 1).focus();
            } else {
                focusGoDown(0);
            }
            break;
        default:
            throw new RuntimeException("case not covered: " + navigation);
        }
    }

    private List<Textbox> getTextBoxes() {
        return Arrays.asList(nameBox, startDateTextBox, endDateTextBox);
    }

    /**
     * When the dateBox loses focus the corresponding textbox is shown instead.
     * @param dateBox
     *            the component that has lost focus
     */
    public void dateBoxHasLostFocus(Datebox dateBox) {
        if (dateBox == startDateBox) {
            hideDateBox(startDateBox, startDateTextBox);
        }
        if (dateBox == endDateBox) {
            hideDateBox(endDateBox, endDateTextBox);
        }
    }

    private void hideDateBox(Datebox dateBoxToDissapear,
            Textbox associatedTextBox) {
        dateBoxToDissapear.setVisible(false);
        associatedTextBox.setVisible(true);
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        findComponents((Treerow) component);
        registerListeners();
        updateComponents();
        task
                .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateComponents();
                    }
                });
    }

    private void registerListeners() {
        if (disabilityConfiguration.isTreeEditable()) {
            registerKeyboardListener(nameBox);
            registerKeyboardListener(startDateTextBox);
            registerKeyboardListener(endDateTextBox);

            registerOnEnterListener(startDateTextBox);
            registerOnEnterListener(endDateTextBox);

            registerOnEnterOpenDateBox(startDateBox);
            registerOnEnterOpenDateBox(endDateBox);

            registerBlurListener(startDateBox);
            registerBlurListener(endDateBox);

            registerOnChange(nameBox);
            registerOnChange(startDateBox);
            registerOnChange(endDateBox);
        }
    }

    private void findComponents(Treerow row) {
        List<Object> rowChildren = row.getChildren();
        List<Treecell> treeCells = ComponentsFinder.findComponentsOfType(Treecell.class,
                rowChildren);
        assert treeCells.size() == 3;
        findComponentsForNameCell(treeCells.get(0));
        findComponentsForStartDateCell(treeCells.get(1));
        findComponentsForEndDateCell(treeCells.get(2));
    }

    private static Datebox findDateBoxOfCell(Treecell treecell) {
        List<Object> children = treecell.getChildren();
        return ComponentsFinder.findComponentsOfType(Datebox.class, children).get(0);
    }

    private static Textbox findTextBoxOfCell(Treecell treecell) {
        List<Object> children = treecell.getChildren();
        return ComponentsFinder.findComponentsOfType(Textbox.class, children).get(0);
    }

    private void findComponentsForNameCell(Treecell treecell) {
        if (disabilityConfiguration.isTreeEditable()) {
            nameBox = (Textbox) treecell.getChildren().get(0);
        } else {
            nameLabel = (Label) treecell.getChildren().get(0);
        }
    }

    private void registerKeyboardListener(final Textbox textBox) {
        textBox.addEventListener("onCtrlKey", new EventListener() {

            @Override
            public void onEvent(Event event) {
                userWantsToMove(textBox, (KeyEvent) event);
            }
        });
    }

    private void registerOnChange(final Component component) {
        component.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) {
                updateBean(component);
            }
        });
    }

    private void registerOnEnterListener(final Textbox textBox) {
        textBox.addEventListener("onOK", new EventListener() {

            @Override
            public void onEvent(Event event) {
                userWantsDateBox(textBox);
            }
        });
    }

    private void registerOnEnterOpenDateBox(final Datebox datebox) {
        datebox.addEventListener("onOK", new EventListener() {

            @Override
            public void onEvent(Event event) {
                datebox.setOpen(true);
            }
        });
    }

    private void findComponentsForStartDateCell(Treecell treecell) {
        if (disabilityConfiguration.isTreeEditable()) {
            startDateTextBox = findTextBoxOfCell(treecell);
            startDateBox = findDateBoxOfCell(treecell);
        } else {
            startDateLabel = (Label) treecell.getChildren().get(0);
        }
    }

    private void findComponentsForEndDateCell(Treecell treecell) {
        if (disabilityConfiguration.isTreeEditable()) {
            endDateBox = findDateBoxOfCell(treecell);
            endDateBox.setDisabled(true);
            endDateTextBox = findTextBoxOfCell(treecell);
        } else {
            endDateLabel = (Label) treecell.getChildren().get(0);
        }
    }

    private void registerBlurListener(final Datebox datebox) {
        datebox.addEventListener("onBlur", new EventListener() {

            @Override
            public void onEvent(Event event) {
                dateBoxHasLostFocus(datebox);
            }
        });
    }

    public void updateBean(Component updatedComponent) {
        if (updatedComponent == getNameBox()) {
            task.setName(getNameBox().getValue());
        } else if (updatedComponent == getStartDateBox()) {
            Date begin = getStartDateBox().getValue();
            task.moveTo(GanttDate.createFrom(begin));
        } else if (updatedComponent == getEndDateBox()) {
            Date newEnd = getEndDateBox().getValue();
            task.resizeTo(LocalDate.fromDateFields(newEnd));
        }
    }

    private void updateComponents() {
        if (disabilityConfiguration.isTreeEditable()) {
            getNameBox().setValue(task.getName());
            getNameBox().setDisabled(!canRenameTask());
            getNameBox().setTooltiptext(task.getName());

            getStartDateBox().setValue(
                    task.getBeginDate().toDayRoundedDate());
            getStartDateBox().setDisabled(!canChangeStartDate());
            getStartDateTextBox().setDisabled(!canChangeStartDate());

            getEndDateBox().setValue(task.getEndDate().toDayRoundedDate());
            getEndDateBox().setDisabled(!canChangeEndDate());
            getEndDateTextBox().setDisabled(!canChangeEndDate());

            getStartDateTextBox().setValue(
                    asString(task.getBeginDate().toDayRoundedDate()));
            getEndDateTextBox().setValue(
                    asString(task.getEndDate().toDayRoundedDate()));
        } else {
            nameLabel.setValue(task.getName());
            nameLabel.setTooltiptext(task.getName());
            startDateLabel.setValue(asString(task.getBeginDate()
                    .toDayRoundedDate()));
            endDateLabel.setValue(asString(task.getEndDate()
                    .toDayRoundedDate()));
        }
    }

    private boolean canChangeStartDate() {
        return disabilityConfiguration.isMovingTasksEnabled()
                && task.canBeExplicitlyMoved();
    }

    private boolean canChangeEndDate() {
        return disabilityConfiguration.isResizingTasksEnabled()
                && task.canBeExplicitlyResized();
    }

    private boolean canRenameTask() {
        return disabilityConfiguration.isRenamingTasksEnabled();
    }

    private String asString(Date date) {
        return dateFormat.format(date);
    }

    public Textbox getStartDateTextBox() {
        return startDateTextBox;
    }

    public void setStartDateTextBox(Textbox startDateTextBox) {
        this.startDateTextBox = startDateTextBox;
    }

    public Textbox getEndDateTextBox() {
        return endDateTextBox;
    }

    public void setEndDateTextBox(Textbox endDateTextBox) {
        this.endDateTextBox = endDateTextBox;
    }

}
