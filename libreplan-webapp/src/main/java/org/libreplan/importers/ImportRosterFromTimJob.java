/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A job that import rosters from Tim SOAP server
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ImportRosterFromTimJob extends QuartzJobBean {

    private IImportRosterFromTim importRosterFromTim;

    public IImportRosterFromTim getImportRosterFromTim() {
        return importRosterFromTim;
    }

    public void setImportRosterFromTim(IImportRosterFromTim importRosterFromTim) {
        this.importRosterFromTim = importRosterFromTim;
    }

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        importRosterFromTim.importRosters();
    }


}
