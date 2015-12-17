package org.libreplan.web.common;

import org.libreplan.business.common.daos.ILimitsDAO;
import org.libreplan.business.common.entities.Limits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Model for operations related to {@link Limits}.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 17.12.15.
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitsModel implements ILimitsModel {

    @Autowired
    private ILimitsDAO limitsDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Limits> getAll() {
        return limitsDAO.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Limits getUsersType() {
        return limitsDAO.getUsersType();
    }

    @Override
    @Transactional(readOnly = true)
    public Limits getWorkersType() {
        return limitsDAO.getWorkersType();
    }
}
