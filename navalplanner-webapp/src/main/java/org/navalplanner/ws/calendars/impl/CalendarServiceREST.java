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

package org.navalplanner.ws.calendars.impl;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.ws.calendars.api.BaseCalendarDTO;
import org.navalplanner.ws.calendars.api.BaseCalendarListDTO;
import org.navalplanner.ws.calendars.api.ICalendarService;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.common.impl.RecoverableErrorException;
import org.navalplanner.ws.labels.api.ILabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link ILabelService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/calendars/")
@Produces("application/xml")
@Service("calendarServiceREST")
public class CalendarServiceREST extends
        GenericRESTService<BaseCalendar, BaseCalendarDTO> implements
        ICalendarService {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Override
    protected IBaseCalendarDAO getIntegrationEntityDAO() {
        return baseCalendarDAO;
    }

    @Override
    protected BaseCalendarDTO toDTO(BaseCalendar entity) {
        return CalendarConverter.toDTO(entity);
    }

    @Override
    protected BaseCalendar toEntity(BaseCalendarDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
        return CalendarConverter.toEntity(entityDTO);
    }

    @Override
    protected void updateEntity(BaseCalendar entity, BaseCalendarDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
        CalendarConverter.update(entity, entityDTO);

    }

    @Override
    @GET
    @Transactional(readOnly = true)
    public BaseCalendarListDTO getBaseCalendars() {
        // Avoid ResourceCalendar entities
        List<BaseCalendar> justBaseCalendars = getIntegrationEntityDAO()
                .getBaseCalendars();
        return new BaseCalendarListDTO(toDTO(justBaseCalendars));
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addBaseCalendars(
            BaseCalendarListDTO baseCalendraListDTO) {
        return save(baseCalendraListDTO.baseCalendars);
    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public BaseCalendarDTO getBaseCalendar(@PathParam("code") String code) {
        return findByCode(code);
    }
}