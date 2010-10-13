/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.adapters.AutoAdapter;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.DefaultFundamentalProperties;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.ITaskFundamentalProperties;
import org.zkoss.ganttz.extensions.ICommand;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Some test data for planner <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DataForPlanner {

    private TaskEditFormComposer taskEditForm = new TaskEditFormComposer();

    public DataForPlanner() {

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
                    public String getImage() {
                        return "";
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

                    @Override
                    public String getImage() {
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
                        return "Remove";
                    }

                    @Override
                    public String getIcon() {
                        return null;
                    }

                    @Override
                    public boolean isApplicableTo(ITaskFundamentalProperties task) {
                        return true;
                    }

                });
        configuration.setDoubleClickCommand(new ICommandOnTask<ITaskFundamentalProperties>() {

                    @Override
                    public void doAction(
                            IContextWithPlannerTask<ITaskFundamentalProperties> context,
                            ITaskFundamentalProperties task) {
                        taskEditForm.init(context.getGanttDiagramGraph(), context.getRelativeTo(),
                                context.getTask());
                    }

                    @Override
                    public String getName() {
                        return "";
                    }

                    @Override
                    public String getIcon() {
                        return null;
                    }

                    @Override
                    public boolean isApplicableTo(ITaskFundamentalProperties task) {
                        return true;
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
                if (object == container) {
                    return containerChildren;
                }
                return new ArrayList<ITaskFundamentalProperties>();
            }

            @Override
            public boolean isLeaf(ITaskFundamentalProperties object) {
                return object != container;
            }

            @Override
            public boolean isMilestone(ITaskFundamentalProperties object) {
                return false;
            }
        };
        return mustStartNotTwoMonthsBeforeThan(now,
                new PlannerConfiguration<ITaskFundamentalProperties>(
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
                }, navigator, list));
    }

    private PlannerConfiguration<ITaskFundamentalProperties> mustStartNotTwoMonthsBeforeThan(
            Date date,
            PlannerConfiguration<ITaskFundamentalProperties> plannerConfiguration) {
        plannerConfiguration.setNotBeforeThan(twoMonthsBefore(date));
        return plannerConfiguration;
    }


    private DefaultFundamentalProperties createTask(String name, Date now,
            Date end) {
        return new DefaultFundamentalProperties(name, end, end.getTime()
                - now.getTime(), _("bla"), now, now, new BigDecimal(0.25),
                new BigDecimal(0.5));
    }

    private void addNewTask(IContext<ITaskFundamentalProperties> context) {
        context.add(createTask(_("New task"), new Date(),
                twoMonthsLater(new Date())));
    }

    private Date twoMonthsBefore(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -2);
        return calendar.getTime();
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
