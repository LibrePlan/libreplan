/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.workreports.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.common.impl.RecoverableErrorException;
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

    @Autowired
    private IOrderElementDAO orderElementDAO;

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
                try {
                    orderElementDAO
                            .updateRelatedSumChargedEffortWithWorkReportLineSet(
                            entity.getWorkReportLines());
                } catch (InstanceNotFoundException e) {
                    //should never happen, because the entity has been
                    //validated before, so we are sure the lines refer
                    //to existing order elements
                }
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