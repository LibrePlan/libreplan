/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.users.bootstrap;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.libreplan.business.common.Configuration;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.users.bootstrap.PredefinedProfiles;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.UserRole;

/**
 * It enumerates the default users (usernames) for the application.<br />
 *
 * {@link PredefinedUsers#ADMIN} user will be always enabled, however
 * {@link PredefinedUsers#WSREADER}, {@link PredefinedUsers#WSWRITER},
 * {@link PredefinedUsers#WSSUBCONTRACTING}, {@link PredefinedUsers#MANAGER},
 * {@link PredefinedUsers#HRESOURCES}, {@link PredefinedUsers#OUTSOURCING} and
 * {@link PredefinedUsers#REPORTS} users could be disabled in compilation time
 * with a Maven option specified via {@link Configuration} class.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum PredefinedUsers {

    ADMIN(Arrays.asList(UserRole.ROLE_SUPERUSER,
        UserRole.ROLE_READ_ALL_PROJECTS,
        UserRole.ROLE_EDIT_ALL_PROJECTS,
            UserRole.ROLE_CREATE_PROJECTS), false) {

        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultAdminPassword();
        }
    },
    WSREADER(Arrays.asList(UserRole.ROLE_WS_READER), Configuration
            .isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultWsreaderPassword();
        }
    },
    WSWRITER(Arrays.asList(UserRole.ROLE_WS_READER, UserRole.ROLE_WS_WRITER),
            Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultWswriterPassword();
        }
    },
    WSSUBCONTRACTING(Arrays.asList(UserRole.ROLE_WS_SUBCONTRACTING),
            Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration()
                    .getChangedDefaultWssubcontractingPassword();
        }
    },
    MANAGER(null,
            Arrays.asList(PredefinedProfiles.PROJECT_MANAGER.getFromDB()),
            Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration()
                    .getChangedDefaultManagerPassword();
        }
    },
    HRESOURCES(null, Arrays
            .asList(PredefinedProfiles.HUMAN_RESOURCES_AND_COSTS_MANAGER
                    .getFromDB()), Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultManagerPassword();
        }
    },
    OUTSOURCING(null, Arrays.asList(PredefinedProfiles.OUTSOURCING_MANAGER
            .getFromDB()), Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultManagerPassword();
        }
    },
    REPORTS(null, Arrays.asList(PredefinedProfiles.REPORTS_RESPONSIBLE
            .getFromDB()), Configuration.isExampleUsersDisabled()) {
        @Override
        public boolean hasChangedDefaultPassword() {
            return getConfiguration().getChangedDefaultManagerPassword();
        }
    };

    public static boolean adminChangedAndSomeOtherNotChanged() {
        return ADMIN.hasChangedDefaultPasswordOrDisabled()
                && someKeepsDefaultPassword(allExcept(ADMIN));
    }

    public static boolean someKeepsDefaultPassword(
            Collection<PredefinedUsers> mandatoryUsers) {
        for (PredefinedUsers each : mandatoryUsers) {
            if (!each.hasChangedDefaultPasswordOrDisabled()) {
                return true;
            }
        }
        return false;
    }

    private static org.libreplan.business.common.entities.Configuration getConfiguration() {
        return Registry.getConfigurationDAO()
                .getConfigurationWithReadOnlyTransaction();
    }

    private Set<UserRole> initialRoles = new HashSet<UserRole>();

    private Set<Profile> initialProfiles = new HashSet<Profile>();

    private final boolean userDisabled;

    private PredefinedUsers(Collection<UserRole> initialUserRoles,
            boolean userDisabled) {
        this(initialUserRoles, null, userDisabled);
    }

    private PredefinedUsers(Collection<UserRole> initialUserRoles,
            Collection<Profile> initialProfiles, boolean userDisabled) {
        if (initialUserRoles != null) {
            this.initialRoles = new HashSet<UserRole>(initialUserRoles);
        }
        if (initialProfiles != null) {
            this.initialProfiles = new HashSet<Profile>(initialProfiles);
        }
        this.userDisabled = userDisabled;
    }

    public boolean isUserDisabled() {
        return userDisabled;
    }

    public boolean hasChangedDefaultPasswordOrDisabled() {
        return isDisabled() || hasChangedDefaultPassword();
    }

    protected abstract boolean hasChangedDefaultPassword();

    public String getLoginName() {
        return this.name().toLowerCase();
    }

    public String getClearPassword() {
        return getLoginName();
    }

    public Set<UserRole> getInitialRoles() {
        return initialRoles;
    }

    public Set<Profile> getInitialProfiles() {
        return initialProfiles;
    }

    public static EnumSet<PredefinedUsers> allExcept(PredefinedUsers mandatoryUser) {
        return EnumSet.complementOf(EnumSet.of(mandatoryUser));
    }

    public boolean isDisabled() {
        try {
            return Registry.getUserDAO().findByLoginName(getLoginName())
                    .isDisabled();
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

}
