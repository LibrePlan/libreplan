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

package org.navalplanner.business.planner.entities.consolidations;

import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class CalculatedConsolidation extends Consolidation {

    private SortedSet<CalculatedConsolidatedValue> consolidatedValues = new TreeSet<CalculatedConsolidatedValue>(
            new ConsolidatedValueComparator());

    private IndirectAdvanceAssignment indirectAdvanceAssignment;

    public static CalculatedConsolidation create(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        return create(new CalculatedConsolidation(indirectAdvanceAssignment));
    }

    public static CalculatedConsolidation create(
            IndirectAdvanceAssignment indirectAdvanceAssignment,
            SortedSet<CalculatedConsolidatedValue> consolidatedValues) {
        return create(new CalculatedConsolidation(indirectAdvanceAssignment,
                consolidatedValues));
    }

    protected CalculatedConsolidation() {

    }

    protected CalculatedConsolidation(
            IndirectAdvanceAssignment indirectAdvanceAssignment,
            SortedSet<CalculatedConsolidatedValue> consolidatedValues) {
        this(indirectAdvanceAssignment);
        this.setConsolidatedValues(consolidatedValues);
    }

    public CalculatedConsolidation(
            IndirectAdvanceAssignment indirectAdvanceAssignment) {
        this.indirectAdvanceAssignment = indirectAdvanceAssignment;
    }

    @Override
    public SortedSet<ConsolidatedValue> getConsolidatedValues() {
        return new TreeSet<ConsolidatedValue>(consolidatedValues);
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

}