/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link Profile}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class ProfileCRUDController extends GenericForwardComposer implements
        IProfileCRUDController {

    private Window createWindow;

    private Window listWindow;

    private IProfileModel profileModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private UserRoleListRenderer userRoleListRenderer = new UserRoleListRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);
    }

    @Override
    public void goToCreateForm() {
        profileModel.initCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void goToEditForm(Profile profile) {
        profileModel.initEdit(profile);
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

    public boolean roleBelongs(UserRole role) {
        return profileModel.roleBelongs(role);
    }

    public void checkRole(UserRole role, boolean checked) {
        if (checked) {
            profileModel.addRole(role);
        }
        else {
            profileModel.removeRole(role);
        }
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }

    /**
     * RowRenderer for a {@link UserRole} element inside a {@link Profile}
     *
     * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
     *
     */
    public class UserRoleListRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            UserRole role = (UserRole) data;

            row.setValue(role);

            // Create boxes
            appendLabel(row);
            appendCheckbox(row);
        }
    }

    public void appendLabel(Row row) {
        Label label = new Label();
        label.setValue(((UserRole)row.getValue()).getDisplayName());
        row.appendChild(label);
    }

    public void appendCheckbox(final Row row) {
        Checkbox checkbox = new Checkbox();
        checkbox.setChecked(roleBelongs((UserRole)row.getValue()));

        checkbox.addEventListener("onCheck", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                checkRole((UserRole)row.getValue(),
                        ((Checkbox)event.getTarget()).isChecked());
            }
        });
        row.appendChild(checkbox);
    }

    public UserRoleListRenderer getRenderer() {
        return userRoleListRenderer;
    }
}
