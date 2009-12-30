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

package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.reports.dtos.HoursWorkedPerWorkerDTO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HoursWorkedPerWorkerModel implements IHoursWorkedPerWorkerModel {

    @Autowired
    private IWorkerDAO workerDAO;

    @Transactional(readOnly = true)
    public JRDataSource getHoursWorkedPerWorkerReport(List<Worker> workers, Date startingDate, Date endingDate) {
        final List<HoursWorkedPerWorkerDTO> workingHoursPerWorkerList = workerDAO.getWorkingHoursPerWorker(workers, startingDate, endingDate);

        if (workingHoursPerWorkerList != null && !workingHoursPerWorkerList.isEmpty()) {
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            return new JREmptyDataSource();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getWorkers() {
        return workerDAO.getWorkers();
    }

}
