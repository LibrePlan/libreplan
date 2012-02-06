/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;
import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.lessOrEqualThan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph.IGraphChangeListener;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.zk.ui.Component;

/**
 * A object that defines several extension points for gantt planner
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class PlannerConfiguration<T> implements IDisabilityConfiguration {

    public interface IPrintAction {
        public void doPrint();

        public void doPrint(Map<String, String> parameters);

        public void doPrint(HashMap<String, String> parameters, Planner planner);
    }

    public interface IReloadChartListener {
        public void reloadChart();
    }

    private static class NullCommand<T> implements ICommand<T> {

        @Override
        public void doAction(IContext<T> context) {
            // do nothing
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getImage() {
            return "";
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

    }

    private static class NullCommandOnTask<T> implements ICommandOnTask<T> {

        @Override
        public void doAction(IContextWithPlannerTask<T> context, T task) {
            // do nothing
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getIcon() {
            return null;
        }

        @Override
        public boolean isApplicableTo(T task) {
            return true;
        }

    }

    private IAdapterToTaskFundamentalProperties<T> adapter;

    private IStructureNavigator<T> navigator;

    private List<? extends T> data;

    private List<ICommand<T>> globalCommands = new ArrayList<ICommand<T>>();

    private List<ICommandOnTask<T>> commandsOnTasks = new ArrayList<ICommandOnTask<T>>();

    private ICommand<T> goingDownInLastArrowCommand = new NullCommand<T>();

    private ICommandOnTask<T> doubleClickCommand = new NullCommandOnTask<T>();

    private Component chartComponent;

    private boolean addingDependenciesEnabled = true;

    private boolean movingTasksEnabled = true;

    private boolean resizingTasksEnabled = true;

    private boolean editingDatesEnabled = true;

    private GanttDate notBeforeThan = null;

    private GanttDate notAfterThan = null;

    private boolean dependenciesConstraintsHavePriority = false;

    private boolean criticalPathEnabled = true;

    private boolean advancesEnabled = true;

    private boolean reportedHoursEnabled = true;

    private boolean expandAllEnabled = true;

    private boolean flattenTreeEnabled = true;

    private boolean showAllResourcesEnabled = true;

    private boolean renamingTasksEnabled = true;

    private boolean treeEditable = true;

    // private String identifier = null;

    private IDetailItemModificator firstLevelModificators = SeveralModificators
            .empty();

    private IDetailItemModificator secondLevelModificators = SeveralModificators
            .empty();

    private List<IReloadChartListener> reloadChartListeners = new ArrayList<IReloadChartListener>();

    private IPrintAction printAction;

    private boolean expandPlanningViewCharts;

    private final List<IGraphChangeListener> preGraphChangeListeners = new ArrayList<IGraphChangeListener>();

    private final List<IGraphChangeListener> postGraphChangeListeners = new ArrayList<IGraphChangeListener>();

    private boolean scheduleBackwards = false;

    public PlannerConfiguration(IAdapterToTaskFundamentalProperties<T> adapter,
            IStructureNavigator<T> navigator, List<? extends T> data) {
        this.adapter = adapter;
        this.navigator = navigator;
        this.data = data;
    }

    public IAdapterToTaskFundamentalProperties<T> getAdapter() {
        return adapter;
    }

    public IStructureNavigator<T> getNavigator() {
        return navigator;
    }

    public List<? extends T> getData() {
        return data;
    }

    public void addCommandOnTask(ICommandOnTask<T> commandOnTask) {
        Validate.notNull(commandOnTask);
        this.commandsOnTasks.add(commandOnTask);
    }

    public void addGlobalCommand(ICommand<T> command) {
        Validate.notNull(command);
        this.globalCommands.add(command);
    }

    public List<ICommandOnTask<T>> getCommandsOnTasks() {
        return Collections.unmodifiableList(commandsOnTasks);
    }

    public List<ICommand<T>> getGlobalCommands() {
        return Collections.unmodifiableList(globalCommands);
    }

    public ICommand<T> getGoingDownInLastArrowCommand() {
        return goingDownInLastArrowCommand;
    }

    public void setNotBeforeThan(Date notBeforeThan) {
        this.notBeforeThan = GanttDate.createFrom(notBeforeThan);
    }

    public void setNotAfterThan(Date notAfterThan) {
        this.notAfterThan = GanttDate.createFrom(notAfterThan);
    }

    public void setGoingDownInLastArrowCommand(
            ICommand<T> goingDownInLastArrowCommand) {
        Validate.notNull(goingDownInLastArrowCommand);
        this.goingDownInLastArrowCommand = goingDownInLastArrowCommand;
    }

    public ICommandOnTask<T> getDoubleClickCommand() {
        return doubleClickCommand;
    }

    public void setDoubleClickCommand(ICommandOnTask<T> editTaskCommand) {
        Validate.notNull(editTaskCommand);
        this.doubleClickCommand = editTaskCommand;
    }

    public void setChartComponent(Component chartComponent) {
        this.chartComponent = chartComponent;
    }

    public Component getChartComponent() {
        return chartComponent;
    }

    public void setAddingDependenciesEnabled(boolean addingDependenciesEnabled) {
        this.addingDependenciesEnabled = addingDependenciesEnabled;
    }

    @Override
    public boolean isAddingDependenciesEnabled() {
        return addingDependenciesEnabled;
    }

    @Override
    public boolean isMovingTasksEnabled() {
        return movingTasksEnabled;
    }

    public void setMovingTasksEnabled(boolean movingTasksEnabled) {
        this.movingTasksEnabled = movingTasksEnabled;
    }

    @Override
    public boolean isResizingTasksEnabled() {
        return resizingTasksEnabled;
    }

    public void setResizingTasksEnabled(boolean resizingTasksEnabled) {
        this.resizingTasksEnabled = resizingTasksEnabled;
    }

    @Override
    public boolean isEditingDatesEnabled() {
        return editingDatesEnabled;
    }

    public void setEditingDatesEnabled(boolean editingDatesEnabled) {
        this.editingDatesEnabled = editingDatesEnabled;
    }

    public static List<Constraint<GanttDate>> getStartConstraintsGiven(
            GanttDate notBeforeThan) {
        if (notBeforeThan != null) {
            return Collections.singletonList(biggerOrEqualThan(notBeforeThan));
        }
        return Collections.emptyList();
    }

    public List<Constraint<GanttDate>> getStartConstraints() {
        return getStartConstraintsGiven(notBeforeThan);
    }

    public static List<Constraint<GanttDate>> getEndConstraintsGiven(
            GanttDate notAfterThan) {
        if (notAfterThan != null) {
            return Collections.singletonList(lessOrEqualThan(notAfterThan));
        }
        return Collections.emptyList();
    }

    public List<Constraint<GanttDate>> getEndConstraints() {
        return getEndConstraintsGiven(notAfterThan);
    }

    public boolean isDependenciesConstraintsHavePriority() {
        return dependenciesConstraintsHavePriority;
    }

    public void setDependenciesConstraintsHavePriority(boolean haveDependenciesPriority) {
        this.dependenciesConstraintsHavePriority = haveDependenciesPriority;
    }

    public void setCriticalPathEnabled(boolean criticalPathEnabled) {
        this.criticalPathEnabled = criticalPathEnabled;
    }

    @Override
    public boolean isCriticalPathEnabled() {
        return criticalPathEnabled;
    }

    public void setAdvancesEnabled(boolean advancesEnabled) {
        this.advancesEnabled = advancesEnabled;
    }

    @Override
    public boolean isAdvancesEnabled() {
        return advancesEnabled;
    }

    public void setReportedHoursEnabled(boolean reportedHoursEnabled) {
        this.reportedHoursEnabled = reportedHoursEnabled;
    }

    @Override
    public boolean isReportedHoursEnabled() {
        return reportedHoursEnabled;
    }

    public void setExpandAllEnabled(boolean expandAllEnabled) {
        this.expandAllEnabled = expandAllEnabled;
    }

    @Override
    public boolean isExpandAllEnabled() {
        return expandAllEnabled;
    }

    public void setFlattenTreeEnabled(boolean flattenTreeEnabled) {
        this.flattenTreeEnabled = flattenTreeEnabled;
    }

    @Override
    public boolean isShowAllResourcesEnabled() {
        return showAllResourcesEnabled;
    }

    public void setShowAllResourcesEnabled(boolean showAllResourcesEnabled) {
        this.showAllResourcesEnabled = showAllResourcesEnabled;
    }

    @Override
    public boolean isFlattenTreeEnabled() {
        return flattenTreeEnabled;
    }

    public void setRenamingTasksEnabled(boolean renamingTasksEnabled) {
        this.renamingTasksEnabled = renamingTasksEnabled;
    }

    @Override
    public boolean isRenamingTasksEnabled() {
        return renamingTasksEnabled;
    }

    public IDetailItemModificator getSecondLevelModificators() {
        return secondLevelModificators;
    }

    public void setSecondLevelModificators(
            IDetailItemModificator... secondLevelModificators) {
        this.secondLevelModificators = SeveralModificators
                .create(secondLevelModificators);
    }

    public IDetailItemModificator getFirstLevelModificators() {
        return firstLevelModificators;
    }

    public void setFirstLevelModificators(
            IDetailItemModificator... firstLevelModificators) {
        this.firstLevelModificators = SeveralModificators
                .create(firstLevelModificators);
    }

    public void addReloadChartListener(IReloadChartListener reloadChartListener) {
        Validate.notNull(reloadChartListener);
        this.reloadChartListeners.add(reloadChartListener);
    }

    public void reloadCharts() {
        for (IReloadChartListener each : this.reloadChartListeners) {
            each.reloadChart();
        }
    }

    public boolean isPrintEnabled() {
        return printAction != null;
    }

    public void setPrintAction(IPrintAction printAction) {
        this.printAction = printAction;
    }

    public void print() {
        if (!isPrintEnabled()) {
            throw new UnsupportedOperationException("print not supported");
        }
        printAction.doPrint();
    }

    public void print(Map<String, String> parameters) {
        if (!isPrintEnabled()) {
            throw new UnsupportedOperationException("print not supported");
        }
        printAction.doPrint(parameters);
    }

    public void print(HashMap<String, String> parameters, Planner planner) {
        if (!isPrintEnabled()) {
            throw new UnsupportedOperationException("print not supported");
        }
        printAction.doPrint(parameters, planner);
    }

    public void setExpandPlanningViewCharts(boolean expandPlanningViewCharts) {
        this.expandPlanningViewCharts = expandPlanningViewCharts;
    }

    @Override
    public boolean isExpandPlanningViewCharts() {
        return expandPlanningViewCharts;
    }

    public void addPreGraphChangeListener(
            IGraphChangeListener preGraphChangeListener) {
        Validate.notNull(preGraphChangeListener);
        if (!preGraphChangeListeners.contains(preGraphChangeListener)) {
            preGraphChangeListeners.add(preGraphChangeListener);
        }
    }

    public void addPostGraphChangeListener(
            IGraphChangeListener postGraphChangeListener) {
        Validate.notNull(postGraphChangeListener);
        if (!postGraphChangeListeners.contains(postGraphChangeListener)) {
            postGraphChangeListeners.add(postGraphChangeListener);
        }
    }

    public List<IGraphChangeListener> getPreChangeListeners() {
        return Collections.unmodifiableList(preGraphChangeListeners);
    }

    public List<IGraphChangeListener> getPostChangeListeners() {
        return Collections.unmodifiableList(postGraphChangeListeners);
    }

    public void setTreeEditable(boolean treeEditable) {
        this.treeEditable = treeEditable;
    }

    @Override
    public boolean isTreeEditable() {
        return treeEditable;
    }

    public boolean isScheduleBackwards() {
        return scheduleBackwards;
    }

    public void setScheduleBackwards(boolean scheduleBackwards) {
        this.scheduleBackwards = scheduleBackwards;
    }

}
