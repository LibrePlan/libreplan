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

package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.Milestone;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.Task.IReloadResourcesTextRequested;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;
import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Div;

/**
 * Graphical component which represents a {@link Task}.
 *
 * @author Javier Morán Rúa <jmoran@igalia.com>
 */
public class TaskComponent extends Div implements AfterCompose {

    private static final Log LOG = LogFactory.getLog(TaskComponent.class);

    private static final int HEIGHT_PER_TASK = 10;
    private static final int CONSOLIDATED_MARK_HALF_WIDTH = 3;
    private static final int HALF_DEADLINE_MARK = 3;


    private static Pattern pixelsSpecificationPattern = Pattern
            .compile("\\s*(\\d+)px\\s*;?\\s*");

    private static int stripPx(String pixels) {
        Matcher matcher = pixelsSpecificationPattern.matcher(pixels);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("pixels " + pixels
                    + " is not valid. It must be "
                    + pixelsSpecificationPattern.pattern());
        }
        return Integer.valueOf(matcher.group(1));
    }

    private static Command _updatecmd = new ComponentCommand(
            "onUpdatePosition", 0) {

        protected void process(AuRequest request) {

            final TaskComponent ta = (TaskComponent) request.getComponent();

            if (ta == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if (requestData == null || requestData.length != 2) {
                throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                        new Object[] { Objects.toString(requestData), this });
            } else {

                ta.doUpdatePosition(requestData[0], requestData[1]);
                Events.postEvent(new Event(getId(), ta, request.getData()));
            }
        }

    };

    private static Command _updatewidthcmd = new ComponentCommand(
            "onUpdateWidth", 0) {

        protected void process(AuRequest request) {

            final TaskComponent ta = (TaskComponent) request.getComponent();

            if (ta == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if (requestData == null || requestData.length != 1) {
                throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                        new Object[] { Objects.toString(requestData), this });
            } else {

                ta.doUpdateSize(requestData[0]);
                Events.postEvent(new Event(getId(), ta, request.getData()));
            }
        }
    };

    private static Command _adddependencycmd = new ComponentCommand(
            "onAddDependency", 0) {

        protected void process(AuRequest request) {

            final TaskComponent taskComponent = (TaskComponent) request.getComponent();

            if (taskComponent == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if (requestData == null || requestData.length != 1) {
                throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                        new Object[] { Objects.toString(requestData), this });
            } else {
                taskComponent.doAddDependency(requestData[0]);
                Events.postEvent(new Event(getId(), taskComponent, request.getData()));
            }
        }
    };

    protected final IDisabilityConfiguration disabilityConfiguration;

    private PropertyChangeListener criticalPathPropertyListener;

    private PropertyChangeListener showingAdvancePropertyListener;

    private PropertyChangeListener showingReportedHoursPropertyListener;

    public static TaskComponent asTaskComponent(Task task,
            IDisabilityConfiguration disabilityConfiguration,
            boolean isTopLevel) {
        final TaskComponent result;
        if (task.isContainer()) {
            result = TaskContainerComponent.asTask((TaskContainer) task,
                    disabilityConfiguration);
        } else if (task instanceof Milestone) {
            result = new MilestoneComponent(task, disabilityConfiguration);
        } else {
            result = new TaskComponent(task, disabilityConfiguration);
        }
        result.isTopLevel = isTopLevel;
        return TaskRow.wrapInRow(result);
    }

    public static TaskComponent asTaskComponent(Task task,
            IDisabilityConfiguration disabilityConfiguration) {
        return asTaskComponent(task, disabilityConfiguration, true);
    }

    private IReloadResourcesTextRequested reloadResourcesTextRequested;

    public TaskComponent(Task task,
            IDisabilityConfiguration disabilityConfiguration) {
        setHeight(HEIGHT_PER_TASK + "px");
        setContext("idContextMenuTaskAssignment");
        this.task = task;
        setClass(calculateCSSClass());

        setId(UUID.randomUUID().toString());
        this.disabilityConfiguration = disabilityConfiguration;
        taskViolationListener = new IConstraintViolationListener<GanttDate>() {

            @Override
            public void constraintViolated(Constraint<GanttDate> constraint,
                    GanttDate value) {
                // TODO mark graphically task as violated
            }

            @Override
            public void constraintSatisfied(Constraint<GanttDate> constraint,
                    GanttDate value) {
                // TODO mark graphically dependency as not violated
            }
        };
        this.task.addConstraintViolationListener(taskViolationListener);
        reloadResourcesTextRequested = new IReloadResourcesTextRequested() {

            @Override
            public void reloadResourcesTextRequested() {
                if (canShowResourcesText()) {
                    smartUpdate("resourcesText", getResourcesText());
                }
                String cssClass = calculateCSSClass();

                response("setClass", new AuInvoke(TaskComponent.this,
                        "setClass", cssClass));

                // FIXME: Refactorize to another listener
                updateDeadline();
                invalidate();
            }

        };
        this.task.addReloadListener(reloadResourcesTextRequested);
    }

    /* Generate CSS class attribute depending on task properties */
    protected String calculateCSSClass() {
        String cssClass = isSubcontracted() ? "box subcontracted-task"
                : "box standard-task";
        cssClass += isResizingTasksEnabled() ? " yui-resize" : "";
        if (isContainer()) {
            cssClass += task.isExpanded() ? " expanded" : " closed ";
            cssClass += task.isInCriticalPath() && !task.isExpanded() ? " critical"
                    : "";
        } else {
            cssClass += task.isInCriticalPath() ? " critical" : "";
        }
        cssClass += " " + task.getAssignedStatus();
        if (task.isLimiting()) {
            cssClass += task.isLimitingAndHasDayAssignments() ? " limiting-assigned "
                    : " limiting-unassigned ";
        }
        return cssClass;
    }


    protected void updateClass() {
        response(null, new AuInvoke(this, "setClass",
                new Object[] { calculateCSSClass() }));
    }

    public final void afterCompose() {
        updateProperties();
        if (propertiesListener == null) {
            propertiesListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (isInPage()) {
                        updateProperties();
                    }
                }
            };
        }
        this.task
                .addFundamentalPropertiesChangeListener(propertiesListener);

        if (showingAdvancePropertyListener == null) {
            showingAdvancePropertyListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (isInPage() && !(task instanceof Milestone)) {
                        try {
                            updateCompletionAdvance();
                        } catch (Exception e) {
                            LOG.error("failure at updating completion", e);
                        }
                    }
                }
            };
        }
        this.task
                .addAdvancesPropertyChangeListener(showingAdvancePropertyListener);

        if (showingReportedHoursPropertyListener == null) {
            showingReportedHoursPropertyListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (isInPage() && !(task instanceof Milestone)) {
                        try {
                            updateCompletionReportedHours();
                        } catch (Exception e) {
                            LOG.error("failure at updating completion", e);
                        }
                    }
                }
            };
        }
        this.task
                .addReportedHoursPropertyChangeListener(showingReportedHoursPropertyListener);

        if (criticalPathPropertyListener == null) {
            criticalPathPropertyListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateClass();
                }

            };
        }
        this.task
                .addCriticalPathPropertyChangeListener(criticalPathPropertyListener);

        updateClass();
    }

    /**
     * Note: This method is intended to be overridden.
     */
    protected boolean canShowResourcesText() {
        return true;
    }

    private String _color;

    private boolean isTopLevel;

    private final Task task;
    private transient PropertyChangeListener propertiesListener;
    private IConstraintViolationListener<GanttDate> taskViolationListener;

    public TaskRow getRow() {
        if (getParent() == null) {
            throw new IllegalStateException(
                    "the TaskComponent should have been wraped by a "
                            + TaskRow.class.getName());
        }
        return (TaskRow) getParent();
    }

    public Task getTask() {
        return task;
    }

    public String getTaskName() {
        return task.getName();
    }

    public String getLength() {
        return null;
    }

    public Command getCommand(String cmdId) {
        Command result = null;
        if ("updatePosition".equals(cmdId)
                && isMovingTasksEnabled()) {
            result = _updatecmd;
        } else if ("updateSize".equals(cmdId)
                && isResizingTasksEnabled()) {
            result = _updatewidthcmd;
        } else if ("addDependency".equals(cmdId)) {
            result = _adddependencycmd;
        }
        return result;
    }

    public boolean isResizingTasksEnabled() {
        return (disabilityConfiguration != null)
                && disabilityConfiguration.isResizingTasksEnabled()
                && !task.isSubcontracted() && task.canBeExplicitlyResized();
    }

    public boolean isMovingTasksEnabled() {
        return (disabilityConfiguration != null)
                && disabilityConfiguration.isMovingTasksEnabled()
                && task.canBeExplicitlyMoved();
    }

    void doUpdatePosition(String leftX, String topY) {
        GanttDate startBeforeMoving = this.task.getBeginDate();
        LocalDate newPosition = getMapper().toDate(stripPx(leftX));
        this.task.moveTo(GanttDate.createFrom(newPosition));
        boolean remainsInOriginalPosition = this.task.getBeginDate().equals(
                startBeforeMoving);
        if (remainsInOriginalPosition) {
            updateProperties();
        }
    }

    void doUpdateSize(String size) {
        int pixels = stripPx(size);
        DateTime end = new DateTime(this.task.getBeginDate()
                .toDayRoundedDate().getTime()).plus(getMapper().toDuration(
                pixels));
        this.task.resizeTo(end.toLocalDate());
        updateProperties();
    }

    void doAddDependency(String destinyTaskId) {
        getTaskList().addDependency(this,
                ((TaskComponent) getFellow(destinyTaskId)));
    }

    public String getColor() {
        return _color;
    }

    public void setColor(String color) {

        if ((color != null) && (color.length() == 0)) {
            color = null;
        }

        if (!Objects.equals(_color, color)) {
            _color = color;
        }
    }

    /*
     * We override the method of getRealStyle to put the color property as part
     * of the style
     */

    protected String getRealStyle() {

        final StringBuffer sb = new StringBuffer(super.getRealStyle());

        if (getColor() != null) {
            HTMLs.appendStyle(sb, "background-color", getColor());
        }
        HTMLs.appendStyle(sb, "position", "absolute");

        return sb.toString();
    }

    /*
     * We send a response to the client to create the arrow we are going to use
     * to create the dependency
     */

    public void addDependency() {
        response("depkey", new AuInvoke(this, "addDependency"));
    }

    private IDatesMapper getMapper() {
        return getTaskList().getMapper();
    }

    public TaskList getTaskList() {
        return getRow().getTaskList();
    }

    @Override
    public void setParent(Component parent) {
        Validate.isTrue(parent == null || parent instanceof TaskRow);
        super.setParent(parent);
    }

    public final void zoomChanged() {
        updateProperties();
    }

    public void updateProperties() {
        if (!isInPage()) {
            return;
        }
        setLeft("0");
        setLeft(this.task.getBeginDate().toPixels(getMapper()) + "px");
        updateWidth();
        smartUpdate("name", this.task.getName());
        DependencyList dependencyList = getDependencyList();
        if (dependencyList != null) {
            dependencyList.redrawDependenciesConnectedTo(this);
        }
        updateDeadline();
        updateCompletionIfPossible();
        updateClass();
    }

    private void updateWidth() {
        setWidth("0");
        int pixelsEnd = this.task.getEndDate().toPixels(getMapper());
        int pixelsStart = this.task.getBeginDate().toPixels(getMapper());

        setWidth((pixelsEnd - pixelsStart) + "px");
    }

    private void updateDeadline() {
        if (task.getDeadline() != null) {
            String position = (getMapper().toPixels(
                    LocalDate.fromDateFields(task.getDeadline())) - HALF_DEADLINE_MARK)
                    + "px";
            response(null, new AuInvoke(this, "moveDeadline", position));
        } else {
            // Move deadline out of visible area
            response(null, new AuInvoke(this, "moveDeadline","-100px"));
        }

        if (task.getConsolidatedline() != null) {
            int pixels = getMapper().toPixels(
                    LocalDate.fromDateFields(task.getConsolidatedline()
                            .toDayRoundedDate()))
                    - CONSOLIDATED_MARK_HALF_WIDTH;
            String position = pixels + "px";
            response(null, new AuInvoke(this, "moveConsolidatedline", position));
        } else {
            // Move consolidated line out of visible area
            response(null, new AuInvoke(this, "moveConsolidatedline", "-100px"));
        }
    }

    public void updateCompletionIfPossible() {
        if (task instanceof Milestone) {
            return;
        }
        try {
            updateCompletionReportedHours();
            updateCompletionAdvance();
        } catch (Exception e) {
            LOG.error("failure at updating completion", e);
        }
    }

    private void updateCompletionReportedHours() {
        if (task.isShowingReportedHours()) {
            int startPixels = this.task.getBeginDate().toPixels(getMapper());
            String widthHoursAdvancePercentage = pixelsFromStartUntil(
                    startPixels,
                this.task.getHoursAdvanceEndDate()) + "px";
            response(null, new AuInvoke(this, "resizeCompletionAdvance",
                widthHoursAdvancePercentage));
        } else {
            response(null, new AuInvoke(this, "resizeCompletionAdvance", "0px"));
        }
    }

    private void updateCompletionAdvance() {
        if (task.isShowingAdvances()) {
            int startPixels = this.task.getBeginDate().toPixels(getMapper());
            String widthAdvancePercentage = pixelsFromStartUntil(startPixels,
                this.task.getAdvanceEndDate()) + "px";
            response(null, new AuInvoke(this, "resizeCompletion2Advance",
                    widthAdvancePercentage));
        } else {
            response(null,
                    new AuInvoke(this, "resizeCompletion2Advance", "0px"));
        }
    }

    public void updateCompletion(String progressType) {
        if (task.isShowingAdvances()) {
            int startPixels = this.task.getBeginDate().toPixels(getMapper());

            String widthAdvancePercentage = pixelsFromStartUntil(startPixels,
                    this.task.getAdvanceEndDate(progressType)) + "px";
            response(null, new AuInvoke(this, "resizeCompletion2Advance",
                    widthAdvancePercentage));
        } else {
            response(null,
                    new AuInvoke(this, "resizeCompletion2Advance", "0px"));
        }
    }

    private int pixelsFromStartUntil(int startPixels, GanttDate until) {
        int endPixels = until.toPixels(getMapper());
        assert endPixels >= startPixels;
        return endPixels - startPixels;
    }

    public void updateTooltipText() {
        smartUpdate("taskTooltipText", task.updateTooltipText());
    }

    public void updateTooltipText(String progressType) {
        smartUpdate("taskTooltipText", task.updateTooltipText(progressType));
    }

    private DependencyList getDependencyList() {
        return getGanntPanel().getDependencyList();
    }

    private GanttPanel getGanntPanel() {
        return getTaskList().getGanttPanel();
    }

    private boolean isInPage() {
        return getPage() != null;
    }

    void publishTaskComponents(Map<Task, TaskComponent> resultAccumulated) {
        resultAccumulated.put(getTask(), this);
        publishDescendants(resultAccumulated);
    }

    protected void publishDescendants(Map<Task, TaskComponent> resultAccumulated) {

    }

    protected void remove() {
        this.getRow().detach();
        task.removeReloadListener(reloadResourcesTextRequested);
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

    public String getTooltipText() {
        return task.getTooltipText();
    }

    public String getLabelsText() {
        return task.getLabelsText();
    }

    public String getLabelsDisplay() {
        Planner planner = getTaskList().getGanttPanel().getPlanner();
        return planner.isShowingLabels() ? "inline" : "none";
    }

    public String getResourcesText() {
        return task.getResourcesText();
    }

    public String getResourcesDisplay() {
        Planner planner = getTaskList().getGanttPanel().getPlanner();
        return planner.isShowingResources() ? "inline" : "none";
    }

    public boolean isSubcontracted() {
        return task.isSubcontracted();
    }

    public boolean isContainer() {
        return task.isContainer();
    }

    @Override
    public String toString() {
        return task.toString();
    }

}
