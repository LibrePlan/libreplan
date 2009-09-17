package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskElementDAO extends IGenericDAO<TaskElement, Long> {

    /**
     * Removes {@link DayAssignment} that have no parent
     */
    void removeOrphanedDayAssignments();

}
