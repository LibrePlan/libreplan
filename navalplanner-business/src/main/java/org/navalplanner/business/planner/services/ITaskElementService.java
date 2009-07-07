package org.navalplanner.business.planner.services;

import org.navalplanner.business.planner.entities.TaskElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskElementService {

    void save(TaskElement task);

    TaskElement findById(Long id);

}
