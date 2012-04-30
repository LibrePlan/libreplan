/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.ws.workreports.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.daos.ISumChargedEffortDAO;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.impl.GenericRESTService;
import org.libreplan.ws.common.impl.RecoverableErrorException;
import org.libreplan.ws.workreports.api.IWorkReportService;
import org.libreplan.ws.workreports.api.WorkReportDTO;
import org.libreplan.ws.workreports.api.WorkReportListDTO;
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

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ISumChargedEffortDAO sumChargedEffortDAO;

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

    /**
     * It saves (inserts or updates) an entity DTO by using a new transaction.
     *
     * @throws ValidationException if validations are not passed
     * @throws RecoverableErrorException if a recoverable error occurs
     * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
     *
     */
    protected void insertOrUpdate(final WorkReportDTO entityDTO)
        throws ValidationException, RecoverableErrorException {
        /*
         * NOTE: ValidationException and RecoverableErrorException are runtime
         * exceptions. In consequence, if any of them occurs, transaction is
         * automatically rolled back.
         */

        IOnTransaction<Void> save = new IOnTransaction<Void>() {

            @Override
            public Void execute() {

                WorkReport entity = null;
                IIntegrationEntityDAO<WorkReport> entityDAO =
                    getIntegrationEntityDAO();

                /* Insert or update? */
                try {
                    entity = entityDAO.findByCode(entityDTO.code);
                    updateEntity(entity, entityDTO);
                } catch (InstanceNotFoundException e) {
                    entity = toEntity(entityDTO);
                }

                /*
                 * Validate and save (insert or update) the entity.
                 */
                entity.validate();
                sumChargedEffortDAO
                        .updateRelatedSumChargedEffortWithWorkReportLineSet(entity
                                .getWorkReportLines());
                entityDAO.saveWithoutValidating(entity);

                return null;

            }

        };

        transactionService.runOnAnotherTransaction(save);

    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getWorkReport(@PathParam("code") String code) {
        return getDTOByCode(code);
    }
}