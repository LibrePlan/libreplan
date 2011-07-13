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

package org.navalplanner.web.externalcompanies;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.common.BaseCRUDController;
import org.navalplanner.web.common.components.Autocomplete;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Column;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for CRUD actions over a {@link User}
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@SuppressWarnings("serial")
public class ExternalCompanyCRUDController extends BaseCRUDController<ExternalCompany>
        implements IExternalCompanyCRUDController {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(ExternalCompanyCRUDController.class);

    private IExternalCompanyModel externalCompanyModel;

    private Textbox appURI;

    private Textbox ourCompanyLogin;

    private Textbox ourCompanyPassword;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        appURI = (Textbox) editWindow.getFellow("appURI");
        ourCompanyLogin = (Textbox) editWindow.getFellow("ourCompanyLogin");
        ourCompanyPassword = (Textbox) editWindow
                .getFellow("ourCompanyPassword");
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

    protected void save() throws ValidationException {
        externalCompanyModel.confirmSave();
    }

    public List<ExternalCompany> getCompanies() {
        return externalCompanyModel.getCompanies();
    }

    public List<ExternalCompanyDTO> getCompaniesDTO() {
        List<ExternalCompanyDTO> result = new ArrayList<ExternalCompanyDTO>();
        for (ExternalCompany company : getCompanies()) {
            result.add(new ExternalCompanyDTO(company));
        }
        return result;
    }

    public ExternalCompany getCompany() {
        return externalCompanyModel.getCompany();
    }

    public void setCompanyUser(Comboitem selectedItem) {
        if (selectedItem != null) {
            externalCompanyModel.setCompanyUser((User) selectedItem.getValue());
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
        appURI.setConstraint("no empty:" + _("cannot be null or empty"));
        ourCompanyLogin.setConstraint("no empty:"
                + _("cannot be null or empty"));
        ourCompanyPassword.setConstraint("no empty:"
                + _("cannot be null or empty"));
    }

    private void disableInteractionFields() {
        appURI.setDisabled(true);
        ourCompanyLogin.setDisabled(true);
        ourCompanyPassword.setDisabled(true);
        appURI.setConstraint("");
        ourCompanyLogin.setConstraint("");
        ourCompanyPassword.setConstraint("");
    }

    public void sortByDefaultByName() {
        Column column = (Column) listWindow.getFellowIfAny("columnName");
        if (column != null) {
            if (column.getSortDirection().equals("ascending")) {
                column.sort(false, false);
                column.setSortDirection("ascending");
            } else if (column.getSortDirection().equals("descending")) {
                column.sort(true, false);
                column.setSortDirection("descending");
            }
        }
    }

    @Override
    protected String getEntityType() {
        return _("External company");
    }

    @Override
    protected String getPluralEntityType() {
        return _("External companies");
    }

    @Override
    protected void initCreate() {
        externalCompanyModel.initCreate();
        setInteractionFieldsActivation(getCompany()
                .getInteractsWithApplications());
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
}
