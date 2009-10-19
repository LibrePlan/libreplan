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

package org.navalplanner.web.resources.machine;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.resources.worker.CriterionSatisfactionDTO;
import org.zkoss.zk.ui.WrongValueException;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IAssignedMachineCriterionsModel {

    void addCriterionSatisfaction();

    public boolean checkNotAllowSimultaneousCriterionsPerResource(
            CriterionSatisfactionDTO satisfaction);

    public boolean checkSameCriterionAndSameInterval(
            CriterionSatisfactionDTO satisfaction);

    Set<CriterionSatisfactionDTO> getAllCriterionSatisfactions();

    List<CriterionWithItsType> getCriterionWithItsType();

    Set<CriterionSatisfactionDTO> getFilterCriterionSatisfactions();

    void prepareForEdit(Resource resource);

    void reattachmentResource();

    void remove(CriterionSatisfactionDTO criterionSatisfactionDTO);

    void save() throws ValidationException;

    public void setCriterionWithItsType(
            CriterionSatisfactionDTO criterionSatisfactionDTO,
            CriterionWithItsType criterionAndType) throws WrongValueException;

    void prepareForCreate(Resource resource);
}
