package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.LabelType;

/**
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
     *
     */
    void initCreate();

    /**
     *
     */
    void confirmSave() throws ValidationException;

    /**
     *
     */
    void initEdit(LabelType labelType);

}
