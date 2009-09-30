/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.criterion;

import java.util.List;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.zkoss.zul.TreeModel;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface ICriterionTreeModel {

    public TreeModel asTree();

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
}
