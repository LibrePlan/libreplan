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

package org.libreplan.business.planner.entities.consolidations;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.util.deepcopy.AfterCopy;
import org.libreplan.business.util.deepcopy.DeepCopy;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;


/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class NonCalculatedConsolidation extends Consolidation {

    private SortedSet<NonCalculatedConsolidatedValue> consolidatedValues = new TreeSet<NonCalculatedConsolidatedValue>(
            new ConsolidatedValueComparator());

    @AfterCopy
    private void instantiateConsolidatedValuesWithComparator() {
        SortedSet<NonCalculatedConsolidatedValue> previous = consolidatedValues;
        consolidatedValues = new TreeSet<NonCalculatedConsolidatedValue>(
                new ConsolidatedValueComparator());
        consolidatedValues.addAll(previous);
    }

    @OnCopy(Strategy.SHARE)
    private DirectAdvanceAssignment directAdvanceAssignment;

    public static NonCalculatedConsolidation create(Task task,
            DirectAdvanceAssignment directAdvanceAssignment) {
        return create(new NonCalculatedConsolidation(task,
                directAdvanceAssignment));
    }

    public static NonCalculatedConsolidation create(Task task,
            DirectAdvanceAssignment directAdvanceAssignment,
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        return create(new NonCalculatedConsolidation(task,
                directAdvanceAssignment,
                consolidatedValues));
    }

    /**
     * Constructor for {@link DeepCopy}. DO NOT USE!
     */
    public NonCalculatedConsolidation() {

    }

    protected NonCalculatedConsolidation(Task task,
            DirectAdvanceAssignment directAdvanceAssignment,
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        this(task, directAdvanceAssignment);
        this.setConsolidatedValues(consolidatedValues);
    }

    public NonCalculatedConsolidation(Task task,
            DirectAdvanceAssignment directAdvanceAssignment) {
        super(task);
        this.directAdvanceAssignment = directAdvanceAssignment;
    }

    @Override
    public SortedSet<ConsolidatedValue> getConsolidatedValues() {
        TreeSet<ConsolidatedValue> result = new TreeSet<ConsolidatedValue>(
                new Comparator<ConsolidatedValue>() {
                    @Override
                    public int compare(ConsolidatedValue arg0,
                            ConsolidatedValue arg1) {
                        return arg0.getDate().compareTo(arg1.getDate());
                    }
                });
        result.addAll(consolidatedValues);
        return result;
    }

    public SortedSet<NonCalculatedConsolidatedValue> getNonCalculatedConsolidatedValues() {
        return consolidatedValues;
    }

    public void setConsolidatedValues(
            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues) {
        this.consolidatedValues = consolidatedValues;
    }

    public void setDirectAdvanceAssignment(
            DirectAdvanceAssignment directAdvanceAssignment) {
        this.directAdvanceAssignment = directAdvanceAssignment;
    }

    public DirectAdvanceAssignment getDirectAdvanceAssignment() {
        return directAdvanceAssignment;
    }

    public void addConsolidatedValue(NonCalculatedConsolidatedValue value) {
        if (!consolidatedValues.contains(value)) {
            value.setConsolidation(this);
            this.consolidatedValues.add(value);
        }
    }

    @Override
    public boolean isCalculated() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return consolidatedValues.isEmpty();
    }
}
