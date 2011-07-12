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

package org.navalplanner.ws.calendarexceptiontypes.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeDTO;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeListDTO;
import org.navalplanner.ws.calendarexceptiontypes.api.ICalendarExceptionTypeService;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.common.impl.RecoverableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link ICalendarExceptionTypeService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/calendarexceptiontypes/")
@Produces("application/xml")
@Service("calendarExceptionTypeServiceREST")
public class CalendarExceptionTypeServiceREST extends
        GenericRESTService<CalendarExceptionType, CalendarExceptionTypeDTO>
        implements
        ICalendarExceptionTypeService {

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Override
    protected IIntegrationEntityDAO<CalendarExceptionType> getIntegrationEntityDAO() {
        return calendarExceptionTypeDAO;
    }

    @Override
    protected CalendarExceptionTypeDTO toDTO(CalendarExceptionType entity) {
        return CalendarExceptionTypeConverter.toDTO(entity);
    }

    @Override
    protected CalendarExceptionType toEntity(CalendarExceptionTypeDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
        return CalendarExceptionTypeConverter.toEntity(entityDTO);
    }

    @Override
    protected void updateEntity(CalendarExceptionType entity,
            CalendarExceptionTypeDTO entityDTO) throws ValidationException,
            RecoverableErrorException {
        CalendarExceptionTypeConverter.updateCalendarExceptionType(entity,
                entityDTO);
    }

    @Override
    @GET
    @Transactional(readOnly = true)
    public CalendarExceptionTypeListDTO getCalendarExceptionType() {
        return new CalendarExceptionTypeListDTO(findAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addCalendarExceptionTypes(
            CalendarExceptionTypeListDTO calendarExceptionTypeListDTO) {
        return save(calendarExceptionTypeListDTO.calendarExceptionTypes);
    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getCalendarExceptionType(@PathParam("code") String code) {
        return getDTOByCode(code);
    }
}
