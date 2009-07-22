package org.zkoss.ganttz.extensions;

import org.zkoss.ganttz.adapters.PlannerConfiguration;


/**
 * A facade for operations allowed to extensions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IContext<T> {

    void add(T domainObject);

    void reload(PlannerConfiguration<?> configuration);

}
