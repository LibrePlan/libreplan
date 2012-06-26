/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.dashboard;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.util.Clients;

/**
*
* @author Diego Pino García <dpino@igalia.com>
*
*/
public class GlobalProgressChart {

    public static final String SPREAD_PROGRESS = _("Spreading progress");

    public static final String ALL_TASKS_HOURS = _("By all tasks hours");

    public static final String CRITICAL_PATH_HOURS = _("By critical path hours");

    public static final String CRITICAL_PATH_DURATION = _("By critical path duration");

    private final Map<String, BigDecimal> current = new LinkedHashMap<String, BigDecimal>();

    private final Map<String, BigDecimal> expected = new LinkedHashMap<String, BigDecimal>();

    private static List<Series> series = new ArrayList<Series>() {
        {
            add(Series.create(_("Current"), "#004469"));
            add(Series.create(_("Expected"), "#3C90BE"));
        }
    };

    private GlobalProgressChart() {

    }

    public void current(String key, BigDecimal value) {
        current.put(key, value);
    }

    public void expected(String key, BigDecimal value) {
        expected.put(key, value);
    }

    public static GlobalProgressChart create() {
        return new GlobalProgressChart();
    }

    public String getPercentages() {
        return String.format("'[%s, %s]'",
                jsonifyPercentages(current.values()),
                jsonifyPercentages(expected.values()));
    }

    private String jsonifyPercentages(Collection<BigDecimal> array) {
        List<String> result = new ArrayList<String>();

        int i = 1;
        for (BigDecimal each : array) {
            result.add(String.format(Locale.ROOT, "[%.2f, %d]", each.doubleValue(), i++));
        }
        return String.format("[%s]", StringUtils.join(result, ","));
    }

    private String jsonify(Collection<?> list) {
        Collection<String> result = new ArrayList<String>();
        for (Object each : list) {
            if (each.getClass() == String.class) {
                result.add(String.format("\"%s\"", each.toString()));
            } else {
                result.add(String.format("%s", each.toString()));
            }
        }
        return String.format("'[%s]'", StringUtils.join(result, ','));
    }

    public String getSeries() {
        return jsonify(series);
    }

    /**
     * The order of the ticks is taken from the keys in current
     *
     * @return
     */
    public String getTicks() {
        return jsonify(current.keySet());
    }

    public void render() {
        String command = String.format(
                "global_progress.render(%s, %s, %s);", getPercentages(),
                getTicks(), getSeries());
        Clients.evalJavaScript(command);
    }


    /**
    *
    * @author Diego Pino García <dpino@igalia.com>
    *
    */
   static class Series {

       private String label;

       private String color;

       private Series() {

       }

       public static Series create(String label) {
           Series series = new Series();
           series.label = label;
           return series;
       }

       public static Series create(String label, String color) {
           Series series = new Series();
           series.label = label;
           series.color = color;
           return series;
       }

       @Override
       public String toString() {
           return String.format("{\"label\": \"%s\", \"color\": \"%s\"}",
                   label, color);
       }

   }

}
