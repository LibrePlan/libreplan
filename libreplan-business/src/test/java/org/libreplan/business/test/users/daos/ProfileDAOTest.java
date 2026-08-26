/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.test.users.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IProfileDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.ProfileOrderAuthorization;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@ProfileDAO}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class ProfileDAOTest {

    @Autowired
    IProfileDAO profileDAO;

    @Autowired
    IOrderAuthorizationDAO orderAuthorizationDAO;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(profileDAO);
    }

    private Profile createValidProfile() {
        Set<UserRole> roles = new HashSet<UserRole>();
        return Profile.create(UUID.randomUUID().toString(), roles);
    }

    @Test
    @Transactional
    public void testSaveProfile() {
        Profile profile = createValidProfile();
        profileDAO.save(profile);
        assertNotNull(profile.getId());
    }

    @Test
    @Transactional
    public void testRemoveProfile() throws InstanceNotFoundException {
        Profile profile = createValidProfile();
        profileDAO.save(profile);
        profileDAO.remove(profile.getId());
        assertFalse(profileDAO.exists(profile.getId()));
    }

    @Test
    @Transactional
    public void testListProfiles() {
        int previous = profileDAO.list(Profile.class).size();
        Profile profile = createValidProfile();
        profileDAO.save(profile);
        assertEquals(previous + 1, profileDAO.list(Profile.class).size());
    }

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findByProfileName/getOrderAuthorizationsByProfile/listSorted
     * had no test coverage before. Run once here (pre-migration, still on the old Criteria API)
     * to confirm they pass against known-correct behavior, then re-run unchanged after the DAO
     * is rewritten.
     */

    @Test
    @Transactional
    public void testFindByProfileNameIsCaseInsensitive() throws InstanceNotFoundException {
        Profile profile = createValidProfile();
        String mixedCaseName = "MiXeD-" + UUID.randomUUID();
        profile.setProfileName(mixedCaseName);
        profileDAO.save(profile);

        assertEquals(profile.getId(), profileDAO.findByProfileName(mixedCaseName).getId());
        assertEquals(profile.getId(), profileDAO.findByProfileName(mixedCaseName.toLowerCase()).getId());
        assertEquals(profile.getId(), profileDAO.findByProfileName(mixedCaseName.toUpperCase()).getId());
    }

    @Test(expected = InstanceNotFoundException.class)
    @Transactional
    public void testFindByProfileNameThrowsWhenNotFound() throws InstanceNotFoundException {
        profileDAO.findByProfileName("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    @Transactional
    public void testGetOrderAuthorizationsByProfile() {
        Profile profile1 = createValidProfile();
        profileDAO.save(profile1);
        Profile profile2 = createValidProfile();
        profileDAO.save(profile2);

        ProfileOrderAuthorization auth1 = ProfileOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
        auth1.setProfile(profile1);
        orderAuthorizationDAO.save(auth1);

        // Belongs to a different profile - must NOT be returned for profile1
        ProfileOrderAuthorization auth2 = ProfileOrderAuthorization.create(OrderAuthorizationType.READ_AUTHORIZATION);
        auth2.setProfile(profile2);
        orderAuthorizationDAO.save(auth2);

        List<OrderAuthorization> results = profileDAO.getOrderAuthorizationsByProfile(profile1);
        assertEquals(1, results.size());
        assertEquals(auth1.getId(), results.get(0).getId());
    }

    @Test
    @Transactional
    public void testListSortedOrdersByProfileNameAscending() {
        String commonPrefix = UUID.randomUUID().toString();

        Profile first = createValidProfile();
        first.setProfileName(commonPrefix + "-1-aaa");
        profileDAO.save(first);

        Profile second = createValidProfile();
        second.setProfileName(commonPrefix + "-2-bbb");
        profileDAO.save(second);

        Profile third = createValidProfile();
        third.setProfileName(commonPrefix + "-3-ccc");
        profileDAO.save(third);

        // listSorted() returns every profile in the database, not just the 3 created above
        // (other bootstrap/fixture data may exist), so filter down to just the ones we created
        // before asserting on their relative order.
        List<Profile> ourProfilesInReturnedOrder = new ArrayList<>();
        for (Profile p : profileDAO.listSorted()) {
            if (p.getProfileName().startsWith(commonPrefix)) {
                ourProfilesInReturnedOrder.add(p);
            }
        }

        assertEquals(3, ourProfilesInReturnedOrder.size());
        assertEquals(first.getId(), ourProfilesInReturnedOrder.get(0).getId());
        assertEquals(second.getId(), ourProfilesInReturnedOrder.get(1).getId());
        assertEquals(third.getId(), ourProfilesInReturnedOrder.get(2).getId());
    }

}
