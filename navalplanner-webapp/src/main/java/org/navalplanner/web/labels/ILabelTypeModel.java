package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.labels.entities.LabelType;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ILabelTypeModel {

    List<LabelType> getLabelTypes();

}
