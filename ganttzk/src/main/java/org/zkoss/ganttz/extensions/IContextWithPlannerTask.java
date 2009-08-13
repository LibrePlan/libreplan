package org.zkoss.ganttz.extensions;

import org.zkoss.ganttz.data.Task;

/**
 * A context that adds a method to retrieve the task associated to the action
 * performed.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IContextWithPlannerTask<T> extends IContext<T> {

    /**
     * @return the task associated to the action
     */
    public Task getTask();

}
