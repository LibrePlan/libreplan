package org.navalplanner.business.common;

import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Entities implementing this interface have a method <code>checkValid</code>.
 * This method validates the business rules of the entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IValidable {

    /**
     * Checks if an entity is or not valid.
     *
     * @throws ValidationException
     *             if entity is not valid.
     */
    void checkValid() throws ValidationException;

}
