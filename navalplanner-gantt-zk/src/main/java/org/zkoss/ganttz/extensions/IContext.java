package org.zkoss.ganttz.extensions;

import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.zk.ui.Component;


/**
 * A facade for operations allowed to extensions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IContext<T> {

    void add(T domainObject);

    void reload(PlannerConfiguration<?> configuration);

    void remove(T domainObject);

    public Component getRelativeTo();

}
