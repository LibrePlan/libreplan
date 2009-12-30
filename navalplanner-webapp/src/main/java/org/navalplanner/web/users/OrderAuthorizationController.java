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

import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;

/**
 * Controller for CRUD actions over an {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class OrderAuthorizationController extends GenericForwardComposer{

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("orderAuthorizationController", this, true);
    }

    public void setOrder(Order order) {
        // TODO implement

    }

    public void save() {
        // TODO implement

    }

    public List<ProfileOrderAuthorization> getProfileOrderAuthorizations() {
        // TODO implement
        return null;
    }

    public List<UserOrderAuthorization> getUserOrderAuthorizations() {
        // TODO implement
        return null;
    }

    public void addOrderAuthorization(Comboitem comboItem,
            boolean readAuthorization, boolean writeAuthorization) {
        // TODO implement

    }

    public void removeOrderAuthorization(OrderAuthorization orderAuthorization) {
        // TODO implement

    }
}
