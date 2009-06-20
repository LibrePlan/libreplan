package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;

public class TaskDetail extends HtmlMacroComponent implements AfterCompose {

    static String format(Date date) {
        return dateFormat.format(date);
    }

    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private static final Log LOG = LogFactory.getLog(TaskDetail.class);

    private final TaskBean taskBean;

    public TaskBean getTaskBean() {
        return taskBean;
    }

    private Textbox nameBox;

    public Textbox getNameBox() {
        return nameBox;
    }

    public void setNameBox(Textbox nameBox) {
        this.nameBox = nameBox;
    }

    public Datebox getStartDateBox() {
        return startDateBox;
    }

    public void setStartDateBox(Datebox startDateBox) {
        this.startDateBox = startDateBox;
        this.startDateBox.setCompact(true);
        this.startDateBox.setFormat("dd/MM/yyyy");
    }

    public Datebox getEndDateBox() {
        return endDateBox;
    }

    public void setEndDateBox(Datebox endDateBox) {
        this.endDateBox = endDateBox;
        this.endDateBox.setFormat("dd/MM/yyyy");
    }

    private Datebox startDateBox;

    private Datebox endDateBox;

    public static TaskDetail create(TaskBean bean) {
        return new TaskDetail(bean);
    }

    private TaskDetail(TaskBean task) {
        this.taskBean = task;
    }

    public TaskBean getData() {
        return taskBean;
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        updateComponents();
        taskBean.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateComponents();
            }
        });
    }

    public void updateBean() {
        if (getEndDateBox().getValue().before(getStartDateBox().getValue())) {
            updateComponents();
            return;
        }
        taskBean.setName(getNameBox().getValue());
        taskBean.setBeginDate(getStartDateBox().getValue());
        taskBean.setEndDate(getEndDateBox().getValue());
    }

    private void updateComponents() {
        getNameBox().setValue(taskBean.getName());
        getStartDateBox().setValue(taskBean.getBeginDate());
        getEndDateBox().setValue(taskBean.getEndDate());
    }

}
