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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Abstract class defining common behavior for controllers of CRUD screens. <br />
 *
 * Those screens must define the following components:
 * <ul>
 * <li>{@link Component} messagesContainer: A container to show the different
 * messages to users.</li>
 * <li>{@link Window} listWindow: A window where the list of elements is shown.</li>
 * <li>{@link Window} editWindow: A window with creation/edition form.</li>
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public abstract class BaseCRUDController extends GenericForwardComposer {

    private OnlyOneVisible visibility;

    protected IMessagesForUser messagesForUser;

    private Component messagesContainer;

    protected Window listWindow;

    protected Window editWindow;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
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
     * Show edit form with edition title
     */
    protected void showEditWindow() {
        showEditWindow(false);
    }

    /**
     * Show edit form with different title depending on parameter
     *
     * @param creation
     *            If true creation title is used
     */
    protected void showEditWindow(boolean creation) {
        getVisibility().showOnly(editWindow);
        if (creation) {
            editWindow.setTitle(_("Create {0}", getEntityType()));
        } else {
            editWindow.setTitle(_("Edit {0}", getEntityType()));
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

}
