/*
 * This file is part of NavalPlan
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

package org.zkoss.ganttz.data.resourceload;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

public class LoadTimeLine {

    private final String conceptName;
    private final List<LoadPeriod> loadPeriods;

    public LoadTimeLine(String conceptName, List<LoadPeriod> loadPeriods) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = LoadPeriod.sort(loadPeriods);
        this.conceptName = conceptName;
    }

    public List<LoadPeriod> getLoadPeriods() {
        return loadPeriods;
    }

    public String getConceptName() {
        return conceptName;
    }

    private LoadPeriod getFirst() {
        return loadPeriods.get(0);
    }

    private LoadPeriod getLast() {
        return loadPeriods.get(loadPeriods.size() - 1);
    }

    public LocalDate getStart() {
        if (isEmpty()) {
            return null;
        }
        return getFirst().getStart();
    }

    public boolean isEmpty() {
        return loadPeriods.isEmpty();
    }

    public LocalDate getEnd() {
        if (isEmpty()) {
            return null;
        }
        return getLast().getEnd();
    }
}
