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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.joda.time.LocalDate;

/**
 * Persists a Joda {@link LocalDate} as a native SQL DATE column.
 * <br />
 * Replaces org.jadira.usertype.dateandtime.joda.PersistentLocalDate, which depends on Hibernate 5
 * SPI classes removed in Hibernate 6. Jadira also auto-registered this globally for every bare
 * (no explicit type=) LocalDate property in .hbm.xml mappings - that auto-registration has no
 * Hibernate 6 equivalent, so each such property now needs this type set explicitly.
 */
public class LocalDateType implements UserType<LocalDate> {

    @Override
    public int getSqlType() {
        return Types.DATE;
    }

    @Override
    public Class<LocalDate> returnedClass() {
        return LocalDate.class;
    }

    @Override
    public boolean equals(LocalDate x, LocalDate y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(LocalDate x) {
        return x.hashCode();
    }

    @Override
    public LocalDate nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner) throws SQLException {
        Date sqlDate = rs.getDate(position);
        if (sqlDate == null) {
            return null;
        }
        return LocalDate.fromDateFields(sqlDate);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, LocalDate value, int index,
            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.DATE);
        } else {
            st.setDate(index, new Date(value.toDateTimeAtStartOfDay().getMillis()));
        }
    }

    @Override
    public LocalDate deepCopy(LocalDate value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(LocalDate value) {
        return value;
    }

    @Override
    public LocalDate assemble(Serializable cached, Object owner) {
        return (LocalDate) cached;
    }

    @Override
    public LocalDate replace(LocalDate original, LocalDate target, Object owner) {
        return original;
    }

}
