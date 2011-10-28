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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.libreplan.web.resources.worker;

import java.util.List;
import java.util.Set;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.IIntegrationEntityModel;
import org.zkoss.zk.ui.WrongValueException;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAssignedCriterionsModel extends IIntegrationEntityModel {

    Set<CriterionSatisfactionDTO> getAllCriterionSatisfactions();

    Set<CriterionSatisfactionDTO> getFilterCriterionSatisfactions();

    void prepareForEdit(Worker worker);

    void prepareForCreate(Worker worker);

    void addCriterionSatisfaction();

    void reattachmentWorker();

    void confirm()throws ValidationException;

    void validate()throws ValidationException;

    void remove(CriterionSatisfactionDTO criterionSatisfactionDTO);

    List<CriterionWithItsType> getCriterionWithItsType();

    public void setCriterionWithItsType(CriterionSatisfactionDTO criterionSatisfactionDTO,
            CriterionWithItsType criterionAndType) throws WrongValueException;

    public boolean checkSameCriterionAndSameInterval(CriterionSatisfactionDTO satisfaction);

    public boolean checkNotAllowSimultaneousCriterionsPerResource(
            CriterionSatisfactionDTO satisfaction);

    List<CriterionWithItsType> getCriterionWorkersWithItsType();
}
