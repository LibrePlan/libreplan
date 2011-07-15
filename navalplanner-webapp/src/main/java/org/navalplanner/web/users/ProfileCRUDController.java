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
import org.navalplanner.web.common.BaseCRUDController;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;

/**
 * Controller for CRUD actions over a {@link Profile}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class ProfileCRUDController extends BaseCRUDController<Profile> {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(ProfileCRUDController.class);

    private IProfileModel profileModel;

    private Combobox userRolesCombo;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        userRolesCombo = (Combobox) editWindow.getFellowIfAny("userRolesCombo");
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

    protected void save() throws ValidationException{
        profileModel.confirmSave();
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
        Util.reloadBindings(editWindow);
    }

    public void removeRole(UserRole role) {
        profileModel.removeRole(role);
        Util.reloadBindings(editWindow);
    }

    @Override
    protected String getEntityType() {
        return _("Profile");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Profiles");
    }

    @Override
    protected void initCreate() {
        profileModel.initCreate();
    }

    @Override
    protected void initEdit(Profile profile) {
        profileModel.initEdit(profile);
    }

    @Override
    protected Profile getEntityBeingEdited() {
        return profileModel.getProfile();
    }

    private boolean isReferencedByOtherEntities(Profile profile) {
        try {
            profileModel.checkHasUsers(profile);
            return false;
        } catch (ValidationException e) {
            showCannotDeleteProfileDialog(e.getInvalidValue().getMessage(),
                    profile);
        }
        return true;
    }

    private void showCannotDeleteProfileDialog(String message, Profile profile) {
        try {
            Messagebox.show(_(message), _("Warning"), Messagebox.OK,
                    Messagebox.EXCLAMATION);
        } catch (InterruptedException e) {
            LOG.error(
                    _("Error on showing warning message removing typeOfWorkHours: ",
                            profile.getId()), e);
        }
    }
    @Override
    protected boolean beforeDeleting(Profile profile){
        return !isReferencedByOtherEntities(profile);
    }

    @Override
    protected void delete(Profile profile) throws InstanceNotFoundException {
        profileModel.confirmRemove(profile);
    }
}
