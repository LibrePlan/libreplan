package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;

/**
 * Contract for edit {@link Task} popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IEditTaskModel {

    /**
     * Returns the duration of a {@link Task} in number of days.
     *
     * @param task
     *            The {@link Task} to get the duration
     * @return The days of the {@link Task}
     */
    Integer getDuration(Task task);

}
