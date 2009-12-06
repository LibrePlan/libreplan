/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.worker;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zk.ui.WrongValueException;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAssignedCriterionsModel {

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
