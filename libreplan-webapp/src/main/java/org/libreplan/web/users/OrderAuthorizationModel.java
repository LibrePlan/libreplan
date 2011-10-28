/*
 * This file is part of LibrePlan
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

package org.libreplan.web.users;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.ProfileOrderAuthorization;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserOrderAuthorization;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderAuthorizationModel implements IOrderAuthorizationModel {

    private Order order;

    private List<ProfileOrderAuthorization> profileOrderAuthorizationList;

    private List<UserOrderAuthorization> userOrderAuthorizationList;

    private List<OrderAuthorization> orderAuthorizationRemovalList;

    @Autowired
    private IUserDAO userDAO;

    private PlanningState planningState;

    @Override
    public List<OrderAuthorizationType> addProfileOrderAuthorization(
            Profile profile, List<OrderAuthorizationType> authorizations) {
        List<OrderAuthorizationType> duplicated =
            new ArrayList<OrderAuthorizationType>();
        List<ProfileOrderAuthorization> existingAuthorizations =
            listAuthorizationsByProfile(profile);
        for(OrderAuthorizationType type : authorizations) {
            if(listContainsAuthorizationType(existingAuthorizations, type)) {
                duplicated.add(type);
            }
            else {
                ProfileOrderAuthorization orderAuthorization =
                    createProfileOrderAuthorization(order, profile);
                orderAuthorization.setAuthorizationType(type);
                profileOrderAuthorizationList.add(orderAuthorization);
                planningState.addOrderAuthorization(orderAuthorization);
            }
        }
        return duplicated.isEmpty()? null : duplicated;
    }

    @Override
    public List<OrderAuthorizationType> addUserOrderAuthorization(
            User user, List<OrderAuthorizationType> authorizations) {
        List<OrderAuthorizationType> duplicated =
            new ArrayList<OrderAuthorizationType>();
        List<UserOrderAuthorization> existingAuthorizations =
            listAuthorizationsByUser(user);
        for(OrderAuthorizationType type : authorizations) {
            if(listContainsAuthorizationType(existingAuthorizations, type)) {
                duplicated.add(type);
            }
            else {
                UserOrderAuthorization orderAuthorization =
                    createUserOrderAuthorization(order, user);
                orderAuthorization.setAuthorizationType(type);
                userOrderAuthorizationList.add(orderAuthorization);
                planningState.addOrderAuthorization(orderAuthorization);
            }
        }
        return duplicated.isEmpty()? null : duplicated;
    }

    @Override
    @Transactional
    public void confirmSave() {
        // Do nothing
    }

    @Override
    public List<ProfileOrderAuthorization> getProfileOrderAuthorizations() {
        return profileOrderAuthorizationList;
    }

    @Override
    public List<UserOrderAuthorization> getUserOrderAuthorizations() {
        return userOrderAuthorizationList;
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreate(PlanningState planningState) {
        this.planningState = planningState;
        this.order = planningState.getOrder();
        initializeLists();
        //add write authorization for current user
        try {
            User user = userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
            UserOrderAuthorization orderAuthorization =
                createUserOrderAuthorization(order, user);
            orderAuthorization.setAuthorizationType(OrderAuthorizationType.WRITE_AUTHORIZATION);
            userOrderAuthorizationList.add(orderAuthorization);
            planningState.addOrderAuthorization(orderAuthorization);
        }
        catch(InstanceNotFoundException e) {
            //this case shouldn't happen, because it would mean that there isn't a logged user
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(PlanningState planningState) {
        this.planningState = planningState;
        this.order = planningState.getOrder();
        initializeLists();
        //Retrieve the OrderAuthorizations associated with this order
        for (OrderAuthorization authorization : planningState
                .getOrderAuthorizations()) {
            if(authorization instanceof UserOrderAuthorization) {
                userOrderAuthorizationList.add(
                        (UserOrderAuthorization) authorization);
            }
            if(authorization instanceof ProfileOrderAuthorization) {
                profileOrderAuthorizationList.add(
                        (ProfileOrderAuthorization) authorization);
            }
        }
    }

    private void initializeLists() {
        profileOrderAuthorizationList =
            new ArrayList<ProfileOrderAuthorization>();
        userOrderAuthorizationList =
            new ArrayList<UserOrderAuthorization>();
        orderAuthorizationRemovalList =
            new ArrayList<OrderAuthorization>();
    }

    @Override
    public void removeOrderAuthorization(OrderAuthorization orderAuthorization) {
        if(orderAuthorization instanceof UserOrderAuthorization) {
            userOrderAuthorizationList.remove(
                    (UserOrderAuthorization) orderAuthorization);
        }
        if(orderAuthorization instanceof ProfileOrderAuthorization) {
            profileOrderAuthorizationList.remove(
                    (ProfileOrderAuthorization) orderAuthorization);
        }
        if(!orderAuthorization.isNewObject()) {
            orderAuthorizationRemovalList.add(orderAuthorization);
        }
        planningState.removeOrderAuthorization(orderAuthorization);
    }

    private ProfileOrderAuthorization createProfileOrderAuthorization(
            Order order, Profile profile) {
        ProfileOrderAuthorization orderAuthorization =
            ProfileOrderAuthorization.create();
        orderAuthorization.setOrder(order);
        orderAuthorization.setProfile(profile);
        return orderAuthorization;
    }

    private UserOrderAuthorization createUserOrderAuthorization(
            Order order, User user) {
        UserOrderAuthorization orderAuthorization =
            UserOrderAuthorization.create();
        orderAuthorization.setOrder(order);
        orderAuthorization.setUser(user);
        return orderAuthorization;
    }

    private List<UserOrderAuthorization> listAuthorizationsByUser(User user) {
        List<UserOrderAuthorization> list = new ArrayList<UserOrderAuthorization>();
        for(UserOrderAuthorization authorization : userOrderAuthorizationList) {
            if(authorization.getUser().getId().equals(user.getId())) {
                list.add(authorization);
            }
        }
        return list;
    }

    private List<ProfileOrderAuthorization> listAuthorizationsByProfile(Profile profile){
        List<ProfileOrderAuthorization> list = new ArrayList<ProfileOrderAuthorization>();
        for(ProfileOrderAuthorization authorization : profileOrderAuthorizationList) {
            if(authorization.getProfile().getId().equals(profile.getId())) {
                list.add(authorization);
            }
        }
        return list;
    }

    private boolean listContainsAuthorizationType(List<? extends OrderAuthorization> list,
            OrderAuthorizationType authorizationType) {
        for(OrderAuthorization authorization : list) {
            if(authorization.getAuthorizationType().equals(authorizationType)) {
                return true;
            }
        }
        return false;
    }
}
