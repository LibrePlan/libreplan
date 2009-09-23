package org.navalplanner.business.advance.entities;

import org.navalplanner.business.orders.entities.OrderLineGroup;

/**
 * Represents an {@link AdvanceAssignment} that is defined in some of the
 * children of this {@link OrderLineGroup}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class IndirectAdvanceAssignment extends AdvanceAssignment {

    public static IndirectAdvanceAssignment create() {
        IndirectAdvanceAssignment indirectAdvanceAssignment = new IndirectAdvanceAssignment();
        indirectAdvanceAssignment.setNewObject(true);
        return indirectAdvanceAssignment;
    }

    public static IndirectAdvanceAssignment create(boolean reportGlobalAdvance) {
        IndirectAdvanceAssignment advanceAssignment = new IndirectAdvanceAssignment(
                reportGlobalAdvance);
        advanceAssignment.setNewObject(true);
        return advanceAssignment;
    }

    public IndirectAdvanceAssignment() {
        super();
    }

    private IndirectAdvanceAssignment(boolean reportGlobalAdvance) {
        super(reportGlobalAdvance);
    }

}
