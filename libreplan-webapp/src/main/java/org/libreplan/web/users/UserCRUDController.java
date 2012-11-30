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

package org.libreplan.web.users;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.User.UserAuthenticationType;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.libreplan.web.resources.worker.IWorkerCRUDControllerEntryPoints;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.users.bootstrap.PredefinedUsers;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Groupbox;

/**
 * Controller for CRUD actions over a {@link User}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@SuppressWarnings("serial")
public class UserCRUDController extends BaseCRUDController<User> implements
        IUserCRUDController {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(UserCRUDController.class);

    @Resource
    private IWorkerCRUDControllerEntryPoints workerCRUD;

    private IUserModel userModel;

    private Textbox passwordBox;

    private Textbox passwordConfirmationBox;

    private Combobox userRolesCombo;

    private Groupbox boundResourceGroupbox;

    private Combobox profilesCombo;

    private IURLHandlerRegistry URLHandlerRegistry;

    private RowRenderer usersRenderer = new RowRenderer() {

        @Override
        public void render(Row row, Object data) throws Exception {
            final User user = (User) data;
            row.setValue(user);

            Util.appendLabel(row, user.getLoginName());
            Util.appendLabel(row, user.isDisabled() ? _("Yes") : _("No"));
            Util.appendLabel(row, user.isSuperuser() ? _("Yes") : _("No"));
            Util.appendLabel(row, _(user.getUserType().toString()));
            Util.appendLabel(row, user.isBound() ? user.getWorker()
                    .getShortDescription() : "");

            Button[] buttons = Util.appendOperationsAndOnClickEvent(row,
                    new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    goToEditForm(user);
                }
            }, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    confirmDelete(user);
                }
            });

            // Disable remove button for default admin as it's mandatory
            if (isDefaultAdmin(user)) {
                buttons[1].setDisabled(true);
                buttons[1]
                        .setTooltiptext(_("Default user \"admin\" cannot be removed as it is mandatory"));
            }
            buttons[1].setDisabled(true);
        }

    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        passwordBox = (Textbox) editWindow.getFellowIfAny("password");
        passwordConfirmationBox = (Textbox) editWindow.getFellowIfAny("passwordConfirmation");
        profilesCombo = (Combobox) editWindow.getFellowIfAny("profilesCombo");
        userRolesCombo = (Combobox) editWindow.getFellowIfAny("userRolesCombo");
        appendAllUserRolesExceptRoleBoundUser(userRolesCombo);
        appendAllProfiles(profilesCombo);
        boundResourceGroupbox = (Groupbox) editWindow
                .getFellowIfAny("boundResourceGroupbox");

        final EntryPointsHandler<IUserCRUDController> handler = URLHandlerRegistry
                .getRedirectorFor(IUserCRUDController.class);
        handler.register(this, page);
    }

    /**
     * Appends the existing UserRoles to the Combobox passed.
     * @param combo
     */
    private void appendAllUserRolesExceptRoleBoundUser(Combobox combo) {
        List<UserRole> roles = new ArrayList<UserRole>(Arrays.asList(UserRole
                .values()));
        roles.remove(UserRole.ROLE_BOUND_USER);

        Collections.sort(roles, new Comparator<UserRole> () {
            @Override
            public int compare(UserRole arg0, UserRole arg1) {
                return _(arg0.getDisplayName()).compareTo(
                        _(arg1.getDisplayName()));
            }
        });

        for (UserRole role : roles) {
            Comboitem item = combo.appendItem(_(role.getDisplayName()));
            item.setValue(role);
        }
    }

    /**
     * Appends the existing Profiles to the Combobox passed.
     *
     * @param combo
     */
    private void appendAllProfiles(Combobox combo) {
        List<Profile> profiles = userModel.getAllProfiles();
        for (Profile profile : profiles) {
            Comboitem item = combo.appendItem(profile.getProfileName());
            item.setValue(profile);
        }
    }

    public List<User> getUsers() {
        return userModel.getUsers();
    }

    @Override
    protected void save() throws ValidationException {
        userModel.confirmSave();
        PasswordUtil.showOrHideDefaultPasswordWarnings();
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
        Util.reloadBindings(editWindow);
    }

    public void removeRole(UserRole role) {
        userModel.removeRole(role);
        Util.reloadBindings(editWindow);
    }

    public List<Profile> getProfiles() {
        return userModel.getProfiles();
    }

    public void addSelectedProfile() {
        Comboitem comboItem = profilesCombo.getSelectedItem();
        if(comboItem != null) {
            addProfile((Profile)comboItem.getValue());
        }
    }

    public void addProfile(Profile profile) {
        userModel.addProfile(profile);
        Util.reloadBindings(editWindow);
    }

    public void removeProfile(Profile profile) {
        userModel.removeProfile(profile);
        Util.reloadBindings(editWindow);
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
        ((Textbox)editWindow.getFellowIfAny("passwordConfirmation")).
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

    @Override
    protected String getEntityType() {
        return _("User");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Users");
    }

    @Override
    protected void initCreate() {
        userModel.initCreate();
        //password is compulsory when creating
        passwordBox.setConstraint("no empty:" +
                _("Password cannot be empty"));
        //clean the password boxes, they are not cleared automatically
        //because they are not directly associated to an attribute
        passwordBox.setRawValue("");
        passwordConfirmationBox.setRawValue("");
        prepareAuthenticationTypesCombo();
    }

    @Override
    protected void initEdit(User user) {
        userModel.initEdit(user);
        //password is not compulsory when editing, so we remove
        //the constraint
        passwordBox.setConstraint((Constraint)null);
        //cleans the box and forces the check of the new Constraint (null)
        passwordBox.setValue("");
        passwordConfirmationBox.setValue("");
        //setup authentication type combo box
        prepareAuthenticationTypesCombo();
    }

    private void prepareAuthenticationTypesCombo() {
        Combobox combo = (Combobox) editWindow
                .getFellowIfAny("authenticationTypeCombo");
        combo.getChildren().clear();
        for (UserAuthenticationType type : UserAuthenticationType.values()) {
            Comboitem item = combo.appendItem(_(type.toString()));
            item.setValue(type);
            if (type.equals(getAuthenticationType())) {
                combo.setSelectedItem(item);
            }
        }

        Row comboRow = (Row) editWindow
                .getFellowIfAny("authenticationTypeComboRow");
        comboRow.setVisible(true);
    }

    @Override
    protected User getEntityBeingEdited() {
        return userModel.getUser();
    }

    @Override
    protected boolean beforeDeleting(User user) {
        Worker worker = user.getWorker();
        if (worker != null) {
            try {
                return Messagebox
                        .show(_("User is bound to resource \"{0}\" and it will be unbound. Do you want to continue with user removal?",
                                worker.getShortDescription()),
                                _("Confirm remove user"), Messagebox.YES
                                        | Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    protected void delete(User user) throws InstanceNotFoundException {
        userModel.confirmRemove(user);
    }

    public boolean isLdapUser() {
        User user = userModel.getUser();
        if (user == null) {
            return false;
        }
        return !user.isLibrePlanUser();
    }

    public boolean isLdapUserLdapConfiguration() {
        return (isLdapUser() && userModel.isLDAPBeingUsed());
    }

    public boolean getLdapUserRolesLdapConfiguration() {
        return (isLdapUser() && userModel.isLDAPRolesBeingUsed());
    }

    public RowRenderer getRolesRenderer() {
        return new RowRenderer() {
            @Override
            public void render(Row row, Object data) throws Exception {
                final UserRole role = (UserRole) data;

                row.appendChild(new Label(_(role.getDisplayName())));

                Button removeButton = Util
                        .createRemoveButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        removeRole(role);
                    }
                });
                removeButton.setDisabled(true);
                row.appendChild(removeButton);
            }
        };
    }

    public RowRenderer getUsersRenderer() {
        return usersRenderer;
    }

    public String hasBoundResource() {
        User user = getUser();
        if (user != null && user.isBound()) {
            return _("Yes");
        }
        return _("No");
    }

    public String getBoundResource() {
        User user = getUser();
        if (user != null && user.isBound()) {
            return user.getWorker().getShortDescription();
        }
        return "";
    }

    public boolean isBound() {
        User user = getUser();
        if (user != null) {
            return user.isBound();
        }
        return false;
    }

    public void goToWorkerEdition() {
        Worker worker = getUser().getWorker();
        if (worker != null) {
            if (showConfirmWorkerEditionDialog() == Messagebox.OK) {
                workerCRUD.goToEditForm(worker);
            }
        }
    }

    private int showConfirmWorkerEditionDialog() {
        try {
            return Messagebox
                    .show(_("Unsaved changes will be lost. Would you like to continue?"),
                            _("Confirm worker edition"), Messagebox.OK
                                    | Messagebox.CANCEL, Messagebox.QUESTION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void unboundResource() {
        userModel.unboundResource();
        Util.reloadBindings(boundResourceGroupbox);
    }

    public boolean isNoRoleWorkers() {
        return !SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_WORKERS);
    }

    public String getWorkerEditionButtonTooltip() {
        if (isNoRoleWorkers()) {
            return _("You do not have permissions to go to worker edition window");
        }
        return "";
    }

    private boolean isDefaultAdmin(final User user) {
        return user.getLoginName().equals(PredefinedUsers.ADMIN.getLoginName());
    }

    private boolean isUserDefaultAdmin() {
        User user = userModel.getUser();
        if (user != null) {
            return isDefaultAdmin(user);
        }
        return false;
    }

    public boolean areRolesAndProfilesDisabled() {
        return (isLdapUser() && userModel.isLDAPBeingUsed() && userModel
                .isLDAPRolesBeingUsed()) || isUserDefaultAdmin();
    }

    public boolean isLdapUserOrDefaultAdmin() {
        return isLdapUser() || isUserDefaultAdmin();
    }

    public UserAuthenticationType getAuthenticationType() {
        User user = getUser();
        if (user != null) {
            return user.getUserType();
        }
        return null;
    }

    public void setAuthenticationType(Comboitem item) {
        if (item == null) {
            throw new WrongValueException(
                    editWindow.getFellowIfAny("authenticationTypeCombo"),
                    _("cannot be empty"));
        }
        UserAuthenticationType authenticationType = (UserAuthenticationType) item
                .getValue();
        User user = getUser();
        if (user != null) {
            user.setLibrePlanUser(authenticationType
                    .equals(UserAuthenticationType.DATABASE));
        }
    }

}
