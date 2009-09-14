package org.navalplanner.web.labels;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LabelTypeModel implements ILabelTypeModel {

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    private LabelType labelType;

    private ClassValidator<LabelType> validator = new ClassValidator<LabelType>(
            LabelType.class);

    public LabelTypeModel() {

    }

    @Override
    @Transactional(readOnly=true)
    public List<LabelType> getLabelTypes() {
        return labelTypeDAO.getAll();
    }

    @Override
    @Transactional
    public void confirmDelete(LabelType labelType) {
        try {
            labelTypeDAO.remove(labelType.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void prepareForCreate() {
        labelType = LabelType.create("");
    }

    @Override
    public LabelType getLabelType() {
        return labelType;
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        InvalidValue[] invalidValues = validator.getInvalidValues(labelType);

        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        labelTypeDAO.save(labelType);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(LabelType labelType) {
        Validate.notNull(labelType);
        this.labelType = getFromDB(labelType);
    }

    private LabelType getFromDB(LabelType labelType) {
        return getFromDB(labelType.getId());
    }

    private LabelType getFromDB(Long id) {
        try {
            return labelType = labelTypeDAO.find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
