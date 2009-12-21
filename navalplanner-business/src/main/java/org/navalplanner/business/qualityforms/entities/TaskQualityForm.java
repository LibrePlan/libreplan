package org.navalplanner.business.qualityforms.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

public class TaskQualityForm extends BaseEntity {

    public static TaskQualityForm create(OrderElement orderElement,
            QualityForm qualityForm) {
        TaskQualityForm taskQualityForm = new TaskQualityForm(orderElement,
                qualityForm);
        taskQualityForm.setNewObject(true);
        return taskQualityForm;
    }

    protected TaskQualityForm() {

    }

    private TaskQualityForm(OrderElement orderElement, QualityForm qualityForm) {
        this.orderElement = orderElement;
        this.qualityForm = qualityForm;
        createTaskQualityFormItems();
    }

    private OrderElement orderElement;

    private QualityForm qualityForm;

    private List<TaskQualityFormItem> taskQualityFormItems = new ArrayList<TaskQualityFormItem>();

    @Valid
    public List<TaskQualityFormItem> getTaskQualityFormItems() {
        return Collections.unmodifiableList(taskQualityFormItems);
    }

    public void setTaskQualityFormItems(
            List<TaskQualityFormItem> taskQualityFormItems) {
        this.taskQualityFormItems = taskQualityFormItems;
    }

    @NotNull
    public OrderElement getOrderElement() {
        return orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @NotNull
    public QualityForm getQualityForm() {
        return qualityForm;
    }

    public void setQualityForm(QualityForm qualityForm) {
        this.qualityForm = qualityForm;
    }

    private void createTaskQualityFormItems() {
        Validate.notNull(qualityForm);
        for (QualityFormItem qualityFormItem : qualityForm
                .getQualityFormItems()) {
            TaskQualityFormItem taskQualityFormItem = TaskQualityFormItem
                    .create(qualityFormItem);
            taskQualityFormItems.add(taskQualityFormItem);
        }
    }

}
