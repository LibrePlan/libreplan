package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 * Implements a {@link IFinder} class for providing {@link LabelType}
 * elements
 *
 */
@Repository
public class LabelTypeFinder extends Finder implements IFinder {

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Transactional(readOnly = true)
    public List<LabelType> getAll() {
        return labelTypeDAO.getAll();
    }

    @Override
    public String _toString(Object value) {
        return ((LabelType) value).getName();
    }

}
