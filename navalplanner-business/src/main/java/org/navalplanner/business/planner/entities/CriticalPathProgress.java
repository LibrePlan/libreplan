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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.List;

import org.navalplanner.business.common.BaseEntity;

/**
 *
 * @author Diego Pino García<dpino@igalia.com>
 *
 */
public class CriticalPathProgress extends BaseEntity {

    public static CriticalPathProgress create(TaskGroup rootTask) {
        return new CriticalPathProgress(rootTask);
    }

    private TaskGroup rootTask;

    private BigDecimal byDuration;

    private BigDecimal byNumHours;

    protected CriticalPathProgress() {

    }

    public BigDecimal getByDuration() {
        return byDuration;
    }

    public BigDecimal getByNumHours() {
        return byNumHours;
    }

    private CriticalPathProgress(TaskGroup rootTask) {
        this.rootTask = rootTask;
    }

    public void update(List<Task> criticalPath) {
        byDuration = calculateByDuration(criticalPath);
        byNumHours = calculateByNumHours(criticalPath);
    }

    private BigDecimal calculateByDuration(List<Task> criticalPath) {
        Integer totalDuration = new Integer(0), duration;
        BigDecimal totalProgress = BigDecimal.ZERO, progress;

        for (Task each: criticalPath) {
            duration = each.getWorkableDays();
            progress = each.getAdvancePercentage();
            progress = progress.multiply(BigDecimal.valueOf(duration));

            totalDuration = totalDuration + duration;
            totalProgress = totalProgress.add(progress);
        }
        return totalProgress.divide(BigDecimal.valueOf(totalDuration), 8,
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
        return totalProgress.divide(BigDecimal.valueOf(totalNumHours), 8,
                BigDecimal.ROUND_HALF_EVEN);
    }

}
