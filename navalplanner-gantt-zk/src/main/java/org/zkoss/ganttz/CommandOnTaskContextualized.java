package org.zkoss.ganttz;

import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.ContextWithPlannerTask;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.zk.ui.event.Event;

public class CommandOnTaskContextualized<T> {

    public static <T> CommandOnTaskContextualized<T> create(
            ICommandOnTask<T> commandOnTask, IDomainAndBeansMapper<T> mapper,
            IContext<T> context) {
        return new CommandOnTaskContextualized<T>(commandOnTask, mapper,
                context);
    }

    private final ICommandOnTask<T> commandOnTask;

    private final IContext<T> context;

    private final IDomainAndBeansMapper<T> mapper;

    private CommandOnTaskContextualized(ICommandOnTask<T> commandOnTask,
            IDomainAndBeansMapper<T> mapper, IContext<T> context) {
        this.commandOnTask = commandOnTask;
        this.mapper = mapper;
        this.context = context;
    }

    public void doAction(Task task) {
        doAction(domainObjectFor(task));
    }

    private T domainObjectFor(Task task) {
        return mapper.findAssociatedDomainObject(task);
    }

    private void doAction(IContext<T> context, T domainObject) {
        IContextWithPlannerTask<T> contextWithTask = ContextWithPlannerTask
                .create(context, mapper
                .findAssociatedBean(domainObject));
        commandOnTask.doAction(contextWithTask, domainObject);
    }

    public void doAction(T task) {
        doAction(context, task);
    }

    public String getName() {
        return commandOnTask.getName();
    }

    ItemAction<TaskComponent> toItemAction() {
        return new ItemAction<TaskComponent>() {
            @Override
            public void onEvent(TaskComponent choosen, Event event) {
                doAction(choosen.getTask());
            }
        };
    }

}
