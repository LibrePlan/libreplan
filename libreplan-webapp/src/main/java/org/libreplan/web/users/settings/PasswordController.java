/*
 * This file is part of LibrePlan
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

package org.libreplan.web.users.settings;

import static org.libreplan.web.I18nHelper._t;

import org.apache.commons.lang3.StringUtils;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.web.UserUtil;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.users.PasswordUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for password changes.
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class PasswordController extends GenericForwardComposer {

    private Window passwordWindow;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private IPasswordModel passwordModel;

    private Textbox password;

    public PasswordController() {
        passwordModel = (IPasswordModel) SpringUtil.getBean("passwordModel");
    }

    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("passwordController", this, true);

        // Util.createBindingsFor looks up a component attribute literally named "controller" to
        // find the controller to bind against - it does not know about this page's
        // "passwordController" variable name (used by the ZUML's own @load/@save/onClick
        // expressions), so both attributes need to be set.
        comp.setAttribute("controller", this, true);

        messages = new MessagesForUser(messagesContainer);
        passwordModel.initEditLoggedUser();

        // AnnotateBinderInit's own page-level pass (<?init class="AnnotateBinderInit"?>) only
        // calls Util.createBindingsFor - it never loads the bindings it registers, so without an
        // explicit initial load here the form stays unpopulated until some other action happens
        // to trigger a reload. This is a single-window page (no separate hidden edit form), so a
        // comp-wide reload here is safe.
        org.libreplan.web.common.Util.createBindingsFor(comp);
        org.libreplan.web.common.Util.reloadBindings(comp);
    }

    public void save() {
        ConstraintChecker.isValid(passwordWindow);
        try {
            passwordModel.confirmSave();
            messages.showMessage(Level.INFO, _t("Password saved"));
            PasswordUtil.showOrHideDefaultPasswordWarnings();
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
    }

    /**
     * Tells the SettingsModel to set the password attribute of the inner {@link org.libreplan.business.users.entities.User} object.
     *
     * @param password String with the <b>unencrypted</b> password.
     */
    public void setPassword(String password) {
        passwordModel.setPassword(password);
    }

    public boolean isLdapUser() {
        return !UserUtil.getUserFromSession().isLibrePlanUser() && passwordModel.isLdapAuthEnabled();
    }

    public Constraint validatePasswordConfirmation() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                if (StringUtils.isEmpty((String)value) || StringUtils.isEmpty(password.getValue())) {
                    throw new WrongValueException(comp, _t("passwords can not be empty"));
                }
                if (!value.equals(password.getValue())) {
                    throw new WrongValueException(comp, _t("passwords don't match"));
                }
            }
        };
    }

    public Constraint validateCurrentPassword() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value) throws WrongValueException {
                if (!passwordModel.validateCurrentPassword((String)value)) {
                    throw new WrongValueException(comp, _t("Current password is incorrect"));
                }
            }
        };
    }

}
