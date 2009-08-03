package org.navalplanner.business.resources.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Hibernate DAO for the <code>Worker</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 *
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkerDAOHibernate extends GenericDAOHibernate<Worker, Long>
    implements IWorkerDAO {

    @Override
    public Worker findUniqueByNif(String nif) throws InstanceNotFoundException {
        Criteria criteria = getSession().createCriteria(Worker.class);
        criteria.add(Restrictions.eq("nif", nif).ignoreCase());

        List<Worker> list = criteria.list();

        if (list.size() != 1) {
            throw new InstanceNotFoundException(nif, Worker.class.getName());
        }

        return list.get(0);
    }

}
