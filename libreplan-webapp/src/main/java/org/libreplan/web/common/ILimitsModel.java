package org.libreplan.web.common;

import org.libreplan.business.common.entities.Limits;

import java.util.List;

/**
 * Contract for {@link Limits}
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 17.12.2015.
 */
public interface ILimitsModel {
    List<Limits> getAll();

    Limits getUsersType();
    Limits getWorkersType();
}
