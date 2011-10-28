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

package org.libreplan.web.resources.criterion;

import java.util.Collection;
import java.util.List;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.IIntegrationEntityModel;

/**
 * CriterionsModel contract <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsModel extends IIntegrationEntityModel {

    boolean getAllowHierarchy();

    List<Worker> getAllWorkers();

    Criterion getCriterion();

    Collection<Criterion> getCriterionsFor(ICriterionType<?> type);

    ICriterionTreeModel getCriterionTreeModel();

    ICriterionType<?> getCriterionType();

    <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass);

    ICriterionType<?> getTypeFor(Criterion criterion);

    List<CriterionType> getTypes();

    boolean isApplyableToWorkers(Criterion criterion);

    boolean isEditing();

    void prepareForCreate();

    void prepareForCreate(CriterionType criterionType);

    public void prepareForEdit(CriterionType criterionType);

    /**
     * Reloads {@link CriterionType} from DB and all its criterions
     * This method should be call after saveAndContinue() from controller to
     * synchronize what has been committed to DB after saving and the model
     */
    void reloadCriterionType();

    public void confirmRemove(CriterionType criterionType);

    void saveCriterionType() throws ValidationException;

    int numberOfRelatedEntities(Criterion criterion);

    boolean isDeletable(Criterion criterion);

    boolean canRemove(CriterionType criterionType);

    void addForRemoval(Criterion criterion);
}
