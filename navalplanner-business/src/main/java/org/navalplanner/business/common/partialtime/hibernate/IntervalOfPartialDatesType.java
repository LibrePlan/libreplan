/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.common.partialtime.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.navalplanner.business.common.partialtime.IntervalOfPartialDates;
import org.navalplanner.business.common.partialtime.PartialDate;

public class IntervalOfPartialDatesType implements CompositeUserType {

    private static final String[] PROPERTY_NAMES = { "start", "end" };
    private final PartialDateType PARTIAL_DATE_TYPE = new PartialDateType();

    @Override
    public IntervalOfPartialDates assemble(Serializable cached,
            SessionImplementor session,
            Object owner) throws HibernateException {
        Object[] components = (Object[]) cached;
        PartialDate start = PARTIAL_DATE_TYPE.assemble(
                (Serializable) components[0], owner);
        PartialDate end = PARTIAL_DATE_TYPE.assemble(
                (Serializable) components[1], owner);
        return new IntervalOfPartialDates(start, end);
    }

    @Override
    public Serializable disassemble(Object value, SessionImplementor session)
            throws HibernateException {
        IntervalOfPartialDates interval = (IntervalOfPartialDates) value;
        return new Object[] {
                PARTIAL_DATE_TYPE.disassemble(interval.getStart()),
                PARTIAL_DATE_TYPE.disassemble(interval.getEnd()) };
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }


    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public String[] getPropertyNames() {
        return PROPERTY_NAMES.clone();
    }

    @Override
    public Type[] getPropertyTypes() {
        Properties emptyProperties = new Properties();
        CustomType partialDateTypeAsType = new CustomType(
                PartialDateType.class,
                emptyProperties);
        return new Type[] { partialDateTypeAsType, partialDateTypeAsType };
    }

    @Override
    public Object getPropertyValue(Object component, int property)
            throws HibernateException {
        IntervalOfPartialDates interval = (IntervalOfPartialDates) component;
        return property == 0 ? interval.getStart() : interval.getEnd();
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        PartialDate start = PARTIAL_DATE_TYPE.nullSafeGet(rs, subArray(names,
                0, 2), owner);
        PartialDate end = PARTIAL_DATE_TYPE.nullSafeGet(rs, subArray(names, 2,
                2), owner);
        if (start == null || end == null) {
            return null;
        }
        return new IntervalOfPartialDates(start, end);
    }

    private static String[] subArray(String[] array, int initialPosition,
            int size) {
        String[] result = new String[size];
        System.arraycopy(array, initialPosition, result, 0, size);
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SessionImplementor session) throws HibernateException, SQLException {
        IntervalOfPartialDates interval = (IntervalOfPartialDates) value;
        PARTIAL_DATE_TYPE.nullSafeSet(st, interval.getStart(), index);
        PARTIAL_DATE_TYPE.nullSafeSet(st, interval.getEnd(), index + 2);
    }

    @Override
    public Object replace(Object original, Object target,
            SessionImplementor session, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class returnedClass() {
        return IntervalOfPartialDates.class;
    }

    @Override
    public void setPropertyValue(Object component, int property, Object value)
            throws HibernateException {
        throw new UnsupportedOperationException(
                "IntervalOfPartialDates is immutable");
    }

}
