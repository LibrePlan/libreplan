/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.planner.entities.consolidations;

import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.util.deepcopy.AfterCopy;
import org.navalplanner.business.util.deepcopy.DeepCopy;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class CalculatedConsolidation extends Consolidation {

    private SortedSet<CalculatedConsolidatedValue> consolidatedValues = new TreeSet<CalculatedConsolidatedValue>(
            new ConsolidatedValueComparator());

    @AfterCopy
    private void instantiateConsolidatedValuesWithComparator() {
        SortedSet<CalculatedConsolidatedValue> previous = consolidatedValues;
        consolidatedValues = new TreeSet<CalculatedConsolidatedValue>(
                new ConsolidatedValueComparator());
        consolidatedValues.addAll(previous);
    }

    @OnCopy(Strategy.SHARE)
    private IndirectAdvanceAssignment indirectAdvanceAssignment;

    public static CalculatedConsolidation create(Task task,
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        return create(new CalculatedConsolidation(task,
                indirectAdvanceAssignment));
    }

    public static CalculatedConsolidation create(Task task,
            IndirectAdvanceAssignment indirectAdvanceAssignment,
            SortedSet<CalculatedConsolidatedValue> consolidatedValues) {
        return create(new CalculatedConsolidation(task,
                indirectAdvanceAssignment,
                consolidatedValues));
    }

    /**
     * Constructor for {@link DeepCopy}. DO NOT USE!
     */
    public CalculatedConsolidation() {

    }

    protected CalculatedConsolidation(Task task,
            IndirectAdvanceAssignment indirectAdvanceAssignment,
            SortedSet<CalculatedConsolidatedValue> consolidatedValues) {
        this(task, indirectAdvanceAssignment);
        this.setConsolidatedValues(consolidatedValues);
    }

    public CalculatedConsolidation(Task task,
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        super(task);
        this.indirectAdvanceAssignment = indirectAdvanceAssignment;
    }

    @Override
    public SortedSet<ConsolidatedValue> getConsolidatedValues() {
        SortedSet<ConsolidatedValue> result = new TreeSet<ConsolidatedValue>(
                new ConsolidatedValueComparator());
        result.addAll(consolidatedValues);
        return result;
    }

    public SortedSet<CalculatedConsolidatedValue> getCalculatedConsolidatedValues() {
        return consolidatedValues;
    }

    public void setConsolidatedValues(
            SortedSet<CalculatedConsolidatedValue> consolidatedValues) {
        this.consolidatedValues = consolidatedValues;
    }

    public void setIndirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        this.indirectAdvanceAssignment = indirectAdvanceAssignment;
    }

    public IndirectAdvanceAssignment getIndirectAdvanceAssignment() {
        return indirectAdvanceAssignment;
    }

    public void addConsolidatedValue(CalculatedConsolidatedValue value) {
        if (!consolidatedValues.contains(value)) {
            value.setConsolidation(this);
            this.consolidatedValues.add(value);
        }
    }

    @Override
    public boolean isCalculated() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return consolidatedValues.isEmpty();
    }

}