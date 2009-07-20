package org.navalplanner.web.planner;

import java.util.List;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.extensions.ICommand;

/**
 * Contract for {@link SaveCommand} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ISaveCommand extends ICommand<TaskElement> {

    public void setState(List<TaskElement> taskElements);

}
