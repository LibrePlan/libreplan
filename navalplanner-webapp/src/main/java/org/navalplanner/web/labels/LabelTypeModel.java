package org.navalplanner.web.labels;

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
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
    ILabelTypeDAO labelTypeDAO;

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
}
