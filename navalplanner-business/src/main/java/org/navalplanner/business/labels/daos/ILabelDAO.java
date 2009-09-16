package org.navalplanner.business.labels.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.labels.entities.Label;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelDAO extends IGenericDAO<Label, Long> {

    List<Label> getAll();

}
