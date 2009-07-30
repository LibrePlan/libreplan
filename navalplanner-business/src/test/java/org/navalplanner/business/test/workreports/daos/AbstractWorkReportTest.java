package org.navalplanner.business.test.workreports.daos;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.navalplanner.business.orders.daos.OrderElementDao;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.daos.impl.CriterionTypeDAO;
import org.navalplanner.business.resources.daos.impl.ResourceDaoHibernate;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.daos.WorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractWorkReportTest {

    @Autowired
    CriterionTypeDAO criterionTypeDAO;

    @Autowired
    WorkReportTypeDAO workReportTypeDAO;

    @Autowired
    ResourceDaoHibernate resourceDAO;

    @Autowired
    OrderElementDao orderElementDAO;

    // Create a Set of CriterionType
    public Set<CriterionType> createValidCriterionTypes() {
        Set<CriterionType> criterionTypes = new HashSet<CriterionType>();

        CriterionType criterionType = new CriterionType(UUID.randomUUID()
                .toString());
        criterionTypeDAO.save(criterionType);
        criterionTypes.add(criterionType);

        return criterionTypes;
    }

    public WorkReportType createValidWorkReportType() {
        Set<CriterionType> criterionTypes = createValidCriterionTypes();
        return new WorkReportType(UUID.randomUUID().toString(), criterionTypes);
    }

    public WorkReportLine createValidWorkReportLine() {
        WorkReportLine workReportLine = new WorkReportLine();
        workReportLine.setNumHours(100);
        workReportLine.setResource(createValidWorker());
        workReportLine.setOrderElement(createValidOrderElement());
        return workReportLine;
    }

    private Resource createValidWorker() {
        Worker worker = new Worker();
        worker.setFirstName(UUID.randomUUID().toString());
        worker.setSurname(UUID.randomUUID().toString());
        worker.setNif(UUID.randomUUID().toString());
        worker.setDailyHours(10);
        resourceDAO.save(worker);
        return worker;
    }

    private OrderElement createValidOrderElement() {
        OrderLine orderLine = new OrderLine();
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
        WorkReport workReport = new WorkReport();

        workReport.setDate(new Date());
        workReport.setPlace(UUID.randomUUID().toString());
        workReport.setResponsible(UUID.randomUUID().toString());

        WorkReportType workReportType = createValidWorkReportType();
        workReportTypeDAO.save(workReportType);
        workReport.setWorkReportType(workReportType);

        workReport.setWorkReportLines(new HashSet<WorkReportLine>());

        return workReport;
    }
}
