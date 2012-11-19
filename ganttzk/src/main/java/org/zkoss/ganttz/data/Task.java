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

package org.zkoss.ganttz.data;

import static org.zkoss.ganttz.data.constraint.ConstraintOnComparableValues.biggerOrEqualThan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHook;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHookFactory;
import org.zkoss.ganttz.data.GanttDiagramGraph.INotificationAfterDependenciesEnforcement;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.util.ConstraintViolationNotificator;
import org.zkoss.ganttz.util.WeakReferencedListeners.Mode;

/**
 * This class contains the information of a task. It can be modified and
 * notifies of the changes to the interested parties. <br/>
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public abstract class Task implements ITaskFundamentalProperties {

    public interface IReloadResourcesTextRequested {
        public void reloadResourcesTextRequested();
    }

    private List<IReloadResourcesTextRequested> reloadRequestedListeners = new ArrayList<IReloadResourcesTextRequested>();

    private PropertyChangeSupport fundamentalPropertiesListeners = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport visibilityProperties = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport criticalPathProperty = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport advancesProperty = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport reportedHoursProperty = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport moneyCostBarProperty = new PropertyChangeSupport(
            this);

    private final ITaskFundamentalProperties fundamentalProperties;

    private boolean visible = true;

    private boolean inCriticalPath = false;

    private boolean showingAdvances = false;

    private boolean showingReportedHours = false;

    private boolean showingMoneyCostBar = false;

    private ConstraintViolationNotificator<GanttDate> violationNotificator = ConstraintViolationNotificator
            .create();

    private IDependenciesEnforcerHook dependenciesEnforcerHook = GanttDiagramGraph
            .doNothingHook();

    private final INotificationAfterDependenciesEnforcement notifyDates = new INotificationAfterDependenciesEnforcement() {

        @Override
        public void onStartDateChange(GanttDate previousStart,
                GanttDate previousEnd, GanttDate newStart) {
            fundamentalPropertiesListeners.firePropertyChange("beginDate",
                    previousStart, fundamentalProperties.getBeginDate());
            fireEndDate(previousEnd);
        }

        @Override
        public void onEndDateChange(GanttDate previousEnd, GanttDate newEnd) {
            fireEndDate(previousEnd);
        }

        private void fireEndDate(GanttDate previousEnd) {
            fundamentalPropertiesListeners.firePropertyChange("endDate",
                    previousEnd, fundamentalProperties.getEndDate());
        }
    };

    public Task(ITaskFundamentalProperties fundamentalProperties) {
        this.fundamentalProperties = fundamentalProperties;
    }

    @Override
    public List<Constraint<GanttDate>> getStartConstraints() {
        return violationNotificator.withListener(fundamentalProperties
                .getStartConstraints());
    }

    public List<Constraint<GanttDate>> getEndConstraints() {
        return violationNotificator.withListener(fundamentalProperties
                .getEndConstraints());
    }

    @Override
    public void doPositionModifications(final IModifications modifications) {
        fundamentalProperties.doPositionModifications(new IModifications() {

            @Override
            public void doIt(IUpdatablePosition p) {
                modifications.doIt(position);

            }
        });
    }

    private final IUpdatablePosition position = new IUpdatablePosition() {

        @Override
        public void setEndDate(GanttDate value) {
            if (value == null) {
                return;
            }
            GanttDate previousEnd = fundamentalProperties.getEndDate();
            getFundamentalPropertiesPosition().setEndDate(value);
            dependenciesEnforcerHook.setNewEnd(previousEnd,
                    fundamentalProperties.getEndDate());
        }

        @Override
        public void setBeginDate(GanttDate newStart) {
            GanttDate previousValue = fundamentalProperties.getBeginDate();
            GanttDate previousEnd = fundamentalProperties.getEndDate();
            getFundamentalPropertiesPosition().setBeginDate(newStart);
            dependenciesEnforcerHook.setStartDate(previousValue, previousEnd,
                    newStart);
        }

        @Override
        public void resizeTo(GanttDate newEnd) {
            GanttDate previousEnd = getEndDate();
            getFundamentalPropertiesPosition().resizeTo(newEnd);
            dependenciesEnforcerHook.setNewEnd(previousEnd, newEnd);
        }

        @Override
        public void moveTo(GanttDate date) {
            GanttDate previousStart = getBeginDate();
            GanttDate previousEnd = getEndDate();
            getFundamentalPropertiesPosition().moveTo(date);
            dependenciesEnforcerHook.setStartDate(previousStart, previousEnd,
                    date);
        }

        private IUpdatablePosition getFundamentalPropertiesPosition() {
            final IUpdatablePosition[] result = new IUpdatablePosition[1];
            fundamentalProperties.doPositionModifications(new IModifications() {

                @Override
                public void doIt(IUpdatablePosition position) {
                    result[0] = position;
                }
            });
            assert result[0] != null;
            return result[0];
        }
    };

    public abstract boolean isLeaf();

    public abstract boolean isContainer();

    public abstract boolean isExpanded() throws UnsupportedOperationException;

    public abstract List<Task> getTasks()
            throws UnsupportedOperationException;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        boolean previousValue = this.visible;
        this.visible = visible;
        visibilityProperties.firePropertyChange("visible", previousValue,
                this.visible);
    }

    public boolean isInCriticalPath() {
        return inCriticalPath;
    }

    public void setInCriticalPath(boolean inCriticalPath) {
        boolean previousValue = this.inCriticalPath;
        this.inCriticalPath = inCriticalPath;
        criticalPathProperty.firePropertyChange("inCriticalPath",
                previousValue, this.inCriticalPath);
    }

    public void setShowingAdvances(boolean showingAdvances) {
        boolean previousValue = this.showingAdvances;
        this.showingAdvances = showingAdvances;
        advancesProperty.firePropertyChange("showingAdvances", previousValue,
                this.showingAdvances);
    }

    public boolean isShowingAdvances() {
        return showingAdvances;
    }

    public void setShowingReportedHours(boolean showingReportedHours) {
        boolean previousValue = this.showingReportedHours;
        this.showingReportedHours = showingReportedHours;
        reportedHoursProperty.firePropertyChange("showingReportedHours",
                previousValue, this.showingReportedHours);
    }

    public boolean isShowingReportedHours() {
        return showingReportedHours;
    }

    public void setShowingMoneyCostBar(boolean showingMoneyCostBar) {
        boolean previousValue = this.showingMoneyCostBar;
        this.showingMoneyCostBar = showingMoneyCostBar;
        moneyCostBarProperty.firePropertyChange("showingMoneyCostBar",
                previousValue, this.showingMoneyCostBar);
    }

    public boolean isShowingMoneyCostBar() {
        return showingMoneyCostBar;
    }

    public String getName() {
        return fundamentalProperties.getName();
    }

    public void setName(String name) {
        String previousValue = fundamentalProperties.getName();
        fundamentalProperties.setName(name);
        fundamentalPropertiesListeners.firePropertyChange("name",
                previousValue, name);
    }

    public void registerDependenciesEnforcerHook(
            IDependenciesEnforcerHookFactory<Task> factory) {
        Validate.notNull(factory);
        dependenciesEnforcerHook = factory.create(this, notifyDates);
        Validate.notNull(dependenciesEnforcerHook);
    }

    public void enforceDependenciesDueToPositionPotentiallyModified() {
        dependenciesEnforcerHook.positionPotentiallyModified();
    }

    public GanttDate getBeginDate() {
        return fundamentalProperties.getBeginDate();
    }

    public long getLengthMilliseconds() {
        return getEndDate().toDayRoundedDate().getTime()
                - getBeginDate().toDayRoundedDate().getTime();
    }

    public ReadableDuration getLength() {
        return new Duration(getBeginDate().toDayRoundedDate().getTime(),
                getEndDate().toDayRoundedDate().getTime());
    }

    public void addVisibilityPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.visibilityProperties.addPropertyChangeListener(listener);
    }

    public void addCriticalPathPropertyChangeListener(
            PropertyChangeListener listener) {
        this.criticalPathProperty.addPropertyChangeListener(listener);
    }

    public void addAdvancesPropertyChangeListener(
            PropertyChangeListener listener) {
        this.advancesProperty.addPropertyChangeListener(listener);
    }

    public void addReportedHoursPropertyChangeListener(
            PropertyChangeListener listener) {
        this.reportedHoursProperty.addPropertyChangeListener(listener);
    }

    public void addMoneyCostBarPropertyChangeListener(
            PropertyChangeListener listener) {
        this.moneyCostBarProperty.addPropertyChangeListener(listener);
    }

    public void addFundamentalPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.fundamentalPropertiesListeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.fundamentalPropertiesListeners
                .removePropertyChangeListener(listener);
    }

    public void removeVisibilityPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.visibilityProperties.removePropertyChangeListener(listener);
    }

    @Override
    public GanttDate getEndDate() {
        return fundamentalProperties.getEndDate();
    }

    @Override
    public List<Constraint<GanttDate>> getCurrentLengthConstraint() {
        if (isContainer()) {
            return Collections.emptyList();
        }
        return violationNotificator.withListener(fundamentalProperties
                .getCurrentLengthConstraint());
    }

    public Constraint<GanttDate> getEndDateBiggerThanStartDate() {
        return violationNotificator
                .withListener(biggerOrEqualThan(getBeginDate()));
    }

    public String getNotes() {
        return fundamentalProperties.getNotes();
    }

    public void setNotes(String notes) {
        String previousValue = fundamentalProperties.getNotes();
        this.fundamentalProperties.setNotes(notes);
        fundamentalPropertiesListeners.firePropertyChange("notes",
                previousValue, this.fundamentalProperties.getNotes());
    }

    public void resizeTo(final LocalDate date) {
        if (date.compareTo(getBeginDateAsLocalDate()) < 0) {
            return;
        }
        doPositionModifications(new IModifications() {

            @Override
            public void doIt(IUpdatablePosition position) {
                position.resizeTo(GanttDate.createFrom(date));
            }
        });
    }

    public void removed() {
        setVisible(false);
    }

    @Override
    public BigDecimal getHoursAdvanceBarPercentage() {
        return fundamentalProperties.getHoursAdvanceBarPercentage();
    }

    @Override
    public BigDecimal getAdvancePercentage() {
        return fundamentalProperties.getAdvancePercentage();
    }

    @Override
    public GanttDate getHoursAdvanceBarEndDate() {
        return fundamentalProperties.getHoursAdvanceBarEndDate();
    }

    @Override
    public GanttDate getMoneyCostBarEndDate() {
        return fundamentalProperties.getMoneyCostBarEndDate();
    }

    @Override
    public BigDecimal getMoneyCostBarPercentage() {
        return fundamentalProperties.getMoneyCostBarPercentage();
    }

    @Override
    public GanttDate getAdvanceBarEndDate() {
        return fundamentalProperties.getAdvanceBarEndDate();
    }

    public GanttDate getAdvanceBarEndDate(String progressType) {
        return fundamentalProperties.getAdvanceBarEndDate(progressType);
    }

    public String getTooltipText() {
        return fundamentalProperties.getTooltipText();
    }

    public String updateTooltipText() {
        return fundamentalProperties.updateTooltipText();
    }

    public String updateTooltipText(String progressType) {
        return fundamentalProperties.updateTooltipText(progressType);
    }

    public String getLabelsText() {
        return fundamentalProperties.getLabelsText();
    }

    public String getResourcesText() {
        return fundamentalProperties.getResourcesText();
    }

    @Override
    public Date getDeadline() {
        return fundamentalProperties.getDeadline();
    }

    @Override
    public void setDeadline(Date date) {
        Date previousValue = fundamentalProperties.getDeadline();
        fundamentalProperties.setDeadline(date);
        fundamentalPropertiesListeners.firePropertyChange("deadline",
                previousValue, date);
    }

    @Override
    public GanttDate getConsolidatedline() {
        return fundamentalProperties.getConsolidatedline();
    }

    public void addConstraintViolationListener(
            IConstraintViolationListener<GanttDate> listener, Mode mode) {
        violationNotificator.addConstraintViolationListener(listener, mode);
    }

    public void addReloadListener(
            IReloadResourcesTextRequested reloadResourcesTextRequested) {
        Validate.notNull(reloadResourcesTextRequested);
        this.reloadRequestedListeners.add(reloadResourcesTextRequested);
    }

    public void removeReloadListener(
            IReloadResourcesTextRequested reloadResourcesTextRequested) {
        this.reloadRequestedListeners.remove(reloadResourcesTextRequested);
    }

    public void reloadResourcesText() {
        for (IReloadResourcesTextRequested each : reloadRequestedListeners) {
            each.reloadResourcesTextRequested();
        }
    }

    public static void reloadResourcesText(IContextWithPlannerTask<?> context) {
        Task task = context.getTask();
        task.reloadResourcesText();
        List<? extends TaskContainer> parents = context.getMapper().getParents(
                task);
        for (TaskContainer each : parents) {
            each.reloadResourcesText();
        }
    }

    public boolean isSubcontracted() {
        return fundamentalProperties.isSubcontracted();
    }

    public boolean isLimiting() {
        return fundamentalProperties.isLimiting();
    }

    public boolean isLimitingAndHasDayAssignments() {
        return fundamentalProperties.isLimitingAndHasDayAssignments();
    }

    public boolean hasConsolidations() {
        return fundamentalProperties.hasConsolidations();
    }

    public boolean canBeExplicitlyResized() {
        return fundamentalProperties.canBeExplicitlyResized();
    }

    public abstract boolean canBeExplicitlyMoved();

    @Override
    public String toString() {
        return fundamentalProperties.getName();
    }

    public List<Task> getAllTaskLeafs() {
        return Arrays.asList(this);
    }

    public String getAssignedStatus() {
        return fundamentalProperties.getAssignedStatus();
    }

    public boolean isFixed() {
        return fundamentalProperties.isFixed();
    }

    public LocalDate getBeginDateAsLocalDate() {
        return LocalDate.fromDateFields(getBeginDate().toDayRoundedDate());
    }

    @Override
    public boolean isManualAnyAllocation() {
        return fundamentalProperties.isManualAnyAllocation();
    }

    public boolean belongsClosedProject() {
        return fundamentalProperties.belongsClosedProject();
    }

    public boolean isRoot() {
        return fundamentalProperties.isRoot();
    }

    public void updateSizeDueToDateChanges(GanttDate previousStart, GanttDate previousEnd) {
        dependenciesEnforcerHook.setStartDate(previousStart, previousEnd,
                getBeginDate());
    }

    @Override
    public boolean isUpdatedFromTimesheets() {
        return fundamentalProperties.isUpdatedFromTimesheets();
    }

    @Override
    public Date getFirstTimesheetDate() {
        return fundamentalProperties.getFirstTimesheetDate();
    }

    @Override
    public Date getLastTimesheetDate() {
        return fundamentalProperties.getLastTimesheetDate();
    }

    public void firePropertyChangeForTaskDates() {
        fundamentalPropertiesListeners.firePropertyChange("beginDate", null,
                getBeginDate());
        fundamentalPropertiesListeners.firePropertyChange("endDate", null,
                getEndDate());
    }

}
