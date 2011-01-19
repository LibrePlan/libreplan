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

package org.navalplanner.web.common.typeconverters;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

/**
 * Converter for the type java.util.Date
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
public class TimeConverter implements TypeConverter {

    @Override
    public Object coerceToBean(Object arg0, Component arg1) {
        return null;
    }

    @Override
    public Object coerceToUi(Object object, Component component) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("h:mm a");
        return object != null ? fmt.print((LocalTime) object) : new String("");
    }
}
