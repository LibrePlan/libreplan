package org.navalplanner.web.users;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;
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

    @Autowired
    private IOrderAuthorizationDAO dao;

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    public List<OrderAuthorizationType> addProfileOrderAuthorization(
            Profile profile, List<OrderAuthorizationType> authorizations) {
        for(OrderAuthorizationType type : authorizations) {
            ProfileOrderAuthorization orderAuthorization =
                createProfileOrderAuthorization(order, profile);
            orderAuthorization.setAuthorizationType(type);
            profileOrderAuthorizationList.add(orderAuthorization);
        }
        return null;
    }

    @Override
    public List<OrderAuthorizationType> addUserOrderAuthorization(
            User user, List<OrderAuthorizationType> authorizations) {
        for(OrderAuthorizationType type : authorizations) {
            UserOrderAuthorization orderAuthorization =
                createUserOrderAuthorization(order, user);
            orderAuthorization.setAuthorizationType(type);
            userOrderAuthorizationList.add(orderAuthorization);
        }
        return null;
    }

    @Override
    @Transactional
    public void confirmSave() {
        try {
            if(order.isNewObject()) {
                //if it was new, we reload the order from the DAO
                Order newOrder = orderDAO.find(order.getId());
                replaceOrder(newOrder);
            }
        }catch (InstanceNotFoundException e) {
            InvalidValue invalidValue = new InvalidValue(_("Order does not exist"),
                    OrderAuthorization.class, "order", order, null);
            throw new ValidationException(invalidValue);
        }
        for(OrderAuthorization authorization : profileOrderAuthorizationList) {
            dao.save(authorization);
        }
        for(OrderAuthorization authorization : userOrderAuthorizationList) {
            dao.save(authorization);
        }
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
    public void initSetExistingOrder(Order order) {
        this.order = order;
        profileOrderAuthorizationList =
            new ArrayList<ProfileOrderAuthorization>();
        userOrderAuthorizationList =
            new ArrayList<UserOrderAuthorization>();

        //Retrieve the OrderAuthorizations associated with this order
        for(OrderAuthorization authorization : dao.listByOrder(order)) {
            forceLoadEntities(authorization);
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

    @Override
    public void initSetNewOrder(Order order) {
        this.order = order;
        profileOrderAuthorizationList =
            new ArrayList<ProfileOrderAuthorization>();
        userOrderAuthorizationList =
            new ArrayList<UserOrderAuthorization>();
    }

    private void forceLoadEntities(OrderAuthorization authorization) {
        authorization.getOrder().getName();
        if(authorization instanceof UserOrderAuthorization) {
            ((UserOrderAuthorization)authorization).getUser().getLoginName();
        }
        if(authorization instanceof ProfileOrderAuthorization) {
            ((ProfileOrderAuthorization)authorization).getProfile().getProfileName();
        }
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

    private void replaceOrder(Order newOrder) {
        for(OrderAuthorization authorization : profileOrderAuthorizationList) {
            authorization.setOrder(newOrder);
            dao.save(authorization);
        }
        for(OrderAuthorization authorization : userOrderAuthorizationList) {
            authorization.setOrder(newOrder);
            dao.save(authorization);
        }
        this.order = newOrder;
    }

}
