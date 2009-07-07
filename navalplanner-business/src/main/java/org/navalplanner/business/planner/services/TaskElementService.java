package org.navalplanner.business.planner.services;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.ITaskElementDao;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class TaskElementService implements ITaskElementService {

    @Autowired
    private ITaskElementDao taskElementDao;

    @Override
    public void save(TaskElement task) {
        taskElementDao.save(task);
    }

    @Override
    public TaskElement findById(Long id) {
        try {
            return taskElementDao.find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
