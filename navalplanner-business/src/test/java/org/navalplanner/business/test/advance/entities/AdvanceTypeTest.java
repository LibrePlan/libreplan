package org.navalplanner.business.test.advance.entities;

import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class AdvanceTypeTest {

    @Autowired
    private IAdvanceTypeDAO dao;

    @Test(expected = DataIntegrityViolationException.class)
    public void typeNameMustBeUniqueInDB() {
        String repeatedName = "bla";
        AdvanceType advanceType = AdvanceType.create(repeatedName, new BigDecimal(
                5), false, new BigDecimal(1), true, false);
        AdvanceType other = AdvanceType.create(repeatedName, new BigDecimal(4),
                false, new BigDecimal(2), true, false);
        dao.save(advanceType);
        dao.save(other);
        dao.flush();
    }
}
