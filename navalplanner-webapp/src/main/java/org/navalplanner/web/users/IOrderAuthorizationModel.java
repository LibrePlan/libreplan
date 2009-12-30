package org.navalplanner.web.users;

import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.Profile;
import org.navalplanner.business.users.entities.ProfileOrderAuthorization;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserOrderAuthorization;

/**
 * Model for UI operations related to {@link OrderAuthorization}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public interface IOrderAuthorizationModel {

    void initSetOrder(Order order);

    void confirmSave();

    List<ProfileOrderAuthorization> getProfileOrderAuthorizations();

    List<UserOrderAuthorization> getUserOrderAuthorizations();

    /**
     * Adds {@link UserOrderAuthorization} objects in the model.
     *
     * @param user User object to receive the authorization
     * @param authorizations list of AuthorizationTypes
     * @return A list of the AuthorizationTypes which failed,
     * or null if all AuthorizationTypes were added successfully.
     */
    List<OrderAuthorizationType> addUserOrderAuthorization(
            User user, List<OrderAuthorizationType> authorizations);

    /**
     * Adds {@link ProfileOrderAuthorization} objects in the model.
     *
     * @param profile Profile object to receive the authorization
     * @param authorizations list of AuthorizationTypes
     * @return A list of the AuthorizationTypes which failed,
     * or null if all AuthorizationTypes were added successfully.
     */
    List<OrderAuthorizationType> addProfileOrderAuthorization(
            Profile profile, List<OrderAuthorizationType> authorizations);

    void removeOrderAuthorization(OrderAuthorization orderAuthorization);

}
