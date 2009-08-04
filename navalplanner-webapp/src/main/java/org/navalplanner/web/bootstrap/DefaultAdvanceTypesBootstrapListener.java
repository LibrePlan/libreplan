package org.navalplanner.web.bootstrap;

import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("singleton")
public class DefaultAdvanceTypesBootstrapListener implements IDataBootstrap {

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

    @Transactional
    @Override
    public void loadRequiredData() {
        for (PredefinedAdvancedTypes predefinedType : PredefinedAdvancedTypes.values()) {
            if (!advanceTypeDAO.existsNameAdvanceType(predefinedType.getTypeName())) {
                advanceTypeDAO.save(predefinedType.createType());
            }
        }
    }

}
