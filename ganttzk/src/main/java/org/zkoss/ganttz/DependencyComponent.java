package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.Task;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

/**
 *
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class DependencyComponent extends XulElement implements AfterCompose {

    private TaskComponent source;

    private TaskComponent destination;

    private DependencyType type;

    private FunctionalityExposedForExtensions<?> context;

    public DependencyComponent(FunctionalityExposedForExtensions<?> context,
            TaskComponent source, TaskComponent destination) {
        this.type = DependencyType.END_START;
        this.context = context;
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");
        if (destination == null)
            throw new IllegalArgumentException("destination cannot be null");
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void afterCompose() {
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                redrawDependency();
            }
        };
        this.source.getTask().addFundamentalPropertiesChangeListener(listener);
        this.destination.getTask().addFundamentalPropertiesChangeListener(listener);
    }

    /**
     * @return the idTaskOrig
     */
    public String getIdTaskOrig() {
        return source.getUuid();
    }

    public void setIdTaskOrig(String idTaskOrig) {
        this.source = findTaskComponent(idTaskOrig);

    }

    private TaskComponent findTaskComponent(String idTaskOrig) {
        return (TaskComponent) getFellow(idTaskOrig);
    }

    /**
     * @return the idTaskEnd
     */
    public String getIdTaskEnd() {
        return destination.getUuid();
    }

    public void setIdTaskEnd(String idTaskEnd) {
        this.destination = findTaskComponent(idTaskEnd);
    }

    public void zoomChanged() {
        redrawDependency();
    }

    public void redrawDependency() {
        response("zoomChanged", new AuInvoke(this, "draw"));
    }

    public boolean contains(Task task) {
        Task sourceTask = getSource().getTask();
        Task destinationTask = getDestination().getTask();
        return task.equals(sourceTask) || task.equals(destinationTask);
    }

    public TaskComponent getSource() {
        return source;
    }

    public TaskComponent getDestination() {
        return destination;
    }

    public Dependency getDependency() {
        return context.getDiagramGraph().getDependencyFrom(source.getTask(),
                destination.getTask());
    }

    public DependencyType getDependencyType() {
        return this.type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    public boolean hasSameSourceAndDestination(Dependency dependency) {
        Task sourceTask = source.getTask();
        Task destinationTask = destination.getTask();
        return sourceTask.equals(dependency.getSource())
                && destinationTask.equals(dependency.getDestination());
    }

}
