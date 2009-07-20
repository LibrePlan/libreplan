package org.zkoss.ganttz.adapters;

import org.zkoss.ganttz.data.Task;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IDomainAndBeansMapper<T> {

    /**
     * @param task
     * @return the associated domain object
     * @throws IllegalArgumentException
     *             if <code>taskBean</code> is null or not domain object found
     */
    T findAssociatedDomainObject(Task task)
            throws IllegalArgumentException;

    /**
     * @param domainObject
     * @return the associated {@link Task}
     * @throws IllegalArgumentException
     *             if <code>domainObject</code> is null or not {@link Task}
     *             is found
     */
    Task findAssociatedBean(T domainObject) throws IllegalArgumentException;

}
