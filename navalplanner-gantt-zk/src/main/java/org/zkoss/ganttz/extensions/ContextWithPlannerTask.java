package org.zkoss.ganttz.extensions;

import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.zk.ui.Component;

/**
 * An implementation of {@link IContextWithPlannerTask} that wraps another
 * context and specifies the task to be returned by
 * {@link IContextWithPlannerTask#getTask()}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ContextWithPlannerTask<T> implements IContextWithPlannerTask<T> {

    private final IContext<T> context;
    private final Task task;

    public static <T> IContextWithPlannerTask<T> create(IContext<T> context,
            Task task) {
        return new ContextWithPlannerTask<T>(context, task);
    }

    public ContextWithPlannerTask(IContext<T> context, Task task) {
        this.context = context;
        this.task = task;

    }

    public void add(T domainObject) {
        context.add(domainObject);
    }

    public void reload(PlannerConfiguration<?> configuration) {
        context.reload(configuration);
    }

    public void remove(T domainObject) {
        context.remove(domainObject);
    }

    @Override
    public Component getRelativeTo() {
        return context.getRelativeTo();
    }

    @Override
    public void replace(T oldDomainObject, T newDomainObject) {
        context.replace(oldDomainObject, newDomainObject);
    }

    @Override
    public Task getTask() {
        return task;
    }



}
