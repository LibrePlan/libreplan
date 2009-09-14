package org.navalplanner.business.labels.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelTypeDAO extends IGenericDAO<LabelType, Long> {

    List<LabelType> getAll();

    boolean existsByName(LabelType labelType);

}
