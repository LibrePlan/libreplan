package org.navalplanner.business.workreports.daos;

import java.util.List;
import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.workreports.entities.WorkReportLine;

/**
 * Dao for {@link WorkReportLine}
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IWorkReportLineDAO extends IGenericDao<WorkReportLine, Long>{
    public List<WorkReportLine> findByOrderElement(OrderElement orderElement);
}
