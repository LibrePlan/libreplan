/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Window;

/**
 * Abstract class defining common behavior for controllers of CRUD screens. <br />
 *
 * Those screens must define the following components:
 * <ul>
 * <li>{@link #messagesContainer}: A {@link Component} to show the different
 * messages to users.</li>
 * <li>{@link #listWindow}: A {@link Window} where the list of elements is
 * shown.</li>
 * <li>{@link #editWindow}: A {@link Window} with creation/edition form.</li>
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public abstract class BaseCRUDController<T extends BaseEntity> extends
        GenericForwardComposer {

    private OnlyOneVisible visibility;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    protected Window listWindow;

    protected Window editWindow;

    private enum CRUCControllerState {
        LIST, CREATE, EDIT
    };

    private CRUCControllerState state = CRUCControllerState.LIST;

    /**
     * Call to super and do some extra stuff: <br />
     * <ul>
     * <li>Set "controller" variable to be used in .zul files.</li>
     * <li>Initialize {@link #messagesForUser}.</li>
     * <li>Show list view.</li>
     * </ul>
     *
     * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        messagesForUser = new MessagesForUser(messagesContainer);

        listWindow.setTitle(_("{0} List", getPluralEntityType()));
        showListWindow();
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow);
        }
        return visibility;
    }

    protected void showListWindow() {
        getVisibility().showOnly(listWindow);
    }

    /**
     * Show edit form with different title depending on controller state
     */
    protected void showEditWindow() {
        getVisibility().showOnly(editWindow);
        switch (state) {
            case CREATE:
                editWindow.setTitle(_("Create {0}", getEntityType()));
                break;
        case EDIT:
                editWindow.setTitle(_("Edit {0}", getEntityType()));
                break;
        default:
            throw new IllegalStateException(
                    "BaseCRUDController#goToEditForm or BaseCRUDController#goToCreateForm"
                            + " must be called first in order to use this method");
        }
    }

    /**
     * Returns the translated text to represent one entity type
     *
     * @return Text representing one entity
     */
    protected abstract String getEntityType();

    /**
     * Returns the translated text to represent multiple entity types
     *
     * @return Text representing several entities
     */
    protected abstract String getPluralEntityType();

    /**
     * Show list window and reload bindings there
     */
    public void goToList() {
        state = CRUCControllerState.LIST;
        showListWindow();
        Util.reloadBindings(listWindow);
    }

    /**
     * Show create form. Delegate in {@link #initCreate()} that should be
     * implemented in subclasses.
     */
    public void goToCreateForm() {
        state = CRUCControllerState.CREATE;
        initCreate();
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    /**
     * Performs needed operations to initialize the creation of a new entity.
     */
    protected abstract void initCreate();

    /**
     * Show edit form for entity passed as parameter. Delegate in
     * {@link #initEdit(entity)} that should be implemented in subclasses.
     *
     * @param entity
     *            Entity to be edited
     */
    public void goToEditForm(T entity) {
        state = CRUCControllerState.EDIT;
        initEdit(entity);
        showEditWindow();
        Util.reloadBindings(editWindow);
    }

    /**
     * Performs needed operations to initialize the edition of a new entity.
     *
     * @param entity
     *            Entity to be edited
     */
    protected abstract void initEdit(T entity);

    /**
     * Save current form and go to list view. Delegate in {@link #save()} that
     * should be implemented in subclasses.
     */
    public void saveAndExit() {
        try {
            save();
            messagesForUser.showMessage(Level.INFO,
                    _("{0} saved", getEntityType()));
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Save current form and continue in edition view. Delegate in
     * {@link #save()} that should be implemented in subclasses.
     */
    public void saveAndContinue() {
        try {
            save();
            messagesForUser.showMessage(Level.INFO,
                    _("{0} saved", getEntityType()));
            goToEditForm(getEntityBeingEdited());
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Performs actions to save current form
     *
     * @throws ValidationException
     *             If entity is not valid
     */
    protected abstract void save() throws ValidationException;

    /**
     * Returns entity being edited in the form
     *
     * @return Current entity being edited
     */
    protected abstract T getEntityBeingEdited();

    /**
     * Close form and go to list view. Delegate in {@link #cancel()} that should
     * be implemented in subclasses.
     */
    public void cancelForm() {
        cancel();
        goToList();
    }

    /**
     * Performs needed actions to cancel edition
     */
    protected abstract void cancel();

    /**
     * Shows a dialog asking for confirmation to user and if ok remove entity
     * passed as parameter. Delegate in {@link #delete(entity)} that should be
     * implemented in subclasses.
     *
     * @param entity
     *            Entity to be removed
     */
    public void confirmDelete(T entity) {
        try {
            if (Messagebox.show(
                    _("Delete {0}. Are you sure?", getEntityType()),
                    _("Confirm"), Messagebox.OK | Messagebox.CANCEL,
                    Messagebox.QUESTION) == Messagebox.OK) {
                delete(entity);
                Util.reloadBindings(listWindow);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs actions needed to remove entity passed as parameter
     *
     * @param entity
     *            Entity to be removed
     */
    protected abstract void delete(T entity);

}
