/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

/**
 * An XMLAdapaters that will be applied within this package.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
        @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(type = DateTime.class, value = org.libreplan.importers.tim.TimDateTimeAdapter.class),
        @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(type = LocalDate.class, value = org.libreplan.importers.tim.TimLocalDateAdapter.class),
        @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(type = Date.class, value = org.libreplan.importers.tim.TimTimeAdapter.class)
})
package org.libreplan.importers.tim;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
