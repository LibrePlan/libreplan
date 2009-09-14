package org.navalplanner.business.test.planner.entities.hibernate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ResourcesPerDayTypeTest {

    @Autowired
    private SessionFactory sessionFactory;

    private EntityContainingResourcePerDay entity;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private void givenEntity(ResourcesPerDay resourcesPerDay) {
        this.entity = new EntityContainingResourcePerDay();
        this.entity.setResourcesPerDay(resourcesPerDay);
    }

    @Test
    public void canBeSavedAndRetrieved() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.7));
        givenEntity(resourcesPerDay);
        getSession().save(entity);
        getSession().flush();
        getSession().evict(entity);
        EntityContainingResourcePerDay reloaded = (EntityContainingResourcePerDay) getSession()
                .get(EntityContainingResourcePerDay.class,
                entity.getId());
        assertNotSame(reloaded.getResourcesPerDay(), resourcesPerDay);
        assertThat(reloaded.getResourcesPerDay(), equalTo(resourcesPerDay));
    }

}
