/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.common;

import org.libreplan.business.common.daos.ILimitsDAO;
import org.libreplan.business.common.daos.LimitsDAO;
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
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitsModel implements ILimitsModel {

    public static final String USER_LIMITS_TYPE = "users";
    
    public static final String RESOURCES_LIMITS_TYPE = "workers+machines";

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
        return limitsDAO.getLimitsByType(USER_LIMITS_TYPE);
    }

    @Override
    @Transactional(readOnly = true)
    public Limits getResourcesType() {
        return limitsDAO.getLimitsByType(RESOURCES_LIMITS_TYPE);
    }

}
