package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

public class ListDetails extends HtmlMacroComponent {

    /**
     * @author Óscar González Fernández <ogonzalez@igalia.com>
     */
    private final class TaskBeanRenderer implements TreeitemRenderer {
        @Override
        public void render(Treeitem item, Object data) throws Exception {
            SimpleTreeNode node = (SimpleTreeNode) data;
            TaskBean taskBean = (TaskBean) node.getData();
            Treerow treerow = new Treerow();
            treerow.setParent(item);
            Treecell treecell = new Treecell();
            treecell.setParent(treerow);
            TaskDetail taskDetail = TaskDetail.create(taskBean);
            taskDetail.setParent(treecell);
            taskDetail.afterCompose();
        }
    }

    private static Log LOG = LogFactory.getLog(ListDetails.class);

    private TaskRemovedListener taskRemovedListener;

    private final List<TaskBean> taskBeans;

    private SimpleTreeNode rootNode;

    private SimpleTreeModel tasksTreeModel;

    private Tree tasksTree;

    public ListDetails(List<TaskBean> taskBeans) {
        this.taskBeans = taskBeans;
    }

    private static List<SimpleTreeNode> asSimpleTreeNodes(
            List<TaskBean> taskBeans2) {
        return asSimpleTreeNodes(taskBeans2, new HashSet<TaskBean>());
    }

    private static List<SimpleTreeNode> asSimpleTreeNodes(
            List<TaskBean> taskBeans2, Set<TaskBean> alreadyIncluded) {
        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (TaskBean taskBean : taskBeans2) {
            if (alreadyIncluded.contains(taskBean))
                continue;
            SimpleTreeNode node = asSimpleTreeNode(taskBean);
            if (taskBean instanceof TaskContainerBean) {
                TaskContainerBean container = (TaskContainerBean) taskBean;
                node.getChildren()
                        .addAll(
                                asSimpleTreeNodes(container.getTasks(),
                                        alreadyIncluded));
            }
            result.add(node);
            alreadyIncluded.add(taskBean);
        }
        return result;
    }

    private static SimpleTreeNode asSimpleTreeNode(TaskBean taskBean) {
        return new SimpleTreeNode(taskBean, new ArrayList<SimpleTreeNode>());
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    public void taskRemoved(TaskBean taskRemoved) {
        // TODO pending
    }

    public void addTask() {
        TaskBean newTask = new TaskBean();
        newTask.setName("Nova Tarefa");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        addTask(newTask);
        getPlanner().addTask(newTask);
    }

    public void addTaskContainer() {
        TaskContainerBean newTask = new TaskContainerBean();
        newTask.setName("Novo Contedor de Tarefas");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        addTask(newTask);
        getPlanner().addTask(newTask);
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
        tasksTree = (Tree) getFellow("tasksTree");
        rootNode = new SimpleTreeNode(null, asSimpleTreeNodes(taskBeans));
        tasksTreeModel = new SimpleTreeModel(rootNode);
        tasksTree.setModel(tasksTreeModel);
        tasksTree.setTreeitemRenderer(new TaskBeanRenderer());
    }

    private void addTask(TaskBean taskBean) {
        rootNode.getChildren().add(
                new SimpleTreeNode(taskBean, new ArrayList<TaskBean>()));
        tasksTree.setModel(tasksTreeModel);
    }

}
