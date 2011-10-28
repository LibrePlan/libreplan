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

package org.libreplan.web.resources.search;

import java.util.List;

import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.common.components.ResourceAllocationBehaviour;
import org.libreplan.web.planner.allocation.INewAllocationsAdder;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for searching for {@link Resource}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public abstract class AllocationSelectorController extends
        GenericForwardComposer {

    // injected by name
    protected IResourcesSearcher resourcesSearcher;

    // injected by name
    protected ResourceAllocationBehaviour behaviour;

    public AllocationSelectorController() {

    }

    /**
     * Returns list of selected {@link Criterion}, selects only those which are
     * leaf nodes
     * @return
     */
    public abstract List<Criterion> getSelectedCriterions();

    public abstract void onClose();

    public abstract void clearAll();

    public abstract void addTo(INewAllocationsAdder allocationsAdder);

}
