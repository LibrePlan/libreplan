/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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
package org.libreplan.business.workingday.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Objects;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTime;

/**
 * Persists a Joda {@link DateTime} as a native SQL TIMESTAMP column.
 * <br />
 * Replaces org.jadira.usertype.dateandtime.joda.PersistentDateTime, which depends on Hibernate 5
 * SPI classes removed in Hibernate 6. Jadira also auto-registered this globally for every bare
 * (no explicit type=) DateTime property in .hbm.xml mappings - that auto-registration has no
 * Hibernate 6 equivalent, so each such property now needs this type set explicitly.
 */
public class DateTimeType implements UserType<DateTime> {

    @Override
    public int getSqlType() {
        return Types.TIMESTAMP;
    }

    @Override
    public Class<DateTime> returnedClass() {
        return DateTime.class;
    }

    @Override
    public boolean equals(DateTime x, DateTime y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(DateTime x) {
        return x.hashCode();
    }

    @Override
    public DateTime nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(position);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, DateTime value, int index,
            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.TIMESTAMP);
        } else {
            st.setTimestamp(index, new Timestamp(value.getMillis()));
        }
    }

    @Override
    public DateTime deepCopy(DateTime value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(DateTime value) {
        return value;
    }

    @Override
    public DateTime assemble(Serializable cached, Object owner) {
        return (DateTime) cached;
    }

    @Override
    public DateTime replace(DateTime original, DateTime target, Object owner) {
        return original;
    }

}
