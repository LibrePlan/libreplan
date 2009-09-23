package org.navalplanner.web.labels;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private ClassValidator<LabelType> validatorLabelType = new ClassValidator<LabelType>(
            LabelType.class);

    private ClassValidator<Label> validatorLabel = new ClassValidator<Label>(
            Label.class);

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
        ArrayList<InvalidValue> invalidValues = new ArrayList<InvalidValue>();

        // Check properties
        invalidValues.addAll(checkLabelTypeProperties());
        invalidValues.addAll(checkLabelsProperties());
        if (invalidValues.size() > 0) {
            throw new ValidationException(invalidValues
                    .toArray(new InvalidValue[0]));
        }

        // Check elements are unique
        invalidValues.addAll(checkLabelTypeUnique());
        invalidValues.addAll(checkLabelsUnique());
        if (invalidValues.size() > 0) {
            throw new ValidationException(invalidValues
                    .toArray(new InvalidValue[0]));
        }

        labelTypeDAO.save(labelType);
    }

    /**
     * Check errors in {@link LabelType}
     *
     * @return
     */
    private List<InvalidValue> checkLabelTypeProperties() {
        List<InvalidValue> result = new ArrayList<InvalidValue>();
        result.addAll(Arrays.asList(validatorLabelType
                .getInvalidValues(labelType)));
        return result;
    }

    /**
     * Check errors in {@link Label}
     *
     * @return
     */
    private List<InvalidValue> checkLabelsProperties() {
        List<InvalidValue> result = new ArrayList<InvalidValue>();
        for (Label label : labelType.getLabels()) {
            result.addAll(Arrays.asList(validatorLabel
                            .getInvalidValues(label)));
        }
        return result;
    }

    /**
     * Check {@link LabelType} name is unique
     *
     * @return
     */
    private List<InvalidValue> checkLabelTypeUnique() {
        List<InvalidValue> result = new ArrayList<InvalidValue>();
        if (!labelTypeDAO.isUnique(labelType)) {
            result.add(createInvalidValue(labelType));
        }
        return result;
    }

    private InvalidValue createInvalidValue(LabelType labelType) {
        return new InvalidValue(_(
                "{0} already exists", labelType.getName()),
                LabelType.class, "name", labelType.getName(), labelType);
    }

    /**
     * Check {@link Label} name is unique
     *
     * @return
     * @throws ValidationException
     */
    private List<InvalidValue> checkLabelsUnique() throws ValidationException {
        List<InvalidValue> result = new ArrayList<InvalidValue>();

        List<Label> labels = new ArrayList<Label>(labelType.getLabels());
        for (int i = 0; i < labels.size(); i++) {
            for (int j = i + 1; j < labels.size(); j++) {
                if (labels.get(j).getName().equals(labels.get(i).getName())) {
                    result.add(createInvalidValue(labels.get(j)));
                }
            }
        }
        return result;
    }

    private InvalidValue createInvalidValue(Label label) {
        return new InvalidValue(_(
                "{0} already exists", label.getName()),
                LabelType.class, "name", label.getName(), label);
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
    public List<Label> getLabels() {
        // Safe copy
        List<Label> labels = new ArrayList<Label>();
        if (labelType != null) {
            labels.addAll(labelType.getLabels());
        }
        return labels;
    }

    @Override
    public void addLabel() {
        Label label = Label.create("");
        label.setType(labelType);
        labelType.addLabel(label);
    }

    @Override
    public void confirmDeleteLabel(Label label) {
        labelType.removeLabel(label);
    }

    @Override
    public boolean labelNameIsUnique(String name) {
        int count = 0;

        for (Label label : labelType.getLabels()) {
            if (name.equals(label.getName())) {
                count++;
            }
        }
        return (count == 1);
    }

}
