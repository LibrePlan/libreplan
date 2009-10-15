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

package org.navalplanner.ws.common.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.navalplanner.ws.common.api.InternalErrorDTO;
import org.springframework.stereotype.Component;

/**
 * Exception mapper for <code>RuntimeException</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Provider
@Component("runtimeExceptionMapper")
public class RuntimeExceptionMapper
    implements ExceptionMapper<RuntimeException> {

    public Response toResponse(RuntimeException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new InternalErrorDTO(e.getMessage(),
                getStackTrace(e))).type("application/xml").build();
    }

    private String getStackTrace(RuntimeException e) {

        StringWriter stringWriter = new StringWriter();

        e.printStackTrace(new PrintWriter(stringWriter));

        return stringWriter.toString();

    }

}
