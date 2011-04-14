/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner.allocation.streches;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunction.Interval;
import org.navalplanner.business.planner.entities.StretchesFunctionTypeEnum;
import org.navalplanner.web.planner.allocation.streches.StretchesFunctionController.IGraphicGenerator;
import org.zkoss.zul.SimpleXYModel;
import org.zkoss.zul.XYModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class GraphicForStreches implements IGraphicGenerator {

    public static IGraphicGenerator forType(StretchesFunctionTypeEnum type) {
        switch (type) {
        case STRETCHES:
            return new ForDefaultStreches();
        case INTERPOLATED:
            return new ForInterpolation();
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public XYModel getAccumulatedHoursChartData(
            IStretchesFunctionModel stretchesFunctionModel) {
        List<Stretch> stretches = stretchesFunctionModel.getStretches();
        if (stretches.isEmpty()) {
            return new SimpleXYModel();
        } else {
            return getAccumulatedHoursChartData(stretches,
                    stretchesFunctionModel.getTaskStartDate(), new BigDecimal(
                            stretchesFunctionModel.getAllocationHours()));
        }
    }

    @Override
    public XYModel getDedicationChart(
            IStretchesFunctionModel stretchesFunctionModel) {
        List<Stretch> stretches = stretchesFunctionModel.getStretches();
        if (stretches.isEmpty()) {
            return new SimpleXYModel();
        }
        return getDedicationChart(stretches, stretchesFunctionModel
                .getTaskStartDate(), new BigDecimal(stretchesFunctionModel
                .getAllocationHours()), stretchesFunctionModel.getTaskCalendar());
    }

    protected abstract XYModel getDedicationChart(List<Stretch> stretches,
            LocalDate startDate, BigDecimal totalHours,
            BaseCalendar taskCalendar);

    protected abstract XYModel getAccumulatedHoursChartData(
            List<Stretch> stretches, LocalDate startDate, BigDecimal taskHours);

    private static class ForDefaultStreches extends GraphicForStreches {

        @Override
        public boolean areChartsEnabled(IStretchesFunctionModel model) {
            return true;
        }

        @Override
        public XYModel getAccumulatedHoursChartData(List<Stretch> stretches,
                LocalDate startDate, BigDecimal taskHours) {
            XYModel xymodel = new SimpleXYModel();

            String title = "percentage";

            xymodel.addValue(title, startDate.toDateTimeAtStartOfDay()
                    .getMillis(), 0);

            for (Stretch stretch : stretches) {
                BigDecimal amountWork = stretch.getAmountWorkPercentage()
                        .multiply(taskHours);

                xymodel.addValue(title, stretch.getDate()
                        .toDateTimeAtStartOfDay().getMillis(), amountWork);
            }

            return xymodel;
        }

        protected XYModel getDedicationChart(List<Stretch> stretches,
                LocalDate startDate, BigDecimal taskHours,
                BaseCalendar calendar){
            XYModel xymodel = new SimpleXYModel();
            String title = "hours";

            LocalDate previousDate = startDate;
            BigDecimal previousPercentage = BigDecimal.ZERO;
            xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                    .getMillis(), 0);

            for (Stretch stretch : stretches) {
                BigDecimal amountWork = stretch.getAmountWorkPercentage()
                        .subtract(previousPercentage).multiply(taskHours);
                Integer days = Days
                        .daysBetween(previousDate, stretch.getDate()).getDays();

                if (calendar != null) {
                    days -= calendar.getNonWorkableDays(previousDate,
                            stretch.getDate()).size();
                }

                BigDecimal hoursPerDay = BigDecimal.ZERO;
                if (days > 0) {
                    hoursPerDay = amountWork.divide(new BigDecimal(days),
                            RoundingMode.DOWN);
                }

                xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                        .getMillis() + 1, hoursPerDay);
                xymodel.addValue(title, stretch.getDate()
                        .toDateTimeAtStartOfDay().getMillis(), hoursPerDay);

                previousDate = stretch.getDate();
                previousPercentage = stretch.getAmountWorkPercentage();
            }

            xymodel.addValue(title, previousDate.toDateTimeAtStartOfDay()
                    .getMillis() + 1, 0);

            return xymodel;
        }

    }

    private static class ForInterpolation extends GraphicForStreches {

        @Override
        public boolean areChartsEnabled(IStretchesFunctionModel model) {
            return canComputeChartFrom(model.getStretches(), model
                    .getTaskStartDate());
        }

        @Override
        protected XYModel getAccumulatedHoursChartData(List<Stretch> stretches,
                LocalDate startDate, BigDecimal taskHours) {
            if (!canComputeChartFrom(stretches, startDate)) {
                return new SimpleXYModel();
            }
            int[] hoursForEachDayUsingSplines = hoursForEachDayInterpolatedUsingSplines(
                    stretches, startDate, taskHours);
            return createModelFrom(startDate,
                    accumulatedFrom(hoursForEachDayUsingSplines));
        }

        @Override
        protected XYModel getDedicationChart(List<Stretch> stretches,
                LocalDate startDate, BigDecimal totalHours,
                BaseCalendar taskCalendar) {
            if (!canComputeChartFrom(stretches, startDate)) {
                return new SimpleXYModel();
            }
            int[] dataForChart = hoursForEachDayInterpolatedUsingSplines(
                    stretches, startDate, totalHours);
            return createModelFrom(startDate, dataForChart);
        }

        private boolean canComputeChartFrom(List<Stretch> stretches,
                LocalDate start) {
            return StretchesFunctionModel.areValidForInterpolation(stretches,
                    start);
        }

        private int[] hoursForEachDayInterpolatedUsingSplines(
                List<Stretch> stretches, LocalDate startDate,
                BigDecimal taskHours) {
            List<Interval> intervals = StretchesFunction
                    .intervalsFor(stretches);
            double[] dayPoints = Interval.getDayPointsFor(startDate, intervals);
            double[] hourPoints = Interval.getHoursPointsFor(taskHours
                    .intValue(), intervals);
            final Stretch lastStretch = stretches.get(stretches.size() - 1);
            return StretchesFunctionTypeEnum.hoursForEachDayUsingSplines(
                    dayPoints, hourPoints, startDate, lastStretch.getDate());
        }

        private int[] accumulatedFrom(int[] hoursForEachDayUsingSplines) {
            int[] result = new int[hoursForEachDayUsingSplines.length];
            int accumulated = 0;
            for (int i = 0; i < hoursForEachDayUsingSplines.length; i++) {
                accumulated += hoursForEachDayUsingSplines[i];
                result[i] = accumulated;
            }
            return result;
        }

        private XYModel createModelFrom(LocalDate startDate,
                int[] hoursForEachDay) {
            SimpleXYModel result = new SimpleXYModel();
            for (int i = 0; i < hoursForEachDay.length; i++) {
                result.addValue("series",
                        asMilliseconds(startDate.plusDays(i)),
                        hoursForEachDay[i]);
            }
            return result;
        }

        private long asMilliseconds(LocalDate day) {
            return day.toDateTimeAtStartOfDay().getMillis();
        }
    }

}
