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

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

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
     * <code>xsd:localTime</code> XML type to a <code>LocalTime</code>.<br/>
     * <br/>
     * If the localTime passed as a parameter is <code>null</code>, it also
     * returns <code>null</code>.
     */
    public final static LocalTime toLocalTime(XMLGregorianCalendar date) {

        if (date == null) {
            return null;
        } else {
            if (isDefined(date.getHour()) && isDefined(date.getMinute())
                    && isDefined(date.getSecond())){
                return new LocalTime(date.getHour(), date.getMinute(), date
                    .getSecond());
            }
            return null;
        }

    }

    private static boolean isDefined(int hour){
        return hour != DatatypeConstants.FIELD_UNDEFINED;
    }

    /**
     * It converts a <code>XMLGregorianCalendar</code> representing a
     * <code>xsd:date</code> XML type to a Joda's <code>LocalDate</code>. <br/>
     * <br/>
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

    /**
     * It converts a <code>Date</code> to a <code>XMLGregorianCalendar</code>
     * representing a <code>xsd:date</code> XML type.<br/>
     * <br/>
     *
     * If the date passed as a parameter is <code>null</code>, it also returns
     * <code>null</code>.
     *
     * @throws DatatypeConfigurationException
     */
    public final static XMLGregorianCalendar toXMLGregorianCalendar(
            LocalDate localDate) {
        if (localDate == null) {
            return null;
        } else {
            DatatypeFactory factory;
            try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
            return factory.newXMLGregorianCalendarDate(localDate.getYear(),
                    localDate.getMonthOfYear(), localDate.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED);
        }
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
        if(date == null) {
            return null;
        }
        return toXMLGregorianCalendar(LocalDate.fromDateFields(date));
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(LocalTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            DatatypeFactory factory;
            try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }

            return factory.newXMLGregorianCalendarTime(dateTime.getHourOfDay(),
                    dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute(),
                    dateTime.getMillisOfSecond(),
                    DatatypeConstants.FIELD_UNDEFINED);
        }
    }
}
