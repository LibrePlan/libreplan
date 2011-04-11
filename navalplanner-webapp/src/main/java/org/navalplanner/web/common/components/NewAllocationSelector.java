/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.common.components;

import java.util.List;

import org.navalplanner.business.resources.daos.IResourcesSearcher;
import org.navalplanner.business.resources.daos.IResourcesSearcher.IResourcesQuery;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.I18nHelper;
import org.navalplanner.web.planner.allocation.INewAllocationsAdder;
import org.navalplanner.web.resources.search.NewAllocationSelectorController;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

/**
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         ZK macro component for searching {@link Worker} entities
 *
 */
@SuppressWarnings("serial")
public class NewAllocationSelector extends AllocationSelector {

    private NewAllocationSelectorController selectorController;

    private ResourceAllocationBehaviour behaviour;

    public enum AllocationType {
        GENERIC_WORKERS(_("generic workers allocation")) {
            @Override
            public void addTo(NewAllocationSelectorController controller,
                    INewAllocationsAdder allocationsAdder) {
                allocationsAdder.addGeneric(ResourceEnum.WORKER,
                        controller.getSelectedCriterions(),
                        controller.getSelectedWorkers());
            }

            @Override
            public IResourcesQuery<?> doQueryOn(
                    IResourcesSearcher resourceSearchModel) {
                return resourceSearchModel.searchWorkers();
            }

            @Override
            public String asCaption(List<Criterion> criterions) {
                return Criterion.getCaptionFor(ResourceEnum.WORKER, criterions);
            }
        },
        GENERIC_MACHINES(_("generic machines allocation")) {
            @Override
            public void addTo(
                    NewAllocationSelectorController controller,
                    INewAllocationsAdder allocationsAdder) {
                List<Criterion> criteria = controller.getSelectedCriterions();
                allocationsAdder.addGeneric(
                        ResourceEnum.MACHINE,
                        criteria, controller.getSelectedWorkers());
            }

            @Override
            public IResourcesQuery<?> doQueryOn(
                    IResourcesSearcher resourceSearchModel) {
                return resourceSearchModel.searchMachines();
            }

            @Override
            public String asCaption(List<Criterion> criterions) {
                return Criterion
                        .getCaptionFor(ResourceEnum.MACHINE, criterions);
            }
        },
        SPECIFIC(_("specific allocation")) {
            @Override
            public void addTo(NewAllocationSelectorController controller,
                    INewAllocationsAdder allocationsAdder) {
                allocationsAdder.addSpecific(controller.getSelectedWorkers());
            }

            @Override
            public IResourcesQuery<?> doQueryOn(
                    IResourcesSearcher resourceSearchModel) {
                return resourceSearchModel.searchBoth();
            }

            @Override
            public String asCaption(List<Criterion> criterions) {
                throw new UnsupportedOperationException();
            }
        };

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private final String name;

        private AllocationType(String name) {
            this.name = name;
        }

        public String getName() {
            return I18nHelper._(name);
        }

        public void doTheSelectionOn(Radiogroup radioGroup) {
            for (int i = 0; i < radioGroup.getItemCount(); i++) {
                Radio radio = radioGroup.getItemAtIndex(i);
                if (name.equals(radio.getLabel())) {
                    radioGroup.setSelectedIndex(i);
                    break;
                }
            }
        }

        public abstract void addTo(
                NewAllocationSelectorController newAllocationSelectorController,
                INewAllocationsAdder allocationsAdder);

        public abstract IResourcesQuery<?> doQueryOn(
                IResourcesSearcher resourceSearchModel);

        public abstract String asCaption(List<Criterion> criterions);

    }   // AllocationType

    public NewAllocationSelectorController getController() {
        if (selectorController == null) {
            selectorController = new NewAllocationSelectorController(behaviour);
            try {
                selectorController.doAfterCompose(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selectorController;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = ResourceAllocationBehaviour.valueOf(behaviour);
    }

}
