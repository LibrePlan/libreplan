package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.extensions.ICommandOnTask;

public interface IAddMilestoneCommand extends ICommandOnTask<TaskElement> {

    public void setState(PlanningState planningState);

}
