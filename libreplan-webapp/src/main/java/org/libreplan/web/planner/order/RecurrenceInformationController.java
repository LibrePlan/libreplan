/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

package org.libreplan.web.planner.order;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.recurring.RecurrenceInformation;
import org.libreplan.business.recurring.RecurrencePeriodicity;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.api.Box;
import org.zkoss.zul.api.Radiogroup;
import org.zkoss.zul.api.Spinner;

/**
 * Controller for subcontract a task.
 *
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@org.springframework.stereotype.Component("recurringController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RecurrenceInformationController extends GenericForwardComposer {

    private static <T extends Component> List<T> findDescendants(
            Class<T> componentType, Component component) {
        List<T> result = new ArrayList<T>();
        @SuppressWarnings("unchecked")
        List<Component> children = component.getChildren();
        for (Component each : children) {
            if (componentType.isInstance(each)) {
                result.add(componentType.cast(each));
            }
            result.addAll(findDescendants(componentType, each));
        }
        return result;
    }

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;
    private Radiogroup recurrencePattern;
    private Radiogroup repeatOnDayWeekGroup;
    private Div repeatOnMonthDayDiv;
    private Spinner recurrenceOccurences;

    private Box amountOfPeriodsGroup;
    private Spinner amountOfPeriodsSpinner;

    private int repetitions = 0;

    private RecurrencePeriodicity recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;

    private int amountOfPeriods = 1;

    private Integer repeatOnDayForWeek;

    private Integer repeatOnDayForMonth;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public void init(Task task) {
        RecurrenceInformation recurrenceInformation = task
                .getRecurrenceInformation();

        this.repetitions = recurrenceInformation.getRepetitions();
        RecurrencePeriodicity periodicity = recurrenceInformation
                .getPeriodicity();
        this.amountOfPeriods = recurrenceInformation
                .getAmountOfPeriodsPerRepetition();

        this.repeatOnDayForWeek = periodicity == RecurrencePeriodicity.WEEKLY ? recurrenceInformation
                .getRepeatOnDay() : null;
        this.repeatOnDayForMonth = periodicity == RecurrencePeriodicity.MONTHLY ? recurrenceInformation
                .getRepeatOnDay() : null;

        prepareRadioBoxes(periodicity);
        setPeriodicity(periodicity);
    }

    private void setPeriodicity(RecurrencePeriodicity periodicity){
        Validate.notNull(periodicity);
        this.recurrencePeriodicity = periodicity;
        enableOrDisableSpinner();
        enableOrDisableAmountOfPeriods();
        visualizeOrHideRepeatOnDayWeek();
        visualizeOrHideRepeatOnDayMonth();
        if (this.recurrencePeriodicity == RecurrencePeriodicity.WEEKLY) {
            prepareRadioBoxesForRepeatOnDayForWeek(repeatOnDayForWeek);
        } else if (this.recurrencePeriodicity == RecurrencePeriodicity.MONTHLY) {
            prepareButtonsForRepeatOnDayForMonth();
        }
    }

    private void visualizeOrHideRepeatOnDayWeek() {
        repeatOnDayWeekGroup
                .setVisible(this.recurrencePeriodicity == RecurrencePeriodicity.WEEKLY);
    }

    private void visualizeOrHideRepeatOnDayMonth() {
        repeatOnMonthDayDiv
                .setVisible(this.recurrencePeriodicity == RecurrencePeriodicity.MONTHLY);
    }

    private void prepareRadioBoxesForRepeatOnDayForWeek(Integer repeatOnDay) {
        repeatOnDay = repeatOnDay == null ? 0 : repeatOnDay;
        @SuppressWarnings("unchecked")
        List<Radio> children = repeatOnDayWeekGroup.getChildren();
        if (children.isEmpty()) {
            String[] labels = { _("Not specify"), _("Monday"), _("Tuesday"),
                    _("Wednesday"), _("Thursday"), _("Friday"), _("Saturday"),
                    _("Sunday") };
            int i = 0;
            for (String label : labels) {
                Radio radio = new Radio(label);
                radio.setValue(i + "");
                children.add(radio);
                radio.setSelected(i == repeatOnDay);
                i++;
            }
        } else {
            for (Radio each : children) {
                each.setSelected(each.getValue().equals(repeatOnDay + ""));
            }
        }
    }

    private static final String dayOnMonthClass = "repeat-on-day-month";

    @SuppressWarnings("unchecked")
    private void prepareButtonsForRepeatOnDayForMonth() {
        List<Component> children = repeatOnMonthDayDiv.getChildren();
        if (!children.isEmpty()) {
            updateSelectedLabelForRepeatOnDayForMonth();
            return;
        }
        Vlayout vlayout = new Vlayout();
        vlayout.setSpacing("1px");
        children.add(vlayout);
        int n = 1;
        for (int i = 0; i < 5; i++) {
            Hlayout hLayout = new Hlayout();
            hLayout.setSpacing("0");
            vlayout.getChildren().add(hLayout);
            for (int j = 0; j < 7 && n <= 31; j++, n++) {
                Label label = new Label();
                label.setPre(true);
                label.setStyle("font-family: monospace;");
                label.setValue(String.format("%2d", n));
                final int thisN = n;
                label.addEventListener(Events.ON_CLICK, new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        repeatOnDayMonthChosen(thisN);
                    }
                });
                label.setSclass(dayOnMonthClass);
                hLayout.getChildren().add(label);
            }
        }
    }

    private void repeatOnDayMonthChosen(Integer repeatOnDay) {
        if (ObjectUtils.equals(repeatOnDay, this.repeatOnDayForMonth)) {
            this.repeatOnDayForMonth = null;
        } else {
            this.repeatOnDayForMonth = repeatOnDay;
        }
        updateSelectedLabelForRepeatOnDayForMonth();
    }

    private void updateSelectedLabelForRepeatOnDayForMonth() {
        List<Label> labels = findDescendants(Label.class, repeatOnMonthDayDiv);
        for (Label each : labels) {
            each.setSclass(dayOnMonthClass);
        }
        if (this.repeatOnDayForMonth == null) {
            return;
        }
        Label label = labels.get(this.repeatOnDayForMonth - 1);
        label.setSclass("repeat-on-day-month-day-selected");
    }

    public void updateRepeatOnDayOfWeek() {
        int v = Integer.parseInt(repeatOnDayWeekGroup.getSelectedItemApi()
                .getValue());
        this.repeatOnDayForWeek = v == 0 ? null : v;
    }

    private void enableOrDisableSpinner() {
        recurrenceOccurences.setDisabled(recurrencePeriodicity
                .isNoPeriodicity());

        this.repetitions = recurrencePeriodicity.limitRepetitions(repetitions);
        if (repetitions == 0 && recurrencePeriodicity.isPeriodicity()) {
            repetitions = 1;
        }
        Util.reloadBindings(recurrenceOccurences);
    }

    private void enableOrDisableAmountOfPeriods() {
        amountOfPeriodsGroup.setVisible(recurrencePeriodicity.isPeriodicity());
        this.amountOfPeriods = recurrencePeriodicity
                .limitAmountOfPeriods(amountOfPeriods);
        if (amountOfPeriods == 0 && recurrencePeriodicity.isPeriodicity()) {
            this.amountOfPeriods = 1;
        }
        Util.reloadBindings(amountOfPeriodsGroup);
    }

    private void prepareRadioBoxes(
            RecurrencePeriodicity currentPeriodicity) {
        if (recurrencePattern.getChildren().isEmpty()) {
            buildRadioBoxes(currentPeriodicity);
        } else {
            selectRadioBox(currentPeriodicity);
        }
    }

    @SuppressWarnings("unchecked")
    private void selectRadioBox(RecurrencePeriodicity currentPeriodicity) {
        for (Radio each : findDescendants(Radio.class, recurrencePattern)) {
            each.setSelected(Enum.valueOf(RecurrencePeriodicity.class,
                    each.getValue()) == currentPeriodicity);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildRadioBoxes(RecurrencePeriodicity currentPeriodicity) {
        Vlayout layout = new Vlayout();
        recurrencePattern.getChildren().add(layout);
        List<Component> children = layout.getChildren();
        for (RecurrencePeriodicity each : RecurrencePeriodicity.values()) {
            Radio radio = new Radio();
            radio.setLabel(_(each.getLabel()));
            radio.setValue(each.toString());
            children.add(radio);
            radio.setSelected(currentPeriodicity == each);
        }
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void updateRecurrencePeriodicity() {
        Radio selected = (Radio) recurrencePattern.getSelectedItemApi();

        RecurrencePeriodicity p = Enum.valueOf(RecurrencePeriodicity.class,
                selected.getValue());
        setPeriodicity(Enum.valueOf(RecurrencePeriodicity.class,
                selected.getValue()));
        enableOrDisableSpinner();
        enableOrDisableAmountOfPeriods();
    }

    public int getAmountOfPeriods() {
        return amountOfPeriods;
    }

    public void setAmountOfPeriods(int amountOfPeriods) {
        this.amountOfPeriods = amountOfPeriods;
    }

    private static final EnumSet<RecurrencePeriodicity> supportRepeatOnDay = EnumSet
            .of(RecurrencePeriodicity.WEEKLY, RecurrencePeriodicity.MONTHLY);

    public String getPeriodUnitLabel() {
        String unitLabel = _(recurrencePeriodicity.getUnitLabel());
        String repeatOn = supportRepeatOnDay.contains(recurrencePeriodicity) ? " "
                + _("on: ")
                : "";
        return unitLabel + repeatOn;
    }

    public RecurrenceInformation getModifiedRecurrenceInformation() {
        RecurrenceInformation result = RecurrenceInformation.endAtNumberOfRepetitions(repetitions, recurrencePeriodicity,
                amountOfPeriods);
        if (recurrencePeriodicity == RecurrencePeriodicity.WEEKLY
                && repeatOnDayForWeek != null) {
            result = result.repeatOnDay(repeatOnDayForWeek);
        } else if (recurrencePeriodicity == RecurrencePeriodicity.MONTHLY
                && repeatOnDayForMonth != null) {
            result = result.repeatOnDay(repeatOnDayForMonth);
        }
        return result;
    }


}
