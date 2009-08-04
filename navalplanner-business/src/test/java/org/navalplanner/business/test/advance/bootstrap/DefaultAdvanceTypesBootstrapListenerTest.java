package org.navalplanner.business.test.advance.bootstrap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.bootstrap.DefaultAdvanceTypesBootstrapListener;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class DefaultAdvanceTypesBootstrapListenerTest {

    @Autowired
    private Map<String, IDataBootstrap> dataBootstraps;

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

    private IDataBootstrap getAdvanceTypeBootstrap() {
        String simpleName = DefaultAdvanceTypesBootstrapListener.class
                .getSimpleName();
        return dataBootstraps.get(simpleName.substring(0, 1).toLowerCase()
                + simpleName.substring(1));
    }

    @Test
    public void theBootstrapensuresExistenceOfPredefinedAdvanceTypes() {
        getAdvanceTypeBootstrap().loadRequiredData();
        for (PredefinedAdvancedTypes p : PredefinedAdvancedTypes.values()) {
            advanceTypeDAO.existsNameAdvanceType(p.getTypeName());
        }
    }

    @Test
    public void getAdvanceTypeFromEnum() {
        getAdvanceTypeBootstrap().loadRequiredData();
        for (PredefinedAdvancedTypes p : PredefinedAdvancedTypes.values()) {
            AdvanceType advanceType = p.getType();
            assertThat(advanceType.getUnitName(), equalTo(p.getTypeName()));
        }
    }

}
