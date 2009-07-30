package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.services.ITaskElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zul.Messagebox;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * A command that saves the changes in the taskElements.
 * It can be considered the final step in the conversation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SaveCommand implements ISaveCommand {

    @Autowired
    private ITaskElementService taskElementService;
    private PlanningState state;

    @Override
    public void setState(PlanningState state) {
        this.state = state;
    }

    @Override
    @Transactional
    public void doAction(IContext<TaskElement> context) {
        for (TaskElement taskElement : state.getTasksToSave()) {
            taskElementService.save(taskElement);
            if (taskElement instanceof Task) {
                if (!((Task) taskElement).isValidResourceAllocationWorkers()) {
                    throw new RuntimeException("The Task '"
                            + taskElement.getName()
                            + "' has some repeated Worker assigned");
                }
            }
        }
        for (TaskElement taskElement : state
                .getToRemove()) {
            taskElementService.remove(taskElement);
        }

        try {
            Messagebox.show("Scheduling saved", "Information", Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getName() {
        return "Gardar";
    }

}
