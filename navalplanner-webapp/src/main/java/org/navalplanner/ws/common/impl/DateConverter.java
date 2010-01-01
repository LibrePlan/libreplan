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

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A converter from <code>java.util.Date</code> to/from
 * <code>javax.xml.datatype.XMLGregorianCalendar</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class DateConverter {

    private DateConverter() {}

    /**
     * It converts a <code>Date</code> to a <code>XMLGregorianCalendar</code>
     * representing a <code>xsd:date</code> XML type. <br/><br/>
     *
     * If the date passed as a parameter is <code>null</code>, it also returns
     * <code>null</code>.
     */
    public final static XMLGregorianCalendar toXMLGregrorianCalendar(
        Date date) {

        if (date == null) {
            return null;
        }

        Calendar dateAsCalendar = Calendar.getInstance();
        dateAsCalendar.setTime(date);
        XMLGregorianCalendar dateAsXMLGregorianCalendar = null;

        try {
            dateAsXMLGregorianCalendar =
               DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                   dateAsCalendar.get(Calendar.YEAR),
                   convertMonthFieldFromCalendarToXMLGregorianCalendar(
                           dateAsCalendar.get(Calendar.MONTH)),
                   dateAsCalendar.get(Calendar.DAY_OF_MONTH),
                   DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        return dateAsXMLGregorianCalendar;

    }

    /**
     * Converts from @{link Calendar} month field format to
     * @{link XMLGregorianCalendar} format.
     *
     * It is needed the conversion because
     * @{link XMLGregorianCalendar} months go from 1 to 12 while
     * @{link Calendar} months go from 0 to 11
     *
     */
    private final static int convertMonthFieldFromCalendarToXMLGregorianCalendar(int month) {
        return month+1;
    }

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

}
