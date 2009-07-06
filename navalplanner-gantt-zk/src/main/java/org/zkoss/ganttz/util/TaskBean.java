package org.zkoss.ganttz.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

/**
 * This class contains the information of a task. It can be modified and
 * notifies of the changes to the interested parties. <br/>
 * Created at Apr 24, 2009
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskBean {

    private PropertyChangeSupport fundamentalProperties = new PropertyChangeSupport(
            this);

    private PropertyChangeSupport visibilityProperties = new PropertyChangeSupport(
            this);

    private String name;

    private Date beginDate = null;

    private long lengthMilliseconds = 0;

    private String notes;

    private boolean visible = true;

    public boolean isVisible() {
        return visible;
    }

    protected void setVisible(boolean visible) {
        boolean previousValue = this.visible;
        this.visible = visible;
        visibilityProperties.firePropertyChange("visible", previousValue,
                this.visible);
    }

    public TaskBean() {
    }

    public TaskBean(String name, Date beginDate, long lengthMilliseconds) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");
        if (beginDate == null)
            throw new IllegalArgumentException("beginDate cannot be null");
        if (lengthMilliseconds < 0)
            throw new IllegalArgumentException(
                    "length in milliseconds must be positive. Instead it is "
                            + lengthMilliseconds);
        this.name = name;
        this.beginDate = beginDate;
        this.lengthMilliseconds = lengthMilliseconds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String previousValue = this.name;
        this.name = name;
        fundamentalProperties.firePropertyChange("name", previousValue,
                this.name);
    }

    public void setBeginDate(Date beginDate) {
        Date previousValue = this.beginDate;
        this.beginDate = beginDate;
        fundamentalProperties.firePropertyChange("beginDate", previousValue,
                this.beginDate);
    }

    public Date getBeginDate() {
        return new Date(beginDate.getTime());
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        if (lengthMilliseconds < 0)
            throw new IllegalArgumentException(
                    "a task must not have a negative length. Received value: "
                            + lengthMilliseconds);
        long previousValue = this.lengthMilliseconds;
        this.lengthMilliseconds = lengthMilliseconds;
        fundamentalProperties.firePropertyChange("lengthMilliseconds",
                previousValue, this.lengthMilliseconds);
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    public void addVisibilityPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.visibilityProperties.addPropertyChangeListener(listener);
    }

    public void addFundamentalPropertiesChangeListener(
            PropertyChangeListener listener) {
        this.fundamentalProperties.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.fundamentalProperties.removePropertyChangeListener(listener);
    }

    public Date getEndDate() {
        return new Date(beginDate.getTime() + lengthMilliseconds);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        String previousValue = this.notes;
        this.notes = notes;
        fundamentalProperties.firePropertyChange("notes", previousValue,
                this.notes);
    }

    public void setEndDate(Date value) {
        setLengthMilliseconds(value.getTime() - beginDate.getTime());
    }

}