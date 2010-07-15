/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.reports;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.reports.dtos.HoursWorkedPerResourceDTO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HoursWorkedPerWorkerModel implements IHoursWorkedPerWorkerModel {

    @Autowired
    private IResourceDAO resourceDAO;

    private Set<Resource> selectedResources = new HashSet<Resource>();

    private boolean showReportMessage = false;

    @Transactional(readOnly = true)
    public JRDataSource getHoursWorkedPerWorkerReport(List<Resource> resources,
            Date startingDate, Date endingDate) {

        final List<HoursWorkedPerResourceDTO> workingHoursPerWorkerList = resourceDAO
                .getWorkingHoursPerWorker(resources, startingDate, endingDate);

        if (workingHoursPerWorkerList != null && !workingHoursPerWorkerList.isEmpty()) {
            setShowReportMessage(false);
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            setShowReportMessage(true);
            return new JREmptyDataSource();
        }
    }

    @Override
    public void init() {
        this.selectedResources.clear();
    }

    @Override
    public Set<Resource> getResources() {
        return this.selectedResources;
    }

    @Override
    public void removeSelectedResource(Resource resource) {
        this.selectedResources.remove(resource);
    }

    @Override
    public boolean addSelectedResource(Resource resource) {
        if (this.selectedResources.contains(resource)) {
            return false;
        }
        this.selectedResources.add(resource);
        return true;
    }

    public void setShowReportMessage(boolean showReportMessage) {
        this.showReportMessage = showReportMessage;
    }

    @Override
    public boolean isShowReportMessage() {
        return showReportMessage;
    }

}
