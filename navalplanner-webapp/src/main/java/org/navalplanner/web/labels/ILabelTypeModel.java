package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * Interface for {@link LabelTypeModel}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelTypeModel {

    /**
     * Removes {@link LabelType}
     *
     * @param labelType
     */
    void confirmDelete(LabelType labelType);

    /**
     * Ends conversation saving current {@link LabelType}
     */
    void confirmSave() throws ValidationException;

    /**
     * Returns {@link LabelType}
     *
     * @return
     */
    LabelType getLabelType();

    /**
     * Returns all {@link LabelType}
     *
     * @return
     */
    List<LabelType> getLabelTypes();

    /**
     * Starts conversation creating new {@link LabelType}
     */
    void initCreate();

    /**
     * Starts conversation editing {@link LabelType}
     */
    void initEdit(LabelType labelType);

    /**
     * Returns all {@link Label} for current {@link LabelType}
     *
     * @return
     */
    List<Label> getLabels();

    /**
     * Add {@link Label} to {@link LabelType}
     */
    void addLabel();

    /**
     *
     * @param label
     */
    void confirmDeleteLabel(Label label);

    /**
     * Check is {@link Label} name is unique
     *
     * @param value
     */
    boolean labelNameIsUnique(String value);

}
