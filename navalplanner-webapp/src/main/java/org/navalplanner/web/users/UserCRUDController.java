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

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link User}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class UserCRUDController extends GenericForwardComposer implements
        IUserCRUDController {

    private Window createWindow;

    private Window listWindow;

    private IUserModel userModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Textbox passwordBox;

    private Textbox passwordConfirmationBox;

    private Combobox userRolesCombo;

    private Autocomplete profileAutocomplete;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);
        passwordBox = (Textbox) createWindow.getFellowIfAny("password");
        passwordConfirmationBox = (Textbox) createWindow.getFellowIfAny("passwordConfirmation");
        profileAutocomplete = (Autocomplete) createWindow.getFellowIfAny("profileAutocomplete");
        userRolesCombo = (Combobox) createWindow.getFellowIfAny("userRolesCombo");
        appendAllUserRoles(userRolesCombo);
    }

    /**
     * Appends the existing UserRoles to the Combobox passed.
     * @param combo
     */
    private void appendAllUserRoles(Combobox combo) {
        for(UserRole role : UserRole.values()) {
            Comboitem item = combo.appendItem(_(role.getDisplayName()));
            item.setValue(role);
        }
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public List<User> getUsers() {
        return userModel.getUsers();
    }

    @Override
    public void goToCreateForm() {
        userModel.initCreate();
        //password is compulsory when creating
        passwordBox.setConstraint("no empty:" +
                _("The password for a new user cannot be empty"));
        createWindow.setTitle(_("Create User"));
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
        //clean the password boxes, they are not cleared automatically
        //because they are not directly associated to an attribute
        passwordBox.setRawValue("");
        passwordConfirmationBox.setRawValue("");
    }

    @Override
    public void goToEditForm(User user) {
        userModel.initEdit(user);
        //password is not compulsory when editing, so we remove
        //the constraint
        passwordBox.setConstraint((Constraint)null);
        createWindow.setTitle(_("Edit User"));
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
        //cleans the box and forces the check of the new Constraint (null)
        passwordBox.setValue("");
        passwordConfirmationBox.setValue("");
    }

    public void cancel() {
        goToList();
    }

    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditForm(getUser());
        }
    }

    public boolean save() {
        if(!ConstraintChecker.isValid(createWindow)) {
            return false;
        }
        try {
            userModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("User saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
        return false;
    }

    public User getUser() {
        return userModel.getUser();
    }

    public List<UserRole> getRoles() {
        return userModel.getRoles();
    }

    public void addSelectedRole() {
        Comboitem comboItem = userRolesCombo.getSelectedItem();
        if(comboItem != null) {
            addRole((UserRole)comboItem.getValue());
        }
    }

    public void addRole(UserRole role) {
        userModel.addRole(role);
        Util.reloadBindings(createWindow);
    }

    public void removeRole(UserRole role) {
        userModel.removeRole(role);
        Util.reloadBindings(createWindow);
    }

    public List<Profile> getProfiles() {
        return userModel.getProfiles();
    }

    public void addSelectedProfile() {
        Comboitem comboItem = profileAutocomplete.getSelectedItem();
        if(comboItem != null) {
            addProfile((Profile)comboItem.getValue());
        }
    }

    public void addProfile(Profile profile) {
        userModel.addProfile(profile);
        Util.reloadBindings(createWindow);
    }

    public void removeProfile(Profile profile) {
        userModel.removeProfile(profile);
        Util.reloadBindings(createWindow);
    }

    /**
     * Tells the XXXModel to set the password attribute of the inner
     * {@ link User} object.
     *
     * @param password String with the <b>unencrypted</b> password.
     */
    public void setPassword(String password) {
        userModel.setPassword(password);
        //update the constraint on the confirmation password box
        ((Textbox)createWindow.getFellowIfAny("passwordConfirmation")).
            clearErrorMessage(true);
    }

    public Constraint validatePasswordConfirmation() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                ((Textbox)comp).setRawValue(value);
                if(!((String)value).equals(passwordBox.getValue())) {
                    throw new WrongValueException(comp, _("passwords don't match"));
                }
            }
        };
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }
}
