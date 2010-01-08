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

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.LocalDate;

/**
 * A converter from <code>java.util.Date</code> to/from
 * <code>javax.xml.datatype.XMLGregorianCalendar</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class DateConverter {

    private DateConverter() {}

    /**
     * It converts a <code>XMLGregorianCalendar</code> representing a
     * <code>xsd:date</code> XML type to a <code>Date</code>.<br/><br/>
     *
     * If the date passed as a parameter is <code>null</code>, it also returns
     * <code>null</code>.
     */
    public final static Date toDate(XMLGregorianCalendar date) {

        if (date == null) {
            return null;
        } else {
            return date.toGregorianCalendar().getTime();
        }

    }

    /**
     * It converts a <code>XMLGregorianCalendar</code> representing a
     * <code>xsd:date</code> XML type to a Joda's <code>LocalDate</code>.
     * <br/><br/>
     *
     * If the date passed as a parameter is <code>null</code>, it also returns
     * <code>null</code>.
     */
    public final static LocalDate toLocalDate(XMLGregorianCalendar date) {

        if (date == null) {
            return null;
        } else {
            return new LocalDate(date.getYear(), date.getMonth(),
                date.getDay());
        }

    }

}
