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

package org.navalplanner.ws.resources.api;

import javax.ws.rs.core.Response;

import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;

/**
 * Service for managing resources.<br/><br/>
 *
 * NOTE: When a resource can not be imported because one of its criterion
 * satisfactions uses an incorrect criterion type name or criterion name, no
 * other validations on such a resource are executed (because the corresponding
 * criterion satisfaction can not be constructed, and in consequence, the
 * resource itself can not be constructed).
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IResourceService {

    public InstanceConstraintViolationsListDTO addResources(
        ResourceListDTO resources);

    public ResourceListDTO getResources();

    Response getResource(String code);

}
