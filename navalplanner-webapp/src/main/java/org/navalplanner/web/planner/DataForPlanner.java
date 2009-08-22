package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.adapters.AutoAdapter;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.DefaultFundamentalProperties;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.data.TaskLeaf;
import org.zkoss.ganttz.data.resourceload.LoadLevel;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.extensions.ITabFactory;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel;
import org.zkoss.zk.ui.Component;

/**
 * Some test data for planner <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DataForPlanner {

    private TaskEditFormComposer taskEditForm = new TaskEditFormComposer();

    public DataForPlanner() {

    }

    public GanttDiagramGraph getEmpty() {
        return new GanttDiagramGraph();
    }

    private PlannerConfiguration<ITaskFundamentalProperties> setup(
            PlannerConfiguration<ITaskFundamentalProperties> configuration) {
        addCommands(configuration);
        addTabs(configuration);
        return configuration;
    }

    private void addTabs(
            PlannerConfiguration<ITaskFundamentalProperties> configuration) {
        configuration.addTab(new ITabFactory<ITaskFundamentalProperties>() {

            @Override
            public ITab create(
                    final IContext<ITaskFundamentalProperties> context) {
                return new ITab() {

                    private Component parent;

                    private ResourcesLoadPanel loadPanel;

                    @Override
                    public void show() {
                        loadPanel = new ResourcesLoadPanel(
                                createFakeDataForResourcesLoad(), context
                                        .getTimeTracker());
                        parent.appendChild(loadPanel);
                        loadPanel.afterCompose();
                    }

                    @Override
                    public void hide() {
                        if (loadPanel != null) {
                            loadPanel.detach();
                        }
                    }

                    @Override
                    public String getName() {
                        return _("Resource Load");
                    }

                    @Override
                    public void addToParent(Component parent) {
                        this.parent = parent;
                    }
                };
            }
        });
    }

    private List<LoadTimelinesGroup> createFakeDataForResourcesLoad() {
        List<LoadTimelinesGroup> result = new ArrayList<LoadTimelinesGroup>();
        LoadTimeLine resource1 = new LoadTimeLine("resource1",
                createFakePeriodsStartingAt(new LocalDate(2009, 2, 3), Duration
                        .standardDays(20), Duration.standardDays(90), 3));
        LoadTimeLine task1 = new LoadTimeLine("task1",
                createFakePeriodsStartingAt(new LocalDate(2009, 5, 4), Duration
                        .standardDays(20), Duration.standardDays(70), 3));
        LoadTimeLine task2 = new LoadTimeLine("task2",
                createFakePeriodsStartingAt(new LocalDate(2009, 4, 1), Duration
                        .standardDays(20), Duration.standardDays(90), 3));
        LoadTimeLine task3 = new LoadTimeLine("task3",
                createFakePeriodsStartingAt(new LocalDate(2009, 6, 1), Duration
                        .standardDays(20), Duration.standardDays(40), 4));
        LoadTimeLine resource2 = new LoadTimeLine(
                "resource1",
                createFakePeriodsStartingAt(new LocalDate(2009, 10, 1),
                        Duration.standardDays(20), Duration.standardDays(90), 2));
        result.add(new LoadTimelinesGroup(resource1, Arrays
                .asList(task1, task2)));
        result.add(new LoadTimelinesGroup(resource2, Arrays.asList(task3)));
        return result;
    }

    private List<LoadPeriod> createFakePeriodsStartingAt(LocalDate start,
            Duration separation, Duration periodLength, int numberOfPeriods) {
        DateTime current = start.toDateMidnight().toDateTime();
        List<LoadPeriod> result = new ArrayList<LoadPeriod>();
        for (int i = 0; i < numberOfPeriods; i++) {
            DateTime previous = current.plus(separation);
            current = previous.plus(periodLength);
            result
                    .add(new LoadPeriod(previous.toLocalDate(), current
                            .toLocalDate(), new LoadLevel(
                            (int) (Math.random() * 150))));
        }
        return result;
    }

    private void addCommands(
            PlannerConfiguration<ITaskFundamentalProperties> configuration) {
        configuration
                .addGlobalCommand(new ICommand<ITaskFundamentalProperties>() {

                    @Override
                    public String getName() {
                        return _("Add Task");
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
                            IContextWithPlannerTask<ITaskFundamentalProperties> context,
                            ITaskFundamentalProperties task) {
                        context.remove(task);
                    }

                    @Override
                    public String getName() {
                        return _("Remove");
                    }

                });
        configuration
                .setEditTaskCommand(new ICommandOnTask<ITaskFundamentalProperties>() {

                    @Override
                    public void doAction(
                            IContextWithPlannerTask<ITaskFundamentalProperties> context,
                            ITaskFundamentalProperties task) {
                        taskEditForm.showEditFormFor(context.getRelativeTo(),
                                context.getTask());
                    }

                    @Override
                    public String getName() {
                        return "";
                    }
                });
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getLightLoad() {
        return setup(getModelWith(20));
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getMediumLoad() {
        return setup(getModelWith(300));
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getHighLoad() {
        return setup(getModelWith(500));
    }

    private PlannerConfiguration<ITaskFundamentalProperties> getModelWith(
            int tasksToCreate) {
        List<ITaskFundamentalProperties> list = new ArrayList<ITaskFundamentalProperties>();
        Date now = new Date();
        Date end = twoMonthsLater(now);
        final ITaskFundamentalProperties container = createTask(_("container"),
                now, end);
        final List<ITaskFundamentalProperties> containerChildren = new ArrayList<ITaskFundamentalProperties>();
        final ITaskFundamentalProperties child1 = createTask(_("child 1"), now,
                end);
        containerChildren.add(child1);
        final DefaultFundamentalProperties child2 = createTask(_("another"),
                now, end);
        containerChildren.add(child2);
        list.add(container);
        final ITaskFundamentalProperties first = createTask(_("task1"), now,
                end);
        final ITaskFundamentalProperties second = createTask(_("task2"), now,
                end);
        list.add(first);
        list.add(second);
        for (int i = 2; i < tasksToCreate - 3; i++) {
            String name = _("task{0}", (i + 1));
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
                    public List<DomainDependency<ITaskFundamentalProperties>> getOutcomingDependencies(
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

    private DefaultFundamentalProperties createTask(String name, Date now,
            Date end) {
        return new DefaultFundamentalProperties(name, end, end.getTime()
                - now.getTime(), _("bla"));
    }

    private void addNewTask(IContext<ITaskFundamentalProperties> context) {
        Task newTask = new TaskLeaf();
        newTask.setName(_("New task"));
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

    public TaskEditFormComposer getTaskEditForm() {
        return taskEditForm;
    }
}
