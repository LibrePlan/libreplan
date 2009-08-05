package org.navalplanner.web.advance;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link AdvanceType}.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvanceTypeModel implements IAdvanceTypeModel {

    private AdvanceType advanceType;

    private ClassValidator<AdvanceType> advanceTypeValidator = new ClassValidator<AdvanceType>(
            AdvanceType.class);

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

    @Override
    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvanceType> getAdvanceTypes() {
        return advanceTypeDAO.list(AdvanceType.class);
    }

    @Override
    public void prepareForCreate() {
        this.advanceType = new AdvanceType();
    }

    private AdvanceType getFromDB(AdvanceType advanceType) {
        try {
            return advanceTypeDAO.find(advanceType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForEdit(AdvanceType advanceType) {
        Validate.notNull(advanceType);
        this.advanceType = getFromDB(advanceType);
    }

    @Override
    public void prepareForRemove(AdvanceType advanceType) {
        this.advanceType = advanceType;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = advanceTypeValidator
                .getInvalidValues(advanceType);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
        advanceTypeDAO.save(advanceType);
    }

    @Override
    @Transactional
    public void remove(AdvanceType advanceType) {
        try {
            advanceTypeDAO.remove(advanceType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPrecisionValid(BigDecimal precision) {
        return this.advanceType.isPrecisionValid(precision);
    }

    public boolean isDefaultMaxValueValid(BigDecimal defaultMaxValue) {
        return this.advanceType.isDefaultMaxValueValid(defaultMaxValue);
    }

    @Override
    @Transactional
    public boolean distinctNames(String name) {
        if (name.isEmpty())
            return true;
        List<AdvanceType> listAdvanceType = advanceTypeDAO
                .list(AdvanceType.class);
        for (AdvanceType advanceType : listAdvanceType) {
            if ((advanceType.getId() == null)
                    || (!advanceType.getId().equals(this.advanceType.getId()))
                    && (advanceType.getUnitName().contains(name) || (name
                            .contains(advanceType.getUnitName())))) {
                return false;
            }
        }
        return true;
    }

}
