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

import static org.navalplanner.web.I18nHelper._;

import java.util.HashSet;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.allocation.INewAllocationsAdder;
import org.navalplanner.web.resources.search.NewAllocationSelectorController;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.api.Radio;
import org.zkoss.zul.api.Radiogroup;

/**
 * ZK macro component for searching {@link Worker} entities
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class NewAllocationSelector extends HtmlMacroComponent {

    public enum AllocationType {
        SPECIFIC("specific allocation") {
            @Override
            public void addTo(NewAllocationSelectorController controller,
                    INewAllocationsAdder allocationsAdder) {
                allocationsAdder.addSpecific(controller.getSelectedWorkers());
            }
        },
        GENERIC("generic allocation") {
            @Override
            public void addTo(
                    NewAllocationSelectorController controller,
                    INewAllocationsAdder allocationsAdder) {
                allocationsAdder.addGeneric(new HashSet<Criterion>(controller
                        .getSelectedCriterions()), controller
                        .getSelectedWorkers());
            }
        };

        private final String name;

        private AllocationType(String name) {
            this.name = name;
        }

        public String getName() {
            return _(name);
        }

        public static AllocationType getSelected(Radiogroup radioGroup) {
            Radio selectedItemApi = radioGroup.getSelectedItemApi();
            if (selectedItemApi == null) {
                return null;
            }
            String name = selectedItemApi.getValue();
            return AllocationType.valueOf(name);
        }

        public void doTheSelectionOn(Radiogroup radioGroup) {
            for (int i = 0; i < radioGroup.getItemCount(); i++) {
                Radio radio = radioGroup.getItemAtIndexApi(i);
                if (name().equals(radio.getValue())) {
                    radioGroup.setSelectedIndex(i);
                    break;
                }
            }
        }

        public abstract void addTo(
                NewAllocationSelectorController newAllocationSelectorController,
                INewAllocationsAdder allocationsAdder);
    }

    private INewAllocationsAdder allocationsAdder;

    public void clearAll() {
        getController().clearAll();
    }

    private NewAllocationSelectorController getController() {
        return (NewAllocationSelectorController) this
                .getVariable("controller", true);
    }

    public void addChoosen() {

        getController().addTo(allocationsAdder);
    }

    public void setAllocationsAdder(INewAllocationsAdder allocationsAdder) {
        this.allocationsAdder = allocationsAdder;
        if (getController() != null) {
            getController().clearAll();
        }

    }

}
