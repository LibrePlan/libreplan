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

import static org.libreplan.web.I18nHelper._;

import org.apache.commons.lang.StringUtils;
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
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for password changes
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

    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("passwordController", this, true);
        messages = new MessagesForUser(messagesContainer);
        passwordModel.initEditLoggedUser();
    }

    public void save() {
        ConstraintChecker.isValid(passwordWindow);
        try {
            passwordModel.confirmSave();
            messages.showMessage(Level.INFO, _("Password saved"));
            PasswordUtil.showOrHideDefaultPasswordWarnings();
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
    }

    /**
     * Tells the SettingsModel to set the password attribute of the inner
     * {@ link User} object.
     *
     * @param password String with the <b>unencrypted</b> password.
     */
    public void setPassword(String password) {
        passwordModel.setPassword(password);
    }

    public boolean getLdapUser() {
        return !UserUtil.getUserFromSession().isLibrePlanUser();
    }

    public Constraint validatePasswordConfirmation() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if(StringUtils.isEmpty((String)value) || StringUtils.isEmpty(password.getValue())) {
                    throw new WrongValueException(comp, _("passwords can not be empty"));
                }
                if(!((String)value).equals(password.getValue())) {
                    throw new WrongValueException(comp, _("passwords don't match"));
                }
            }
        };
    }

    public Constraint validateCurrentPassword() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if(!passwordModel.validateCurrentPassword((String)value)) {
                    throw new WrongValueException(comp, _("Current password is incorrect"));
                }
            }
        };
    }

}
