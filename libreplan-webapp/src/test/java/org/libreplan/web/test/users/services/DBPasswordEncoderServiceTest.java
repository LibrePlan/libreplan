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

package org.libreplan.web.test.users.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.entities.IConfigurationBootstrap;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.bootstrap.IProfileBootstrap;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.users.bootstrap.IUsersBootstrapInDB;
import org.libreplan.web.users.bootstrap.PredefinedUsers;
import org.libreplan.web.users.services.IDBPasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>DBPasswordEncoderService</code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,

        WEBAPP_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_TEST_FILE,

        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
public class DBPasswordEncoderServiceTest {

    @Autowired
    private IDBPasswordEncoderService dbPasswordEncoderService;

    @Autowired
    private IUsersBootstrapInDB usersBootstrap;

    @Autowired
    private IProfileBootstrap profileBootstrap;

    @Autowired
    private IConfigurationBootstrap configurationBootstrap;

    @Autowired
    private IUserDAO userDAO;

    @Test
    @Transactional
    public void testEncodePassword() throws InstanceNotFoundException {

        configurationBootstrap.loadRequiredData();
        profileBootstrap.loadRequiredData();
        usersBootstrap.loadRequiredData();

        for (PredefinedUsers u : PredefinedUsers.values()) {

            // Not asserting encodePassword(...).equals(user.getPassword()): the current
            // scheme (BCrypt) embeds a fresh random salt on every call, so encoding the same
            // clear password twice never produces the same string twice. matches() is the
            // right check - see jdk25-migration-baseline/CHANGES-and-WHY.md.
            User user = userDAO.findByLoginName(u.getLoginName());

            assertTrue(dbPasswordEncoderService.matches(
                u.getClearPassword(), u.getLoginName(), user.getPassword()));

        }

    }

    /**
     * Independently computed offline (Python's hashlib, not this codebase) as
     * sha512("secret123{testuser}").hexdigest() - i.e. reproducing what a real password
     * stored before the migration to BCrypt would look like for clear password "secret123"
     * and login name "testuser". See jdk25-migration-baseline/CHANGES-and-WHY.md for how the
     * legacy "password{loginName}" merge format and SHA-512/hex encoding were verified
     * byte-for-byte against the actual pre-migration Spring Security source.
     */
    private static final String LEGACY_HASH_OF_secret123_FOR_testuser =
        "8bd62d877242d1ac746b2f9a7d897aac2033694ab486a7fe419ee50bc49eae02f941fc77b7e731659fa03d70064b8df890f7806e3c5751757401267dc710f287";

    @Test
    public void testMatchesAcceptsLegacyHash() {
        assertTrue(dbPasswordEncoderService.matches(
            "secret123", "testuser", LEGACY_HASH_OF_secret123_FOR_testuser));

        assertFalse(dbPasswordEncoderService.matches(
            "wrongpassword", "testuser", LEGACY_HASH_OF_secret123_FOR_testuser));

        // The salt is derived from the login name, so the same clear password verified
        // under a different login name must NOT match.
        assertFalse(dbPasswordEncoderService.matches(
            "secret123", "otheruser", LEGACY_HASH_OF_secret123_FOR_testuser));
    }

    @Test
    public void testNeedsRehashDistinguishesLegacyFromCurrentHashes() {
        assertTrue(dbPasswordEncoderService.needsRehash(LEGACY_HASH_OF_secret123_FOR_testuser));

        String currentHash = dbPasswordEncoderService.encodePassword("secret123", "testuser");
        assertFalse(dbPasswordEncoderService.needsRehash(currentHash));
    }

}
