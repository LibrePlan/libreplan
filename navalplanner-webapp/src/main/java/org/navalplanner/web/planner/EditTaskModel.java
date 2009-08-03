package org.navalplanner.web.planner;

import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to edit {@link Task} popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskModel implements IEditTaskModel {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Override
    @Transactional(readOnly = true)
    public Integer getDuration(Task task) {
        taskElementDAO.save(task);

        return task.getDuration();
    }

}
