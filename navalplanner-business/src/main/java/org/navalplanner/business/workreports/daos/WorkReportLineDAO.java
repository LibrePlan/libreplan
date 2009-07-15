package org.navalplanner.business.workreports.daos;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link WorkReportLineDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportLineDAO extends
        GenericDaoHibernate<WorkReportLine, Long> implements IWorkReportLineDAO {

}
