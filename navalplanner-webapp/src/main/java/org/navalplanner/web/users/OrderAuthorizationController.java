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

package org.navalplanner.web.users;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.security.SecurityUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.api.Button;

/**
 * Controller for CRUD actions over an {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class OrderAuthorizationController extends GenericForwardComposer{

    private Component window;

    private IOrderAuthorizationModel orderAuthorizationModel;

    private IMessagesForUser messagesForUser;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderAuthorizationController", this, true);
        this.window = comp;
    }

    public void initCreate(Order order) {
        orderAuthorizationModel.initCreate(order);
        checkCreationPermissions();
        Util.reloadBindings(window);
    }

    public void initEdit(Order order) {
        orderAuthorizationModel.initEdit(order);
        checkWritePermissions();
        Util.reloadBindings(window);
    }

    public void save() {
        orderAuthorizationModel.confirmSave();
    }

    public List<ProfileOrderAuthorization> getProfileOrderAuthorizations() {
        return orderAuthorizationModel.getProfileOrderAuthorizations();
    }

    public List<UserOrderAuthorization> getUserOrderAuthorizations() {
        return orderAuthorizationModel.getUserOrderAuthorizations();
    }

    public void addOrderAuthorization(Comboitem comboItem,
            boolean readAuthorization, boolean writeAuthorization) {
        if(comboItem != null) {
            if(!readAuthorization && !writeAuthorization) {
                messagesForUser.showMessage(Level.WARNING,
                        _("No authorizations were added because you did not select any."));
                return;
            }
            List<OrderAuthorizationType> authorizations =
                new ArrayList<OrderAuthorizationType>();
            if(readAuthorization) {
                authorizations.add(OrderAuthorizationType.READ_AUTHORIZATION);
            }
            if(writeAuthorization) {
                authorizations.add(OrderAuthorizationType.WRITE_AUTHORIZATION);
            }
            if (comboItem.getValue() instanceof User) {
                List<OrderAuthorizationType> result =
                    orderAuthorizationModel.addUserOrderAuthorization(
                            (User)comboItem.getValue(), authorizations);
                if(result != null && result.size()==authorizations.size()) {
                    messagesForUser.showMessage(Level.WARNING,
                            _("Could not add those authorizations to user {0} " +
                                    "because they were already present.",
                                    ((User)comboItem.getValue()).getLoginName()));
                }
            }
            else if (comboItem.getValue() instanceof Profile) {
                List<OrderAuthorizationType> result =
                    orderAuthorizationModel.addProfileOrderAuthorization(
                            (Profile)comboItem.getValue(), authorizations);
                if(result != null && result.size()==authorizations.size()) {
                    messagesForUser.showMessage(Level.WARNING,
                            _("Could not add those authorizations to profile {0} " +
                                    "because they were already present.",
                                    ((Profile)comboItem.getValue()).getProfileName()));
                }
            }
        }
        Util.reloadBindings(window);
    }

    public void removeOrderAuthorization(OrderAuthorization orderAuthorization) {
        orderAuthorizationModel.removeOrderAuthorization(orderAuthorization);
        Util.reloadBindings(window);
    }

    public void setMessagesForUserComponent(IMessagesForUser component) {
        messagesForUser = component;
    }

    /**
     * Checks the creation permissions of the current user and enables/disables
     * the save buttons accordingly.
     */
    private void checkCreationPermissions() {
        if(SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            ((Button)window.getFellowIfAny("save")).setDisabled(false);
            ((Button)window.getFellowIfAny("save_and_continue")).setDisabled(false);
        }
        else {
            ((Button)window.getFellowIfAny("save")).setDisabled(true);
            ((Button)window.getFellowIfAny("save_and_continue")).setDisabled(true);
        }
    }

    /**
     * Checks the write permissions of the current user on this Order and enables/disables
     * the save buttons accordingly.
     */
    private void checkWritePermissions() {
        if(orderAuthorizationModel.userCanWrite(SecurityUtils.getSessionUserLoginName())) {
            ((Button)window.getFellowIfAny("save")).setDisabled(false);
            ((Button)window.getFellowIfAny("save_and_continue")).setDisabled(false);
        }
        else {
            ((Button)window.getFellowIfAny("save")).setDisabled(true);
            ((Button)window.getFellowIfAny("save_and_continue")).setDisabled(true);
        }
    }
}
