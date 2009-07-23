package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.extensions.ICommand;

/**
 * Contract for {@link SaveCommand} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ISaveCommand extends ICommand<TaskElement> {

    public void setState(PlanningState planningState);

}
