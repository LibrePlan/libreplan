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

package org.navalplanner.business.resources.daos;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;
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

    List<CriterionType> findByName(CriterionType criterionType);

    public boolean existsOtherCriterionTypeByName(CriterionType criterionType);

    boolean existsByNameAnotherTransaction(CriterionType criterionType);

    public void removeByName(CriterionType criterionType);

    List<CriterionType> getCriterionTypes();

    List<CriterionType> getCriterionTypesByResources(
            Collection<ResourceEnum> resources);

}
