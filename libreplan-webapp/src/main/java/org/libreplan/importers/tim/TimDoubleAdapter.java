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

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter responsible for converting from <code>Double</code> to
 * string(tim-string-double) and vice versa
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class TimDoubleAdapter extends XmlAdapter<String, Double> {

    @Override
    public String marshal(Double value) throws Exception {
        if(value == null) {
            return null;
        }
        return String.format(Locale.GERMAN, "%1$,.2f", value);
    }

    @Override
    public Double unmarshal(String value) throws Exception {
        return DatatypeConverter.parseDouble(value);
    }

}
