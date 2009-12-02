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

package org.navalplanner.web.costcategories;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Checkbox;
import org.zkoss.zul.api.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link TypeOfWorkHours}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class TypeOfWorkHoursCRUDController extends GenericForwardComposer implements
        ITypeOfWorkHoursCRUDController {

    private Window createWindow;

    private Window listWindow;

    private ITypeOfWorkHoursModel typeOfWorkHoursModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Textbox code;
    private Textbox name;
    private Textbox defaultPrice;
    private Checkbox enabled;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        code = (Textbox) createWindow.getFellowIfAny("code");
        name = (Textbox) createWindow.getFellowIfAny("name");
        defaultPrice = (Textbox) createWindow.getFellowIfAny("defaultPrice");
        enabled = (Checkbox) createWindow.getFellowIfAny("enabled");
        getVisibility().showOnly(listWindow);
    }

    @Override
    public void goToCreateForm(TypeOfWorkHours typeOfWorkHours) {
        // TODO Auto-generated method stub

    }

    @Override
    public void goToEditForm(TypeOfWorkHours typeOfWorkHours) {
        typeOfWorkHoursModel.initEdit(typeOfWorkHours);
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
            goToEditForm(getTypeOfWorkHours());
        }
    }

    public boolean save() {
        if(!validate()) {
            return false;
        }
        try {
            typeOfWorkHoursModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("Type of work hours saved"));
            return true;
        } catch (ValidationException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
        }
        return false;
    }

    /* validates the constraints of the elements in ZK */
    private boolean validate() {
        try {
            //we 'touch' the attributes in the interface
            //if any of their constraints is active, they
            //will throw an exception
            code.getValue();
            name.getValue();
            defaultPrice.getValue();
            enabled.isChecked();
            return true;
        }
        catch (WrongValueException e) {
            return false;
        }
    }

    public List<TypeOfWorkHours> getTypesOfWorkHours() {
        return typeOfWorkHoursModel.getTypesOfWorkHours();
    }

    public TypeOfWorkHours getTypeOfWorkHours() {
        return typeOfWorkHoursModel.getTypeOfWorkHours();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }
}
