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

package org.zkoss.ganttz.adapters;

import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.data.Position;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IDomainAndBeansMapper<T> {

    /**
     * @param task
     * @return the {@link Position} that specifies the path to reach the task
     */
    Position findPositionFor(Task task);

    /**
     * @param domainObject
     * @return the {@link Position} that specifies the path to reach the task
     *         associated to the domain object
     * @see findPositionFor
     */
    Position findPositionFor(T domainObject);

    /**
     * @param task
     * @return the associated domain object
     * @throws IllegalArgumentException
     *             if <code>taskBean</code> is null or not domain object found
     */
    T findAssociatedDomainObject(Task task)
            throws IllegalArgumentException;

    /**
     * @param domainObject
     * @return the associated {@link Task}
     * @throws IllegalArgumentException
     *             if <code>domainObject</code> is null or not {@link Task}
     *             is found
     */
    Task findAssociatedBean(T domainObject) throws IllegalArgumentException;

    /**
     * Used to know the parents for a given task.
     * @return If the task is top level returns an empty list.<br />
     *         Otherwise it returns the list of parents from more immediate to
     *         most distant parent
     */
    List<? extends TaskContainer> getParents(Task task);

    public Map<T, Task> getMapDomainToTask();
}
