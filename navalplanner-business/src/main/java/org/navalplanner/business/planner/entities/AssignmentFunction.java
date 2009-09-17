package org.navalplanner.business.planner.entities;

import org.navalplanner.business.common.BaseEntity;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignmentFunction extends BaseEntity {

    public static AssignmentFunction create() {
        return (AssignmentFunction) create(new AssignmentFunction());
    }

    public AssignmentFunction() {

    }
}
