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

package org.navalplanner.web.workreports;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link WorkReportType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class WorkReportTypeCRUDController extends GenericForwardComposer
        implements IWorkReportTypeCRUDControllerEntryPoints {

     private static final org.apache.commons.logging.Log LOG = LogFactory
     .getLog(WorkReportTypeCRUDController.class);

    private Window listWindow;

    private Window createWindow;

    private Window editWindow;

    private IWorkReportTypeModel workReportTypeModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportCRUDControllerEntryPoints workReportCRUD;

    private IURLHandlerRegistry URLHandlerRegistry;

    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeModel.getWorkReportTypes();
    }

    public WorkReportType getWorkReportType() {
        return workReportTypeModel.getWorkReportType();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        final URLHandler<IWorkReportTypeCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportTypeCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(WorkReportType workReportType) {
        workReportTypeModel.initEdit(workReportType);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void save() {
        try {
            workReportTypeModel.save();
            messagesForUser.showMessage(Level.INFO, _("Work report type saved"));
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    public void confirmRemove(WorkReportType workReportType) {
        if (thereAreWorkReportsFor(workReportType)) {
            try {
                Messagebox.show(_("Cannot delete work report type. There are some work reports bound to it."),
                        _("Warning"), Messagebox.OK, Messagebox.EXCLAMATION);
            } catch (InterruptedException e) {
                LOG.error(_("Error on showing warning message removing workReportType: ", workReportType.getId()), e);
            }
            return;
        }

        // Show remove confirming window
        try {
            if (Messagebox.show(_("Delete item. Are you sure?"), _("Confirm"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {
                workReportTypeModel.confirmRemove(workReportType);
                final Grid workReportTypes = (Grid) listWindow.getFellowIfAny("listing");
                if (workReportTypes != null) {
                    Util.reloadBindings(workReportTypes);
                }
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(
                    Level.ERROR, e.getMessage());
            LOG.error(_("Error on removing workReportType: ", workReportType.getId()), e);
        }
    }

    private boolean thereAreWorkReportsFor(WorkReportType workReportType) {
        return workReportTypeModel.thereAreWorkReportsFor(workReportType);
    }

    public void remove(WorkReportType workReportType) {
        workReportTypeModel.confirmRemove(workReportType);
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(
            Level.INFO, _("Removed {0}", workReportType.getName()));
    }

    public void goToEditForm() {
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToCreateForm() {
        workReportTypeModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, createWindow,
                    editWindow);
        }
        return visibility;
    }

    public void goToEditNewWorkReportForm(WorkReportType workReportType) {
        workReportCRUD.goToCreateForm(workReportType);
    }

}
