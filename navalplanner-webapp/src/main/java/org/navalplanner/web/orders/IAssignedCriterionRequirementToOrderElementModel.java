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

    void assignCriterionRequirementWrapper();

    void deleteCriterionRequirementWrapper(
            CriterionRequirementWrapper requirement);

    void confirm()throws ValidationException;

    void setOrderModel(IOrderModel orderModel);

    List<CriterionRequirementWrapper> getCriterionRequirementWrappers();

    List<CriterionWithItsType> getCriterionWithItsTypes();

    boolean canSetCriterionWithItsType(
            CriterionRequirementWrapper requirementWrapper,
            CriterionWithItsType criterionAndType);

    CriterionRequirementWrapper validateWrappers(
            List<CriterionRequirementWrapper> list);

    CriterionRequirementWrapper validateHoursGroupWrappers();

    void setValidCriterionRequirementWrapper(
            CriterionRequirementWrapper requirement, boolean valid);

    // Operations to manage the hours Groups
    public void addNewHoursGroupWrapper();

    List<HoursGroupWrapper> getHoursGroupsWrappers();

    void deleteHoursGroupWrapper(HoursGroupWrapper hoursGroupWrapper);

    void addCriterionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper);

    CriterionRequirementWrapper addExceptionToHoursGroupWrapper(
            HoursGroupWrapper hoursGroupWrapper);

    boolean selectCriterionToHoursGroup(
            HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper exception,
            CriterionWithItsType criterionAndType);

    void deleteCriterionToHoursGroup(HoursGroupWrapper hoursGroupWrapper,
            CriterionRequirementWrapper requirementWrapper);

    void updateHoursGroup();

}