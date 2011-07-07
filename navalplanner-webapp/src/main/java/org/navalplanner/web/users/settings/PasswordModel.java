/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
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

package org.navalplanner.web.users.settings;

import org.navalplanner.business.common.Configuration;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.navalplanner.web.security.SecurityUtils;
import org.navalplanner.web.users.PasswordUtil;
import org.navalplanner.web.users.services.IDBPasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to user password
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/settings/changePassword.zul")
public class PasswordModel extends PasswordUtil implements IPasswordModel{

    @Autowired
    private IUserDAO userDAO;

    private User user;

    @Autowired
    private IDBPasswordEncoderService dbPasswordEncoderService;

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        try {
            if (getClearNewPassword() != null) {

                /*
                 * it ckecks if the user password who have admin role has
                 * changed and if so sets true in the field
                 * changedDefaultAdminPassword.
                 */
                if (Configuration.isDefaultPasswordsControl()) {
                    checkIfChangeDefaultPasswd(user);
                }

                user.setPassword(dbPasswordEncoderService.encodePassword(
                        getClearNewPassword(), user.getLoginName()));
            }
        } catch (IllegalArgumentException e) {
        }
        user.validate();
        userDAO.save(user);
    }

    @Override
    public void setPassword(String password) {
        // password is not encrypted right away, because
        // user.getLoginName must exist to do that, and we're
        // not sure at this point
        if (password != "") {
            setClearNewPassword(password);
        } else {
            setClearNewPassword(null);
        }
    }

    private User findByLoginUser(String login) {
        try {
            return user = userDAO.findByLoginName(login);
        } catch (InstanceNotFoundException e) {
               throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void initEditLoggedUser() {
        User user = findByLoginUser(SecurityUtils.getSessionUserLoginName());
        this.user = getFromDB(user);
    }

    @Transactional(readOnly = true)
    private User getFromDB(User user) {
        return getFromDB(user.getId());
    }

    private User getFromDB(Long id) {
        try {
            User result = userDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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
    public boolean validateCurrentPassword(String value)
    {
        String currentPasswordEncoded = dbPasswordEncoderService.encodePassword((String)value, user.getLoginName());
        if(!(currentPasswordEncoded).equals(user.getPassword())) {
            return false;
        }
        return true;
    }
}
