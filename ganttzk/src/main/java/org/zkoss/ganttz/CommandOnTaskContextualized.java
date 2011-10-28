/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zkoss.ganttz;

import org.zkoss.ganttz.adapters.IDomainAndBeansMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.ContextRelativeToOtherComponent;
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

    public void doAction(TaskComponent taskComponent) {
        doAction(ContextRelativeToOtherComponent.makeRelativeTo(context,
                taskComponent), domainObjectFor(taskComponent.getTask()));
    }

    public void doAction(Task task) {
        doAction(domainObjectFor(task));
    }

    private T domainObjectFor(Task task) {
        return mapper.findAssociatedDomainObject(task);
    }

    private void doAction(IContext<T> context, T domainObject) {
        IContextWithPlannerTask<T> contextWithTask = ContextWithPlannerTask
                .create(context, mapper.findAssociatedBean(domainObject));
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
                doAction(choosen);
            }
        };
    }

    public String getIcon() {
        return commandOnTask.getIcon();
    }

    public boolean accepts(TaskComponent taskComponent) {
        T domainObject = domainObjectFor(taskComponent.getTask());
        return commandOnTask.isApplicableTo(domainObject);
    }

    public IDomainAndBeansMapper<T> getMapper() {
        return this.mapper;
    }
}
