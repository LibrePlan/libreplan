package org.navalplanner.web.labels;

import static org.navalplanner.web.I18nHelper._;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
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

    @Autowired
    private ILabelDAO labelDAO;

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
    public void initCreate() {
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

        if (labelTypeDAO.existsByName(labelType)) {
            InvalidValue[] _invalidValues = { new InvalidValue(_(
                    "{0} already exists", labelType.getName()),
                    LabelType.class, "name", labelType.getName(), labelType) };
            throw new ValidationException(_invalidValues);
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
            LabelType labelType = labelTypeDAO.find(id);
            reattachLabels(labelType);
            return labelType;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void reattachLabels(LabelType labelType) {
        for (Label label : labelType.getLabels()) {
            label.getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Label> getLabels() {
        return (labelType != null) ? labelType.getLabels()
                : new HashSet<Label>();
    }

    @Override
    public void addLabel() {
        Label label = Label.create("");
        label.setType(labelType);
        labelType.addLabel(label);
    }

}
