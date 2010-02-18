/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.externalcompanies;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for CRUD actions over a {@link User}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class ExternalCompanyCRUDController extends GenericForwardComposer
        implements IExternalCompanyCRUDController {

    private IExternalCompanyModel externalCompanyModel;

    private Window createWindow;

    private Window listWindow;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Textbox appURI;

    private Textbox ourCompanyLogin;

    private Textbox ourCompanyPassword;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);
        appURI = (Textbox) createWindow.getFellow("appURI");
        ourCompanyLogin = (Textbox) createWindow.getFellow("ourCompanyLogin");
        ourCompanyPassword = (Textbox) createWindow.getFellow("ourCompanyPassword");
    }

    @Override
    public void goToCreateForm() {
        externalCompanyModel.initCreate();
        getVisibility().showOnly(createWindow);
        setInteractionFieldsActivation(getCompany().getInteractsWithApplications());
        clearAutocompleteUser();
        Util.reloadBindings(createWindow);
    }

    private void clearAutocompleteUser() {
        Autocomplete user = (Autocomplete) createWindow.getFellowIfAny("user");
        if (user != null) {
            user.setValue("");
        }
    }

    @Override
    public void goToEditForm(ExternalCompany company) {
        externalCompanyModel.initEdit(company);
        getVisibility().showOnly(createWindow);
        setInteractionFieldsActivation(company.getInteractsWithApplications());
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
            goToEditForm(getCompany());
        }
    }

    public boolean save() {
        if(!ConstraintChecker.isValid(createWindow)) {
            return false;
        }
        try {
            externalCompanyModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("Company saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
        return false;
    }

    public List<ExternalCompany> getCompanies() {
        return externalCompanyModel.getCompanies();
    }

    public ExternalCompany getCompany() {
        return externalCompanyModel.getCompany();
    }

    public void setCompanyUser(Comboitem selectedItem) {
        if (selectedItem != null) {
            externalCompanyModel.setCompanyUser((User) selectedItem.getValue());
        }
        else {
            externalCompanyModel.setCompanyUser(null);
        }
    }

    public void setInteractionFieldsActivation(boolean active) {
        if(active) {
            enableInteractionFields();
        }
        else {
            disableInteractionFields();
        }
    }

    private void enableInteractionFields() {
        appURI.setDisabled(false);
        ourCompanyLogin.setDisabled(false);
        ourCompanyPassword.setDisabled(false);
        appURI.setConstraint("no empty:" + _("cannot be null or empty"));
        ourCompanyLogin.setConstraint("no empty:" + _("cannot be null or empty"));
        ourCompanyPassword.setConstraint("no empty:" + _("cannot be null or empty"));
    }

    private void disableInteractionFields() {
        appURI.setDisabled(true);
        ourCompanyLogin.setDisabled(true);
        ourCompanyPassword.setDisabled(true);
        appURI.setConstraint("");
        ourCompanyLogin.setConstraint("");
        ourCompanyPassword.setConstraint("");
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }
}
