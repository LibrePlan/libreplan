/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.INewObject;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.util.deepcopy.AfterCopy;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;


/**
 * Base class for all the application entities.
 *
 * It provides the basic behavior for id and version fields.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class BaseEntity implements INewObject {

    /**
     * Groups the entities by id. Entities with null id are also included.
     *
     * @param entities
     * @return entities grouped by id
     */
    public static <T extends BaseEntity> Map<Long, Set<T>> byId(
            Collection<? extends T> entities) {
        Map<Long, Set<T>> result = new HashMap<Long, Set<T>>();
        for (T each : entities) {
            if (!result.containsKey(each.getId())) {
                result.put(each.getId(), new HashSet<T>());
            }
            result.get(each.getId()).add(each);
        }
        return result;
    }

    private static final Log LOG = LogFactory.getLog(BaseEntity.class);

    @OnCopy(Strategy.IGNORE)
    private Long id;

    @OnCopy(Strategy.IGNORE)
    private Long version;

    @OnCopy(Strategy.IGNORE)
    private boolean newObject = false;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        if (isNewObject()) {
            return null;
        }

        return version;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    protected void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    @AfterCopy
    private void afterCopyIsANewObject() {
        setNewObject(true);
    }

    public boolean isNewObject() {
        return newObject;
    }

    protected static <T extends BaseEntity> T create(T baseEntity) {
        baseEntity.newObject = true;
        return baseEntity;
    }

    /**
     * Once the has been really saved in DB (not a readonly transaction), it
     * could be necessary to unmark the object as newObject. This is the case if
     * you must use the same instance after the transaction. <br />
     */
    public void dontPoseAsTransientObjectAnymore() {
        setNewObject(false);
    }

    @SuppressWarnings("unchecked")
    public void validate() throws ValidationException {
        NavalPlanClassValidator classValidator = new NavalPlanClassValidator(this.getClass());
        InvalidValue[] invalidValues = classValidator.getInvalidValues(this);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
    }

    @Override
    public String toString() {
        try {
            return super.toString() + getExtraInformation();
        } catch (Exception e) {
            final String message = "error doing toString";
            LOG.error(message, e);
            return message;
        }
    }

    public String getExtraInformation() {
        return "[ id: " + getId() + ", newObject: "
                + isNewObject() + "]";
    }

}
