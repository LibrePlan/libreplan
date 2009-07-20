/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.ganttz.data.TaskBean;
import org.zkoss.ganttz.data.TaskContainerBean;
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
 * @author javi
 */
public class Task extends Div implements AfterCompose {

    private static final int HEIGHT_PER_TASK = 10;
    private static final String STANDARD_TASK_COLOR = "#007bbe";

    private static Pattern pixelsSpecificationPattern = Pattern
            .compile("\\s*(\\d+)px\\s*;?\\s*");

    private static int stripPx(String pixels) {
        Matcher matcher = pixelsSpecificationPattern.matcher(pixels);
        if (!matcher.matches())
            throw new IllegalArgumentException("pixels " + pixels
                    + " is not valid. It must be "
                    + pixelsSpecificationPattern.pattern());
        return Integer.valueOf(matcher.group(1));
    }

    private static Command _updatecmd = new ComponentCommand(
            "onUpdatePosition", 0) {

        protected void process(AuRequest request) {

            final Task ta = (Task) request.getComponent();

            if (ta == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if ((requestData != null) && (requestData.length != 2)) {
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

            final Task ta = (Task) request.getComponent();

            if (ta == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if ((requestData != null) && (requestData.length != 1)) {
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

            final Task task = (Task) request.getComponent();

            if (task == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if ((requestData != null) && (requestData.length != 1)) {
                throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                        new Object[] { Objects.toString(requestData), this });
            } else {
                task.doAddDependency(requestData[0]);
                Events.postEvent(new Event(getId(), task, request.getData()));
            }
        }
    };

    public static Task asTask(TaskBean taskBean, TaskList taskList) {
        if (taskBean.isContainer()) {
            return TaskContainer.asTask((TaskContainerBean) taskBean, taskList);
        }
        return new Task(taskBean);
    }

    public Task(TaskBean taskBean) {
        setHeight(HEIGHT_PER_TASK + "px");
        setContext("idContextMenuTaskAssigment");
        this.taskBean = taskBean;
        setColor(STANDARD_TASK_COLOR);
        setId(UUID.randomUUID().toString());
    }

    protected String calculateClass() {
        return "box";
    }

    protected void updateClass() {
        response(null, new AuInvoke(this, "setClass",
                new Object[] { calculateClass() }));
    }

    public void afterCompose() {
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
        this.taskBean
                .addFundamentalPropertiesChangeListener(propertiesListener);
        updateClass();
    }

    private String _color;

    private List<WeakReference<DependencyAddedListener>> dependencyListeners = new LinkedList<WeakReference<DependencyAddedListener>>();

    private final TaskBean taskBean;
    private PropertyChangeListener propertiesListener;

    public TaskBean getTaskBean() {
        return taskBean;
    }

    public String getTaskName() {
        return taskBean.getName();
    }

    public String getLength() {
        return null;
    }

    public void addDependencyListener(DependencyAddedListener listener) {
        dependencyListeners.add(new WeakReference<DependencyAddedListener>(
                listener));
    }

    private void fireDependenceAdded(Dependency dependency) {
        ArrayList<DependencyAddedListener> active = new ArrayList<DependencyAddedListener>();
        synchronized (this) {
            ListIterator<WeakReference<DependencyAddedListener>> iterator = dependencyListeners
                    .listIterator();
            while (iterator.hasNext()) {
                WeakReference<DependencyAddedListener> next = iterator.next();
                DependencyAddedListener listener = next.get();
                if (listener == null) {
                    iterator.remove();
                } else {
                    active.add(listener);
                }
            }
        }
        for (DependencyAddedListener listener : active) {
            listener.dependenceAdded(dependency);
        }
    }

    public Command getCommand(String cmdId) {

        Command c = null;

        if ("updatePosition".equals(cmdId))
            c = _updatecmd;
        else if ("updateSize".equals(cmdId))
            c = _updatewidthcmd;
        else if ("addDependency".equals(cmdId))
            c = _adddependencycmd;

        return c;
    }

    // Command action to do
    void doUpdatePosition(String leftX, String topY) {
        this.taskBean.setBeginDate(getMapper().toDate(stripPx(leftX)));
    }

    void doUpdateSize(String size) {
        int pixels = stripPx(size);
        this.taskBean.setLengthMilliseconds(getMapper().toMilliseconds(pixels));
    }

    void doAddDependency(String destinyTaskId) {
        Dependency dependency = new Dependency(this,
                ((Task) getFellow(destinyTaskId)));
        if (getPlanner().canAddDependency(dependency.getDependencyBean())) {
            fireDependenceAdded(dependency);
        }
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

    private DatesMapper getMapper() {
        return getTaskList().getMapper();
    }

    public TaskList getTaskList() {
        return (TaskList) getParent();
    }

    public Planner getPlanner() {
        return getTaskList().getPlanner();
    }

    @Override
    public void setParent(Component parent) {
        if (parent != null && !(parent instanceof TaskList))
            throw new UiException("Unsupported parent for rows: " + parent);
        super.setParent(parent);
    }

    public void zoomChanged() {
        updateProperties();
    }

    private void updateProperties() {
        setLeft("0");
        setLeft(getMapper().toPixels(this.taskBean.getBeginDate()) + "px");
        setWidth("0");
        setWidth(getMapper().toPixels(this.taskBean.getLengthMilliseconds())
                + "px");
        smartUpdate("name", this.taskBean.getName());
        DependencyList dependencyList = getDependencyList();
        if (dependencyList != null) {
            dependencyList.redrawDependenciesConnectedTo(this);
        }
    }

    private DependencyList getDependencyList() {
        return getPlanner().getDependencyList();
    }

    private boolean isInPage() {
        return getParent() != null;
    }

    public void remove() {
        getTaskList().removeTask(this);
    }

    void publishTasks(Map<TaskBean, Task> resultAccumulated) {
        resultAccumulated.put(getTaskBean(), this);
        publishDescendants(resultAccumulated);
    }

    protected void publishDescendants(Map<TaskBean, Task> resultAccumulated) {

    }
}
