package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.extensions.ICommandOnTask;

/**
 * Contract for {@link EditTaskCommand} <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IEditTaskCommand extends ICommandOnTask<TaskElement> {

    void setTaskEditFormComposer(TaskEditFormComposer taskEditFormComposer);

}
