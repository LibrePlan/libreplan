/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.workingday.hibernate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.business.workingday.EffortDuration.hours;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class EffortDurationTypeTest {

    @Autowired
    private SessionFactory sessionFactory;

    private EntityContainingEffortDuration entity;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private void givenEntity(EffortDuration duration) {
        this.entity = new EntityContainingEffortDuration();
        this.entity.setDuration(duration);
    }

    @Test
    public void canBeSavedAndRetrieved() {
        EffortDuration duration = hours(2).and(30, Granularity.MINUTES);
        givenEntity(duration);
        getSession().save(entity);
        getSession().flush();
        getSession().evict(entity);
        EntityContainingEffortDuration reloaded = (EntityContainingEffortDuration) getSession()
                .get(EntityContainingEffortDuration.class, entity.getId());
        assertNotSame(reloaded.getDuration(), duration);
        assertThat(reloaded.getDuration(), equalTo(duration));
    }

}
