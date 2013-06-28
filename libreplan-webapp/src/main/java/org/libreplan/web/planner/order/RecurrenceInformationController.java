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
import org.libreplan.business.recurring.RecurrencePeriodicity;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Radio;
import org.zkoss.zul.api.Radiogroup;

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

    private IRecurringTaskModel recurringTaskModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    private void configurePeriodicity() {
        switch (recurringTaskModel.getPeriodicity()) {
        case NO_PERIODICTY:
            setRadioSelectedWithValue("Not recurrent");
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

    private void setRadioSelectedWithValue(String value) {
        for (Object r : recurrencePattern.getItems()) {
            if (((Radio) r).getValue().equals(value)) {
                recurrencePattern.setSelectedItemApi((Radio) r);
            }
        }
    }

    public void init(Task task) {
        recurringTaskModel.init(task);
        configurePeriodicity();
    }

    public int getRepetitions() {
        return recurringTaskModel.getRepetitions();
    }

    public void setRepetitions(int repetitions) {
        recurringTaskModel.setRepetitions(repetitions);
    }

    public void updateRecurrencePeriodicity() {
        Radio selected = recurrencePattern.getSelectedItemApi();
        if (selected.getValue().equals("Not recurrent")) {
            recurringTaskModel
                    .setRecurrencePeriodicity(
                            RecurrencePeriodicity.NO_PERIODICTY);
        } else if (selected.getValue().equals("daily")) {
            recurringTaskModel
                    .setRecurrencePeriodicity(RecurrencePeriodicity.DAILY);
        } else if (selected.getValue().equals("monthly")) {
            recurringTaskModel
                    .setRecurrencePeriodicity(RecurrencePeriodicity.MONTHLY);

        } else if (selected.getValue().equals("weekly")) {
            recurringTaskModel
                    .setRecurrencePeriodicity(RecurrencePeriodicity.WEEKLY);
        } else {
            // TODO: Probably an exception should be thrown in this
            // case.
        }
    }

}
