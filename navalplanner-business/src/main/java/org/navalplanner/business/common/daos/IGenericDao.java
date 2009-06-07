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
     * It updates and inserts the object passed as a parameter.
     */
    public void save(E entity);

    public E find(PK id) throws InstanceNotFoundException;

    public boolean exists(PK id);

    public void remove(PK id) throws InstanceNotFoundException;

    public <T extends E> List<T> list(Class<T> klass);

}
