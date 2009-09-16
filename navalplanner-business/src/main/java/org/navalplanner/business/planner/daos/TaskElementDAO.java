package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.DayAssigment;
import org.navalplanner.business.planner.entities.GenericDayAssigment;
import org.navalplanner.business.planner.entities.SpecificDayAssigment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementDAO extends GenericDAOHibernate<TaskElement, Long>
        implements ITaskElementDAO {

    @Override
    @Transactional
    public void removeOrphanedDayAssignments() {
        deleteAll(getOrphanedDayAssigments());
    }

    private void deleteAll(List<DayAssigment> orphaned) {
        for (DayAssigment dayAssigment : orphaned) {
            getSession().delete(dayAssigment);
        }
    }

    private List<DayAssigment> getOrphanedDayAssigments() {
        List<DayAssigment> orphaned = new ArrayList<DayAssigment>();
        orphaned.addAll(findOrphanedGenericDayAssignments());
        orphaned.addAll(findOrphanedSpecificDayAssignments());
        return orphaned;
    }

    @SuppressWarnings("unchecked")
    private List<GenericDayAssigment> findOrphanedGenericDayAssignments() {
        return getSession().createCriteria(GenericDayAssigment.class).add(
                Restrictions.isNull("genericResourceAllocation")).list();
    }

    @SuppressWarnings("unchecked")
    private List<SpecificDayAssigment> findOrphanedSpecificDayAssignments() {
        return getSession().createCriteria(SpecificDayAssigment.class).add(
                Restrictions.isNull("specificResourceAllocation")).list();
    }
}
