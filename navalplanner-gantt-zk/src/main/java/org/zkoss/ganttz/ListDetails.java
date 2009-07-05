package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.TaskDetail.ITaskDetailNavigator;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

public class ListDetails extends HtmlMacroComponent {

    private final class TaskBeanRenderer implements TreeitemRenderer {
        public void render(Treeitem item, Object data) throws Exception {
            SimpleTreeNode node = (SimpleTreeNode) data;
            TaskBean taskBean = (TaskBean) node.getData();
            Treerow treerow = new Treerow();
            treerow.setParent(item);
            item.setOpen(isOpened(taskBean));
            Treecell treecell = new Treecell();
            treecell.setParent(treerow);
            final int[] path = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    node);
            TaskDetail taskDetail = TaskDetail.create(taskBean,
                    new TreeNavigator(tasksTreeModel, path));
            if (taskBean instanceof TaskContainerBean) {
                expandWhenOpened((TaskContainerBean) taskBean, item);
            }
            taskDetail.setParent(treecell);
            detailsForBeans.put(taskBean, taskDetail);
            taskDetail.afterCompose();
        }

        private void expandWhenOpened(final TaskContainerBean taskBean,
                Treeitem item) {
            item.addEventListener("onOpen", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    OpenEvent openEvent = (OpenEvent) event;
                    taskBean.setExpanded(openEvent.isOpen());
                }
            });
        }

    }

    public boolean isOpened(TaskBean taskBean) {
        if (taskBean instanceof TaskContainerBean) {
            TaskContainerBean container = (TaskContainerBean) taskBean;
            return container.isExpanded();
        }
        return true;
    }

    private Map<TaskBean, TaskDetail> detailsForBeans = new HashMap<TaskBean, TaskDetail>();

    private final class TreeNavigator implements ITaskDetailNavigator {
        private final int[] pathToNode;

        private final TreeModel treemodel;

        private SimpleTreeNode parentCached;

        private TreeNavigator(TreeModel treemodel, int[] pathToNode) {
            this.treemodel = treemodel;
            this.pathToNode = pathToNode;
        }

        @Override
        public TaskDetail getAboveDetail() {
            SimpleTreeNode parent = getParent(pathToNode);
            int lastPosition = pathToNode[pathToNode.length - 1];
            if (lastPosition != 0) {
                return getChild(parent, lastPosition - 1);
            } else if (treemodel.getRoot() != parent) {
                return detailsForBeans.get(getTaskBean(parent));
            }
            return null;
        }

        private TaskDetail getChild(SimpleTreeNode parent, int position) {
            SimpleTreeNode node = (SimpleTreeNode) parent.getChildren().get(
                    position);
            TaskBean bean = getTaskBean(node);
            return detailsForBeans.get(bean);
        }

        @Override
        public TaskDetail getBelowDetail() {
            SimpleTreeNode parent = getParent(pathToNode);
            int childCount = parent.getChildCount();
            int lastPosition = pathToNode[pathToNode.length - 1];
            int belowPosition = lastPosition + 1;
            if (belowPosition < childCount) {
                return getChild(parent, belowPosition);
            }
            return null;
        }

        private SimpleTreeNode getParent(int[] path) {
            if (parentCached != null)
                return parentCached;
            SimpleTreeNode current = (SimpleTreeNode) treemodel.getRoot();
            for (int i = 0; i < path.length - 1; i++) {
                current = (SimpleTreeNode) current.getChildren().get(path[i]);
            }
            return parentCached = current;
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

    private static TaskBean getTaskBean(SimpleTreeNode node) {
        return (TaskBean) node.getData();
    }

    private static List<SimpleTreeNode> asSimpleTreeNodes(
            List<TaskBean> taskBeans) {
        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (TaskBean taskBean : taskBeans) {
            SimpleTreeNode node = asSimpleTreeNode(taskBean);
            if (taskBean instanceof TaskContainerBean) {
                TaskContainerBean container = (TaskContainerBean) taskBean;
                node.getChildren().addAll(
                        asSimpleTreeNodes(container.getTasks()));
            }
            result.add(node);
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
