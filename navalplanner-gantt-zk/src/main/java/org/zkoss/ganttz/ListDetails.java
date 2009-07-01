package org.zkoss.ganttz;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class ListDetails extends HtmlMacroComponent {

    private static Log LOG = LogFactory.getLog(ListDetails.class);

    private TaskRemovedListener taskRemovedListener;

    private final List<TaskBean> taskBeans;

    public ListDetails(List<TaskBean> taskBeans) {
        this.taskBeans = taskBeans;
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    private List<TaskDetail> getTaskDetails() {
        List<Object> children = getInsertionPoint().getChildren();
        return Planner.findComponentsOfType(TaskDetail.class, children);
    }

    public void taskRemoved(TaskBean taskRemoved) {
        List<TaskDetail> taskDetails = getTaskDetails();
        for (TaskDetail taskDetail : taskDetails) {
            if (taskDetail.getTaskBean().equals(taskRemoved)) {
                removeDetail(taskDetail);
                return;
            }
        }
        throw new RuntimeException("not found taskDetail for " + taskRemoved);
    }

    private void removeDetail(TaskDetail taskDetail) {
        getInsertionPoint().getChildren().remove(taskDetail);
    }

    public void addTask() {
        TaskBean newTask = new TaskBean();
        newTask.setName("Nova Tarefa");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        TaskDetail newDetail = addTask(newTask);
        newDetail.receiveFocus();
        getPlanner().addTask(newTask);
    }

    public void addTaskContainer() {
        TaskContainerBean newTask = new TaskContainerBean();
        newTask.setName("Novo Contedor de Tarefas");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        TaskDetail newDetail = addTask(newTask);
        newDetail.receiveFocus();
        getPlanner().addTaskContainer(newTask);
    }

    private static Date threeMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        return calendar.getTime();
    }

    @Override
    public void afterCompose() {
        setClass("listdetails");
        super.afterCompose();
        for (TaskBean taskBean : taskBeans) {
            addTask(taskBean);
        }
    }

    private TaskDetail addTask(TaskBean taskBean) {
        TaskDetail taskDetail = TaskDetail.create(taskBean);
        getInsertionPoint().appendChild(taskDetail);
        taskDetail.afterCompose();
        return taskDetail;
    }

    private Component getInsertionPoint() {
        return getFellow("insertionPoint");
    }

}
