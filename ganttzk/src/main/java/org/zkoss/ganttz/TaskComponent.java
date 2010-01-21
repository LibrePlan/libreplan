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

package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.adapters.IDisabilityConfiguration;
import org.zkoss.ganttz.data.Milestone;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.Task.IReloadResourcesTextRequested;
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

    private final IDisabilityConfiguration disabilityConfiguration;

    private PropertyChangeListener criticalPathPropertyListener;

    public static TaskComponent asTaskComponent(Task task, TaskList taskList,
            boolean isTopLevel) {
        final TaskComponent result;
        if (task.isContainer()) {
            result = TaskContainerComponent.asTask((TaskContainer) task,
                    taskList);
        } else if (task instanceof Milestone) {
            result = new MilestoneComponent(task, taskList
                    .getDisabilityConfiguration());
        } else {
            result = new TaskComponent(task, taskList
                    .getDisabilityConfiguration());
        }
        result.isTopLevel = isTopLevel;
        return result;
    }

    public static TaskComponent asTaskComponent(Task task, TaskList taskList) {
        return asTaskComponent(task, taskList, true);
    }

    private IReloadResourcesTextRequested reloadResourcesTextRequested;

    public TaskComponent(Task task,
            IDisabilityConfiguration disabilityConfiguration) {
        setHeight(HEIGHT_PER_TASK + "px");
        setContext("idContextMenuTaskAssignment");
        this.task = task;
        if (task.isSubcontracted()) {
            setClass("box subcontracted-task");
        } else {
            setClass("box standard-task");
        }

        setId(UUID.randomUUID().toString());
        this.disabilityConfiguration = disabilityConfiguration;
        taskViolationListener = new IConstraintViolationListener<Date>() {

            @Override
            public void constraintViolated(Constraint<Date> constraint,
                    Date value) {
                // TODO mark graphically task as violated
            }
        };
        this.task.addConstraintViolationListener(taskViolationListener);
        reloadResourcesTextRequested = new IReloadResourcesTextRequested() {

            @Override
            public void reloadResourcesTextRequested() {
                smartUpdate("resourcesText", getResourcesText());
            }
        };
        this.task.addReloadListener(reloadResourcesTextRequested);
    }

    protected String calculateClass() {
        String classText;
        if (getSclass().equals("null")) {
            classText = "box";
        } else {
            classText = getSclass();
        }
        if (task.isInCriticalPath()) {
            classText += " critical";
        }
        return classText;
    }

    protected void updateClass() {
        response(null, new AuInvoke(this, "setClass",
                new Object[] { calculateClass() }));
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

    private String _color;

    private boolean isTopLevel;

    private final Task task;
    private transient PropertyChangeListener propertiesListener;

    private IConstraintViolationListener<Date> taskViolationListener;

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
        return disabilityConfiguration.isResizingTasksEnabled();
    }

    public boolean isMovingTasksEnabled() {
        return disabilityConfiguration.isMovingTasksEnabled();
    }

    void doUpdatePosition(String leftX, String topY) {
        this.task.moveTo(getMapper().toDate(stripPx(leftX)));
    }

    void doUpdateSize(String size) {
        int pixels = stripPx(size);
        this.task.setLengthMilliseconds(getMapper().toMilliseconds(pixels));
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
        return (TaskList) getParent();
    }

    @Override
    public void setParent(Component parent) {
        if (parent != null && !(parent instanceof TaskList)) {
            throw new UiException("Unsupported parent for rows: " + parent);
        }
        super.setParent(parent);
    }

    public final void zoomChanged() {
        updateProperties();
    }

    private void updateProperties() {
        if (!isInPage()) {
            return;
        }
        setLeft("0");
        setLeft(getMapper().toPixels(this.task.getBeginDate()) + "px");
        setWidth("0");
        setWidth(getMapper().toPixels(this.task.getLengthMilliseconds())
                + "px");
        smartUpdate("name", this.task.getName());
        DependencyList dependencyList = getDependencyList();
        if (dependencyList != null) {
            dependencyList.redrawDependenciesConnectedTo(this);
        }
        updateDeadline();
        updateCompletionIfPossible();
    }

    private void updateDeadline() {
        if (task.getDeadline() != null) {
            String position = getMapper().toPixels(task.getDeadline()) + "px";
            response(null, new AuInvoke(this, "moveDeadline", position));
        }
    }

    private void updateCompletionIfPossible() {
        try {
            updateCompletion();
        } catch (Exception e) {
            LOG.error("failure at updating completion", e);
        }
    }

    private void updateCompletion() {
        long beginMilliseconds = this.task.getBeginDate().getTime();

        long hoursAdvanceEndMilliseconds = this.task.getHoursAdvanceEndDate()
                .getTime()
                - beginMilliseconds;
        if (hoursAdvanceEndMilliseconds < 0) {
            hoursAdvanceEndMilliseconds = 0;
        }
        String widthHoursAdvancePercentage = getMapper().toPixels(
                hoursAdvanceEndMilliseconds)
                + "px";
        response(null, new AuInvoke(this, "resizeCompletionAdvance",
                widthHoursAdvancePercentage));

        long advanceEndMilliseconds = this.task.getAdvanceEndDate()
                .getTime()
                - beginMilliseconds;
        if (advanceEndMilliseconds < 0) {
            advanceEndMilliseconds = 0;
        }
        String widthAdvancePercentage = getMapper().toPixels(
                advanceEndMilliseconds)
                + "px";
        response(null, new AuInvoke(this, "resizeCompletion2Advance",
                widthAdvancePercentage));
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
        this.detach();
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

    public String getResourcesText() {
        return task.getResourcesText();
    }

}
