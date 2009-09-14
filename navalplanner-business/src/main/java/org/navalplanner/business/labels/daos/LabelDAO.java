package org.navalplanner.business.labels.daos;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.labels.entities.Label;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link Label}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelDAO extends GenericDAOHibernate<Label, Long> implements
        ILabelDAO {

}
