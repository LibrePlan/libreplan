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

package org.navalplanner.business.common.partialtime.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

import net.sf.json.JSONObject;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.navalplanner.business.common.partialtime.TimeQuantity;
import org.navalplanner.business.common.partialtime.PartialDate.Granularity;

public class TimeQuantityType implements UserType {

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    private static String asString(TimeQuantity timeQuantity) {
        JSONObject jsonObject = new JSONObject();
        for (Granularity granularity : Granularity.values()) {
            Integer value = timeQuantity.valueFor(granularity);
            if (value != 0)
                jsonObject.put(granularity.name(), value);
        }
        return jsonObject.toString();
    }

    private static TimeQuantity fromString(String timeQuantityAsString) {
        JSONObject jsonObject = JSONObject.fromObject(timeQuantityAsString);
        Iterator keys = jsonObject.keys();
        TimeQuantity result = TimeQuantity.empty();
        while(keys.hasNext()){
            Object key = keys.next();
            Object object = jsonObject.get(key);
            result = result.plus((Integer) object, Granularity
                    .valueOf((String) key));
        }
        return result;
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return fromString((String) cached);
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return asString((TimeQuantity) value);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }


    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;
        return x.equals(y);
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
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        String timeQuantityAsString = (String) Hibernate.STRING.nullSafeGet(rs,
                names);
        return fromString(timeQuantityAsString);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        Hibernate.STRING.nullSafeSet(st, asString((TimeQuantity) value), index);
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

    @Override
    public Class returnedClass() {
        return TimeQuantity.class;
    }

}
