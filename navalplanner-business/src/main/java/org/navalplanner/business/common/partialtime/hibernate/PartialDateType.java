/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.sql.Timestamp;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.navalplanner.business.common.partialtime.PartialDate;

/**
 * Persists a {@link PartialDate} through hibernate. <br />
 * @author Óscar González Fernández<ogonzalez@igalia.com>
 */
public class PartialDateType implements UserType {
    // TODO consider the possibility of using an integer for storing the enum
    private static final int[] SQL_TYPES = { Types.TIMESTAMP, Types.VARCHAR };

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class returnedClass() {
        return PartialDate.class;
    }

    public static class CachedRepresentation implements Serializable {
        private final Serializable[] fields;

        private CachedRepresentation(Serializable[] fields) {
            this.fields = fields;
        }

        public PartialDate toOriginal() {
            return PartialDate.createFromDataForPersistence(fields[0],
                    fields[1]);
        }
    }

    @Override
    public PartialDate assemble(Serializable cached, Object owner)
            throws HibernateException {
        CachedRepresentation representation = (CachedRepresentation) cached;
        return representation.toOriginal();
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        PartialDate partialDate = (PartialDate) value;
        return new CachedRepresentation(partialDate.getDataForPersistence());
    }

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
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public PartialDate nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        Timestamp timestamp = (Timestamp) Hibernate.TIMESTAMP.nullSafeGet(rs,
                names[0]);
        String granularity = (String) Hibernate.STRING
                .nullSafeGet(rs, names[1]);
        if (timestamp == null || granularity == null) {
            return null;
        }
        return PartialDate.createFromDataForPersistence(timestamp, granularity);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        Timestamp timeToSet = null;
        String granularityToSet = null;
        PartialDate partialDate = (PartialDate) value;
        if (partialDate != null) {
            Serializable[] dataForPersistence = partialDate
                    .getDataForPersistence();
            timeToSet = (Timestamp) dataForPersistence[0];
            granularityToSet = (String) dataForPersistence[1];
        }
        Hibernate.TIMESTAMP.nullSafeSet(st, timeToSet, index);
        Hibernate.STRING.nullSafeSet(st, granularityToSet, index + 1);
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }
}
