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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.AllocationInput;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IBack;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AdvancedAllocationTabCreator {

    private final class ResultReceiver implements
            IAdvanceAllocationResultReceiver {

        private final CalculatedValue calculatedValue;
        private final AggregateOfResourceAllocations aggregate;

        ResultReceiver(CalculatedValue calculatedValue,
                AggregateOfResourceAllocations aggregate) {
            this.calculatedValue = calculatedValue;
            this.aggregate = aggregate;
        }

        @Override
        public Restriction createRestriction() {
            return Restriction.build(createRestrictionSource());
        }

        private IRestrictionSource createRestrictionSource() {
            return new IRestrictionSource() {

                @Override
                public int getTotalHours() {
                    return aggregate.getTotalHours();
                }

                @Override
                public LocalDate getStart() {
                    return aggregate.getStart();
                }

                @Override
                public LocalDate getEnd() {
                    return aggregate.getEnd();
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return calculatedValue;
                }
            };
        }

        @Override
        public void cancel() {
            // do nothing
        }

        @Override
        public void accepted(AggregateOfResourceAllocations modifiedAllocations) {
            // TODO apply changes
        }
    }

    private static final String ADVANCED_ALLOCATION_VIEW = _("Advanced Allocation");
    private final Mode mode;
    private final IAdHocTransactionService adHocTransactionService;
    private final IOrderDAO orderDAO;
    private AdvancedAllocationController advancedAllocationController;


    public static ITab create(final Mode mode,
            IAdHocTransactionService adHocTransactionService,
            IOrderDAO orderDAO, ITaskElementDAO taskElementDAO) {
        return new AdvancedAllocationTabCreator(mode, adHocTransactionService,
                orderDAO).build();
    }

    private AdvancedAllocationTabCreator(Mode mode,
            IAdHocTransactionService adHocTransactionService,
 IOrderDAO orderDAO) {
        Validate.notNull(mode);
        Validate.notNull(adHocTransactionService);
        Validate.notNull(orderDAO);
        this.adHocTransactionService = adHocTransactionService;
        this.orderDAO = orderDAO;
        this.mode = mode;
    }

    private ITab build() {
        IComponentCreator advanceAllocationComponentCreator = new IComponentCreator() {
            @Override
            public Component create(final Component parent) {
                return adHocTransactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Component>() {
                            @Override
                            public Component execute() {
                                return createComponent(parent);
                            }

                        });
            }

        };
        return new CreatedOnDemandTab(ADVANCED_ALLOCATION_VIEW,
                advanceAllocationComponentCreator) {
            private boolean firstTime = true;
            @Override
            protected void afterShowAction() {
                if (firstTime) {
                    firstTime = false;
                    return;
                }
                adHocTransactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {

                            @Override
                            public Void execute() {
                                resetController();
                                return null;
                            }
                        });
            }
        };
    }

    private Component createComponent(final Component parent) {
        Order order = mode.getOrder();
        return Executions.createComponents("advance_allocation.zul", parent,
                argsWithController(order));
    }

    private Map<String, Object> argsWithController(Order order) {
        Map<String, Object> result = new HashMap<String, Object>();
        advancedAllocationController = new AdvancedAllocationController(
                createBack(), createAllocationInputsFor(order));
        result.put("advancedAllocationController",
                        advancedAllocationController);
        return result;
    }

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private IBack createBack() {
        return new IBack() {
            @Override
            public void goBack() {
                // do nothing
            }
        };
    }

    private List<AllocationInput> createAllocationInputsFor(Order order) {
        Order orderReloaded = reload(order);
        return createAllocationsWithOrderReloaded(orderReloaded);
    }

    private List<AllocationInput> createAllocationsWithOrderReloaded(
            Order orderReloaded) {
        List<AllocationInput> result = new ArrayList<AllocationInput>();
        for (TaskElement taskElement : orderReloaded.getTaskElements()) {
            addAllocations(result, taskElement);
            if (taskElement instanceof Task) {
                Task t = (Task) taskElement;
                result.add(createAllocationInputFor(t));
            }
        }
        return result;
    }

    private void addAllocations(List<AllocationInput> result,
            TaskElement taskElement) {
        if (taskElement instanceof Task) {
            result.add(createAllocationInputFor((Task) taskElement));
        }
        if (!taskElement.isLeaf()) {
            for (TaskElement each : taskElement.getChildren()) {
                addAllocations(result, each);
            }
        }
    }

    private AllocationInput createAllocationInputFor(Task task) {
        Set<ResourceAllocation<?>> resourceAllocations = task
                .getResourceAllocations();
        AggregateOfResourceAllocations aggregate = new AggregateOfResourceAllocations(
                resourceAllocations);
        return new AllocationInput(aggregate, task, new ResultReceiver(task
                .getCalculatedValue(), aggregate));
    }

    private void resetController() {
        Order order = mode.getOrder();
        advancedAllocationController.reset(createBack(),
                createAllocationInputsFor(order));
    }

}
