/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resources.criterion;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.zkoss.zul.TreeModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface ICriterionTreeModel {

    TreeModel asTree();

    CriterionType getCriterionType();

    void addCriterion(String name);

    void addCriterionAt(CriterionDTO node,String name);

    void move(CriterionDTO toBeMoved, CriterionDTO destination,int position);

    void moveToRoot(CriterionDTO toBeMoved, int position);

    void removeNode(CriterionDTO node);

    void flattenTree();

    void thereIsOtherWithSameNameAndType(String name)throws ValidationException;

    void validateNameNotEmpty(String name)throws ValidationException;

    void updateEnabledCriterions(boolean isChecked);

    void updateEnabledCriterions(boolean isChecked,CriterionDTO criterion);

    void saveCriterions(CriterionType criterionType);

    void up(CriterionDTO node);

    void down(CriterionDTO node);

    void indent(CriterionDTO nodeToIndent);

    void unindent(CriterionDTO nodeToIndent);

    void regenerateCodeForUnsavedCriteria(int numberOfDigits);
}
