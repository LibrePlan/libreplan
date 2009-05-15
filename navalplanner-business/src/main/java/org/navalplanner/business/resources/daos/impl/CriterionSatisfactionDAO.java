package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.ICriterionSatisfactionDAO;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;

/**
 * Implementation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionSatisfactionDAO extends
        GenericDaoHibernate<CriterionSatisfaction, Long> implements
        ICriterionSatisfactionDAO {
}
