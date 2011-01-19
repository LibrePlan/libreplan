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

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link Profile}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class ProfileCRUDController extends GenericForwardComposer implements
        IProfileCRUDController {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(ProfileCRUDController.class);

    private Window createWindow;

    private Window listWindow;

    private IProfileModel profileModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Combobox userRolesCombo;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        userRolesCombo = (Combobox) createWindow.getFellowIfAny("userRolesCombo");
        getVisibility().showOnly(listWindow);
        appendAllUserRoles(userRolesCombo);
    }

    /**
     * Appends the existing UserRoles to the Combobox passed.
     * @param combo
     */
    private void appendAllUserRoles(Combobox combo) {
        for(UserRole role : getAllRoles()) {
            Comboitem item = combo.appendItem(_(role.getDisplayName()));
            item.setValue(role);
        }
    }

    @Override
    public void goToCreateForm() {
        profileModel.initCreate();
        createWindow.setTitle(_("Create Profile"));
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void goToEditForm(Profile profile) {
        profileModel.initEdit(profile);
        createWindow.setTitle(_("Edit Profile"));
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
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
            goToEditForm(getProfile());
        }
    }

    public boolean save() {
        if(!ConstraintChecker.isValid(createWindow)) {
            return false;
        }
        try {
            profileModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("Profile saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
        return false;
    }

    public List<Profile> getProfiles() {
        return profileModel.getProfiles();
    }

    public Profile getProfile() {
        return profileModel.getProfile();
    }

    public List<UserRole> getAllRoles() {
        return profileModel.getAllRoles();
    }

    public void addSelectedRole() {
        Comboitem comboItem = userRolesCombo.getSelectedItem();
        if(comboItem != null) {
            addRole((UserRole)comboItem.getValue());
        }
    }

    public List<UserRole> getRoles() {
        return profileModel.getRoles();
    }

    public void addRole(UserRole role) {
        profileModel.addRole(role);
        Util.reloadBindings(createWindow);
    }

    public void removeRole(UserRole role) {
        profileModel.removeRole(role);
        Util.reloadBindings(createWindow);
    }

    public void removeProfile(Profile profile) {
        try {
            int status = Messagebox.show(_("Confirm deleting this profile. Are you sure?"), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                profileModel.confirmRemove(profile);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(
                    Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", profile.getId()), e);
        } catch (InstanceNotFoundException e) {
            messagesForUser.showMessage(
                    Level.ERROR, _("Cannot delete profile: it does not exist anymore"));
            LOG.error(_("Error removing element: ", profile.getId()), e);
        }
        goToList();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }

}
