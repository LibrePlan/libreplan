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

package org.navalplanner.ws.workreports.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.workreports.api.IWorkReportService;
import org.navalplanner.ws.workreports.api.WorkReportDTO;
import org.navalplanner.ws.workreports.api.WorkReportListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IWorkReportService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/workreports/")
@Produces("application/xml")
@Service("workReportServiceREST")
public class WorkReportServiceREST implements IWorkReportService {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addWorkReports(
            WorkReportListDTO workReportListDTO) {
        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();
        Long numItem = new Long(1);

        for (WorkReportDTO workReportDTO : workReportListDTO.workReports) {
            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;

            try {
                WorkReport workReport = WorkReportConverter
                        .toEntity(workReportDTO);

                workReport.validate();
                workReportDAO.save(workReport);
            } catch (InstanceNotFoundException e) {
                instanceConstraintViolationsDTO = InstanceConstraintViolationsDTO
                        .create(
                                Util.generateInstanceConstraintViolationsDTOId(
                                        numItem, workReportDTO), e.getMessage());
            } catch (ValidationException e) {
                instanceConstraintViolationsDTO = ConstraintViolationConverter
                        .toDTO(Util.generateInstanceConstraintViolationsDTOId(
                                numItem, workReportDTO), e.getInvalidValues());
            }

            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList
                        .add(instanceConstraintViolationsDTO);
            }

            numItem++;
        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);
    }

}