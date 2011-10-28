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

package org.libreplan.ws.resources.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.ws.resources.api.IResourceHoursService;
import org.libreplan.ws.resources.api.ResourceWorkedHoursDTO;
import org.libreplan.ws.resources.api.ResourceWorkedHoursListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IResourceHoursService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/resourceshours/")
@Produces("application/xml")
@Service("resourceHoursServiceREST")
public class ResourceHoursServiceREST implements IResourceHoursService {

    public static final SimpleDateFormat SERVICE_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd");

    @Autowired
    private IWorkerDAO workerDAO;

    @Override
    @GET
    @Path("/{startDate}/{endDate}")
    @Transactional(readOnly = true)
    public ResourceWorkedHoursListDTO getHoursAllWorkersBetween(
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate) {
        return getHoursOfWorker(null, startDate, endDate);
    }

    @Override
    @GET
    @Path("/{resourceCode}/{startDate}/{endDate}")
    @Transactional(readOnly = true)
    public ResourceWorkedHoursListDTO getHoursOfWorker(
            @PathParam("resourceCode") String resourceCode,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate) {
        List<ResourceWorkedHoursDTO> result = new ArrayList<ResourceWorkedHoursDTO>();

        Date startingDate;
        Date endingDate;
        try {
            startingDate = SERVICE_DATE_FORMAT.parse(startDate);
            endingDate = SERVICE_DATE_FORMAT.parse(endDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<String> workerNifs = null;
        if (resourceCode != null) {
            workerNifs = Arrays.asList(resourceCode);
        }

        List<Object[]> hoursPerWorker = workerDAO
                .getWorkingHoursGroupedPerWorker(workerNifs, startingDate,
                        endingDate);

        for (Object[] pair : hoursPerWorker) {
            ResourceWorkedHoursDTO resourceWorkedHoursDTO = new ResourceWorkedHoursDTO(
                    (String) pair[0], ((Long) pair[1]).intValue());
            result.add(resourceWorkedHoursDTO);
        }

        return new ResourceWorkedHoursListDTO(result, startingDate, endingDate);
    }

}