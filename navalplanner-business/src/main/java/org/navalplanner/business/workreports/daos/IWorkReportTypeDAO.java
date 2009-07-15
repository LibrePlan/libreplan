package org.navalplanner.business.workreports.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link WorkReportType}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public interface IWorkReportTypeDAO extends IGenericDao<WorkReportType, Long> {

}
