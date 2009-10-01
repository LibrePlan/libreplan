package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 * Implements a {@link IFinder} class for providing {@link Label}
 * elements
 *
 */
@Repository
public class LabelFinder extends Finder implements IFinder {

    @Autowired
    private ILabelDAO labelDAO;

    @Transactional(readOnly = true)
    public List<Label> getAll() {
        return labelDAO.getAll();
    }

    @Override
    public String _toString(Object value) {
        return ((Label) value).getName();
    }

}
