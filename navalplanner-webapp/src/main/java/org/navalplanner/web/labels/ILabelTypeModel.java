package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.labels.entities.LabelType;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelTypeModel {

    /**
     * Returns all {@link LabelType}
     *
     * @return
     */
    List<LabelType> getLabelTypes();

    /**
     * Removes {@link LabelType}
     *
     * @param labelType
     */
    void confirmDelete(LabelType labelType);

}
