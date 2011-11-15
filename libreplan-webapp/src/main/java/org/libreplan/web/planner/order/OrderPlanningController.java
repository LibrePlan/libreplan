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

package org.libreplan.web.planner.order;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.ViewSwitcher;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.orders.OrderCRUDController;
import org.libreplan.web.planner.TaskElementPredicate;
import org.libreplan.web.planner.advances.AdvanceAssignmentPlanningController;
import org.libreplan.web.planner.calendar.CalendarAllocationController;
import org.libreplan.web.planner.consolidations.AdvanceConsolidationController;
import org.libreplan.web.planner.taskedition.AdvancedAllocationTaskController;
import org.libreplan.web.planner.taskedition.EditTaskController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.FilterAndParentExpandedPredicates;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.extensions.ContextWithPlannerTask;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
import org.zkoss.ganttz.util.ProfilingLogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderPlanningController implements Composer {

    private static final Log PROFILING_LOG = ProfilingLogFactory
            .getLog(OrderPlanningController.class);

    @Autowired
    private ViewSwitcher viewSwitcher;

    private Map<String, String[]> parameters;

    @Autowired
    private IOrderPlanningModel model;

    private Planner planner;

    @Autowired
    private CalendarAllocationController calendarAllocationController;

    @Autowired
    private EditTaskController editTaskController;

    @Autowired
    private AdvancedAllocationTaskController advancedAllocationTaskController;

    @Autowired
    private AdvanceConsolidationController advanceConsolidationController;

    @Autowired
    private AdvanceAssignmentPlanningController advanceAssignmentPlanningController;

    @Autowired
    private OrderCRUDController orderCRUDController;

    private GenericForwardComposer currentControllerToShow;

    private Order order;

    private TaskElement task;

    private List<ICommand<TaskElement>> additional = new ArrayList<ICommand<TaskElement>>();

    private Vbox orderElementFilter;
    private Datebox filterStartDateOrderElement;
    private Datebox filterFinishDateOrderElement;
    private Checkbox labelsWithoutInheritance;
    private BandboxMultipleSearch bdFiltersOrderElement;
    private Textbox filterNameOrderElement;

    private Popup filterOptionsPopup;

    public OrderPlanningController() {
    }

    public List<org.libreplan.business.planner.entities.TaskElement> getCriticalPath() {
        return planner != null ? planner.getCriticalPath() : null;
    }

    public void setOrder(Order order,
            ICommand<TaskElement>... additionalCommands) {
        Validate.notNull(additionalCommands);
        Validate.noNullElements(additionalCommands);
        this.order = order;
        this.additional = Arrays.asList(additionalCommands);
        if (planner != null) {
            ensureIsInPlanningOrderView();
            updateConfiguration();
        }
    }

    public void setShowedTask(TaskElement task) {
        this.task = task;
    }

    public CalendarAllocationController getCalendarAllocationController() {
        return calendarAllocationController;
    }

    private void ensureIsInPlanningOrderView() {
        viewSwitcher.goToPlanningOrderView();
    }

    public ViewSwitcher getViewSwitcher() {
        return viewSwitcher;
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        this.planner = (Planner) comp;
        String zoomLevelParameter = null;
        if ((parameters != null) && (parameters.get("zoom") != null)
                && !(parameters.isEmpty())) {
            zoomLevelParameter = parameters.get("zoom")[0];
        }
        if (zoomLevelParameter != null) {
            planner.setInitialZoomLevel(ZoomLevel
                    .getFromString(zoomLevelParameter));
        }
        planner.setAreContainersExpandedByDefault(Planner
                .guessContainersExpandedByDefault(parameters));

        planner.setAreShownAdvancesByDefault(Planner
                .guessShowAdvancesByDefault(parameters));

        planner.setAreShownReportedHoursByDefault(Planner
                .guessShowReportedHoursByDefault(parameters));

        orderElementFilter = (Vbox) planner.getFellow("orderElementFilter");
        // Configuration of the order filter
        org.zkoss.zk.ui.Component filterComponent = Executions
                .createComponents("/orders/_orderElementTreeFilter.zul",
                        orderElementFilter, new HashMap<String, String>());
        filterComponent.setVariable("treeController", this, true);
        filterOptionsPopup = (Popup) filterComponent
                .getFellow("filterOptionsPopup");
        filterStartDateOrderElement = (Datebox) filterOptionsPopup
                .getFellow("filterStartDateOrderElement");
        filterFinishDateOrderElement = (Datebox) filterOptionsPopup
                .getFellow("filterFinishDateOrderElement");
        labelsWithoutInheritance = (Checkbox) filterOptionsPopup
                .getFellow("labelsWithoutInheritance");
        bdFiltersOrderElement = (BandboxMultipleSearch) filterComponent
                .getFellow("bdFiltersOrderElement");
        bdFiltersOrderElement.setFinder("taskElementsMultipleFiltersFinder");
        filterNameOrderElement = (Textbox) filterComponent
                .getFellow("filterNameOrderElement");
        filterComponent.setVisible(true);
        updateConfiguration();
    }

    private void updateConfiguration() {
        if (order != null) {
            long time = System.currentTimeMillis();
            model.setConfigurationToPlanner(planner, order, viewSwitcher,
                    editTaskController, advancedAllocationTaskController,
                    advanceAssignmentPlanningController,
                    advanceConsolidationController,
                    calendarAllocationController, additional);
            PROFILING_LOG.info("setConfigurationToPlanner took: "
                    + (System.currentTimeMillis() - time) + " ms");
            planner.updateSelectedZoomLevel();
            showResorceAllocationIfIsNeeded();

        }
    }

    public EditTaskController getEditTaskController() {
        return editTaskController;
    }

    public AdvancedAllocationTaskController getAdvancedAllocationTaskController() {
        return advancedAllocationTaskController;
    }

    public OrderCRUDController getOrderCRUDController() {
        return orderCRUDController;
    }

    public void setURLParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public Order getOrder() {
        return model.getOrder();
    }

    public void onApplyFilter() {
        filterByPredicate(createPredicate());
    }

    private TaskElementPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFiltersOrderElement
                .getSelectedElements();
        Date startDate = filterStartDateOrderElement.getValue();
        Date finishDate = filterFinishDateOrderElement.getValue();
        boolean ignoreLabelsInheritance = labelsWithoutInheritance.isChecked();
        String name = filterNameOrderElement.getValue();

        if (listFilters.isEmpty() && startDate == null && finishDate == null
                && name == null) {
            return null;
        }

        return new TaskElementPredicate(listFilters, startDate, finishDate,
                name, ignoreLabelsInheritance);
    }

    public Checkbox getLabelsWithoutInheritance() {
        return labelsWithoutInheritance;
    }

    public void setLabelsWithoutInheritance(Checkbox labelsWithoutInheritance) {
        this.labelsWithoutInheritance = labelsWithoutInheritance;
    }

    private void filterByPredicate(final TaskElementPredicate predicate) {
        LongOperationFeedback.execute(orderElementFilter, new ILongOperation() {

            @Override
            public void doAction() {
                // FIXME remove or change
                model.forceLoadLabelsAndCriterionRequirements();

                final IContext<?> context = planner.getContext();
                FilterAndParentExpandedPredicates newPredicate = new FilterAndParentExpandedPredicates(
                        context) {
                    @Override
                    public boolean accpetsFilterPredicate(Task task) {
                        if (predicate == null) {
                            return true;
                        }
                        TaskElement taskElement = (TaskElement) context
                                .getMapper()
                                .findAssociatedDomainObject(task);
                        return taskElement.isMilestone()
                                || predicate.accepts(taskElement);
                    }

                };
                newPredicate.setFilterContainers(planner.getPredicate()
                        .isFilterContainers());
                planner.setTaskListPredicate(newPredicate);
            }

            @Override
            public String getName() {
                return _("filtering");
            }

        });
    }

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(org.zkoss.zk.ui.Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDateOrderElement.getValue() != null)
                        && (finishDate.compareTo(filterStartDateOrderElement
                                .getValue()) < 0)) {
                    filterFinishDateOrderElement.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be greater than start date"));
                }
            }

        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(org.zkoss.zk.ui.Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDateOrderElement.getValue() != null)
                        && (startDate.compareTo(filterFinishDateOrderElement
                                .getValue()) > 0)) {
                    filterStartDateOrderElement.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public void showResorceAllocationIfIsNeeded() {
        if ((task != null) && (planner != null)) {

            planner.expandAllAlways();

            Task foundTask = null;
            TaskElement foundTaskElement = null;
            IContext<TaskElement> context = (IContext<TaskElement>) planner
                    .getContext();
            Map<TaskElement, Task> map = context.getMapper()
                    .getMapDomainToTask();

            for (Entry<TaskElement, Task> entry : map.entrySet()) {
                if (task.getId().equals(entry.getKey().getId())) {
                    foundTaskElement = entry.getKey();
                    foundTask = entry.getValue();
                }
            }

            if ((foundTask != null) && (foundTaskElement != null)) {
                IContextWithPlannerTask<TaskElement> contextTask = ContextWithPlannerTask
                        .create(context, foundTask);
                if (this.getCurrentControllerToShow().equals(
                        getEditTaskController())) {
                    this.editTaskController.showEditFormResourceAllocation(
                            contextTask, foundTaskElement,
                            model.getPlanningState());
                } else if (this.getCurrentControllerToShow().equals(
                        this.getAdvanceAssignmentPlanningController())) {
                    getAdvanceAssignmentPlanningController().showWindow(
                            contextTask, foundTaskElement,
                            model.getPlanningState());
                }
            }
        }
    }

    public AdvanceConsolidationController getAdvanceConsolidationController() {
        return advanceConsolidationController;
    }

    public AdvanceAssignmentPlanningController getAdvanceAssignmentPlanningController() {
        return advanceAssignmentPlanningController;
    }

    public void setCurrentControllerToShow(GenericForwardComposer currentControllerToShow) {
        this.currentControllerToShow = currentControllerToShow;
    }

    public GenericForwardComposer getCurrentControllerToShow() {
        return currentControllerToShow;
    }

}
