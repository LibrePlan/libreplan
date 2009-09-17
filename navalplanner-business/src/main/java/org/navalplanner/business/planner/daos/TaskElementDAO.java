package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
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
        deleteAll(getOrphanedDayAssignments());
    }

    private void deleteAll(List<DayAssignment> orphaned) {
        for (DayAssignment dayAssignment : orphaned) {
            getSession().delete(dayAssignment);
        }
    }

    private List<DayAssignment> getOrphanedDayAssignments() {
        List<DayAssignment> orphaned = new ArrayList<DayAssignment>();
        orphaned.addAll(findOrphanedGenericDayAssignments());
        orphaned.addAll(findOrphanedSpecificDayAssignments());
        return orphaned;
    }

    @SuppressWarnings("unchecked")
    private List<GenericDayAssignment> findOrphanedGenericDayAssignments() {
        return getSession().createCriteria(GenericDayAssignment.class).add(
                Restrictions.isNull("genericResourceAllocation")).list();
    }

    @SuppressWarnings("unchecked")
    private List<SpecificDayAssignment> findOrphanedSpecificDayAssignments() {
        return getSession().createCriteria(SpecificDayAssignment.class).add(
                Restrictions.isNull("specificResourceAllocation")).list();
    }
}
