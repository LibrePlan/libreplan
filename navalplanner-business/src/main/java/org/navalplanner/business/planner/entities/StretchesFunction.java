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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;

/**
 * Assignment function by stretches.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class StretchesFunction extends AssignmentFunction {

    public static StretchesFunction create() {
        return (StretchesFunction) create(new StretchesFunction());
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public StretchesFunction() {
    }

    private List<Stretch> stretches = new ArrayList<Stretch>();

    public void setStretches(List<Stretch> stretches) {
        this.stretches = stretches;
    }

    private void sortStretches() {
        Collections.sort(stretches, new Comparator<Stretch>() {
            @Override
            public int compare(Stretch o1, Stretch o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }

    @Valid
    public List<Stretch> getStretches() {
        sortStretches();
        return Collections.unmodifiableList(stretches);
    }

    public void addStretch(Stretch stretch) {
        stretches.add(stretch);
    }

    public void removeStretch(Stretch stretch) {
        stretches.remove(stretch);
    }

    public void removeAllStretches() {
        stretches.clear();
    }

    @AssertTrue(message = "At least one stretch is needed")
    public boolean checkNoEmpty() {
        return !stretches.isEmpty();
    }

    @AssertTrue(message = "Some stretch has higher or equal values than the "
            + "previous stretch")
    public boolean checkStretchesOrder() {
        if (stretches.isEmpty()) {
            return false;
        }

        sortStretches();

        Iterator<Stretch> iterator = stretches.iterator();
        Stretch previous = iterator.next();
        while (iterator.hasNext()) {
            Stretch current = iterator.next();
            if (current.getDate().compareTo(previous.getDate()) <= 0) {
                return false;
            }
            if (current.getLengthPercentage().compareTo(
                    previous.getLengthPercentage()) <= 0) {
                return false;
            }
            if (current.getAmountWorkPercentage().compareTo(
                    previous.getAmountWorkPercentage()) <= 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    @AssertTrue(message = "Last stretch should have one hundred percent for "
            + "length and amount of work percentage")
    public boolean checkOneHundredPercent() {
        if (stretches.isEmpty()) {
            return false;
        }
        sortStretches();

        Stretch lastStretch = stretches.get(stretches.size() - 1);
        if (lastStretch.getLengthPercentage().compareTo(BigDecimal.ONE) != 0) {
            return false;
        }
        if (lastStretch.getAmountWorkPercentage().compareTo(BigDecimal.ONE) != 0) {
            return false;
        }

        return true;
    }

}
