package org.navalplanner.business.workreports.daos;

import java.util.List;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.stereotype.Repository;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * Dao for {@link WorkReportLineDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportLineDAO extends
        GenericDaoHibernate<WorkReportLine, Long> implements IWorkReportLineDAO {

    @Override
    public List<WorkReportLine> findByOrderElement(OrderElement orderElement){
        Criteria c = getSession().createCriteria(WorkReportLine.class).createCriteria("orderElement");
        c.add(Restrictions.idEq(orderElement.getId()));
	return (List<WorkReportLine>)c.list();
    }
}
