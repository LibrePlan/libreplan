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

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.IHumanIdentifiable;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
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
public abstract class BaseCRUDController<T extends IHumanIdentifiable> extends
        GenericForwardComposer {

    private OnlyOneVisible visibility;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    protected Window listWindow;

    protected Window editWindow;

    public enum CRUDControllerState {
        LIST, CREATE, EDIT
    };

    protected CRUDControllerState state = CRUDControllerState.LIST;

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

    /**
     * Show list window and reload bindings
     */
    protected void showListWindow() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    /**
     * Show edit form with different title depending on controller state and
     * reload bindings
     */
    protected void showEditWindow() {
        getVisibility().showOnly(editWindow);
        updateWindowTitle();
        Util.reloadBindings(editWindow);
    }

    public final void updateWindowTitle() {
        T entityBeingEdited = getEntityBeingEdited();
        if (entityBeingEdited == null) {
            throw new IllegalStateException(
                    "You should be editing one entity in order to use this method");
        }

        String humanId = entityBeingEdited.getHumanId();
        switch (state) {
        case CREATE:
            if (StringUtils.isEmpty(humanId)) {
                editWindow.setTitle(_("Create {0}", getEntityType()));
            } else {
                editWindow.setTitle(_("Create {0}: {1}", getEntityType(),
                        humanId));
            }
            break;
        case EDIT:
            editWindow.setTitle(_("Edit {0}: {1}", getEntityType(), humanId));
            break;
        default:
            throw new IllegalStateException(
                    "You should be in creation or edition mode to use this method");
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
    public final void goToList() {
        state = CRUDControllerState.LIST;
        showListWindow();
    }

    /**
     * Show create form. Delegate in {@link #initCreate()} that should be
     * implemented in subclasses.
     */
    public final void goToCreateForm() {
        state = CRUDControllerState.CREATE;
        initCreate();
        showEditWindow();
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
    public final void goToEditForm(T entity) {
        state = CRUDControllerState.EDIT;
        initEdit(entity);
        showEditWindow();
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
    public final void saveAndExit() {
        try {
            saveCommonActions();
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Common save actions:<br />
     * <ul>
     * <li>Delegate in {@link #beforeSaving()} that could be implemented if
     * needed in subclasses.</li>
     * <li>Use {@link ConstraintChecker} to validate form.</li>
     * <li>Delegate in {@link #save()} that should be implemented in subclasses.
     * </li>
     * <li>Show message to user.</li>
     * </ul>
     *
     * @throws ValidationException
     *             If form is not valid or save has any validation problem
     */
    private void saveCommonActions() throws ValidationException {
        beforeSaving();

        save();

        messagesForUser.showMessage(
                Level.INFO,
                _("{0} \"{1}\" saved", getEntityType(), getEntityBeingEdited()
                        .getHumanId()));
    }

    /**
     * Save current form and continue in edition view. Delegate in
     * {@link #save()} that should be implemented in subclasses.
     */
    public final void saveAndContinue() {
        try {
            saveCommonActions();
            goToEditForm(getEntityBeingEdited());
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Performs additional operations before saving (usually do some checks or
     * generate codes of related entities).
     *
     * Default behavior use {@link ConstraintChecker} to see if
     * {@link #editWindow} is valid, however it could be overridden if needed.
     */
    protected void beforeSaving() throws ValidationException {
        ConstraintChecker.isValid(editWindow);
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
     * Close form and go to list view. Delegate in {@link #cancel()} that could
     * be implemented in subclasses if needed.
     */
    public final void cancelForm() {
        cancel();
        goToList();
    }

    /**
     * Performs needed actions to cancel edition
     *
     * Default behavior do nothing, however it could be overridden if needed.
     */
    protected void cancel() {
        // Do nothing
    }

    /**
     * First call {@link #beforeDeleting(entity)} in order to perform some
     * checkings before trying to delete if needed. Then show a dialog asking
     * for confirmation to user and if ok remove entity passed as parameter.
     * Delegate in {@link #delete(entity)} that should be implemented in
     * subclasses.
     *
     * @param entity
     *            Entity to be removed
     */
    public final void confirmDelete(T entity) {
        if (!beforeDeleting(entity)) {
            return;
        }

        try {
            if (Messagebox.show(
                    _("Delete {0} \"{1}\". Are you sure?", getEntityType(),
                            entity.getHumanId()),
                    _("Confirm"), Messagebox.OK | Messagebox.CANCEL,
                    Messagebox.QUESTION) == Messagebox.OK) {
                delete(entity);
                messagesForUser.showMessage(
                        Level.INFO,
                        _("{0} \"{1}\" deleted", getEntityType(),
                                entity.getHumanId()));
                Util.reloadBindings(listWindow);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (InstanceNotFoundException ie) {
            messagesForUser.showMessage(
                    Level.ERROR,
                    _("{0} \"{1}\" could not be deleted, it was already removed", getEntityType(),
                            entity.getHumanId()));
        }
    }

    /**
     * Performs additional operations before deleting (usually check some wrong
     * conditions before deleting).
     *
     * Default behavior do nothing, however it could be overridden if needed.
     *
     * @param entity
     *            Entity to be removed
     * @return Return true if deletion can carry on
     */
    protected boolean beforeDeleting(T entity) {
        // Do nothing
        return true;
    }

    /**
     * Performs actions needed to remove entity passed as parameter
     *
     * @param entity
     *            Entity to be removed
     */
    protected abstract void delete(T entity) throws InstanceNotFoundException;

}
