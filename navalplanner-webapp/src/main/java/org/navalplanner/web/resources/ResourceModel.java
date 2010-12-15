/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2010 Igalia S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.resources;

import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceModel implements IResourceModel {

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    @Transactional(readOnly = true)
    public Boolean isCriticalChainSupportEnabled() {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration == null) {
            return false;
        }
        return configuration.getEnableCriticalChainSupport();
    }
}
