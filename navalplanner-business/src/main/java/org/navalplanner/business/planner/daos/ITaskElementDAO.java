package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskElementDAO extends IGenericDAO<TaskElement, Long> {

    /**
     * Removes {@link DayAssigment} that have no parent
     */
    void removeOrphanedDayAssignments();

}
