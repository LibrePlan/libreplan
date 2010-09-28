/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHook;
import org.zkoss.ganttz.data.GanttDiagramGraph.IDependenciesEnforcerHookFactory;
import org.zkoss.ganttz.data.GanttDiagramGraph.INotificationAfterDependenciesEnforcement;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;
import org.zkoss.ganttz.data.constraint.DateConstraint;
import org.zkoss.ganttz.util.ConstraintViolationNotificator;

/**
 * This class contains the information of a task. It can be modified and
 * notifies of the changes to the interested parties. <br/>
 * @author Óscar González Fernández <ogonzalez@igalia.com>
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

    private ITaskFundamentalProperties fundamentalProperties;

    private boolean visible = true;

    private boolean inCriticalPath = false;

    private ConstraintViolationNotificator<Date> violationNotificator = ConstraintViolationNotificator
            .create();

    private IDependenciesEnforcerHook dependenciesEnforcerHook = GanttDiagramGraph.doNothingHook();

    private final INotificationAfterDependenciesEnforcement notifyDates = new INotificationAfterDependenciesEnforcement() {

        @Override
        public void onStartDateChange(Date previousStart, long oldLength,
                Date newStart) {
            fundamentalPropertiesListeners.firePropertyChange("beginDate",
                    previousStart, fundamentalProperties.getBeginDate());
            fireLengthMilliseconds(oldLength);
            reloadResourcesTextIfChange(newStart, previousStart);
        }

        @Override
        public void onLengthChange(long previousLength, long newLength) {
            fireLengthMilliseconds(previousLength);
        }

        private void fireLengthMilliseconds(long previousValue) {
            fundamentalPropertiesListeners.firePropertyChange(
                    "lengthMilliseconds", previousValue, fundamentalProperties
                            .getLengthMilliseconds());
        }
    };

    public Task(ITaskFundamentalProperties fundamentalProperties) {
        this.fundamentalProperties = fundamentalProperties;
    }

    public Task() {
        this(new DefaultFundamentalProperties());
    }

    @Override
    public List<Constraint<Date>> getStartConstraints() {
        return violationNotificator.withListener(fundamentalProperties
                .getStartConstraints());
    }

    public Task(String name, Date beginDate, long lengthMilliseconds) {
        this();
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (beginDate == null) {
            throw new IllegalArgumentException("beginDate cannot be null");
        }
        if (lengthMilliseconds < 0) {
            throw new IllegalArgumentException(
                    "length in milliseconds must be positive. Instead it is "
                            + lengthMilliseconds);
        }
        this.fundamentalProperties.setName(name);
        this.fundamentalProperties.setBeginDate(beginDate);
        this.fundamentalProperties.setLengthMilliseconds(lengthMilliseconds);
    }

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
            IDependenciesEnforcerHookFactory factory) {
        Validate.notNull(factory);
        dependenciesEnforcerHook = factory.create(this, notifyDates);
        Validate.notNull(dependenciesEnforcerHook);
    }

    public long setBeginDate(Date newStart) {
        Date previousValue = fundamentalProperties.getBeginDate();
        long previousLength = fundamentalProperties.getLengthMilliseconds();
        fundamentalProperties.setBeginDate(newStart);
        dependenciesEnforcerHook.setStartDate(previousValue, previousLength,
                newStart);
        return fundamentalProperties.getLengthMilliseconds();
    }

    private void reloadResourcesTextIfChange(Date newDate, Date previousDate) {
        if (!ObjectUtils.equals(newDate, previousDate)) {
            reloadResourcesText();
        }
    }

    public void fireChangesForPreviousValues(Date previousStart,
            long previousLength) {
        dependenciesEnforcerHook.setStartDate(previousStart, previousLength,
                fundamentalProperties.getBeginDate());
        dependenciesEnforcerHook.setLengthMilliseconds(previousLength,
                fundamentalProperties.getLengthMilliseconds());
    }

    public Date getBeginDate() {
        return new Date(fundamentalProperties.getBeginDate().getTime());
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        long previousValue = fundamentalProperties.getLengthMilliseconds();
        fundamentalProperties.setLengthMilliseconds(lengthMilliseconds);
        dependenciesEnforcerHook.setLengthMilliseconds(previousValue,
                lengthMilliseconds);
    }

    public long getLengthMilliseconds() {
        return fundamentalProperties.getLengthMilliseconds();
    }

    public void addVisibilityPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.visibilityProperties.addPropertyChangeListener(listener);
    }

    public void addCriticalPathPropertyChangeListener(
            PropertyChangeListener listener) {
        this.criticalPathProperty.addPropertyChangeListener(listener);
    }

    public void addFundamentalPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.fundamentalPropertiesListeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.fundamentalPropertiesListeners
                .removePropertyChangeListener(listener);
    }

    @Override
    public Date getEndDate() {
        return new Date(getBeginDate().getTime() + getLengthMilliseconds());
    }

    public Constraint<Date> getCurrentLengthConstraint() {
        if (isContainer()) {
            return Constraint.emptyConstraint();
        }
        return violationNotificator.withListener(DateConstraint
                .biggerOrEqualThan(getEndDate()));
    }

    public Constraint<Date> getEndDateBiggerThanStartDate() {
        return violationNotificator.withListener(DateConstraint
                .biggerOrEqualThan(getBeginDate()));
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

    @Override
    public void setEndDate(Date value) {
        if (value == null) {
            return;
        }
        setLengthMilliseconds(value.getTime() - getBeginDate().getTime());
    }

    public void removed() {
        setVisible(false);
    }

    @Override
    public BigDecimal getHoursAdvancePercentage() {
        return fundamentalProperties.getHoursAdvancePercentage();
    }

    @Override
    public BigDecimal getAdvancePercentage() {
        return fundamentalProperties.getAdvancePercentage();
    }

    @Override
    public Date getHoursAdvanceEndDate() {
        return fundamentalProperties.getHoursAdvanceEndDate();
    }

    @Override
    public Date getAdvanceEndDate() {
        return fundamentalProperties.getAdvanceEndDate();
    }

    public String getTooltipText() {
        return fundamentalProperties.getTooltipText();
    }

    public String updateTooltipText() {
        return fundamentalProperties.updateTooltipText();
    }

    public String getLabelsText() {
        return fundamentalProperties.getLabelsText();
    }

    public String getResourcesText() {
        return fundamentalProperties.getResourcesText();
    }

    public void moveTo(Date date) {
        Date previousStart = getBeginDate();
        long previousLength = getLengthMilliseconds();
        fundamentalProperties.moveTo(date);
        dependenciesEnforcerHook.setStartDate(previousStart, previousLength, date);
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
    public Date getConsolidatedline() {
        return fundamentalProperties.getConsolidatedline();
    }

    public void addConstraintViolationListener(
            IConstraintViolationListener<Date> listener) {
        violationNotificator.addConstraintViolationListener(listener);
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

}
