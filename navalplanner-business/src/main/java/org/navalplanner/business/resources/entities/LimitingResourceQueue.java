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

package org.navalplanner.business.resources.entities;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceQueue extends BaseEntity {

    private Resource resource;

    private SortedSet<LimitingResourceQueueElement> limitingResourceQueueElements =
        new TreeSet<LimitingResourceQueueElement>(new LimitingResourceQueueElementComparator());

    public static LimitingResourceQueue create() {
        return create(new LimitingResourceQueue());
    }

    protected LimitingResourceQueue() {

    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void addLimitingResourceQueueElement(LimitingResourceQueueElement element) {
        element.setLimitingResourceQueue(this);
        limitingResourceQueueElements.add(element);
    }

    public void removeLimitingResourceQueueElement(LimitingResourceQueueElement element) {
        limitingResourceQueueElements.remove(element);
    }

    public SortedSet<LimitingResourceQueueElement> getLimitingResourceQueueElements() {
        return Collections.unmodifiableSortedSet(limitingResourceQueueElements);
    }

}
