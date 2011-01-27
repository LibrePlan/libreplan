/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.users;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.navalplanner.web.users.bootstrap.MandatoryUser;
import org.navalplanner.web.users.services.IDBPasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.util.Clients;

/**
 * Model for UI operations related to {@link User}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/users/users.zul")
public class UserModel implements IUserModel {

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IDBPasswordEncoderService dbPasswordEncoderService;

    private User user;

    private String clearNewPassword;

    @Override
    @Transactional(readOnly=true)
    public List<User> getUsers() {
        return userDAO.list(User.class);
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        try {
            //user.getLoginName() has to be validated before encoding password,
            //because it must exist to perform the encoding
            Validate.notEmpty(user.getLoginName());

            if (getClearNewPassword() != null) {

                /**
                 * it ckecks if the admin password has changed and if so sets
                 * true in the field changedDefaultAdminPassword.
                 */
                if (user.getLoginName().equalsIgnoreCase(
                        MandatoryUser.ADMIN.getLoginName())) {
                    checkIfChangeDefaultAdminPasswd();
                }

                user.setPassword(dbPasswordEncoderService.
                        encodePassword(getClearNewPassword(), user.getLoginName()));
            }
        }
        catch (IllegalArgumentException e) {}

        user.validate();
        userDAO.save(user);
    }

    private void checkIfChangeDefaultAdminPasswd() {
        boolean changedPasswd = true;
        if (getClearNewPassword().isEmpty()
                || getClearNewPassword().equals(MandatoryUser.ADMIN
                        .getClearPassword())) {
            changedPasswd = false;
        }
        // save the field changedDefaultAdminPassword in configuration.
        Registry.getConfigurationDAO().saveChangedDefaultAdminPassword(
                changedPasswd);

        // show or hide the warning
        if (changedPasswd) {
            Clients.evalJavaScript("hideWarningDefaultPasswd();");
        } else {
            Clients.evalJavaScript("showWarningDefaultPasswd();");
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void initCreate() {
        this.user = User.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(User user) {
        Validate.notNull(user);
        this.user = getFromDB(user);
    }

    @Transactional(readOnly = true)
    private User getFromDB(User user) {
        return getFromDB(user.getId());
    }

    @Transactional(readOnly = true)
    private User getFromDB(Long id) {
        try {
            User result = userDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param costCategory
     */
    private void forceLoadEntities(User user) {
        user.getLoginName();
        for (UserRole each : user.getRoles()) {
            each.name();
        }
        for (Profile each : user.getProfiles()) {
            each.getProfileName();
        }
    }

    @Override
    public List<UserRole> getRoles() {
        List<UserRole> list = new ArrayList<UserRole>();
        if (user != null) {
            list.addAll(user.getRoles());
        }
        return list;
    }

    @Override
    public void removeRole(UserRole role) {
        user.removeRole(role);
    }

    @Override
    public void addRole(UserRole role) {
        user.addRole(role);
    }

    @Override
    public List<Profile> getProfiles() {
        List<Profile> list = new ArrayList<Profile>();
        if (user != null) {
            list.addAll(user.getProfiles());
        }
        return list;
    }

    @Override
    public void removeProfile(Profile profile) {
        user.removeProfile(profile);
    }

    @Override
    public void addProfile(Profile profile) {
        user.addProfile(profile);
    }

    @Override
    public void setPassword(String password) {
        //password is not encrypted right away, because
        //user.getLoginName must exist to do that, and we're
        //not sure at this point
        if(password != "") {
            setClearNewPassword(password);
        }
        else{
            setClearNewPassword(null);
        }
    }

    public void setClearNewPassword(String clearNewPassword) {
        this.clearNewPassword = clearNewPassword;
    }

    @Override
    public String getClearNewPassword() {
        return clearNewPassword;
    }
}
