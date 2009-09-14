package org.navalplanner.business.labels.daos;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link LabelType}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelTypeDAO extends GenericDAOHibernate<LabelType, Long> implements
        ILabelTypeDAO {

}
