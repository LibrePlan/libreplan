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

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.recurring.RecurrenceInformation;
import org.libreplan.business.recurring.RecurrencePeriodicity;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Radio;
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

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;
    private Radiogroup recurrencePattern;
    private Spinner recurrenceOccurences;

    private int repetitions = 0;

    private RecurrencePeriodicity recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    private void setRadioSelectedWithValue(String value) {
        for (Object r : recurrencePattern.getItems()) {
            if (((Radio) r).getValue().equals(value)) {
                recurrencePattern.setSelectedItemApi((Radio) r);
            }
        }
    }

    public void init(Task task) {
        RecurrenceInformation recurrenceInformation = task
                .getRecurrenceInformation();
        this.repetitions = recurrenceInformation.getRepetitions();
        this.recurrencePeriodicity = recurrenceInformation.getPeriodicity();
        configurePeriodicity(this.recurrencePeriodicity);
        enableOrDisableSpinner();
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

    private void configurePeriodicity(RecurrencePeriodicity periodicity) {
        switch (periodicity) {
        case NO_PERIODICTY:
            setRadioSelectedWithValue("not-recurrent");
            break;
        case DAILY:
            setRadioSelectedWithValue("daily");
            break;
        case WEEKLY:
            setRadioSelectedWithValue("weekly");
            break;
        case MONTHLY:
            setRadioSelectedWithValue("monthly");
            break;
        }
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void updateRecurrencePeriodicity() {
        Radio selected = recurrencePattern.getSelectedItemApi();
        if (selected.getValue().equals("not-recurrent")) {
            this.recurrencePeriodicity = RecurrencePeriodicity.NO_PERIODICTY;
        } else if (selected.getValue().equals("daily")) {
            this.recurrencePeriodicity = RecurrencePeriodicity.DAILY;
        } else if (selected.getValue().equals("monthly")) {
            this.recurrencePeriodicity = RecurrencePeriodicity.MONTHLY;
        } else if (selected.getValue().equals("weekly")) {
            this.recurrencePeriodicity = RecurrencePeriodicity.WEEKLY;
        } else {
            throw new RuntimeException("Don't know how to handle: "
                    + selected.getValue());
        }
        enableOrDisableSpinner();
    }

    public RecurrenceInformation getModifiedRecurrenceInformation() {
        return new RecurrenceInformation(repetitions, recurrencePeriodicity);
    }

}
