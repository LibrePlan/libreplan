/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.common;

import java.util.List;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.entities.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConfigurationModel implements IConfigurationModel {

    /**
     * Conversation state
     */
    private Configuration configuration;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public BaseCalendar getDefaultCalendar() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDefaultCalendar();
    }

    @Override
    @Transactional(readOnly = true)
    public void init() {
        this.configuration = getCurrentConfiguration();
    }

    private Configuration getCurrentConfiguration() {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration == null) {
            configuration = Configuration.create();
        }
        forceLoad(configuration);
        return configuration;
    }

    private void forceLoad(Configuration configuration) {
        configuration.getDefaultCalendar().getName();
    }

    @Override
    public void setDefaultCalendar(BaseCalendar calendar) {
        if (configuration != null) {
            configuration.setDefaultCalendar(calendar);
        }
    }

    @Override
    @Transactional
    public void confirm() {
        configurationDAO.save(configuration);
    }

    @Override
    @Transactional(readOnly = true)
    public void cancel() {
        configuration = getCurrentConfiguration();
    }

}