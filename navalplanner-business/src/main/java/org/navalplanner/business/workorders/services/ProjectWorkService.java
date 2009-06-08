package org.navalplanner.business.workorders.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link IProjectWorkService} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class ProjectWorkService implements IProjectWorkService {

    @Autowired
    private SessionFactory sessionFactory;

    /*
     * Because the dao for project work doesn't have special needs, it's not
     * created an interface for defining its contract
     */

    private GenericDaoHibernate<ProjectWork, Long> dao = new GenericDaoHibernate<ProjectWork, Long>() {

        @Override
        protected Session getSession() {
            return sessionFactory.getCurrentSession();
        }
    };

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ProjectWork projectWork) {
        return dao.exists(projectWork.getId());
    }

    @Override
    public void save(ProjectWork projectWork) throws ValidationException {
        if (projectWork.isEndDateBeforeStart()) {
            throw new ValidationException("endDate must be after startDate");
        }
        dao.save(projectWork);
    }

    @Override
    public List<ProjectWork> getProjectWorks() {
        return dao.list(ProjectWork.class);
    }

    @Override
    public ProjectWork find(Long projectWorkId)
            throws InstanceNotFoundException {
        return dao.find(projectWorkId);
    }

    @Override
    public void remove(ProjectWork projectWork)
            throws InstanceNotFoundException {
        dao.remove(projectWork.getId());
    }

    @Override
    public <T> T onTransaction(OnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
