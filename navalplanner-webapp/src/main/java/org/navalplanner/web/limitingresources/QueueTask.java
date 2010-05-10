/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.limitingresources;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;

public class QueueTask extends Div {

    private static final Log LOG = LogFactory.getLog(QueueTask.class);

    private final LocalDate start;

    private final LocalDate end;

    private final int totalResourceWorkHours;

    private final int assignedHours;

    public QueueTask(LimitingResourceQueueElement element) {
        this(element.getStartDate(), element.getEndDate(), 10, 10);
    }

    public QueueTask(LocalDate start, LocalDate end,
            int totalResourceWorkHours, int assignedHours) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(totalResourceWorkHours);
        Validate.notNull(assignedHours);
        Validate.isTrue(!start.isAfter(end));
        this.start = start;
        this.end = end;
        this.totalResourceWorkHours = totalResourceWorkHours;
        this.assignedHours = assignedHours;
        setAction("onmouseover: zkLimitingDependencies.showDependenciesForQueueElement('"
                + getUuid()
                + "');onmouseout: zkLimitingDependencies.hideDependenciesForQueueElement('"
                + getUuid() + "')");

        final String taskUid = this.getUuid();
        this.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                Clients
                        .evalJavaScript("zkLimitingDependencies.toggleDependenciesForQueueElement('"
                                + taskUid + "')");
            }
        });

    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getTotalResourceWorkHours() {
        return totalResourceWorkHours;
    }

    public int getAssignedHours() {
        return assignedHours;
    }

}
