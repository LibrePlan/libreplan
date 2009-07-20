package org.zkoss.ganttz.extensions;


/**
 * A facade for operations allowed to extensions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IContext<T> {

    void add(T domainObject);

}
