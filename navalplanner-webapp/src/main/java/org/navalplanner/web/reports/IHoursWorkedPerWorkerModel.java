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

package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.reports.dtos.LabelFilterType;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface IHoursWorkedPerWorkerModel {

    JRDataSource getHoursWorkedPerWorkerReport(List<Resource> resources,
            List<Label> labels, LabelFilterType labelFilterType,
            List<Criterion> criterions,
            Date startingDate, Date endingDate);

    void init();

    Set<Resource> getResources();

    List<Label> getSelectedLabels();

    void removeSelectedResource(Resource resource);

    boolean addSelectedResource(Resource resource);

    void removeSelectedLabel(Label label);

    boolean addSelectedLabel(Label label);

    boolean isShowReportMessage();

    List<Label> getAllLabels();

    List<Criterion> getSelectedCriterions();

    void removeSelectedCriterion(Criterion criterion);

    boolean addSelectedCriterion(Criterion criterion);

    List<Criterion> getCriterions();
}
