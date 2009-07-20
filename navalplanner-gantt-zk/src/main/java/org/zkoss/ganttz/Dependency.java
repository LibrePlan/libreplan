/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.zkoss.ganttz.data.DependencyBean;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.impl.XulElement;

/**
 *
 * @author Francisco Javier Moran Rúa
 *
 */
public class Dependency extends XulElement implements AfterCompose {

    private Task source;

    private Task destination;

    public Dependency() {


    }

    public Dependency(Task source, Task destination) {
        this();
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
        this.source.getTaskBean().addFundamentalPropertiesChangeListener(listener);
        this.destination.getTaskBean().addFundamentalPropertiesChangeListener(listener);
    }

    /**
     * @return the idTaskOrig
     */
    public String getIdTaskOrig() {
        return source.getUuid();
    }

    public void setIdTaskOrig(String idTaskOrig) {
        this.source = findTask(idTaskOrig);

    }

    private Task findTask(String idTaskOrig) {
        return (Task) getFellow(idTaskOrig);
    }

    /**
     * @return the idTaskEnd
     */
    public String getIdTaskEnd() {
        return destination.getUuid();
    }

    public void setIdTaskEnd(String idTaskEnd) {
        this.destination = findTask(idTaskEnd);
    }

    public void zoomChanged() {
        redrawDependency();
    }

    public void redrawDependency() {
        response("zoomChanged", new AuInvoke(this, "draw"));
    }

    public boolean contains(Task task) {
        return getSource().equals(task) || getDestination().equals(task);
    }

    public Task getSource() {
        return source;
    }

    public Task getDestination() {
        return destination;
    }

    public DependencyBean getDependencyBean() {
        return new DependencyBean(source.getTaskBean(), destination
                .getTaskBean(), DependencyType.END_START);
    }

}
