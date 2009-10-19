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

import java.util.Date;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.web.calendars.BaseCalendarEditionController;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.resources.worker.CriterionsController;
import org.navalplanner.web.resources.worker.CriterionsMachineController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Radio;
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

    private CriterionsMachineController criterionsController;

    public MachineCRUDController() {

    }

    public List<Machine> getMachines() {
        return machineModel.getMachines();
    }

    public Machine getMachine() {
        return machineModel.getMachine();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        setupCriterionsController();
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

    private void setupCriterionsController() throws Exception {
        final Component comp = editWindow.getFellowIfAny("criterionsContainer");
        criterionsController = new CriterionsMachineController();
        criterionsController.doAfterCompose(comp);
    }

    public void goToCreateForm() {
        machineModel.initCreate();
        criterionsController.prepareForCreate(machineModel.getMachine());
        editWindow.setTitle(_("Create machine"));
        showEditWindow();
    }

    private void showEditWindow() {
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    /**
     * Loads {@link Machine} into model, shares loaded {@link Machine} with
     * {@link CriterionsController}
     *
     * @param machine
     */
    public void goToEditForm(Machine machine) {
        machineModel.initEdit(machine);
        prepareCriterionsForEdit();
        prepareCalendarForEdit();
        editWindow.setTitle(_("Edit machine"));
        showEditWindow();
    }

    private void prepareCriterionsForEdit() {
        criterionsController.prepareForEdit(machineModel.getMachine());
    }

    private void prepareCalendarForEdit() {
        if (isCalendarNull()) {
            return;
        }

        updateCalendarController();
        resourceCalendarModel.initEdit(machineModel.getCalendar());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new Date());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
    }

    public void save() {
        try {
            saveCalendar();
            saveCriterions();
            machineModel.confirmSave();
            goToList();
            messagesForUser.showMessage(Level.INFO, _("Machine saved"));
        } catch (ValidationException e) {
            messagesForUser
                    .showMessage(Level.INFO, _("Could not save Machine"));
            e.printStackTrace();
        }
    }

    private void saveCalendar() throws ValidationException {
        if (baseCalendarEditionController != null) {
            baseCalendarEditionController.save();
        }
    }

    private void saveCriterions() throws ValidationException {
        if (criterionsController != null) {
            criterionsController.save();
        }
    }

    private void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void calendarChecked(Radio radio) {
        Combobox comboboxDerived = (Combobox) radio
                .getFellow("createDerivedCalendar");
        Combobox comboboxCopy = (Combobox) radio
                .getFellow("createCopyCalendar");

        String selectedId = radio.getId();
        if (selectedId.equals("createFromScratch")) {
            comboboxDerived.setDisabled(true);
            comboboxCopy.setDisabled(true);
        } else if (selectedId.equals("createDerived")) {
            comboboxDerived.setDisabled(false);
            comboboxCopy.setDisabled(true);
        } else if (selectedId.equals("createCopy")) {
            comboboxDerived.setDisabled(true);
            comboboxCopy.setDisabled(false);
        }
    }

    public List<BaseCalendar> getBaseCalendars() {
        return machineModel.getBaseCalendars();
    }

    private IBaseCalendarModel resourceCalendarModel;

    public void createCalendar(String optionId) {
        if (optionId.equals("createFromScratch")) {
            resourceCalendarModel.initCreate();
        } else if (optionId.equals("createDerived")) {
            Combobox combobox = (Combobox) editWindow
                    .getFellow("createDerivedCalendar");
            Comboitem selectedItem = combobox.getSelectedItem();
            if (selectedItem == null) {
                throw new WrongValueException(combobox,
                        _("Please, select a calendar"));
            }
            BaseCalendar parentCalendar = (BaseCalendar) combobox
                    .getSelectedItem().getValue();
            resourceCalendarModel.initCreateDerived(parentCalendar);
        } else if (optionId.equals("createCopy")) {
            Combobox combobox = (Combobox) editWindow
                    .getFellow("createCopyCalendar");
            Comboitem selectedItem = combobox.getSelectedItem();
            if (selectedItem == null) {
                throw new WrongValueException(combobox,
                        _("Please, select a calendar"));
            }
            BaseCalendar origCalendar = (BaseCalendar) combobox
                    .getSelectedItem().getValue();
            resourceCalendarModel.initCreateCopy(origCalendar);
        } else {
            throw new RuntimeException(_("Unknow option {0} to create a resource calendar", optionId));
        }

        updateCalendarController();
        machineModel.setCalendar((ResourceCalendar) resourceCalendarModel
                .getBaseCalendar());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new Date());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
        reloadWindow();
    }

    private Window editCalendarWindow;

    private Window createNewVersionWindow;

    private BaseCalendarEditionController baseCalendarEditionController;

    private void updateCalendarController() {
        editCalendarWindow = (Window) editWindow
                .getFellowIfAny("editCalendarWindow");
        createNewVersionWindow = (Window) editWindow
                .getFellowIfAny("createNewVersion");

        createNewVersionWindow.setVisible(true);
        createNewVersionWindow.setVisible(false);

        baseCalendarEditionController = new BaseCalendarEditionController(
                resourceCalendarModel, editCalendarWindow,
                createNewVersionWindow) {

            @Override
            public void goToList() {
                machineModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadWindow();
            }

            @Override
            public void cancel() {
                resourceCalendarModel.cancel();
                machineModel.setCalendar(null);
                reloadWindow();
            }

            @Override
            public void save() {
                machineModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadWindow();
            }

        };

        editCalendarWindow.setVariable("calendarController", this, true);
        createNewVersionWindow.setVariable("calendarController", this, true);
    }

    private void reloadWindow() {
        Util.reloadBindings(editWindow);
    }

    public boolean isCalendarNull() {
        return (machineModel.getCalendar() == null);
    }

    public boolean isCalendarNotNull() {
        return !isCalendarNull();
    }

    public BaseCalendarEditionController getEditionController() {
        return baseCalendarEditionController;
    }

    @SuppressWarnings("unused")
    private CriterionsController getCriterionsController() {
        return (CriterionsController) editWindow.getFellow(
                "criterionsContainer").getAttribute(
                "assignedCriterionsController");
    }

}
