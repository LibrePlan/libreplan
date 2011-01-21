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

package org.navalplanner.web.limitingresources;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zul.impl.XulElement;

/**
 *
 * @author Francisco Javier Moran Rúa <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingDependencyComponent extends XulElement {

    private QueueTask source;

    private QueueTask destination;

    private DependencyType type;

    public LimitingDependencyComponent(QueueTask source, QueueTask destination,
            DependencyType type) {
        this(source, destination);
        this.type = type;
    }

    public LimitingDependencyComponent(QueueTask source, QueueTask destination) {
        Validate.notNull(source);
        Validate.notNull(destination);
        this.source = source;
        this.destination = destination;
    }

    /**
     * @return the idTaskOrig
     */
    public String getIdTaskOrig() {
        return source.getUuid();
    }

    public void setIdTaskOrig(String idTaskOrig) {
        this.source = findTaskComponent(idTaskOrig);

    }

    private QueueTask findTaskComponent(String idTaskOrig) {
        return (QueueTask) getFellow(idTaskOrig);
    }

    /**
     * @return the idTaskEnd
     */
    public String getIdTaskEnd() {
        return destination.getUuid();
    }

    public void setIdTaskEnd(String idTaskEnd) {
        this.destination = findTaskComponent(idTaskEnd);
    }

    public void zoomChanged() {
        redrawDependency();
    }

    public void redrawDependency() {
        response("zoomChanged", new AuInvoke(this, "draw"));
    }

    public QueueTask getSource() {
        return source;
    }

    public QueueTask getDestination() {
        return destination;
    }

    public DependencyType getDependencyType() {
        return type;
    }

}
