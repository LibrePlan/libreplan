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

package org.navalplanner.ws.workreports.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
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
public class WorkReportServiceREST extends
        GenericRESTService<WorkReport, WorkReportDTO> implements
        IWorkReportService {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public WorkReportListDTO getWorkReports() {
        return new WorkReportListDTO(findAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addWorkReports(
            WorkReportListDTO workReportListDTO) {
        return save(workReportListDTO.workReports);
    }

    @Override
    protected WorkReport toEntity(WorkReportDTO entityDTO) {
        try {
            return WorkReportConverter.toEntity(entityDTO);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    protected WorkReportDTO toDTO(WorkReport entity) {
        return WorkReportConverter.toDTO(entity);
    }

    @Override
    protected IIntegrationEntityDAO<WorkReport> getIntegrationEntityDAO() {
        return workReportDAO;
    }

    @Override
    protected void updateEntity(WorkReport entity, WorkReportDTO entityDTO)
            throws ValidationException {

        WorkReportConverter.updateWorkReport(entity, entityDTO);

    }

}