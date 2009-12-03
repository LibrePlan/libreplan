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

package org.navalplanner.business.test.workreports.daos;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.business.workreports.valueobjects.DescriptionField;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractWorkReportTest {

    @Autowired
    IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    IResourceDAO resourceDAO;

    @Autowired
    IOrderElementDAO orderElementDAO;

    @Autowired
    ILabelDAO labelDAO;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    public WorkReportType createValidWorkReportType() {
        return WorkReportType.create(UUID.randomUUID().toString(), UUID
                .randomUUID().toString());
    }

    public WorkReportLine createValidWorkReportLine() {
        WorkReportLine workReportLine = WorkReportLine.create();
        workReportLine.setNumHours(100);
        workReportLine.setResource(createValidWorker());
        workReportLine.setOrderElement(createValidOrderElement());
        return workReportLine;
    }

    private Resource createValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName(UUID.randomUUID().toString());
        worker.setSurname(UUID.randomUUID().toString());
        worker.setNif(UUID.randomUUID().toString());
        resourceDAO.save(worker);
        return worker;
    }

    private OrderElement createValidOrderElement() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName(UUID.randomUUID().toString());
        orderLine.setCode(UUID.randomUUID().toString());
        orderElementDAO.save(orderLine);
        return orderLine;
    }

    public Set<WorkReportLine> createValidWorkReportLines() {
        Set<WorkReportLine> workReportLines = new HashSet<WorkReportLine>();

        WorkReportLine workReportLine = createValidWorkReportLine();
        workReportLines.add(workReportLine);

        return workReportLines;
    }

    public WorkReport createValidWorkReport() {
        WorkReport workReport = WorkReport.create();

        workReport.setDate(new Date());
        workReport.setPlace(UUID.randomUUID().toString());
        workReport.setResponsible(UUID.randomUUID().toString());

        WorkReportType workReportType = createValidWorkReportType();
        workReportTypeDAO.save(workReportType);
        workReport.setWorkReportType(workReportType);

        return workReport;
    }

    public DescriptionField createValidDescriptionField() {
        return DescriptionField.create(UUID.randomUUID().toString(), 1);
    }

    public WorkReportLabelTypeAssigment createValidWorkReportLabelTypeAssigment() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        Label label = Label.create(UUID.randomUUID().toString());
        label.setType(labelType);
        labelDAO.save(label);

        WorkReportLabelTypeAssigment labelAssigment = WorkReportLabelTypeAssigment
                .create();
        labelAssigment.setDefaultLabel(label);
        labelAssigment.setLabelType(labelType);
        return labelAssigment;
    }

}
