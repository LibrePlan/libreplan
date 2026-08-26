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
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;

public class EffortDurationType implements UserType<EffortDuration> {

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<EffortDuration> returnedClass() {
        return EffortDuration.class;
    }

    @Override
    public boolean equals(EffortDuration x, EffortDuration y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(EffortDuration x) {
        return x.hashCode();
    }

    @Override
    public EffortDuration nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner) throws SQLException {
        int seconds = rs.getInt(position);
        if (rs.wasNull()) {
            return null;
        }
        return EffortDuration.elapsing(seconds, Granularity.SECONDS);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, EffortDuration value, int index,
            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.INTEGER);
        } else {
            st.setInt(index, value.getSeconds());
        }
    }

    @Override
    public EffortDuration deepCopy(EffortDuration value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(EffortDuration value) {
        return value.getSeconds();
    }

    @Override
    public EffortDuration assemble(Serializable cached, Object owner) {
        return EffortDuration.seconds((Integer) cached);
    }

    @Override
    public EffortDuration replace(EffortDuration original, EffortDuration target, Object owner) {
        return original;
    }

}
