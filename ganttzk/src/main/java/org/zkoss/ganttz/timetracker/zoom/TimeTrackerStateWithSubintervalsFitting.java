/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.zkoss.ganttz.timetracker.zoom;

import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;

/**
 * @author Óscar González Fernández
 *
 */
public abstract class TimeTrackerStateWithSubintervalsFitting extends
        TimeTrackerState {

    protected TimeTrackerStateWithSubintervalsFitting(
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator) {
        super(firstLevelModificator, secondLevelModificator);
    }

    private final class PeriodicalGenerator extends LazyGenerator<DateTime> {
        private final ReadablePeriod period;

        private PeriodicalGenerator(DateTime first, ReadablePeriod period) {
            super(first);
            Validate.notNull(period);
            this.period = period;
        }

        @Override
        protected DateTime next(DateTime last) {
            return last.plus(period);
        }
    }

    @Override
    protected Iterator<DateTime> getPeriodsFirstLevelGenerator(
            final DateTime start) {
        return new PeriodicalGenerator(start, getPeriodFirstLevel());
    }

    @Override
    protected Iterator<DateTime> getPeriodsSecondLevelGenerator(DateTime start) {
        return new PeriodicalGenerator(start, getPeriodSecondLevel());
    }

    protected abstract ReadablePeriod getPeriodFirstLevel();

    protected abstract ReadablePeriod getPeriodSecondLevel();

}
