package org.navalplanner.business.resources.daos;

import java.util.List;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Hibernate DAO for the <code>Resource</code> entity.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceDAO extends GenericDAOHibernate<Resource, Long> implements
        IResourceDAO {
    @Override
    public List<Worker> getWorkers() {
        return list(Worker.class);
    }
}
