/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.orders;

import java.util.List;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.CriterionWithItsType;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAssignedCriterionRequirementToOrderElementModel {
    OrderElement getOrderElement();
    void setOrderElement(OrderElement orderElement);
    void init(OrderElement orderElement);
    void assignCriterionRequirementDTO();
    void deleteCriterionRequirementDTO(CriterionRequirementDTO requirement);
    void confirm()throws ValidationException;
    void setOrderModel(IOrderModel orderModel);
    List<CriterionRequirementDTO> getCriterionRequirementDTOs();
    List<CriterionWithItsType> getCriterionWithItsTypes();
    boolean canAddCriterionRequirement(CriterionRequirementDTO requirement,CriterionWithItsType criterionAndType);
    void setValidCriterionRequirementDTO(CriterionRequirementDTO requirement, boolean valid);
    CriterionRequirementDTO updateRetrievedCriterionRequirement(CriterionRequirementDTO requirementDTO);
}