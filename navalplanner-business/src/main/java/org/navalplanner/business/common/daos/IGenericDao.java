package org.navalplanner.business.common.daos; 

import java.io.Serializable;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;

public interface IGenericDao <E, PK extends Serializable>{

    /**
     * It updates and inserts the object passed as a parameter.
     */
    public void save(E entity);
        
    public E find(PK id) throws InstanceNotFoundException;
    
    public boolean exists(PK id);

    public void remove(PK id) throws InstanceNotFoundException;
    
}
