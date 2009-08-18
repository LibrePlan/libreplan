package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
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
public class TaskComponent extends Div implements AfterCompose {

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

            final TaskComponent ta = (TaskComponent) request.getComponent();

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

            final TaskComponent ta = (TaskComponent) request.getComponent();

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

            final TaskComponent taskComponent = (TaskComponent) request.getComponent();

            if (taskComponent == null) {
                throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
                        this);
            }

            String[] requestData = request.getData();

            if ((requestData != null) && (requestData.length != 1)) {
                throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                        new Object[] { Objects.toString(requestData), this });
            } else {
                taskComponent.doAddDependency(requestData[0]);
                Events.postEvent(new Event(getId(), taskComponent, request.getData()));
            }
        }
    };

    public static TaskComponent asTaskComponent(Task task, TaskList taskList,
            boolean isTopLevel) {
        final TaskComponent result;
        if (task.isContainer()) {
            result = TaskContainerComponent
                    .asTask((TaskContainer) task, taskList);
        } else {
            result = new TaskComponent(task);
        }
        result.isTopLevel = isTopLevel;
        return result;
    }

    public static TaskComponent asTaskComponent(Task task, TaskList taskList) {
        return asTaskComponent(task, taskList, true);
    }

    public TaskComponent(Task task) {
        setHeight(HEIGHT_PER_TASK + "px");
        setContext("idContextMenuTaskAssigment");
        this.task = task;
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
        this.task
                .addFundamentalPropertiesChangeListener(propertiesListener);
        updateClass();
    }

    private String _color;

    private boolean isTopLevel;

    private final Task task;
    private PropertyChangeListener propertiesListener;

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
        this.task.setBeginDate(getMapper().toDate(stripPx(leftX)));
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
        setLeft(getMapper().toPixels(this.task.getBeginDate()) + "px");
        setWidth("0");
        setWidth(getMapper().toPixels(this.task.getLengthMilliseconds())
                + "px");
        smartUpdate("name", this.task.getName());
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

    void publishTaskComponents(Map<Task, TaskComponent> resultAccumulated) {
        resultAccumulated.put(getTask(), this);
        publishDescendants(resultAccumulated);
    }

    protected void publishDescendants(Map<Task, TaskComponent> resultAccumulated) {

    }

    protected void remove() {
        getDependencyList().taskRemoved(this.getTask());
        this.detach();
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

}
