/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.limitingresources.LimitingResourcesPanel.IToolbarCommand;
import org.navalplanner.web.planner.order.BankHolidaysMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.resourceload.IFilterChangedListener;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 * Controller for limiting resources view
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourcesController implements Composer {

    @Autowired
    private ILimitingResourceQueueModel limitingResourceQueueModel;

    private List<IToolbarCommand> commands = new ArrayList<IToolbarCommand>();

    private Order filterBy;

    private org.zkoss.zk.ui.Component parent;

    private LimitingResourcesPanel limitingResourcesPanel;

    private TimeTracker timeTracker;

    private final LimitingResourceQueueElementsRenderer limitingResourceQueueElementsRenderer =
        new LimitingResourceQueueElementsRenderer();

    private transient IFilterChangedListener filterChangedListener;

    public LimitingResourcesController() {
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.parent = comp;
        // reload();
    }

    public void reload() {
        // by default show the task by resources
        boolean filterByResources = true;
        reload(filterByResources);
    }

    private void reload(boolean filterByResources) {
        try {
            if (filterBy == null) {
                limitingResourceQueueModel.initGlobalView(filterByResources);
            } else {
                limitingResourceQueueModel.initGlobalView(filterBy,
                        filterByResources);
            }
            timeTracker = buildTimeTracker();
            limitingResourcesPanel = buildLimitingResourcesPanel();
            addListeners();

            this.parent.getChildren().clear();
            this.parent.appendChild(limitingResourcesPanel);
            limitingResourcesPanel.afterCompose();
            addCommands(limitingResourcesPanel);
        } catch (IllegalArgumentException e) {
            try {
                Messagebox.show(_("Limiting resources error") + e, _("Error"),
                        Messagebox.OK, Messagebox.ERROR);
            } catch (InterruptedException o) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addListeners() {
        filterChangedListener = new IFilterChangedListener() {

            @Override
            public void filterChanged(boolean filter) {
                onApplyFilter(filter);
            }
        };
        // this.limitingResourcesPanel.addFilterListener(filterChangedListener);
    }

    public void onApplyFilter(boolean filterByResources) {
        limitingResourcesPanel.clearComponents();
        reload(filterByResources);
    }

    private void addCommands(LimitingResourcesPanel limitingResourcesPanel) {
        limitingResourcesPanel.add(commands.toArray(new IToolbarCommand[0]));
    }

    private TimeTracker buildTimeTracker() {
        return timeTracker = new TimeTracker(limitingResourceQueueModel
                .getViewInterval(), limitingResourceQueueModel
                .calculateInitialZoomLevel(), SeveralModificators.create(),
                SeveralModificators.create(new BankHolidaysMarker()), parent);
    }

    private LimitingResourcesPanel buildLimitingResourcesPanel() {
        LimitingResourcesPanel result = new LimitingResourcesPanel(
                limitingResourceQueueModel.getLimitingResourceQueues(),
                timeTracker);
        result.setVariable("limitingResourcesController", this, true);
        return result;
    }

    public List<LimitingResourceQueueElement> getUnassignedLimitingResourceQueueElements() {
        return limitingResourceQueueModel.getUnassignedLimitingResourceQueueElements();
    }

    public void filterBy(Order order) {
        this.filterBy = order;
    }

    public LimitingResourceQueueElementsRenderer getLimitingResourceQueueElementsRenderer() {
        return limitingResourceQueueElementsRenderer;
    }

    private class LimitingResourceQueueElementsRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            LimitingResourceQueueElement element = (LimitingResourceQueueElement) data;

            row.appendChild(label(getTaskName(element)));
            row.appendChild(label(getOrderName(element)));
            row.appendChild(assignButton(element));
            row.appendChild(automaticQueueing(element));
        }

        private Button assignButton(final LimitingResourceQueueElement element) {
            Button result = new Button();
            result.setLabel("Assign");
            result.setTooltiptext(_("Assign to queue"));
            result.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    // FIXME: assign element to queue
                }
            });
            return result;
        }

        private Checkbox automaticQueueing(final LimitingResourceQueueElement element) {
            Checkbox result = new Checkbox();
            result.setTooltiptext(_("Select for automatic queuing"));
            return result;
        }

        private Label label(String value) {
            return new Label(value);
        }

        private TaskElement getTask(LimitingResourceQueueElement element) {
            return element.getResourceAllocation().getTask();
        }

        private String getTaskName(LimitingResourceQueueElement element) {
            return getTask(element).getName();
        }

        private Order getOrder(LimitingResourceQueueElement element) {
            return limitingResourceQueueModel.getOrderByTask(getTask(element));
        }

        private String getOrderName(LimitingResourceQueueElement element) {
            return getOrder(element).getName();
        }

    }

}
