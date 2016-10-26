/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.montecarlo;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.IBackGroundOperation;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdate;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdatesEmitter;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;
import org.zkoss.zul.Constraint;

/**
 * Controller for MonteCarlo graphic generation.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonteCarloController extends GenericForwardComposer {

    @Autowired
    private IMonteCarloModel monteCarloModel;

    private static final Integer DEFAULT_ITERATIONS = 10000;

    private static final Integer MAX_NUMBER_ITERATIONS = 100000;

    private final RowRenderer gridCriticalPathTasksRender = new CriticalPathTasksRender();

    private Grid gridCriticalPathTasks;

    private Intbox ibIterations;

    private Button btnRunMonteCarlo;

    private Checkbox cbGroupByWeeks;

    private Listbox lbCriticalPaths;

    private Progressmeter progressMonteCarloCalculation;

    private Window monteCarloChartWindow;

    public MonteCarloController() {

    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);

        ibIterations.setValue(DEFAULT_ITERATIONS);
        lbCriticalPaths.addEventListener(Events.ON_SELECT, event -> reloadGridCriticalPathTasks());

        btnRunMonteCarlo.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                validateRowsPercentages();
                IBackGroundOperation<IDesktopUpdate> operation = this::executeMontecarlo;
                LongOperationFeedback.progressive(self.getDesktop(), operation);
            }

            private void executeMontecarlo(IDesktopUpdatesEmitter<IDesktopUpdate> updatesEmitter) {
                try {
                    updatesEmitter.doUpdate(disableButton(true));
                    int iterations = getIterations();

                    final Map<LocalDate, BigDecimal> monteCarloData = monteCarloModel
                            .calculateMonteCarlo(getSelectedCriticalPath(),
                                    iterations,
                                    percentageCompletedNotifier(updatesEmitter));

                    updatesEmitter.doUpdate(showCalculatedData(monteCarloData));
                } finally {
                    updatesEmitter.doUpdate(disableButton(false));
                }
            }

            private IDesktopUpdate disableButton(final boolean disable) {
                return () -> btnRunMonteCarlo.setDisabled(disable);
            }

            private int getIterations() {
                int iterations = ibIterations.getValue() != null ? ibIterations.getValue().intValue() : 0;

                if ( iterations == 0 ) {
                    throw new WrongValueException(ibIterations, _("cannot be empty"));
                }

                if ( iterations < 0 || iterations > MAX_NUMBER_ITERATIONS ) {
                    throw new WrongValueException(ibIterations, _("Number of iterations should be between 1 and {0}",
                            MAX_NUMBER_ITERATIONS));
                }

                return iterations;
            }

            private void validateRowsPercentages() {
                Intbox intbox;

                int page = 0;
                int counter = 0;

                Rows rows = gridCriticalPathTasks.getRows();
                for (Object each : rows.getChildren()) {
                    Row row = (Row) each;
                    List<org.zkoss.zk.ui.Component> children = row.getChildren();

                    Integer sum = 0;
                    intbox = (Intbox) children.get(3);
                    sum += intbox.getValue();
                    intbox = (Intbox) children.get(5);
                    sum += intbox.getValue();
                    intbox = (Intbox) children.get(7);
                    sum += intbox.getValue();

                    if ( sum != 100 ) {
                        gridCriticalPathTasks.setActivePage(page);
                        throw new WrongValueException(row, _("Percentages should sum 100"));
                    }

                    counter++;
                    if ( counter % gridCriticalPathTasks.getPageSize() == 0 ) {
                        page++;
                    }
                }
            }

            private IDesktopUpdatesEmitter<Integer> percentageCompletedNotifier(
                    final IDesktopUpdatesEmitter<IDesktopUpdate> updatesEmitter) {
                return new IDesktopUpdatesEmitter<Integer>() {

                    @Override
                    public void doUpdate(final Integer percentage) {
                        updatesEmitter.doUpdate(showCompletedPercentage(percentage));
                    }

                    private IDesktopUpdate showCompletedPercentage(final Integer value) {
                        return () -> progressMonteCarloCalculation.setValue(value);
                    }
                };
            }

            private IDesktopUpdate showCalculatedData(final Map<LocalDate, BigDecimal> monteCarloData) {
                return () -> showMonteCarloGraph(monteCarloData);
            }

            private void showMonteCarloGraph(Map<LocalDate, BigDecimal> data) {
                monteCarloChartWindow = createMonteCarloGraphWindow(data);
                monteCarloChartWindow.setMode("modal");
            }

            private Window createMonteCarloGraphWindow(Map<LocalDate, BigDecimal> data) {
                HashMap<String, Object> args = new HashMap<>();
                args.put("monteCarloGraphController", new MonteCarloGraphController());
                Window result = (Window) Executions.createComponents("/planner/montecarlo_function.zul", self, args);
                MonteCarloGraphController controller =
                        (MonteCarloGraphController) result.getAttribute("monteCarloGraphController", true);

                final String orderName = monteCarloModel.getOrderName();
                final boolean groupByWeeks = cbGroupByWeeks.isChecked();

                controller.generateMonteCarloGraph(orderName,
                        data,
                        groupByWeeks,
                        () -> progressMonteCarloCalculation.setValue(0));

                return result;
            }

        });
    }

    private void feedCriticalPathsList() {
        lbCriticalPaths.setModel(new SimpleListModel<>(monteCarloModel.getCriticalPathNames()));

        if ( !lbCriticalPaths.getChildren().isEmpty() ) {
            lbCriticalPaths.setSelectedIndex(0);
        }
    }

    private void reloadGridCriticalPathTasks() {
        List<MonteCarloTask> selectedCriticalPath = getSelectedCriticalPath();

        if ( selectedCriticalPath != null ) {
            gridCriticalPathTasks.setModel(new SimpleListModel<>(selectedCriticalPath));
        }

        if ( gridCriticalPathTasks.getRowRenderer() == null ) {
            gridCriticalPathTasks.setRowRenderer(gridCriticalPathTasksRender);
            gridCriticalPathTasks.renderAll();
        }
    }

    public List<MonteCarloTask> getSelectedCriticalPath() {
        Listitem selectedItem = lbCriticalPaths.getSelectedItem();
        String selectedPath = selectedItem != null ? selectedItem.getLabel() : null;

        return monteCarloModel.getCriticalPath(selectedPath);
    }

    public void setCriticalPath(List<TaskElement> criticalPath) {
        monteCarloModel.setCriticalPath(criticalPath);

        if ( lbCriticalPaths != null ) {
            feedCriticalPathsList();
            reloadGridCriticalPathTasks();
        }

        btnRunMonteCarlo.setDisabled(monteCarloModel.getCriticalPathNames().isEmpty());
    }

    public Constraint getCheckConstraintIterationNumber() {
        return (comp, value) -> {
            Integer iterationNumber = value != null ? (Integer) value : -1;

            if (iterationNumber == -1) {
                throw new WrongValueException(comp, _("cannot be empty"));
            } else if (iterationNumber < 1 || iterationNumber > 100_000) {
                throw new WrongValueException(comp, _("Number of iterations should be between 1 and {0}",
                        MAX_NUMBER_ITERATIONS));
            }
        };
    }

    private static class CriticalPathTasksRender implements RowRenderer {

        @Override
        public void render(Row row, Object o, int i) throws Exception {
            row.setValue(o);

            MonteCarloTask task = (MonteCarloTask) o;

            row.appendChild(taskName(task));
            row.appendChild(duration(task));
            row.appendChild(optimisticDuration(task));
            row.appendChild(optimisticDurationPercentage(task));
            row.appendChild(normalDuration(task));
            row.appendChild(normalDurationPercentage(task));
            row.appendChild(pessimisticDuration(task));
            row.appendChild(pessimisticDurationPercentage(task));
        }

        private Label taskName(final MonteCarloTask task) {
            return new Label(task.getTaskName());
        }

        private Label duration(final MonteCarloTask task) {
            Double duration = task.getDuration().doubleValue();

            return new Label(duration.toString());
        }

        private Decimalbox pessimisticDuration(final MonteCarloTask task) {
            Decimalbox result = new Decimalbox();
            Util.bind(result,
                    task::getPessimisticDuration,
                    task::setPessimisticDuration);

            return result;
        }

        private Intbox pessimisticDurationPercentage(final MonteCarloTask task) {
            Intbox result = new Intbox();
            Util.bind(result,
                    task::getPessimisticDurationPercentage,
                    task::setPessimisticDurationPercentage);

            return result;
        }

        private Decimalbox normalDuration(final MonteCarloTask task) {
            Decimalbox result = new Decimalbox();
            Util.bind(result,
                    task::getNormalDuration,
                    task::setNormalDuration);

            return result;
        }

        private Intbox normalDurationPercentage(final MonteCarloTask task) {
            Intbox result = new Intbox();
            Util.bind(result,
                    task::getNormalDurationPercentage,
                    task::setNormalDurationPercentage);

            return result;
        }

        private Decimalbox optimisticDuration(final MonteCarloTask task) {
            Decimalbox result = new Decimalbox();
            Util.bind(result,
                    task::getOptimisticDuration,
                    task::setOptimisticDuration);

            return result;
        }

        private Intbox optimisticDurationPercentage(final MonteCarloTask task) {
            Intbox result = new Intbox();
            Util.bind(result,
                    task::getOptimisticDurationPercentage,
                    task::setOptimisticDurationPercentage);

            return result;
        }
    }

}
