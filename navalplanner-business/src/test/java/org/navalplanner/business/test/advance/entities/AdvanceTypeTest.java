package org.navalplanner.business.test.advance.entities;

import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class AdvanceTypeTest {

    @Autowired
    private IAdvanceTypeDAO dao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test(expected = ConstraintViolationException.class)
    public void typeNameMustBeUniqueInDB() {
        String repeatedName = "bla";
        AdvanceType advanceType = new AdvanceType(repeatedName, new BigDecimal(
                5), false, new BigDecimal(1), true);
        AdvanceType other = new AdvanceType(repeatedName, new BigDecimal(4),
                false, new BigDecimal(2), true);
        dao.save(advanceType);
        dao.save(other);
        sessionFactory.getCurrentSession().flush();
    }
}
