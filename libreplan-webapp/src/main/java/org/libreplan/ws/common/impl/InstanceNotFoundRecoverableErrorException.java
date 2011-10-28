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

package org.libreplan.ws.common.impl;

import static org.libreplan.web.I18nHelper._;

import java.util.HashMap;
import java.util.Map;

/**
 * A recoverable error modeling "instance not found".
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@SuppressWarnings("serial")
public class InstanceNotFoundRecoverableErrorException
    extends RecoverableErrorException {

    /**
     *
     * @param type type of the instance not found
     * @param value name of the instance not found
     */
    public InstanceNotFoundRecoverableErrorException(String type,
        String value) {

        super(_("instance not found"),
            RecoverableErrorCodeEnum.INSTANCE_NOT_FOUND);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("type", type);
        properties.put("value", value);

        setProperties(properties);

    }

}
