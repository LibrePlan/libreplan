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

import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * Converts a domain object into a {@link ITaskFundamentalProperties}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IAdapterToTaskFundamentalProperties<T> {

    public ITaskFundamentalProperties adapt(T object);

    public List<DomainDependency<T>> getOutcomingDependencies(T object);

    public List<DomainDependency<T>> getIncomingDependencies(T object);

    public void doRemovalOf(T object);

    public boolean canAddDependency(DomainDependency<T> dependency);

    public void addDependency(DomainDependency<T> dependency);

    public void removeDependency(DomainDependency<T> dependency);

}
