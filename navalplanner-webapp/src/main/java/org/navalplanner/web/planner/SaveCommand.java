package org.navalplanner.web.planner;

import java.util.List;

import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.services.ITaskElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.extensions.IContext;

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

    private List<TaskElement> taskElements;

    @Override
    public void setState(List<TaskElement> taskElements) {
        this.taskElements = taskElements;

    }

    @Override
    @Transactional
    public void doAction(IContext<TaskElement> context) {
        for (TaskElement taskElement : taskElements) {
            taskElementService.save(taskElement);
        }
        // TODO redirect to another page or show message
    }

    @Override
    public String getName() {
        return "Gardar";
    }

}
