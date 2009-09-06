package org.navalplanner.business.resources.daos;

import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Hibernate DAO for the <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino Garcia <dpino@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceDAO extends GenericDAOHibernate<Resource, Long> implements
        IResourceDAO {
    @Override
    public List<Worker> getWorkers() {
        return list(Worker.class);
    }

    @Override
    public List<Resource> getAllByCriterions(Set<Criterion> criterions) {
        String strQuery = "SELECT resource "
                + "FROM Resource resource "
                + "LEFT OUTER JOIN resource.criterionSatisfactions criterionSatisfactions "
                + "LEFT OUTER JOIN criterionSatisfactions.criterion criterion "
                + "WHERE criterion IN (:criterions)";
        Query query = getSession().createQuery(strQuery);
        query.setParameterList("criterions", criterions);
        return (List<Resource>) query.list();
    }

}
