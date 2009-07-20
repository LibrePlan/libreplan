package org.zkoss.ganttz;

import java.util.List;

import org.zkoss.ganttz.data.TaskBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Button;

/**
 * LeftPane of the planner. Responsible of showing global commands and the
 * leftTasksTree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class LeftPane extends HtmlMacroComponent {

    private final List<TaskBean> topLevelTasks;

    private List<? extends CommandContextualized<?>> commands;

    private LeftTasksTree leftTasksTree;

    public void setGoingDownInLastArrowCommand(
            CommandContextualized<?> goingDownInLastArrowCommand) {
        this.leftTasksTree
                .setGoingDownInLastArrowCommand(goingDownInLastArrowCommand);
    }

    public LeftPane(
            List<? extends CommandContextualized<?>> contextualizedCommands,
            List<TaskBean> topLevelTasks) {
        this.commands = contextualizedCommands;
        this.topLevelTasks = topLevelTasks;
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        leftTasksTree = new LeftTasksTree(topLevelTasks);
        addCommands();
        getContainer().appendChild(leftTasksTree);
        leftTasksTree.afterCompose();
    }

    private void addCommands() {
        Component commandsContainer = getCommandsContainer();
        for (CommandContextualized<?> command : commands) {
            Button button = command.toButton();
            commandsContainer.appendChild(button);
        }
    }

    private Component getContainer() {
        Component commandsContainer = getCommandsContainer();
        Component container = commandsContainer.getParent();
        return container;
    }

    private Component getCommandsContainer() {
        Component commandsContainer = getFellow("leftpane_commands");
        return commandsContainer;
    }

    public void taskRemoved(TaskBean taskBean) {
        leftTasksTree.taskRemoved(taskBean);
    }

    public void addTask(TaskBean newTask) {
        leftTasksTree.addTask(newTask);
    }

}
