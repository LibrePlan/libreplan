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
import org.navalplanner.web.common.ConfigurationController;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListitemRenderer;

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

    private ISettingsModel settingsModel;

    public static ListitemRenderer languagesRenderer = new ListitemRenderer() {
        @Override
        public void render(org.zkoss.zul.Listitem item, Object data)
                throws Exception {
            Language language = (Language) data;
            item.setLabel(language.getDisplayName());
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("settingsController", this, true);
        messages = new MessagesForUser(messagesContainer);
        settingsModel.initEditLoggedUser();
    }

    public Language[] getLanguages() {
        return Language.values();
    }

    public boolean save() {
        try {
            settingsModel.confirmSave();
            messages.showMessage(Level.INFO, _("Settings saved"));
            return true;
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
        return false;
    }

    public void setSelectedLanguage(Language language) {
        settingsModel.setApplicationLanguage(language);
    }

    public Language getSelectedLanguage() {
        return settingsModel.getApplicationLanguage();
    }

    public static ListitemRenderer getLanguagesRenderer() {
        return languagesRenderer;
    }

}
