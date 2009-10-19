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

package org.navalplanner.web.resources.machine;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Machine} resource <br />

 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class MachineCRUDController extends GenericForwardComposer {

    private Window listWindow;

    private Window editWindow;

    private IMachineModel machineModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    public MachineCRUDController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        showListWindow();
    }

    private void showListWindow() {
        getVisibility().showOnly(listWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow);
        }
        return visibility;
    }

    public void goToCreateForm() {
        // entity.initCreate();
        editWindow.setTitle(_("Create machine"));
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    private void showEditWindow() {
        getVisibility().showOnly(editWindow);
    }

    public void goToEditForm(Machine machine) {
        // model.initEdit(machine);
        editWindow.setTitle(_("Edit machine"));
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    public void save() {
        validate();
        // model.confirmSave();
        goToList();
        messagesForUser.showMessage(Level.INFO, _("Machine saved"));
    }

    private void validate() {
        // TODO: Validate
    }

    private void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void close() {
        goToList();
    }

    public List<Machine> getMachines() {
        return machineModel.getMachines();
    }

}
