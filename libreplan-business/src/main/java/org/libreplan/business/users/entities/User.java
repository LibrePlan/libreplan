/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2013 Igalia, S.L.
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

package org.libreplan.business.users.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.AssertTrue;
import org.hibernate.validator.constraints.NotEmpty;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.users.daos.IUserDAO;

/**
 * Entity for modeling a user.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class User extends BaseEntity implements IHumanIdentifiable{

    private String loginName = "";

    private String password = "";

    private Language applicationLanguage = Language.BROWSER_LANGUAGE;

    private Set<UserRole> roles = new HashSet<>();

    private Set<Profile> profiles = new HashSet<>();

    private String email;

    private Boolean disabled = false;

    private Scenario lastConnectedScenario;

    /**
     * If a user is a LibrePlan user or not (LDAP).
     */
    private Boolean librePlanUser = true;

    private boolean expandCompanyPlanningViewCharts = false;

    private boolean expandOrderPlanningViewCharts = true;

    private boolean expandResourceLoadViewCharts = true;

    private String firstName = "";

    private String lastName = "";

    private Worker worker;

    private Label projectsFilterLabel = null;

    private Integer projectsFilterPeriodSince;

    private Integer projectsFilterPeriodTo;

    private Boolean projectsFilterFinishedOn = false;

    private Criterion resourcesLoadFilterCriterion = null;

    private Integer resourcesLoadFilterPeriodSince;

    private Integer resourcesLoadFilterPeriodTo;

    private boolean showResourcesOn = false;

    private boolean showAdvancesOn = false;

    private boolean showReportedHoursOn = false;

    private boolean showLabelsOn = false;

    private boolean showMoneyCostBarOn = false;

    /**
     * Necessary for Hibernate. Please, do not call it.
     */
    public User() {

    }

    private User(String loginName, String password, Set<UserRole> roles, Set<Profile> profiles) {
        this.loginName = loginName;
        this.password = password;
        this.roles = roles;
        this.profiles = profiles;
    }

    private User(String loginName, String password, String email) {
        this.loginName = loginName;
        this.password = password;
        this.email = email;
    }

    public static User create(String loginName, String password, Set<UserRole> roles) {
        return create(loginName, password, roles, new HashSet<>());
    }

    public static User create(String loginName, String password, Set<UserRole> roles, Set<Profile> profiles) {
        return create(new User(loginName, password, roles, profiles));
    }

    public static User create() {
        return create(new User());
    }

    public static User create(String loginName, String password, String email) {
        return create(new User(loginName, password, email));
    }

    @NotEmpty(message = "username not specified")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void clearRoles() {
        roles.clear();
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void addRole(UserRole role) {
        roles.add(role);
    }

    public void removeRole(UserRole role) {
        roles.remove(role);
    }

    /**
     * Retrieves UserRoles from related Profiles and returns them together with
     * the UserRoles related directly to the User entity.
     *
     * @return A list of UserRole objects
     */
    public Set<UserRole> getAllRoles() {
        Set<UserRole> allRoles = new HashSet<>(roles);
        for (Profile profile : getProfiles()) {
            allRoles.addAll(profile.getRoles());
        }

        return allRoles;
    }

    /**
     * Checks if current user is in the requested role.
     */
    public boolean isInRole(UserRole role) {
        if ( roles.contains(role) ) {
            return true;
        }

        for (Profile profile : profiles) {
            if ( profile.getRoles().contains(role) ) {
                return true;
            }
        }

        return false;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void addProfile(Profile profile) {
        if ( !containsProfile(profile) ) {
            profiles.add(profile);
        }
    }

    private boolean containsProfile(Profile profile) {
        for (Profile assignedProfile : profiles) {
            if ( assignedProfile.getId().equals(profile.getId()) ) {
                return true;
            }
        }

        return false;
    }

    public void removeProfile(Profile profile) {
        profiles.remove(profile);
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isSuperuser() {
        return isInRole(UserRole.ROLE_SUPERUSER);
    }

    @AssertTrue(message = "username is already being used by another user")
    public boolean isUniqueLoginNameConstraint() {

        IUserDAO userDAO = Registry.getUserDAO();

        if ( isNewObject() ) {
            return !userDAO.existsByLoginNameAnotherTransaction(loginName);
        } else {
            try {
                return userDAO.findByLoginNameAnotherTransaction(loginName).getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }

        }

    }

    public void setLastConnectedScenario(Scenario lastConnectedScenario) {
        this.lastConnectedScenario = lastConnectedScenario;
    }

    public Scenario getLastConnectedScenario() {
        return lastConnectedScenario;
    }

    public Boolean isLibrePlanUser() {
        return librePlanUser;
    }

    public void setLibrePlanUser(Boolean librePlanUser) {
        this.librePlanUser = librePlanUser;
    }

    public Language getApplicationLanguage() {
        return applicationLanguage;
    }

    public void setApplicationLanguage(Language applicationLanguage) {
        this.applicationLanguage = applicationLanguage;
    }

    public boolean isExpandCompanyPlanningViewCharts() {
        return expandCompanyPlanningViewCharts;
    }

    public void setExpandOrderPlanningViewCharts(boolean expandOrderPlanningViewCharts) {
        this.expandOrderPlanningViewCharts = expandOrderPlanningViewCharts;
    }

    public boolean isExpandOrderPlanningViewCharts() {
        return expandOrderPlanningViewCharts;
    }

    public void setExpandResourceLoadViewCharts(boolean expandResourceLoadViewCharts) {
        this.expandResourceLoadViewCharts = expandResourceLoadViewCharts;
    }

    public boolean isExpandResourceLoadViewCharts() {
        return expandResourceLoadViewCharts;
    }

    public void setExpandCompanyPlanningViewCharts(boolean expandCompanyPlanningViewCharts) {
        this.expandCompanyPlanningViewCharts = expandCompanyPlanningViewCharts;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getHumanId() {
        return loginName;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
        if ( worker == null ) {
            roles.remove(UserRole.ROLE_BOUND_USER);
        } else {
            roles.add(UserRole.ROLE_BOUND_USER);
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isBound() {
        return worker != null;
    }

    public enum UserAuthenticationType {

        DATABASE(_("Database")),
        LDAP(_("LDAP"));

        private String name;

        UserAuthenticationType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public UserAuthenticationType getUserType() {
        return isLibrePlanUser() ? UserAuthenticationType.DATABASE : UserAuthenticationType.LDAP;
    }

    @AssertTrue(message = "You have exceeded the maximum limit of users")
    public boolean isMaxUsersConstraint() {
        return Registry.getTransactionService().runOnAnotherReadOnlyTransaction(() -> {
            Configuration configuration = Registry.getConfigurationDAO().getConfiguration();
            if ( configuration == null ) {
                return true;
            }

            Integer maxUsers = configuration.getMaxUsers();
            if ( maxUsers != null && maxUsers > 0 ) {
                List<User> users = Registry.getUserDAO().findAll();
                int usersNumber = users.size();

                if ( isNewObject() ) {
                    usersNumber++;
                }

                if ( usersNumber > maxUsers ) {
                    return false;
                }
            }
            return true;
        });
    }

    public Label getProjectsFilterLabel() {
        return projectsFilterLabel;
    }

    public void setProjectsFilterLabel(Label label) {
        projectsFilterLabel = label;
    }

    public Criterion getResourcesLoadFilterCriterion() {
        return resourcesLoadFilterCriterion;
    }

    public void setResourcesLoadFilterCriterion(Criterion criterion) {
        resourcesLoadFilterCriterion = criterion;
    }

    public Integer getProjectsFilterPeriodSince() {
        return projectsFilterPeriodSince;
    }

    public void setProjectsFilterPeriodSince(Integer period) {
        projectsFilterPeriodSince = period;
    }

    public Integer getProjectsFilterPeriodTo() {
        return projectsFilterPeriodTo;
    }

    public void setProjectsFilterPeriodTo(Integer period) {
        projectsFilterPeriodTo = period;
    }

    public boolean isProjectsFilterFinishedOn() {
        return projectsFilterFinishedOn;
    }

    public void setProjectsFilterFinishedOn(boolean projectsFilterFinishedOn) {
        this.projectsFilterFinishedOn = projectsFilterFinishedOn;
    }

    public Integer getResourcesLoadFilterPeriodSince() {
        return resourcesLoadFilterPeriodSince;
    }

    public void setResourcesLoadFilterPeriodSince(Integer period) {
        resourcesLoadFilterPeriodSince = period;
    }

    public Integer getResourcesLoadFilterPeriodTo() {
        return resourcesLoadFilterPeriodTo;
    }

    public void setResourcesLoadFilterPeriodTo(Integer period) {
        resourcesLoadFilterPeriodTo = period;
    }

    public boolean isShowResourcesOn() {
        return showResourcesOn;
    }

    public void setShowResourcesOn(boolean showResourcesOn) {
        this.showResourcesOn = showResourcesOn;
    }

    public boolean isShowAdvancesOn() {
        return showAdvancesOn;
    }

    public void setShowAdvancesOn(boolean showAdvancesOn) {
        this.showAdvancesOn = showAdvancesOn;
    }

    public boolean isShowReportedHoursOn() {
        return showReportedHoursOn;
    }

    public void setShowReportedHoursOn(boolean showReportedHoursOn) {
        this.showReportedHoursOn = showReportedHoursOn;
    }

    public boolean isShowLabelsOn() {
        return showLabelsOn;
    }

    public void setShowLabelsOn(boolean showLabelsOn) {
        this.showLabelsOn = showLabelsOn;
    }

    public boolean isShowMoneyCostBarOn() {
        return showMoneyCostBarOn;
    }

    public void setShowMoneyCostBarOn(boolean showMoneyCostBarOn) {
            this.showMoneyCostBarOn = showMoneyCostBarOn;
    }

}
