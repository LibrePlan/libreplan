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

package org.navalplanner.web.common.components;

import java.util.List;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.allocation.INewAllocationsAdder;
import org.navalplanner.web.resources.search.NewAllocationSelectorController;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * ZK macro component for searching {@link Worker} entities
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class NewAllocationSelector extends HtmlMacroComponent {

    private INewAllocationsAdder allocationsAdder;

    private List<Worker> getWorkers() {
        NewAllocationSelectorController controller = (NewAllocationSelectorController) this
                .getVariable("controller", true);
        return controller.getSelectedWorkers();
    }

    public void clearAll() {
        NewAllocationSelectorController controller = (NewAllocationSelectorController) this
                .getVariable("controller", true);
        controller.clearAll();
    }

    public void addChoosen() {
        allocationsAdder.addSpecific(getWorkers());
    }

    public void setAllocationsAdder(INewAllocationsAdder allocationsAdder) {
        this.allocationsAdder = allocationsAdder;
    }

}
