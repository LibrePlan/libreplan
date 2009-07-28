package org.zkoss.ganttz.extensions;

import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Task;
import org.zkoss.zk.ui.Component;
import org.zkoss.ganttz.data.Position;

/**
 * A facade for operations allowed to extensions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IContext<T> {
    /**
     * Adding domainObject to the {@link Planner}. It is transformed using
     * {@link IAdapterToTaskFundamentalProperties} and
     * {@link IStructureNavigator}. It appends the created {@link Task} to the
     * top of the planner
     * @param domainObject
     */
    void add(T domainObject);

    /**
     * Reloading all the {@link Planner} from the configuration
     * @param configuration
     */
    void reload(PlannerConfiguration<?> configuration);

    /**
     * Removing the tasks associated to the domainObject
     * @param domainObject
     * @return the Position in which the domainObject's task was
     */

    Position remove(T domainObject);

    /**
     * Retrieves the component associated to the action performed. Normally it
     * is the {@link Planner}, but it can be other. For example, if the action
     * is performed on a {@link TaskComponent} this method might return the said
     * component.
     * @return the component the action is relative to
     */
    public Component getRelativeTo();

    void replace(T oldDomainObject, T newDomainObject);

    void add(Position position, T domainObject);

}
