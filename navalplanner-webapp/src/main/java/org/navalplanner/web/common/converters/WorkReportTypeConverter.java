package org.navalplanner.web.common.converters;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportTypeConverter implements IConverter<WorkReportType> {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Override
    @Transactional(readOnly = true)
    public WorkReportType asObject(String stringRepresentation) {
        long id = Long.parseLong(stringRepresentation);
        try {
            WorkReportType workReportType = workReportTypeDAO.find(id);
            forceLoadCriterionTypes(workReportType);
            return workReportType;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(WorkReportType entity) {
        return entity.getId().toString();
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((WorkReportType) entity);
    }

    @Override
    public Class<WorkReportType> getType() {
        return WorkReportType.class;
    }

    /**
     * Load @{link CriterionType} and its @{link Criterion}
     *
     * @param workReportType
     */
    private void forceLoadCriterionTypes(WorkReportType workReportType) {
        // Load CriterionType
        for (CriterionType criterionType : workReportType.getCriterionTypes()) {
            criterionType.getId();
            // Load Criterion
            for (Criterion criterion : criterionType.getCriterions()) {
                criterion.getId();
            }
        }
    }

}
