package org.navalplanner.web.planner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.adapters.AutoAdapter;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.util.DefaultFundamentalProperties;
import org.zkoss.ganttz.util.GanttDiagramGraph;
import org.zkoss.ganttz.util.DependencyType;
import org.zkoss.ganttz.util.ITaskFundamentalProperties;
import org.zkoss.ganttz.util.TaskContainerBean;

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

    public PlannerConfiguration<ITaskFundamentalProperties> getLightLoad() {
        return getModelWith(20);
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getMediumLoad() {
        return getModelWith(300);
    }

    public PlannerConfiguration<ITaskFundamentalProperties> getHighLoad() {
        return getModelWith(500);
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

    private TaskContainerBean createContainer(String name, Date start, Date end) {
        TaskContainerBean container = new TaskContainerBean();
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

    private static Date twoMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 2);
        return calendar.getTime();
    }
}
