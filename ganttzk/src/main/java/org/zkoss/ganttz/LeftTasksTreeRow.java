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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.ITaskFundamentalProperties.IModifications;
import org.zkoss.ganttz.data.ITaskFundamentalProperties.IUpdatablePosition;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.api.Label;
import org.zkoss.zul.api.Treerow;

/**
 * Row composer for Tasks details Tree <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
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

    private Datebox openedDateBox = null;

    private DateFormat dateFormat;

    private Planner planner;

    private final ILeftTasksTreeNavigator leftTasksTreeNavigator;

    private final IDisabilityConfiguration disabilityConfiguration;

    public static LeftTasksTreeRow create(
            IDisabilityConfiguration disabilityConfiguration, Task bean,
            ILeftTasksTreeNavigator taskDetailnavigator, Planner planner) {
        return new LeftTasksTreeRow(disabilityConfiguration, bean,
                taskDetailnavigator, planner);
    }

    private LeftTasksTreeRow(IDisabilityConfiguration disabilityConfiguration,
            Task task, ILeftTasksTreeNavigator leftTasksTreeNavigator,
            Planner planner) {
        this.disabilityConfiguration = disabilityConfiguration;
        this.task = task;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locales
                .getCurrent());
        this.leftTasksTreeNavigator = leftTasksTreeNavigator;
        this.planner = planner;
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
                createDateBox(startDateTextBox);
            }
        }
        if (component == endDateTextBox) {
            if (canChangeEndDate()) {
                createDateBox(endDateTextBox);
            }
        }
    }

    public void createDateBox(Textbox textbox) {
        openedDateBox = new Datebox();
        openedDateBox.setFormat("short");
        openedDateBox.setButtonVisible(false);

        try {
            openedDateBox.setValue(dateFormat.parse(textbox.getValue()));
        } catch (ParseException e) {
            return;
        }

        registerOnEnterOpenDateBox(openedDateBox);
        registerBlurListener(openedDateBox);
        registerOnChangeDatebox(openedDateBox, textbox);

        textbox.setVisible(false);
        textbox.getParent().appendChild(openedDateBox);
        openedDateBox.setFocus(true);
        openedDateBox.setOpen(true);
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
        dateBox.getPreviousSibling().setVisible(true);
        dateBox.setParent(null);
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        findComponents((Treerow) component);
        registerTextboxesListeners();
        updateComponents();
        task
                .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateComponents();
                    }
                });
    }

    private void registerTextboxesListeners() {
        if (disabilityConfiguration.isTreeEditable()) {
            registerKeyboardListener(nameBox);
            registerOnChange(nameBox);
            registerKeyboardListener(startDateTextBox);
            registerKeyboardListener(endDateTextBox);
            registerOnEnterListener(startDateTextBox);
            registerOnEnterListener(endDateTextBox);
            registerOnChange(startDateTextBox);
            registerOnChange(endDateTextBox);
        }
    }

    public void registerDateboxListeners(Datebox datebox) {
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

    private static Textbox findTextBoxOfCell(Treecell treecell) {
        List<Object> children = treecell.getChildren();
        return ComponentsFinder.findComponentsOfType(Textbox.class, children)
                .get(0);
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

    private void registerOnChangeDatebox(final Datebox datebox,
            final Textbox textbox) {
        datebox.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) {
                textbox.setValue(dateFormat.format(datebox.getValue()));
                updateBean(textbox);
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
        } else {
            startDateLabel = (Label) treecell.getChildren().get(0);
        }
    }

    private void findComponentsForEndDateCell(Treecell treecell) {
        if (disabilityConfiguration.isTreeEditable()) {
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
            if (StringUtils.isEmpty(getNameBox().getValue())) {
                getNameBox().setValue(task.getName());
            }
        } else if (updatedComponent == getStartDateTextBox()) {
            try {
                final Date begin = dateFormat.parse(getStartDateTextBox()
                        .getValue());
                task.doPositionModifications(new IModifications() {

                    @Override
                    public void doIt(IUpdatablePosition position) {
                        position.moveTo(GanttDate.createFrom(begin));
                    }
                });
            } catch (ParseException e) {
                // Do nothing as textbox is rested in the next sentence
            }
            getStartDateTextBox().setValue(
                    dateFormat.format(task.getBeginDate().toDayRoundedDate()));
        } else if (updatedComponent == getEndDateTextBox()) {
            try {
                Date newEnd = dateFormat.parse(getEndDateTextBox().getValue());
                task.resizeTo(LocalDate.fromDateFields(newEnd));
            } catch (ParseException e) {
                // Do nothing as textbox is rested in the next sentence
            }
            getEndDateTextBox().setValue(
                    asString(task.getEndDate().toDayRoundedDate()));
        }
        planner.updateTooltips();
    }

    private void updateComponents() {
        if (disabilityConfiguration.isTreeEditable()) {
            getNameBox().setValue(task.getName());
            getNameBox().setDisabled(!canRenameTask());
            getNameBox().setTooltiptext(task.getName());

            getStartDateTextBox().setDisabled(!canChangeStartDate());
            getEndDateTextBox().setDisabled(!canChangeEndDate());

            getStartDateTextBox().setValue(
                    asString(task.getBeginDate().toDayRoundedDate()));
            getEndDateTextBox().setValue(
                    asString(task.getEndDate().toDayRoundedDate()));
        } else {
            nameLabel.setValue(task.getName());
            nameLabel.setTooltiptext(task.getName());
            nameLabel.setSclass("clickable-rows");
            nameLabel.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    Executions.getCurrent().sendRedirect(
                                    "/planner/index.zul;order="
                                            + task.getProjectCode());
                }
            });
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
