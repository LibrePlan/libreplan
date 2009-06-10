package org.navalplanner.web.workorders;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.workorders.entities.ITaskWorkContainer;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWork;
import org.navalplanner.business.workorders.entities.TaskWorkContainer;
import org.navalplanner.business.workorders.entities.TaskWorkLeaf;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for a the tasks tree for a project <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskTreeModel extends SimpleTreeModel {

    private static List<SimpleTreeNode> asNodes(List<TaskWork> taskWorks) {
        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (TaskWork taskWork : taskWorks) {
            result.add(asNode(taskWork));
        }
        return result;
    }

    private static SimpleTreeNode asNode(TaskWork taskWork) {
        taskWork.forceLoadActivities();
        return new SimpleTreeNode(taskWork, asNodes(taskWork.getChildren()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            ProjectWork project) {
        return new SimpleTreeNode(project, asNodes(project.getTaskWorks()));
    }

    public TaskTreeModel(ProjectWork projectWork) {
        super(createRootNodeAndDescendants(projectWork));
    }

    public void reloadFromProjectWork() {
        ProjectWork root = getRootAsProject();
        SimpleTreeNode rootAsNode = getRootAsNode();
        rootAsNode.getChildren().clear();
        rootAsNode.getChildren().addAll(asNodes(root.getTaskWorks()));
    }

    public void addTask() {
        addTaskAtImpl(getRootAsNode());
        reloadFromProjectWork();
    }

    private TaskWork createNewTask() {
        TaskWork newTask = new TaskWorkLeaf();
        newTask.setName("Nova Tarefa");
        return newTask;
    }

    public void addTaskAt(SimpleTreeNode node) {
        addTaskAtImpl(node);
        reloadFromProjectWork();
    }

    private void addTaskAtImpl(SimpleTreeNode node) {
        addTaskAtImpl(node, createNewTask());
    }

    private void addTaskAtImpl(SimpleTreeNode node, TaskWork task) {
        addTaskAtImpl(node, task, node.getChildCount());
    }

    private void addTaskAtImpl(SimpleTreeNode destinationNode, TaskWork task,
            int position) {
        ITaskWorkContainer container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, task);
    }

    private ITaskWorkContainer turnIntoContainerIfNeeded(
            SimpleTreeNode selectedForTurningIntoContainer) {
        ITaskWorkContainer parentContainer = asTaskContainer(getParent(selectedForTurningIntoContainer));
        if (selectedForTurningIntoContainer.getData() instanceof ITaskWorkContainer)
            return (ITaskWorkContainer) selectedForTurningIntoContainer
                    .getData();
        TaskWork toBeTurned = asTask(selectedForTurningIntoContainer);
        TaskWorkContainer asContainer = toBeTurned.asContainer();
        parentContainer.replace(toBeTurned, asContainer);
        return asContainer;
    }

    private SimpleTreeNode getParent(SimpleTreeNode node) {
        int[] position = getPath(node);
        SimpleTreeNode current = getRootAsNode();
        SimpleTreeNode[] path = new SimpleTreeNode[position.length];
        for (int i = 0; i < position.length; i++) {
            path[i] = (SimpleTreeNode) current.getChildAt(position[i]);
            current = path[i];
        }
        int parentOfLast = path.length - 2;
        if (parentOfLast >= 0)
            return path[parentOfLast];
        else
            return getRootAsNode();
    }

    public void indent(SimpleTreeNode nodeToIndent) {
        SimpleTreeNode parentOfSelected = getParent(nodeToIndent);
        int position = parentOfSelected.getChildren().indexOf(nodeToIndent);
        if (position == 0) {
            return;
        }
        SimpleTreeNode destination = (SimpleTreeNode) parentOfSelected
                .getChildren().get(position - 1);
        moveImpl(nodeToIndent, destination, destination.getChildCount());
        reloadFromProjectWork();
    }

    public void unindent(SimpleTreeNode nodeToUnindent) {
        SimpleTreeNode parent = getParent(nodeToUnindent);
        if (getRootAsNode() == parent) {
            return;
        }
        SimpleTreeNode destination = getParent(parent);
        moveImpl(nodeToUnindent, destination, destination.getChildren()
                .indexOf(parent) + 1);
        reloadFromProjectWork();
    }

    public void move(SimpleTreeNode toBeMoved, SimpleTreeNode destination) {
        moveImpl(toBeMoved, destination, destination.getChildCount());
        reloadFromProjectWork();
    }

    private void moveImpl(SimpleTreeNode toBeMoved, SimpleTreeNode destination,
            int position) {
        if (destination.getChildren().contains(toBeMoved)) {
            return;// it's already moved
        }
        removeNodeImpl(toBeMoved);
        addTaskAtImpl(destination, asTask(toBeMoved), position);
    }

    public int[] getPath(SimpleTreeNode destination) {
        int[] path = getPath(getRootAsNode(), destination);
        return path;
    }

    public void up(SimpleTreeNode node) {
        ITaskWorkContainer taskWorkContainer = asTaskContainer(getParent(node));
        taskWorkContainer.up(asTask(node));
        reloadFromProjectWork();
    }

    public void down(SimpleTreeNode node) {
        ITaskWorkContainer taskWorkContainer = asTaskContainer(getParent(node));
        taskWorkContainer.down(asTask(node));
        reloadFromProjectWork();
    }

    private ProjectWork getRootAsProject() {
        return (ProjectWork) getRootAsNode().getData();
    }

    private static TaskWork asTask(SimpleTreeNode node) {
        return (TaskWork) node.getData();
    }

    private static ITaskWorkContainer asTaskContainer(SimpleTreeNode node) {
        return (ITaskWorkContainer) node.getData();
    }

    private SimpleTreeNode getRootAsNode() {
        return (SimpleTreeNode) getRoot();
    }

    public void removeNode(SimpleTreeNode value) {
        removeNodeImpl(value);
        reloadFromProjectWork();
    }

    private void removeNodeImpl(SimpleTreeNode value) {
        if (value == getRootAsNode())
            return;
        ITaskWorkContainer taskContainer = asTaskContainer(getParent(value));
        taskContainer.remove(asTask(value));
    }

}