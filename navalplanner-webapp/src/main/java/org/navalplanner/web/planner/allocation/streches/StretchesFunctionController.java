/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.allocation.streches;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleXYModel;
import org.zkoss.zul.XYModel;
import org.zkoss.zul.api.Window;

public class StretchesFunctionController extends GenericForwardComposer {

    private Window window;

    private IStretchesFunctionModel stretchesFunctionModel;

    private StretchesRenderer stretchesRenderer = new StretchesRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public AssignmentFunction getAssignmentFunction() {
        return stretchesFunctionModel.getStretchesFunction();
    }

    public void setResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        AssignmentFunction assignmentFunction = resourceAllocation
                .getAssignmentFunction();
        Task task = resourceAllocation.getTask();
        stretchesFunctionModel.init((StretchesFunction) assignmentFunction,
                task);
        reloadStretchesListAndCharts();
    }

    public void showWindow() {
        try {
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void confirm() throws InterruptedException {
        try {
            stretchesFunctionModel.confirm();
            window.setVisible(false);
        } catch (ValidationException e) {
            Messagebox.show(e.getMessage(), _("Error"), Messagebox.OK,
                    Messagebox.ERROR);
        }
    }

    public void cancel() throws InterruptedException {
        int status = Messagebox.show(
                _("You will lose the changes. Are you sure?"),
                _("Confirm cancel"), Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION);
        if (Messagebox.YES == status) {
            stretchesFunctionModel.cancel();
            window.setVisible(false);
        }
    }

    public List<Stretch> getStretches() {
        return stretchesFunctionModel.getStretches();
    }

    public StretchesRenderer getStretchesRenderer() {
        return stretchesRenderer;
    }

    /**
     * Renders a {@link Stretch}.
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    private class StretchesRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            Stretch stretch = (Stretch) data;
            item.setValue(stretch);

            appendDate(item, stretch);
            appendLengthPercentage(item, stretch);
            appendAmountWorkPercentage(item, stretch);
            appendOperations(item, stretch);
        }

        private void appendChild(Listitem item, Component component) {
            Listcell listcell = new Listcell();
            listcell.appendChild(component);
            item.appendChild(listcell);
        }

        private void appendDate(Listitem item, final Stretch stretch) {
            final Datebox tempDatebox = new Datebox();
            Datebox datebox = Util.bind(tempDatebox, new Util.Getter<Date>() {
                @Override
                public Date get() {
                    return stretch.getDate().toDateTimeAtStartOfDay().toDate();
                }
            }, new Util.Setter<Date>() {
                @Override
                public void set(Date value) {
                    try {
                        stretchesFunctionModel.setStretchDate(stretch, value);
                        reloadStretchesListAndCharts();
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempDatebox, e
                                .getMessage());
                    }
                }
            });
            appendChild(item, datebox);
        }

        private void appendLengthPercentage(Listitem item, final Stretch stretch) {
            final Decimalbox tempDecimalbox = new Decimalbox();
            Decimalbox decimalbox = Util.bind(tempDecimalbox,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return stretch.getLengthPercentage().multiply(
                                    new BigDecimal(100));
                        }
                    }, new Util.Setter<BigDecimal>() {
                        @Override
                        public void set(BigDecimal value) {
                            value = value.setScale(2).divide(
                                    new BigDecimal(100), RoundingMode.DOWN);
                            try {
                                stretchesFunctionModel
                                        .setStretchLengthPercentage(stretch,
                                                value);
                                reloadStretchesListAndCharts();
                            } catch (IllegalArgumentException e) {
                                throw new WrongValueException(tempDecimalbox, e
                                        .getMessage());
                            }
                        }
                    });
            appendChild(item, decimalbox);
        }

        private void appendAmountWorkPercentage(Listitem item,
                final Stretch stretch) {
            final Decimalbox decimalBox = new Decimalbox();
            Util.bind(decimalBox,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return stretch.getAmountWorkPercentage().multiply(
                                    new BigDecimal(100));
                        }
                    }, new Util.Setter<BigDecimal>() {
                        @Override
                        public void set(BigDecimal value) {
                            if(value==null){
                                value = BigDecimal.ZERO;
                            }
                            value = value.setScale(2).divide(
                                    new BigDecimal(100), RoundingMode.DOWN);
                            try {
                                stretch.setAmountWorkPercentage(value);
                                reloadStretchesListAndCharts();
                            } catch (IllegalArgumentException e) {
                                throw new WrongValueException(
                                        decimalBox,
                                        _("Amount work percentage should be between 0 and 100"));
                            }
                        }
                    });
            appendChild(item, decimalBox);
        }

        private void appendOperations(Listitem item, final Stretch stretch) {
            Button button = new Button("", "/common/img/ico_borrar1.png");
            button.setHoverImage("/common/img/ico_borrar.png");
            button.setSclass("icono");
            button.setTooltiptext(_("Delete"));

            button.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    stretchesFunctionModel.removeStretch(stretch);
                    reloadStretchesListAndCharts();
                }
            });

            appendChild(item, button);
        }

    }

    public void addStretch() {
        stretchesFunctionModel.addStretch();
        reloadStretchesListAndCharts();
    }

    private void reloadStretchesListAndCharts() {
        Util.reloadBindings(window.getFellow("stretchesList"));
        Util.reloadBindings(window.getFellow("charts"));
    }

    public XYModel getDedicationChartData() {
        XYModel xymodel = new SimpleXYModel();

        List<Stretch> stretches = stretchesFunctionModel.getStretches();
        if (stretches.isEmpty()) {
            return xymodel;
        }

        String title = "hours";

        LocalDate previousDate = stretchesFunctionModel.getTaskStartDate();
        BigDecimal previousPercentage = BigDecimal.ZERO;

        BigDecimal taskHours = new BigDecimal(stretchesFunctionModel
                .getTaskHours());
        BaseCalendar calendar = stretchesFunctionModel.getTaskCalendar();

        xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                .getMillis(), 0);

        for (Stretch stretch : stretches) {
            BigDecimal amountWork = stretch.getAmountWorkPercentage().subtract(
                    previousPercentage).multiply(taskHours);
            Integer days = Days.daysBetween(previousDate, stretch.getDate())
                    .getDays();

            if (calendar != null) {
                days -= calendar.getNonWorkableDays(previousDate, stretch
                        .getDate()).size();
            }

            BigDecimal hoursPerDay = BigDecimal.ZERO;
            if (days > 0) {
                hoursPerDay = amountWork.divide(new BigDecimal(days),
                        RoundingMode.DOWN);
            }

            xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                    .getMillis() + 1, hoursPerDay);
            xymodel.addValue(title, stretch.getDate().toDateTimeAtStartOfDay()
                    .getMillis(), hoursPerDay);

            previousDate = stretch.getDate();
            previousPercentage = stretch.getAmountWorkPercentage();
        }

        xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                .getMillis() + 1, 0);

        return xymodel;
    }

    public XYModel getAccumulatedHoursChartData() {
        XYModel xymodel = new SimpleXYModel();

        List<Stretch> stretches = stretchesFunctionModel.getStretches();
        if (stretches.isEmpty()) {
            return xymodel;
        }

        String title = "percentage";

        LocalDate startDate = stretchesFunctionModel.getTaskStartDate();
        xymodel.addValue(title, startDate.toDateTimeAtStartOfDay().getMillis(),
                0);

        BigDecimal taskHours = new BigDecimal(stretchesFunctionModel
                .getTaskHours());

        for (Stretch stretch : stretches) {
            BigDecimal amountWork = stretch.getAmountWorkPercentage().multiply(
                    taskHours);

            xymodel.addValue(title, stretch.getDate().toDateTimeAtStartOfDay()
                    .getMillis(), amountWork);
        }

        return xymodel;
    }

}
