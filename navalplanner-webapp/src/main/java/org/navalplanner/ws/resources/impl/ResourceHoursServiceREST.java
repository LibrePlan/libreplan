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

package org.navalplanner.ws.resources.impl;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.ws.resources.api.IResourceHoursService;
import org.navalplanner.ws.resources.api.ResourceWorkedHoursListDTO;
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

    @Override
    @GET
    @Transactional(readOnly = true)
    public ResourceWorkedHoursListDTO getHoursAllWorkersBetween(Date startDate,
            Date endDate) {
        // TODO Auto-generated method stub
        return null;
    }

}
