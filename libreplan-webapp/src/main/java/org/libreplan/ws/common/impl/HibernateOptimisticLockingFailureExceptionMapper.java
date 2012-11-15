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

package org.libreplan.ws.common.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.libreplan.ws.common.api.ConcurrentModificationErrorDTO;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Exception mapper for {@link HibernateOptimisticLockingFailureExceptionMapper}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Provider
@Component("hibernateOptimisticLockingFailureException")
public class HibernateOptimisticLockingFailureExceptionMapper implements
        ExceptionMapper<HibernateOptimisticLockingFailureException> {

    public Response toResponse(HibernateOptimisticLockingFailureException e) {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ConcurrentModificationErrorDTO(
                        e.getMessage(), Util.getStackTrace(e)))
                .type("application/xml").build();
    }

}