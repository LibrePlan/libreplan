package org.libreplan.business.common.daos;

import org.libreplan.business.common.entities.Limits;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for {@link Limits}
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 24.09.15.
 */

@Repository
public class LimitsDAO extends GenericDAOHibernate<Limits, Long> implements ILimitsDAO {

    @Override
    public List<Limits> getAll() {
        return list(Limits.class);
    }

    @Override
    public Limits getUsersType() {
        List<Limits> list = list(Limits.class);
        for (Limits item : list)
            if (item.getType().equals("users")) return item;
        return null;
    }

    @Override
    public Limits getWorkersType() {
        List<Limits> list = list(Limits.class);
        for (Limits item : list)
            if (item.getType().equals("workers")) return item;
        return null;
    }

}
