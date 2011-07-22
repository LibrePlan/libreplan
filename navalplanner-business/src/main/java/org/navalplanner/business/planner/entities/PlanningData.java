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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.BaseEntity;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         This is a class, directly associated with a TaskGroup with no parent
 *         (TaskRoot element), which is used to store data about the whole
 *         scheduling
 *
 */
public class PlanningData extends BaseEntity {

    private static Log LOG = LogFactory.getLog(PlanningData.class);

    public static PlanningData create(TaskGroup rootTask) {
        return new PlanningData(rootTask);
    }

    private TaskGroup rootTask;

    private BigDecimal progressAllByNumHours;

    private BigDecimal progressByDuration;

    private BigDecimal progressByNumHours;

    public PlanningData() {

    }

    public BigDecimal getProgressAllByNumHours() {
        return progressAllByNumHours;
    }

    public BigDecimal getProgressByDuration() {
        return progressByDuration;
    }

    public BigDecimal getProgressByNumHours() {
        return progressByNumHours;
    }

    private PlanningData(TaskGroup rootTask) {
        this.rootTask = rootTask;
    }

    public void update(List<Task> criticalPath) {
        if (criticalPath.isEmpty()) {
            LOG.warn("it can't be updated because the critical path provided is empty");
            return;
        }
        progressAllByNumHours = rootTask.getOrderElement()
                .getAdvancePercentageChildren();
        progressByDuration = calculateByDuration(criticalPath);
        progressByNumHours = calculateByNumHours(criticalPath);
    }

    private BigDecimal calculateByDuration(List<Task> criticalPath) {
        int totalDuration = 0;
        BigDecimal totalProgress = BigDecimal.ZERO;

        for (Task each : criticalPath) {
            int duration = each.getWorkableDays();
            BigDecimal progress = each.getAdvancePercentage().multiply(
                    BigDecimal.valueOf(duration));

            totalDuration = totalDuration + duration;
            totalProgress = totalProgress.add(progress);
        }
        return divide(totalProgress, totalDuration);
    }

    /**
     * Prevents division by zero
     *
     * @param numerator
     * @param denominator
     * @return
     */
    private BigDecimal divide(BigDecimal numerator, int denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(BigDecimal.valueOf(denominator), 8,
                BigDecimal.ROUND_HALF_EVEN);
    }

    private BigDecimal calculateByNumHours(List<Task> criticalPath) {
        int totalNumHours = 0, numHours;
        BigDecimal totalProgress = BigDecimal.ZERO, progress;

        for (Task each: criticalPath) {
            numHours = each.getAssignedHours();
            if (numHours == 0) {
                numHours = each.getTotalHours();
            }
            progress = each.getAdvancePercentage();
            progress = progress.multiply(BigDecimal.valueOf(numHours));

            totalNumHours += numHours;
            totalProgress = totalProgress.add(progress);
        }
        return divide(totalProgress, totalNumHours);
    }

}
