/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related with modifiying task recurrence information.
 *
 * @author Lorenzo Tilve <ltilve@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RecurringTaskModel implements IRecurringTaskModel {

    /**
     * Conversation state
     */
    private Task task;

    @Override
    @Transactional(readOnly = true)
    public void init(Task task) {
        this.task = task;
    }

    @Override
    public int getRepetitions() {
        if ((task == null) || (task.getRecurrenceInformation() == null)) {
                return 0;
        }
        return task.getRecurrenceInformation().getRepetitions();
    }

    @Override
    public void setRepetitions(int repetitions) {
        // TODO: The recurrence information should be created
        // always and it is not. Fixme.
        if (task != null) {
            if (task.getRecurrenceInformation() == null) {
                task.setRecurrenceInformation(new RecurrenceInformation());
            }
            task.getRecurrenceInformation().setRepetitions(repetitions);
        }
    }

    @Override
    public void setRecurrencePeriodicity(RecurrencePeriodicity periodicity) {
        // TODO: The recurrence information should be created
        // always and it is not. Fixme.
        if (task != null) {
            if (task.getRecurrenceInformation() == null) {
                task.setRecurrenceInformation(new RecurrenceInformation());
            }
            task.getRecurrenceInformation().setPeriodicity(periodicity);
        }
    }

    @Override
    public RecurrencePeriodicity getPeriodicity() {
        return task.getRecurrenceInformation().getPeriodicity();
    }

}
