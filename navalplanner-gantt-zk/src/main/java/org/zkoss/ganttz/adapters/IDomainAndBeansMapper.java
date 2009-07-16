package org.zkoss.ganttz.adapters;

import org.zkoss.ganttz.util.TaskBean;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IDomainAndBeansMapper<T> {

    /**
     * @param taskBean
     * @return the associated domain object
     * @throws IllegalArgumentException
     *             if <code>taskBean</code> is null or not domain object found
     */
    T findAssociatedDomainObject(TaskBean taskBean)
            throws IllegalArgumentException;

    /**
     * @param domainObject
     * @return the associated {@link TaskBean}
     * @throws IllegalArgumentException
     *             if <code>domainObject</code> is null or not {@link TaskBean}
     *             is found
     */
    TaskBean findAssociatedBean(T domainObject) throws IllegalArgumentException;

}
