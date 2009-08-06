package org.navalplanner.web.workreports;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.daos.IWorkReportDAO;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link WorkReport}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkReportModel implements IWorkReportModel {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    private WorkReport workReport;

    private ClassValidator<WorkReport> workReportValidator = new ClassValidator<WorkReport>(
            WorkReport.class);

    private ClassValidator<WorkReportLine> workReportLineValidator = new ClassValidator<WorkReportLine>(
            WorkReportLine.class);

    private boolean editing = false;

    @Override
    public WorkReport getWorkReport() {
        return workReport;
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate(WorkReportType workReportType) {
        editing = false;
        workReport = new WorkReport();
        workReport.setWorkReportType(workReportType);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(WorkReport workReport) {
        editing = true;
        Validate.notNull(workReport);
        this.workReport = getFromDB(workReport);
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(WorkReport workReport) {
        return getFromDB(workReport.getId());
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(Long id) {
        try {
            WorkReport workReport = workReportDAO.find(id);
            forceLoadEntities(workReport);
            return workReport;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param workReport
     */
    private void forceLoadEntities(WorkReport workReport) {
        // Load WorkReportType
        workReport.getWorkReportType().getName();

        // Load CriterionTypes
        for (CriterionType criterionType : workReport.getWorkReportType()
                .getCriterionTypes()) {
            criterionType.getName();
            // Load Criterions
            for (Criterion criterion : criterionType.getCriterions()) {
                criterion.getName();
            }
        }

        // Load WorkReportLines
        for (WorkReportLine workReportLine : workReport.getWorkReportLines()) {
            workReportLine.getNumHours();
            workReportLine.getResource().getDescription();
            workReportLine.getOrderElement().getName();

            // Load Criterions
            for (Criterion criterion : workReportLine.getCriterions()) {
                criterion.getName();
            }
        }
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = workReportValidator
                .getInvalidValues(workReport);

        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        // Check WorkReportLines
        for (WorkReportLine workReportLine : workReport.getWorkReportLines()) {
            invalidValues = workReportLineValidator
                    .getInvalidValues(workReportLine);

            if (invalidValues.length > 0) {
                throw new ValidationException(invalidValues);
            }
        }
        workReportDAO.save(workReport);
    }

    @Override
    @Transactional
    public OrderElement findOrderElement(String orderCode)
            throws InstanceNotFoundException {
        String[] parts = orderCode.split("-");

        OrderElement parent = orderElementDAO.findUniqueByCodeAndParent(null,
                parts[0]);
        for (int i = 1; i < parts.length && parent != null; i++) {
            OrderElement child = orderElementDAO.findUniqueByCodeAndParent(
                    parent, parts[i]);
            parent = child;
        }

        return parent;
    }

    @Override
    @Transactional
    public Worker findWorker(String nif) throws InstanceNotFoundException {
        return workerDAO.findUniqueByNif(nif);
    }

    @Override
    @Transactional
    public Worker asWorker(Resource resource) throws InstanceNotFoundException {
        return workerDAO.find(resource.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReport> getWorkReports() {
        List<WorkReport> result = new ArrayList<WorkReport>();
        for (WorkReport workReport : workReportDAO.list(WorkReport.class)) {
            workReport.getWorkReportType().getName();
            result.add(workReport);
        }
        return result;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    @Transactional(readOnly = true)
    public String getDistinguishedCode(OrderElement orderElement) throws InstanceNotFoundException {
        return orderElementDAO.getDistinguishedCode(orderElement);
    }

    @Override
    public WorkReportLine addWorkReportLine() {
        WorkReportLine workReportLine = new WorkReportLine();
        workReport.getWorkReportLines().add(workReportLine);
        return workReportLine;
    }

    @Transactional
    public void remove(WorkReport workReport) {
        try {
            workReportDAO.remove(workReport.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
