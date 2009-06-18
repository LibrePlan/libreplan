package org.navalplanner.business.common.daos;

import java.io.Serializable;
import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;

/**
 * The interface all DAOs (Data Access Objects) must implement. In general,
 * a DAO must be implemented for each persistent entity. Concrete DAOs may
 * provide (and usually will provide) additional methods.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 * @param <E> Entity class
 * @param <PK> Primary key class
 */
public interface IGenericDao <E, PK extends Serializable>{

    /**
     * It updates or inserts the instance passed as a parameter. If the
     * instance passed as parameter already exists in the database, the
     * instance is reattached to the underlying ORM session. In this case, if
     * the version field is not equal to the one in the database,
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * is thrown.
     */
    public void save(E entity);

    /**
     * It checks if the version of the instance passed as a parameter is equal
     * to the one in the database. The instance must have methods conforming to
     * the following signatures: <code>java.io.Serializable getId()</code> (to
     * get the key) and <code>[long|Long] getVersion()</code> (to get the
     * version).
     * <br/>
     * If the check is not passed,
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * is thrown. If the key of the entity is <code>null</code> or the entity
     * does not exist in database, the check is considered to be successful.
     * This lets client code to treat creation and modification of instances in
     * a unified way.
     */
    public void checkVersion(E entity);

    /**
     * It sets a WRITE lock on the instance passed as a parameter. The instance
     * must exist in the database and cannot have been previously modified.
     * Other concurrent transactions will be blocked if they try to write (but
     * they can read) on the same persistent instance. If the version field is
     * not equal to the one in the database,
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * is thrown. The lock is released when the transaction finishes.
     */
    public void lock(E entity);

    public E find(PK id) throws InstanceNotFoundException;

    public boolean exists(PK id);

    public void remove(PK id) throws InstanceNotFoundException;

    public <T extends E> List<T> list(Class<T> klass);

}
