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

package org.navalplanner.web.resources.criterion;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * CriterionsModel contract <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsModel {

    List<CriterionType> getTypes();

    Collection<Criterion> getCriterionsFor(ICriterionType<?> type);

    Criterion getCriterion();

    void prepareForCreate(ICriterionType<?> criterionType);

    void workOn(Criterion criterion);

    ICriterionType<?> getTypeFor(Criterion criterion);

    void saveCriterion() throws ValidationException;

    boolean isEditing();

    boolean isApplyableToWorkers(Criterion criterion);

    <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass);

    List<Worker> getAllWorkers();

    boolean isChangeAssignmentsDisabled();

    void activateAll(Collection<? extends Resource> selected);

    void deactivateAll(Collection<? extends Resource> unSelectedWorkers);

    void save(Criterion criterion) throws ValidationException;

}
