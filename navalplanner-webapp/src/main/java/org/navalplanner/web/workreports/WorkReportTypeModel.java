package org.navalplanner.web.workreports;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link WorkReportType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkReportTypeModel implements IWorkReportTypeModel {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    private WorkReportType workReportType;

    private ClassValidator<WorkReportType> workReportTypeValidator = new ClassValidator<WorkReportType>(
            WorkReportType.class);

    private boolean editing = false;

    @Override
    public WorkReportType getWorkReportType() {
        return this.workReportType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeDAO.list(WorkReportType.class);
    }

    @Override
    public void prepareForCreate() {
        editing = false;
        this.workReportType = new WorkReportType();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(WorkReportType workReportType) {
        editing = true;
        Validate.notNull(workReportType);

        this.workReportType = getFromDB(workReportType);
    }

    private WorkReportType getFromDB(WorkReportType workReportType) {
        return getFromDB(workReportType.getId());
    }

    @Transactional(readOnly = true)
    private WorkReportType getFromDB(Long id) {
        try {
            WorkReportType workReportType = workReportTypeDAO.find(id);
            reattachCriterionTypes(workReportType);
            return workReportType;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void reattachCriterionTypes(WorkReportType workReportType) {
        for (CriterionType criterionType : workReportType.getCriterionTypes()) {
            criterionType.getName();
        }
    }

    @Override
    public void prepareForRemove(WorkReportType workReportType) {
        this.workReportType = workReportType;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = workReportTypeValidator
                .getInvalidValues(workReportType);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        workReportTypeDAO.save(workReportType);
    }

    @Override
    @Transactional
    public void remove(WorkReportType workReportType) {
        try {
            workReportTypeDAO.remove(workReportType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CriterionType> getCriterionTypes() {
        return new HashSet<CriterionType>(criterionTypeDAO.getCriterionTypes());
    }

    @Override
    public void setCriterionTypes(Set<CriterionType> criterionTypes) {
        workReportType.setCriterionTypes(criterionTypes);
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

}
