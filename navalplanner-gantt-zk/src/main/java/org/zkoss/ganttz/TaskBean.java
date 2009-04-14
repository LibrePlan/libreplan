package org.zkoss.ganttz;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

public class TaskBean {

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);

    private String name;

    private Date beginDate = null;

    private long lengthMilliseconds = 0;

    private String notes;

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
        propertyChangeSupport.firePropertyChange("name", previousValue,
                this.name);
    }

    public void setBeginDate(Date beginDate) {
        Date previousValue = this.beginDate;
        this.beginDate = beginDate;
        propertyChangeSupport.firePropertyChange("beginDate", previousValue,
                this.beginDate);
    }

    public Date getBeginDate() {
        return new Date(beginDate.getTime());
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        long previousValue = this.lengthMilliseconds;
        this.lengthMilliseconds = lengthMilliseconds;
        propertyChangeSupport.firePropertyChange("lengthMilliseconds",
                previousValue, this.lengthMilliseconds);
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
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
        propertyChangeSupport.firePropertyChange("notes", previousValue,
                this.notes);
    }

    public void setEndDate(Date value) {
        setLengthMilliseconds(value.getTime() - beginDate.getTime());
    }

}