package org.navalplanner.web.users;

import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
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

    void initSetNewOrder(Order order);

    void initSetExistingOrder(Order order);

    void confirmSave();

    List<ProfileOrderAuthorization> getProfileOrderAuthorizations();

    List<UserOrderAuthorization> getUserOrderAuthorizations();

    void addUserOrderAuthorization(User user, boolean readAuthorization,
            boolean writeAuthorization);

    void addProfileOrderAuthorization(Profile profile, boolean readAuthorization,
            boolean writeAuthorization);

    void removeOrderAuthorization(OrderAuthorization orderAuthorization);

}
