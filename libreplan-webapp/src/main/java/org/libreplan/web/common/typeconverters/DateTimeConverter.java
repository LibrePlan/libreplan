/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.web.common.typeconverters;

import java.util.Date;

import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

/**
 * Converter for the type java.util.Date with an hour minute precision
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
public class DateTimeConverter implements TypeConverter {

    @Override
    public Object coerceToBean(Object arg0, Component arg1) {
        return null;
    }

    @Override
    public Object coerceToUi(Object object, Component component) {
        return Util.formatDateTime((Date) object);
    }

}
