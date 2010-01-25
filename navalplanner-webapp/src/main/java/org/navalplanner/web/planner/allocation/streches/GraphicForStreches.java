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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction.Type;
import org.navalplanner.web.planner.allocation.streches.StretchesFunctionController.IGraphicGenerator;
import org.zkoss.zul.SimpleXYModel;
import org.zkoss.zul.XYModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class GraphicForStreches implements IGraphicGenerator {

    public static IGraphicGenerator forType(Type type) {
        switch (type) {
        case DEFAULT:
            return new ForDefaultStreches();
        case INTERPOLATED:
            return new ForInterpolation();
        default:
            throw new RuntimeException();
        }
    }

    private static class ForDefaultStreches extends GraphicForStreches {

        @Override
        public boolean areChartsEnabled(IStretchesFunctionModel model) {
            return true;
        }

        @Override
        public XYModel getAccumulatedHoursChartData(
                IStretchesFunctionModel stretchesFunctionModel) {
            XYModel xymodel = new SimpleXYModel();

            List<Stretch> stretches = stretchesFunctionModel.getStretches();
            if (stretches.isEmpty()) {
                return xymodel;
            }

            String title = "percentage";

            LocalDate startDate = stretchesFunctionModel.getTaskStartDate();
            xymodel.addValue(title, startDate.toDateTimeAtStartOfDay()
                    .getMillis(), 0);

            BigDecimal taskHours = new BigDecimal(stretchesFunctionModel
                    .getTaskHours());

            for (Stretch stretch : stretches) {
                BigDecimal amountWork = stretch.getAmountWorkPercentage()
                        .multiply(taskHours);

                xymodel.addValue(title, stretch.getDate()
                        .toDateTimeAtStartOfDay().getMillis(), amountWork);
            }

            return xymodel;
        }

        @Override
        public XYModel getDedicationChart(
                IStretchesFunctionModel stretchesFunctionModel) {
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
            return false;
        }

        @Override
        public XYModel getAccumulatedHoursChartData(
                IStretchesFunctionModel stretchesFunctionModel) {
            return new SimpleXYModel();
        }

        @Override
        public XYModel getDedicationChart(
                IStretchesFunctionModel stretchesFunctionModel) {
            return new SimpleXYModel();
        }

    }

}
