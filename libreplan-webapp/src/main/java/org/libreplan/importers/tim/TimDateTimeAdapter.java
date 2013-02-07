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

package org.libreplan.importers.tim;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Adapter responsible for converting from <code>DateTime</code> to
 * string(tim-string-datetime) and vice versa
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class TimDateTimeAdapter extends XmlAdapter<String, DateTime> {

    @Override
    public String marshal(DateTime dateTime) throws Exception {
        return dateTime.toString("dd-MM-yyyy");
    }

    @Override
    public DateTime unmarshal(String dateTimeStr) throws Exception {
        DateTimeFormatter fmt = DateTimeFormat
                .forPattern("dd-MM-yyyy HH:mm:ss.SSS");
        return fmt.parseDateTime(dateTimeStr);
    }

}
