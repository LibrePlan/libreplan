package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.ICriterionSatisfactionDAO;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Implementation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CriterionSatisfactionDAO extends
        GenericDaoHibernate<CriterionSatisfaction, Long> implements
        ICriterionSatisfactionDAO {
}
