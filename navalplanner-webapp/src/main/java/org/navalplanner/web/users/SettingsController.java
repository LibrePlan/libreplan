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

package org.navalplanner.web.users;

import static org.navalplanner.web.I18nHelper._;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.entities.Language;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.common.ConfigurationController;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.security.SecurityUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

/**
 * Controller for user settings
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public class SettingsController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(ConfigurationController.class);

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Combobox applicationLanguage;

    private ISettingsModel settingsModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("settingsController", this, true);
        messages = new MessagesForUser(messagesContainer);

        User user = settingsModel.findByLoginUser(SecurityUtils
                .getSessionUserLoginName());

        settingsModel.initEdit(user);

        appendAllLanguages(applicationLanguage);

        applicationLanguage.setSelectedIndex(settingsModel.getUser()
                .getApplicationLanguage().ordinal());
    }

    private void appendAllLanguages(Combobox combo) {
        for (Language language : getLanguageNames()) {
            Comboitem item = combo.appendItem(_(language.getDisplayName()));
            item.setValue(language);
        }
    }

    public Language[] getLanguageNames() {
        return Language.values();
    }

    public boolean save() {
        try {
            settingsModel.setApplicationLanguage(getSelectedLanguage());
            settingsModel.confirmSave();
            messages.showMessage(Level.INFO, _("Settings saved"));
            applicationLanguage.setSelectedItem(applicationLanguage
                    .getSelectedItem());
            return true;
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
        return false;
    }

    private Language getSelectedLanguage() {
        Comboitem selectedItem = applicationLanguage.getSelectedItem();
        if (selectedItem != null) {
            return (Language) selectedItem.getValue();
        }
        return null;
    }

    private User getUser()
    {
        return settingsModel.getUser();
    }
}
