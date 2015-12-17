package org.libreplan.business.common.daos;

import org.libreplan.business.common.entities.Limits;

import java.util.List;

/**
 * DAO interface for the <code>Limits</code> entity.
 * Contract for {@link LimitsDAO}
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 17.12.2015.
 */
public interface ILimitsDAO extends IGenericDAO<Limits, Long> {
    List<Limits> getAll();

    Limits getUsersType();
    Limits getWorkersType();
}
