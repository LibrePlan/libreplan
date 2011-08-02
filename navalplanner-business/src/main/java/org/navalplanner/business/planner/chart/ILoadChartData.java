package org.navalplanner.business.planner.chart;

import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ILoadChartData {

    SortedMap<LocalDate, EffortDuration> getLoad();

    SortedMap<LocalDate, EffortDuration> getAvailability();

    SortedMap<LocalDate, EffortDuration> getOverload();
}