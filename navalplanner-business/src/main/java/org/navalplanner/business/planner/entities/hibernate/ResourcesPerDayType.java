package org.navalplanner.business.planner.entities.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

/**
 * Persists a {@link ResourcesPerDay} through hibernate
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ResourcesPerDayType implements UserType {

    private static final int[] SQL_TYPES = { Types.NUMERIC };

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return ResourcesPerDay.amount((BigDecimal) cached);
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        ResourcesPerDay resourcesPerDay = (ResourcesPerDay) value;
        return resourcesPerDay.getAmount();
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
        BigDecimal bigDecimal = (BigDecimal) Hibernate.BIG_DECIMAL.nullSafeGet(
                rs, names[0]);
        if (bigDecimal == null)
            return null;
        return ResourcesPerDay.amount(bigDecimal);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        BigDecimal amount = null;
        if (value != null) {
            amount = ((ResourcesPerDay) value).getAmount();
        }
        Hibernate.BIG_DECIMAL.nullSafeSet(st, amount, index);
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

    @Override
    public Class returnedClass() {
        return ResourcesPerDay.class;
    }

}
