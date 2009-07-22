package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.adapters.AutoAdapter;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.DefaultFundamentalProperties;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.data.TaskLeaf;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;

/**
 * Some test data for planner <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DataForPlanner {

    public DataForPlanner() {
    }

    public GanttDiagramGraph getEmpty() {
        return new GanttDiagramGraph();
    }

    private PlannerConfiguration<ITaskFundamentalProperties> addCommands(
            PlannerConfiguration<ITaskFundamentalProperties> configuration) {
        configuration
                .addGlobalCommand(new ICommand<ITaskFundamentalProperties>() {

                    @Override
                    public String getName() {
                        return "Add Task";
                    }

                    @Override
                    public void doAction(
                            IContext<ITaskFundamentalProperties> context) {
                        addNewTask(context);
                    }
                });
        configuration
                .setGoingDownInLastArrowCommand(new ICommand<ITaskFundamentalProperties>() {

                    @Override
                    public void doAction(
                            IContext<ITaskFundamentalProperties> context) {
                        addNewTask(context);
                    }

                    @Override
                    public String getName() {
                        return "";
                    }
                });
        configuration
                .addCommandOnTask(new ICommandOnTask<ITaskFundamentalProperties>() {

                    @Override
                    public void doAction(
                            IContext<ITaskFundamentalProperties> context,
                            ITaskFundamentalProperties task) {
                        context.remove(task);
                    }

                    @Override
                    public String getName() {
                        return "Remove";
                    }
                });
        return configuration;
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getLightLoad() {
        return addCommands(getModelWith(20));
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getMediumLoad() {
        return addCommands(getModelWith(300));
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getHighLoad() {
        return addCommands(getModelWith(500));
    }

    private PlannerConfiguration<ITaskFundamentalProperties> getModelWith(
            int tasksToCreate) {
        List<ITaskFundamentalProperties> list = new ArrayList<ITaskFundamentalProperties>();
        Date now = new Date();
        Date end = twoMonthsLater(now);
        final ITaskFundamentalProperties container = createTask("container",
                now, end);
        final List<ITaskFundamentalProperties> containerChildren = new ArrayList<ITaskFundamentalProperties>();
        final ITaskFundamentalProperties child1 = createTask("child 1", now,
                end);
        containerChildren.add(child1);
        final DefaultFundamentalProperties child2 = createTask("another", now,
                end);
        containerChildren.add(child2);
        list.add(container);
        final ITaskFundamentalProperties first = createTask("tarefa1", now, end);
        final ITaskFundamentalProperties second = createTask("tarefa2", now,
                end);
        list.add(first);
        list.add(second);
        for (int i = 2; i < tasksToCreate - 3; i++) {
            String name = "tarefa " + (i + 1);
            ITaskFundamentalProperties task = createTask(name, now, end);
            list.add(task);
        }
        IStructureNavigator<ITaskFundamentalProperties> navigator = new IStructureNavigator<ITaskFundamentalProperties>() {

            @Override
            public List<ITaskFundamentalProperties> getChildren(
                    ITaskFundamentalProperties object) {
                if (object == container)
                    return containerChildren;
                return new ArrayList<ITaskFundamentalProperties>();
            }

            @Override
            public boolean isLeaf(ITaskFundamentalProperties object) {
                return object != container;
            }
        };
        return new PlannerConfiguration<ITaskFundamentalProperties>(
                new AutoAdapter() {
                    @Override
                    public List<DomainDependency<ITaskFundamentalProperties>> getDependenciesOriginating(
                            ITaskFundamentalProperties object) {
                        List<DomainDependency<ITaskFundamentalProperties>> result = new ArrayList<DomainDependency<ITaskFundamentalProperties>>();
                        if (child1 == object) {
                            result.add(DomainDependency.createDependency(
                                    child1, child2, DependencyType.END_START));
                        } else if (first == object) {
                            result.add(DomainDependency.createDependency(first,
                                    second, DependencyType.END_START));
                        }
                        return result;
                    }
                }, navigator, list);
    }

    private TaskContainer createContainer(String name, Date start, Date end) {
        TaskContainer container = new TaskContainer();
        container.setBeginDate(start);
        container.setEndDate(end);
        container.setName(name);
        return container;
    }

    private DefaultFundamentalProperties createTask(String name, Date now,
            Date end) {
        return new DefaultFundamentalProperties(name, end, end.getTime()
                - now.getTime(), "bla");
    }

    private void addNewTask(IContext<ITaskFundamentalProperties> context) {
        Task newTask = new TaskLeaf();
        newTask.setName("Nova Tarefa");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(twoMonthsLater(newTask.getBeginDate()));
        context.add(newTask);
    }

    private static Date twoMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 2);
        return calendar.getTime();
    }
}
