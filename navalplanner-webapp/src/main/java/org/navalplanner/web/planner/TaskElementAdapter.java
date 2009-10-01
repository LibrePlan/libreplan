/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * Responsible of adaptating a {@link TaskElement} into a
 * {@link ITaskFundamentalProperties} <br />
 * This class is managed by spring so version checking and reatachments are
 * allowed, but they're not used by now.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementAdapter implements ITaskElementAdapter {

    private Order order;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    public TaskElementAdapter() {
    }

    private class TaskElementWrapper implements ITaskFundamentalProperties {

        private final TaskElement taskElement;
        private long lengthMilliseconds;

        protected TaskElementWrapper(TaskElement taskElement) {
            this.taskElement = taskElement;
            this.lengthMilliseconds = taskElement.getEndDate().getTime()
                    - taskElement.getStartDate().getTime();
        }

        @Override
        public void setName(String name) {
            taskElement.setName(name);
        }

        @Override
        public void setNotes(String notes) {
            taskElement.setNotes(notes);
        }

        @Override
        public String getName() {
            return taskElement.getName();
        }

        @Override
        public String getNotes() {
            return taskElement.getNotes();
        }

        @Override
        public Date getBeginDate() {
            return taskElement.getStartDate();
        }

        @Override
        public long getLengthMilliseconds() {
            return lengthMilliseconds;
        }

        @Override
        public void setBeginDate(Date beginDate) {
            taskElement.setStartDate(beginDate);
            updateEndDate();
        }

        @Override
        public void setLengthMilliseconds(long lengthMilliseconds) {
            this.lengthMilliseconds = lengthMilliseconds;
            updateEndDate();
        }

        private void updateEndDate() {
            taskElement.setEndDate(new Date(getBeginDate().getTime()
                    + this.lengthMilliseconds));
        }

        @Override
        public BigDecimal getHoursAdvancePercentage() {
            OrderElement orderElement = taskElement.getOrderElement();
            return orderElementDAO.getHoursAdvancePercentage(orderElement);
        }

        @Override
        public BigDecimal getAdvancePercentage() {
            return taskElement.getOrderElement().getAdvancePercentage();
        }

    }

    @Override
    public ITaskFundamentalProperties adapt(final TaskElement taskElement) {
        if (taskElement.getName() == null) {
            taskElement.setName(taskElement.getOrderElement().getName());
        }
        if (taskElement.getStartDate() == null) {
            taskElement.setStartDate(order.getInitDate());
        }
        if (taskElement.getEndDate() == null) {
            Integer workHours = taskElement.getWorkHours();
            long endDateTime = taskElement.getStartDate().getTime()
                    + (workHours * 3600l * 1000);
            taskElement.setEndDate(new Date(endDateTime));
        }
        return new TaskElementWrapper(taskElement);
    }

    @Override
    public List<DomainDependency<TaskElement>> getIncomingDependencies(
            TaskElement taskElement) {
        return toDomainDependencies(taskElement
                .getDependenciesWithThisDestination());
    }

    @Override
    public List<DomainDependency<TaskElement>> getOutcomingDependencies(
            TaskElement taskElement) {
        return toDomainDependencies(taskElement
                .getDependenciesWithThisOrigin());
    }

    private List<DomainDependency<TaskElement>> toDomainDependencies(
            Collection<? extends Dependency> dependencies) {
        List<DomainDependency<TaskElement>> result = new ArrayList<DomainDependency<TaskElement>>();
        for (Dependency dependency : dependencies) {
            result.add(DomainDependency.createDependency(
                    dependency.getOrigin(),
                    dependency.getDestination(), toGanntType(dependency
                            .getType())));
        }
        return result;
    }

    private DependencyType toGanntType(Type type) {
        switch (type) {
        case END_START:
            return DependencyType.END_START;
        case START_START:
            return DependencyType.START_START;
        case END_END:
            return DependencyType.END_END;
        case START_END:
        default:
            throw new RuntimeException(_("{0} not supported yet", type));
        }
    }

    private Type toDomainType(DependencyType type) {
        switch (type) {
        case END_START:
            return Type.END_START;
        case START_START:
            return Type.START_START;
        case END_END:
            return Type.END_END;
        default:
            throw new RuntimeException(_("{0} not supported yet", type));
        }
    }

    @Override
    public void addDependency(DomainDependency<TaskElement> dependency) {
        TaskElement source = dependency.getSource();
        TaskElement destination = dependency.getDestination();
        Type domainType = toDomainType(dependency.getType());
        Dependency.create(source, destination, domainType);
    }

    @Override
    public boolean canAddDependency(DomainDependency<TaskElement> dependency) {
        return true;
    }

    @Override
    public void removeDependency(DomainDependency<TaskElement> dependency) {
        TaskElement source = dependency.getSource();
        Type type = toDomainType(dependency.getType());
        source.removeDependencyWithDestination(dependency.getDestination(),
                type);
    }

}
