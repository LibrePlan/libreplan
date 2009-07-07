package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementDao extends GenericDaoHibernate<TaskElement, Long>
        implements ITaskElementDao {

}
