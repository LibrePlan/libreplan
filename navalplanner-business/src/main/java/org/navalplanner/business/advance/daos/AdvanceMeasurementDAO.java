package org.navalplanner.business.advance.daos;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link AdvanceMeasurement}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceMeasurementDAO extends GenericDaoHibernate<AdvanceMeasurement, Long> implements IAdvanceMeasurementDAO{
}