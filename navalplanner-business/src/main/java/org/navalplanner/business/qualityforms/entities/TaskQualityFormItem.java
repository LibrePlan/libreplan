package org.navalplanner.business.qualityforms.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.INewObject;

public class TaskQualityFormItem implements INewObject {

    public final static String propertyDate = "date";

    public final static String propertyPassed = "passed";

    static TaskQualityFormItem create(QualityFormItem qualityFormItem) {
        TaskQualityFormItem taskQualityFormItem = new TaskQualityFormItem(
                qualityFormItem);
        taskQualityFormItem.setNewObject(true);
        return taskQualityFormItem;
    }

    public TaskQualityFormItem() {

    }

    private TaskQualityFormItem(QualityFormItem qualityFormItem) {
        Validate.notNull(qualityFormItem);
        setName(qualityFormItem.getName());
        setPosition(qualityFormItem.getPosition());
        setPercentage(qualityFormItem.getPercentage());
    }

    private boolean newObject = false;

    private String name;

    private BigDecimal percentage;

    private Integer position;

    private Boolean passed = false;

    private Date date;

    @NotEmpty
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @NotNull
    public BigDecimal getPercentage() {
        return percentage;
    }

    private void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @NotNull
    public Integer getPosition() {
        return position;
    }

    private void setPosition(Integer position) {
        this.position = position;
    }

    @NotNull
    public Boolean getPassed() {
        return passed == null ? false : passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isNewObject() {
        return newObject;
    }

    private void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "percentage should be greater than 0% and less than 100%")
    public boolean checkConstraintQualityFormItemPercentage() {
        if (percentage == null)
            return true;
        if ((percentage.compareTo(new BigDecimal(100).setScale(2)) <= 0)
                && (percentage.compareTo(new BigDecimal(0).setScale(2)) > 0)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "date not specified")
    public boolean checkConstraintIfDateCanBeNull() {
        if ((passed == null) || (!passed)) {
            return true;
        } else {
            return (date != null);
        }
    }
}
