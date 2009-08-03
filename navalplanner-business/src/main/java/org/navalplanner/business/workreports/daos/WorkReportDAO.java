package org.navalplanner.business.workreports.daos;

import org.navalplanner.business.common.daos.impl.GenericDAOHibernate;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link WorkReportDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportDAO extends GenericDAOHibernate<WorkReport, Long>
        implements IWorkReportDAO {

}
