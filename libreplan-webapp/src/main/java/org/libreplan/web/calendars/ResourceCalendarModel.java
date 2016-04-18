/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.calendars;

import org.apache.commons.lang.Validate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link ResourceCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier("subclass")
public class ResourceCalendarModel extends BaseCalendarModel implements IBaseCalendarModel {

    @Override
    public void initCreate() {
        editing = false;
        this.baseCalendar = ResourceCalendar.create();
        this.baseCalendar.setCode("");
    }

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    @Transactional(readOnly = true)
    public void initCreateDerived(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        // the code always is autogenerated when it is created.
        boolean codeGenerated = true;

        this.baseCalendar = getFromDB(baseCalendar)
                .newDerivedResourceCalendar();
        forceLoad(this.baseCalendar);
        this.baseCalendar.setCode("");

        if (codeGenerated) {
            setDefaultCode();
        }
        this.baseCalendar.setCodeAutogenerated(codeGenerated);
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateCopy(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar).newCopyResourceCalendar();
        forceLoad(this.baseCalendar);
        this.baseCalendar.setCode("");

        if (this.baseCalendar.isCodeAutogenerated()) {
            setDefaultCode();
        }
    }

    @Override
    public EntityNameEnum getEntityName() {
        return EntityNameEnum.RESOURCE_CALENDAR;
    }
}
