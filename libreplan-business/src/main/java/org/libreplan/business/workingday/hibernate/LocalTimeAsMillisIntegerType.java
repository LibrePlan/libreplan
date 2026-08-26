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
import java.sql.Types;
import java.util.Objects;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.joda.time.LocalTime;

/**
 * Persists a {@link LocalTime} as an INTEGER column holding the millis-of-day value.
 * <br />
 * Replaces org.jadira.usertype.dateandtime.joda.PersistentLocalTimeAsMillisInteger, which
 * depends on Hibernate 5 SPI classes removed in Hibernate 6.
 */
public class LocalTimeAsMillisIntegerType implements UserType<LocalTime> {

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<LocalTime> returnedClass() {
        return LocalTime.class;
    }

    @Override
    public boolean equals(LocalTime x, LocalTime y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(LocalTime x) {
        return x.hashCode();
    }

    @Override
    public LocalTime nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner) throws SQLException {
        int millisOfDay = rs.getInt(position);
        if (rs.wasNull()) {
            return null;
        }
        return LocalTime.fromMillisOfDay(millisOfDay);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, LocalTime value, int index,
            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.INTEGER);
        } else {
            st.setInt(index, value.getMillisOfDay());
        }
    }

    @Override
    public LocalTime deepCopy(LocalTime value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(LocalTime value) {
        return value.getMillisOfDay();
    }

    @Override
    public LocalTime assemble(Serializable cached, Object owner) {
        return LocalTime.fromMillisOfDay((Integer) cached);
    }

    @Override
    public LocalTime replace(LocalTime original, LocalTime target, Object owner) {
        return original;
    }

}
