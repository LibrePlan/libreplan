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

package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.web.orders.CriterionRequirementDTO.FlagState;
import org.navalplanner.web.orders.CriterionRequirementDTO.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedCriterionRequirementToOrderElementModel  implements
        IAssignedCriterionRequirementToOrderElementModel{

    @Autowired
    IOrderElementDAO orderElementDAO;

    OrderElement orderElement;

    IOrderModel orderModel;

    private ClassValidator<CriterionRequirementDTO> requirementDTOValidator = new ClassValidator<CriterionRequirementDTO>(
            CriterionRequirementDTO.class);

    private List<CriterionWithItsType> criterionWithItsTypes =
            new ArrayList<CriterionWithItsType>();

    private List<CriterionRequirementDTO> criterionRequirementDTOs =
            new ArrayList<CriterionRequirementDTO>();

    @Override
    public OrderElement getOrderElement() {
        return orderElement;
    }

    @Override
    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public void init(OrderElement orderElement) {
        this.orderElement = orderElement;
        if(orderElement != null){
            reattachOrderElement();
            initializeDTOs();
            initializeCriterionWithItsType();
        }
    }

    private void reattachOrderElement() {
        orderElementDAO.reattach(orderElement);
        for(OrderElement child : orderElement.getAllChildren()){
            child.getName();
            reattachCriterionRequirement(child.getCriterionRequirements());
            if(child instanceof OrderLine){
                for(HoursGroup hoursGroup : child.getHoursGroups()){
                    hoursGroup.getWorkingHours();
                    reattachCriterionRequirement(hoursGroup.getCriterionRequirements());
                }
            }
        }
    }

    private void reattachCriterionRequirement(Set<CriterionRequirement> list){
        for(CriterionRequirement requirement : list){
            requirement.getCriterion().getName();
            requirement.getCriterion().getType().getName();
        }
    }

    private void initializeDTOs() {
        criterionRequirementDTOs = new ArrayList<CriterionRequirementDTO>();
        for(CriterionRequirement requirement :
            orderElement.getCriterionRequirements()){
                    CriterionRequirementDTO dto =
                            new CriterionRequirementDTO(requirement);
                    criterionRequirementDTOs.add(dto);
        }
    }

    private void  initializeCriterionWithItsType() {
        criterionWithItsTypes = new ArrayList<CriterionWithItsType>();
        for(CriterionType type : getTypes()){
            if(type.isEnabled()){
                for (Criterion criterion : orderModel.getCriterionsFor(type)) {
                    if(criterion.isActive()){
                        CriterionWithItsType criterionAndType =
                                new CriterionWithItsType(type,criterion);
                        criterionWithItsTypes.add(criterionAndType);
                    }
                }
            }
        }
    }

    private Set<CriterionType> getTypes(){
        return getMapCriterions().keySet();
    }

    private Map<CriterionType, List<Criterion>> getMapCriterions(){
        return orderModel.getMapCriterions();
    }

    @Override
    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return criterionWithItsTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public void assignCriterionRequirementDTO() {
        if((orderModel != null) && (orderElement != null)){
            CriterionRequirementDTO requirement = new CriterionRequirementDTO(Type.DIRECT);
            criterionRequirementDTOs.add(requirement);
        }
    }

    @Override
    public void deleteCriterionRequirementDTO(CriterionRequirementDTO requirement) {
        if(requirement.isOldObject()){
            requirement.setFlagState(FlagState.REMOVED);
        }else{
            criterionRequirementDTOs.remove(requirement);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void confirm() throws ValidationException{
        reattachOrderElement();
        validateDTOs();
        saveDTOs();
    }

    @Override
    public void setOrderModel(IOrderModel orderModel) {
        this.orderModel = orderModel;
    }

    @Override
    public List<CriterionRequirementDTO> getCriterionRequirementDTOs() {
        List<CriterionRequirementDTO> requirementDTOs =
                new ArrayList<CriterionRequirementDTO>();
        if((orderModel != null)&&(getOrderElement() != null)){
            for(CriterionRequirementDTO requirementDTO : criterionRequirementDTOs){
                if(!requirementDTO.getFlagState().equals(FlagState.REMOVED)){
                    requirementDTOs.add(requirementDTO);
                }
            }
        }
        return requirementDTOs;
    }

    @Override
    public CriterionRequirementDTO updateRetrievedCriterionRequirement(
            CriterionRequirementDTO requirementDTO){
        if(requirementDTO.getFlagState().equals(FlagState.RETRIEVED)){
            CriterionRequirementDTO newRequirement = new CriterionRequirementDTO(Type.DIRECT);
            criterionRequirementDTOs.add(newRequirement);
            requirementDTO.setFlagState(FlagState.REMOVED);
            return newRequirement;
        }
        return requirementDTO;
    }

    @Override
    public boolean canAddCriterionRequirement(CriterionRequirementDTO requirementDTO,
            CriterionWithItsType criterionAndType){
        CriterionRequirementDTO removedDTO = findRemovedRequirementDTOWithSameCriterion(
                criterionAndType.getCriterion());
        if(removedDTO != null){
            deleteCriterionRequirementDTO(requirementDTO);
            removedDTO.setFlagState(FlagState.RETRIEVED);
            return true;
        }else{
            return canAddCriterionRequirement(criterionAndType);
        }
    }

    private CriterionRequirementDTO findRemovedRequirementDTOWithSameCriterion(
            Criterion criterion){
        for(CriterionRequirementDTO removedDTO : this.criterionRequirementDTOs){
            if(removedDTO.getFlagState().equals(FlagState.REMOVED)){
                Criterion removedCriterion = removedDTO.
                        getCriterionWithItsType().getCriterion();
                if(criterion.getId().equals(removedCriterion.getId()))
                    return removedDTO;
            }
        }
        return null;
    }

    private boolean canAddCriterionRequirement(CriterionWithItsType criterionAndType){
        if(orderElement != null){
            return (!existSameCriterionRequirementInDTOs(criterionAndType) &&
                    (canAddCriterionRequirementInOrderElement(criterionAndType)));
        }
        return true;
    }

    private boolean existSameCriterionRequirementInDTOs(
            CriterionWithItsType newCriterionAndType){
        for(CriterionRequirementDTO requirementDTO : criterionRequirementDTOs){
            CriterionWithItsType criterionAndType = requirementDTO.getCriterionWithItsType();
            if((criterionAndType != null) &&
                    (criterionAndType.getCriterion().equals(newCriterionAndType.getCriterion()))){
                    return true;
            }
        }
        return false;
    }

    private boolean canAddCriterionRequirementInOrderElement(
            CriterionWithItsType newCriterionAndType){
        return orderElement.canAddCriterionRequirement(DirectCriterionRequirement.
                create(newCriterionAndType.getCriterion()));
    }

    @Override
    public void setValidCriterionRequirementDTO(
            CriterionRequirementDTO requirement,boolean valid){
        requirement.setValid(valid);
    }

    private void validateDTOs() throws ValidationException{
        Set<CriterionRequirementDTO> listDTOs =
                new HashSet<CriterionRequirementDTO>(criterionRequirementDTOs);
        for(CriterionRequirementDTO requirementDTO : listDTOs){
            InvalidValue[] invalidValues;
            invalidValues = requirementDTOValidator.getInvalidValues(requirementDTO);
            if (invalidValues.length > 0){
                throw new ValidationException(invalidValues);
            }
        }
    }

    private void saveDTOs(){
        updateRemoved();
        for(CriterionRequirementDTO requirementDTO : this.criterionRequirementDTOs){
            if(requirementDTO.isNewObject()){
                Criterion criterion = requirementDTO.getCriterionWithItsType().getCriterion();
                CriterionRequirement requirement = DirectCriterionRequirement.create(criterion);
                orderElement.addDirectCriterionRequirement(requirement);
            }else if(requirementDTO._getType().equals(Type.INDIRECT)){
                boolean valid = requirementDTO.isValid();
                CriterionRequirement requirement = requirementDTO.getCriterionRequirement();
                orderElement.setValidCriterionRequirement((IndirectCriterionRequirement)requirement,valid);
            }
        }

    }

    private void updateRemoved(){
        for(CriterionRequirementDTO requirementDTO : criterionRequirementDTOs){
            if(requirementDTO.getFlagState().equals(FlagState.REMOVED)){
                orderElement.removeDirectCriterionRequirement(
                    (DirectCriterionRequirement)requirementDTO.getCriterionRequirement());
            }
        }
    }
}