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

import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDiagramGraph;
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

    public DependencyComponent(TaskComponent source, TaskComponent destination,
            DependencyType type) {
        this.type = type;
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");
        if (destination == null)
            throw new IllegalArgumentException("destination cannot be null");
        if (type == null)
            throw new IllegalArgumentException("type must be not null");
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

    public Dependency getDependency(GanttDiagramGraph diagramGraph) {
        return diagramGraph.getDependencyFrom(source.getTask(),
                destination.getTask());
    }

    public DependencyType getDependencyType() {
        return type;
    }

    public boolean hasSameSourceAndDestination(Dependency dependency) {
        Task sourceTask = source.getTask();
        Task destinationTask = destination.getTask();
        return sourceTask.equals(dependency.getSource())
                && destinationTask.equals(dependency.getDestination());
    }

}
