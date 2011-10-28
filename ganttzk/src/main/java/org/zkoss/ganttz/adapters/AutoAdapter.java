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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AutoAdapter implements
        IAdapterToTaskFundamentalProperties<ITaskFundamentalProperties> {

    @Override
    public ITaskFundamentalProperties adapt(ITaskFundamentalProperties object) {
        return object;
    }

    @Override
    public List<DomainDependency<ITaskFundamentalProperties>> getOutcomingDependencies(
            ITaskFundamentalProperties object) {
        return new ArrayList<DomainDependency<ITaskFundamentalProperties>>();
    }

    @Override
    public List<DomainDependency<ITaskFundamentalProperties>> getIncomingDependencies(
            ITaskFundamentalProperties object) {
        return new ArrayList<DomainDependency<ITaskFundamentalProperties>>();
    }

    @Override
    public void addDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        // do nothing
    }

    @Override
    public boolean canAddDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        return true;
    }

    @Override
    public void removeDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        //do nothing
    }

    @Override
    public void doRemovalOf(ITaskFundamentalProperties object) {
        // do nothing
    }

}
