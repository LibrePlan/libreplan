package org.navalplanner.web.bootstrap;

import java.math.BigDecimal;

import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceType;
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

        if (!advanceTypeDAO.existsNameAdvanceType("porcentaxe")) {
            BigDecimal defaultMaxValue = new BigDecimal(100).setScale(2,
                    BigDecimal.ROUND_HALF_UP);
            BigDecimal precision = new BigDecimal(0.01).setScale(4,
                    BigDecimal.ROUND_HALF_UP);
            AdvanceType advanceTypePorcentaxe = new AdvanceType("porcentaxe",
                    defaultMaxValue, false, precision, true);
            advanceTypeDAO.save(advanceTypePorcentaxe);
        }
        if (!advanceTypeDAO.existsNameAdvanceType("unidades")) {
            BigDecimal defaultMaxValue = new BigDecimal(Integer.MAX_VALUE)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal precision = new BigDecimal(1).setScale(4,
                    BigDecimal.ROUND_HALF_UP);
            AdvanceType advanceTypeUnidades = new AdvanceType("unidades",
                    defaultMaxValue, false, precision, true);
            advanceTypeDAO.save(advanceTypeUnidades);
        }
    }

}
