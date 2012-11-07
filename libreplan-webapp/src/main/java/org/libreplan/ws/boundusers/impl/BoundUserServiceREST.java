/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.ws.boundusers.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.libreplan.web.users.dashboard.IMyTasksAreaModel;
import org.libreplan.ws.boundusers.api.IBoundUserService;
import org.libreplan.ws.boundusers.api.TaskListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST-based implementation of {@link IBoundUserService};
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Path("/bounduser/")
@Produces("application/xml")
@Service("boundUserServiceREST")
public class BoundUserServiceREST implements IBoundUserService {

    @Autowired
    private IMyTasksAreaModel myTasksAreaModel;

    @Override
    @GET
    @Path("/mytasks/")
    public TaskListDTO getTasks() {
        return TaskConverter.toDTO(myTasksAreaModel.getTasks());
    }

}
