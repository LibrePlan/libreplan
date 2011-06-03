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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.ResourceEnum;

/**
 * DAO for {@link CriterionTypeDAO} <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface ICriterionTypeDAO
    extends IIntegrationEntityDAO<CriterionType> {

    CriterionType findUniqueByName(String name)
            throws InstanceNotFoundException;

    CriterionType findUniqueByNameAnotherTransaction(String name)
        throws InstanceNotFoundException;

    CriterionType findUniqueByName(CriterionType criterionType)
            throws InstanceNotFoundException;

    CriterionType findByName(String name);

    public boolean existsOtherCriterionTypeByName(CriterionType criterionType);

    boolean existsByNameAnotherTransaction(CriterionType criterionType);

    boolean hasDiferentTypeSaved(Long id, ResourceEnum resource);

    public void removeByName(CriterionType criterionType);

    List<CriterionType> getCriterionTypes();

    List<CriterionType> getCriterionTypesByResources(
            Collection<ResourceEnum> resources);

    List<CriterionType> getSortedCriterionTypes();

    /**
     * Checks if exists the equivalent {@link CriterionType} on the DB for a
     * {@link CriterionType} created from a {@link PredefinedCriterionTypes}
     *
     * @param criterionType
     * @return
     */
    boolean existsPredefinedType(CriterionType criterionType);

    /**
     * Checks if exists any {@link Criteria} of the {@link CriterionType} has
     * been assigned to any @ Resource}
     * @param criterionType
     * @return
     */
    boolean checkChildrenAssignedToAnyResource(CriterionType criterionType);

    /**
     * Searches for the equivalent {@link CriterionType} on the DB for a
     * CriterionType created from a {@link PredefinedCriterionTypes}
     * @param predefinedCriterionType
     * @return <code>null</code> if there is no {@link CriterionType} for the
     *         predefinedCriterionType. Otherwise the equivalent
     *         {@link CriterionType} on DB
     */
    CriterionType findPredefined(CriterionType predefinedCriterionType);

}
