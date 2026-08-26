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
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * Persists a {@link ResourcesPerDay} through hibernate
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ResourcesPerDayType implements UserType<ResourcesPerDay> {

    @Override
    public int getSqlType() {
        return Types.NUMERIC;
    }

    @Override
    public ResourcesPerDay assemble(Serializable cached, Object owner) {
        return ResourcesPerDay.amount((BigDecimal) cached);
    }

    @Override
    public Serializable disassemble(ResourcesPerDay value) {
        return value.getAmount();
    }

    @Override
    public ResourcesPerDay deepCopy(ResourcesPerDay value) {
        return value;
    }

    @Override
    public boolean equals(ResourcesPerDay x, ResourcesPerDay y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(ResourcesPerDay x) {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public ResourcesPerDay nullSafeGet(ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner) throws SQLException {
        BigDecimal bigDecimal = rs.getBigDecimal(position);
        if (bigDecimal == null) {
            return null;
        }
        return ResourcesPerDay.amount(bigDecimal);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, ResourcesPerDay value, int index,
            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.NUMERIC);
        } else {
            st.setBigDecimal(index, value.getAmount());
        }
    }

    @Override
    public ResourcesPerDay replace(ResourcesPerDay original, ResourcesPerDay target, Object owner) {
        return original;
    }

    @Override
    public Class<ResourcesPerDay> returnedClass() {
        return ResourcesPerDay.class;
    }

}
