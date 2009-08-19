package org.navalplanner.business.common.daos;

import java.io.Serializable;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
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
public interface IGenericDAO <E, PK extends Serializable>{

    /**
     * It inserts the object passed as a parameter in the ORM session, planning
     * it for updating (even though it is not modified before or after the call
     * to this method) or insertion, depending if it is was detached or
     * transient. If another instance with the same key already exists in the
     * ORM session, an exception is thrown. When updating, version check is
     * executed (if the entity has version control enabled) with the possible
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * being thrown.
     */
    public void save(E entity);

    /**
     * It inserts the object passed as a parameter in the ORM session. Unlike
     * <code>save</code>, the entity passed as a parameter must not have been
     * modified, and after calling the method, the entity is not considered
     * dirty (but it will be considered dirty if it is modified after calling
     * the method). Like <code>save</code>, if another instance with the same
     * key already exists in the ORM session, an exception is thrown.
     * <p/>
     * The intended use of the method is for reattachment of detached objects
     * which have not been modified before calling this method and will not
     * be modified during the transaction.
     */
    public void reattachUnmodifiedEntity(E entity);

    /**
     * It merges an entity. The caller must discard the reference passed as
     * a parameter and work with the returned reference. Merging is an
     * alternative technique to reattachment with <code>save</code>, which must
     * be used when it cannot be assessed another instance with the same
     * identifier as the one passed as a parameter already exists in the
     * underlying ORM session. Like <code>save</code>, version check is
     * executed when updating.
     * <p/>
     * Since the caller must discard the reference passed as a parameter and
     * work with the returned reference, merging is a technique more complicated
     * than reattachmnent. Reattachment (by using <code>save</code> and/or
     * <code>reattachUnmodifiedEntity</code>) should be the preferred technique.
     *
     * @param entity
     */
    public E merge(E entity);

    /**
     * It checks if the version of the instance passed as a parameter is equal
     * to the one in the database. The instance must have methods conforming to
     * the following signatures: <code>java.io.Serializable getId()</code> (to
     * get the key) and <code>[long|Long] getVersion()</code> (to get the
     * version).
     * <br/>
     * If the check is not passed,
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * is thrown. If the key or the version of the entity is <code>null</code>,
     * or the entity does not exist in database, the check is considered to be
     * successful. This lets client code to treat creation and modification of
     * instances in a unified way.
     */
    public void checkVersion(E entity);

    /**
     * It sets a WRITE lock on the instance passed as a parameter, causing the
     * same kind of reattachment as <code>reattachUnmodifiedEntity</code>.
     * Other concurrent transactions will be blocked if they try to write or
     * set a WRITE lock (but they can read) on the same persistent instance. If
     * the version field is not equal to the one in the database,
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>
     * is thrown. The lock is released when the transaction finishes.
     * <p/>
     * The intended use of this method is to enable pessimistic locking when
     * the version check mechanism is not enough for controlling concurrent
     * access. Most concurrent cases can be automatically managed with the usual
     * version check mechanism.
     */
    public void lock(E entity);

    public E find(PK id) throws InstanceNotFoundException;

    /**
     * Unlike <code>find(PK)</code>, it returns a runtime exception if the
     * entity does not exist. So, this method should be used when the entity
     * with the key passed as a parameter is supposed to exist.
     */
    public E findExistingEntity(PK id);

    public boolean exists(PK id);

    public void remove(PK id) throws InstanceNotFoundException;

    public <T extends E> List<T> list(Class<T> klass);

    public void flush();

}
