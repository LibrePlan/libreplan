/*
 * This file is part of LibrePlan
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

package org.libreplan.business.qualityforms.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.libreplan.business.INewObject;

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

    @NotEmpty(message = "task quality form item name not specified")
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "percentage not specified")
    public BigDecimal getPercentage() {
        return percentage;
    }

    private void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @NotNull(message = "position not specified")
    public Integer getPosition() {
        return position;
    }

    public String getStringPosition() {
        return position == null ? "" : Integer.valueOf(position + 1).toString();
    }

    private void setPosition(Integer position) {
        this.position = position;
    }

    @NotNull(message = "passed not specified")
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
        if (percentage == null) {
            return true;
        }
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
