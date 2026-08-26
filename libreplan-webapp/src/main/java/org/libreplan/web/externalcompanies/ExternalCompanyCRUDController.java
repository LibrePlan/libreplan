/*
 * This file is part of LibrePlan
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

package org.libreplan.web.externalcompanies;

import static org.libreplan.web.I18nHelper._t;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for CRUD actions over a {@link User}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@SuppressWarnings("serial")
public class ExternalCompanyCRUDController extends BaseCRUDController<ExternalCompany> {

    private IExternalCompanyModel externalCompanyModel;

    private Textbox appURI;

    private Textbox ourCompanyLogin;

    private Textbox ourCompanyPassword;

    public ExternalCompanyCRUDController() {
        externalCompanyModel = (IExternalCompanyModel) SpringUtil.getBean("externalCompanyModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        appURI = (Textbox) editWindow.getFellow("appURI");
        ourCompanyLogin = (Textbox) editWindow.getFellow("ourCompanyLogin");
        ourCompanyPassword = (Textbox) editWindow.getFellow("ourCompanyPassword");

        // BaseCRUDController.doAfterCompose() already calls showListWindow() (which reloads
        // listWindow's bindings), but that happens before AnnotateBinderInit's own later
        // page-level pass has created any binder at all, so it's a no-op. Redo it here, now that
        // the binder exists. Only listWindow is reloaded - editWindow's bindings reference
        // controller.company, which is still null at this point (only set up by
        // initCreate()/initEdit(), called later); showEditWindow() reloads editWindow's own
        // bindings once that has happened.
        org.libreplan.web.common.Util.createBindingsFor(comp);
        org.libreplan.web.common.Util.reloadBindings(listWindow);
    }

    private void clearAutocompleteUser() {
        Autocomplete user = (Autocomplete) editWindow.getFellowIfAny("user");
        if (user != null) {
            user.clear();
        }
    }

    public void goToEditForm(ExternalCompanyDTO dto) {
        goToEditForm(dto.getCompany());
    }

    @Override
    protected void save() throws ValidationException {
        externalCompanyModel.confirmSave();
    }

    public List<ExternalCompany> getCompanies() {
        return externalCompanyModel.getCompanies();
    }

    public List<ExternalCompanyDTO> getCompaniesDTO() {
        List<ExternalCompanyDTO> result = new ArrayList<>();
        for (ExternalCompany company : getCompanies()) {
            result.add(new ExternalCompanyDTO(company));
        }
        // The grid's "model" property is declared as org.zkoss.zul.ListModel - AnnotateBinder has
        // no automatic List->ListModel coercion, so returning a plain List here throws a
        // ClassCastException when the binder tries to load it.
        return new org.zkoss.zul.ListModelList<>(result);
    }

    /**
     * The list's rows used to be declared with a ZUML "each" template (self="@{each=...}") - under
     * this app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for each
     * iteration. Building rows programmatically via RowRenderer sidesteps that "each" bug entirely.
     */
    public org.zkoss.zul.RowRenderer getCompaniesRenderer() {
        return (row, data, index) -> {
            final ExternalCompanyDTO dto = (ExternalCompanyDTO) data;
            row.setValue(dto);
            row.addEventListener(
                    org.zkoss.zk.ui.event.Events.ON_CLICK, event -> goToEditForm(dto.getCompany()));

            row.appendChild(new org.zkoss.zul.Label(dto.getName()));
            row.appendChild(new org.zkoss.zul.Label(dto.getNif()));

            org.zkoss.zul.Checkbox client = new org.zkoss.zul.Checkbox();
            client.setChecked(dto.getClient());
            client.setDisabled(true);
            row.appendChild(client);

            org.zkoss.zul.Checkbox subcontractor = new org.zkoss.zul.Checkbox();
            subcontractor.setChecked(dto.getSubcontractor());
            subcontractor.setDisabled(true);
            row.appendChild(subcontractor);

            row.appendChild(new org.zkoss.zul.Label(dto.getCompanyUser()));

            org.zkoss.zul.Hbox hbox = new org.zkoss.zul.Hbox();
            hbox.appendChild(
                    org.libreplan.web.common.Util.createEditButton(event -> goToEditForm(dto.getCompany())));
            hbox.appendChild(
                    org.libreplan.web.common.Util.createRemoveButton(event -> confirmDelete(dto.getCompany())));
            row.appendChild(hbox);
        };
    }

    public ExternalCompany getCompany() {
        return externalCompanyModel.getCompany();
    }

    public void setCompanyUser(Comboitem selectedItem) {
        if (selectedItem != null) {
            externalCompanyModel.setCompanyUser(selectedItem.getValue());
        } else {
            externalCompanyModel.setCompanyUser(null);
        }
    }

    public void setInteractionFieldsActivation(boolean active) {
        if (active) {
            enableInteractionFields();
        } else {
            disableInteractionFields();
        }
    }

    private void enableInteractionFields() {
        appURI.setDisabled(false);
        ourCompanyLogin.setDisabled(false);
        ourCompanyPassword.setDisabled(false);
        appURI.setConstraint("no empty:" + _t("cannot be empty"));
        ourCompanyLogin.setConstraint("no empty:" + _t("cannot be empty"));
        ourCompanyPassword.setConstraint("no empty:" + _t("cannot be empty"));
    }

    private void disableInteractionFields() {
        appURI.setDisabled(true);
        ourCompanyLogin.setDisabled(true);
        ourCompanyPassword.setDisabled(true);
        appURI.setConstraint("");
        ourCompanyLogin.setConstraint("");
        ourCompanyPassword.setConstraint("");
    }

    @Override
    protected String getEntityType() {
        return _t("Company");
    }

    @Override
    protected String getPluralEntityType() {
        return _t("Companies");
    }

    @Override
    protected void initCreate() {
        externalCompanyModel.initCreate();
        setInteractionFieldsActivation(getCompany().getInteractsWithApplications());
        clearAutocompleteUser();
    }

    @Override
    protected void initEdit(ExternalCompany company) {
        externalCompanyModel.initEdit(company);
        setInteractionFieldsActivation(company.getInteractsWithApplications());
        clearAutocompleteUser();
    }

    @Override
    protected ExternalCompany getEntityBeingEdited() {
        return externalCompanyModel.getCompany();
    }

    @Override
    protected void delete(ExternalCompany company) {
        externalCompanyModel.deleteCompany(company);
    }

    @Override
    protected boolean beforeDeleting(ExternalCompany company) {
        if (externalCompanyModel.isAlreadyInUse(company)) {
            messagesForUser.showMessage(
                    Level.WARNING,
                    _t("{0} \"{1}\" can not be deleted because of it is being used", getEntityType(), company.getHumanId()));
            return false;
        }
        return true;
    }

}
