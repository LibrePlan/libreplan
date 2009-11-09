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

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewDataSortableGrid;
import org.navalplanner.web.resources.worker.CriterionSatisfactionDTO;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vbox;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AssignedCriterionRequirementToOrderElementController extends
        GenericForwardComposer {

    private IAssignedCriterionRequirementToOrderElementModel assignedCriterionRequirementToOrderElementModel;

    private Vbox vbox;

    private NewDataSortableGrid listingRequirements;

    private IMessagesForUser messages;

    private Component messagesContainer;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        if (messagesContainer == null)
            throw new RuntimeException(_("MessagesContainer is needed"));
        messages = new MessagesForUser(messagesContainer);
        comp.setVariable("assignedCriterionRequirementController", this, true);
        vbox = (Vbox) comp;
    }

    public OrderElement getOrderElement() {
        return assignedCriterionRequirementToOrderElementModel.getOrderElement();
    }

    public void setOrderElement(OrderElement orderElement) {
        assignedCriterionRequirementToOrderElementModel.setOrderElement(orderElement);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        assignedCriterionRequirementToOrderElementModel.setOrderModel(orderElementModel
                .getOrderModel());
        openWindow(orderElementModel.getOrderElement());
    }

    public void openWindow(OrderElement orderElement) {
        assignedCriterionRequirementToOrderElementModel.init(orderElement);
        Util.reloadBindings(vbox);
    }

    public boolean close() {
        try{
            assignedCriterionRequirementToOrderElementModel.confirm();
            return true;
        }catch (ValidationException e) {
            showInvalidValues(e);
        }
        return false;
    }

    public List<CriterionRequirementDTO> criterionRequirementDTOs(){
         return assignedCriterionRequirementToOrderElementModel.
                 getCriterionRequirementDTOs();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedCriterionRequirementToOrderElementModel.getCriterionWithItsTypes();
    }

    public void addCriterionRequirementDTO(){
        assignedCriterionRequirementToOrderElementModel.assignCriterionRequirementDTO();
        reload();
    }

    public void remove(CriterionRequirementDTO requirement){
        assignedCriterionRequirementToOrderElementModel.
                deleteCriterionRequirementDTO(requirement);
        reload();
    }

    public void invalidate(CriterionRequirementDTO requirement){
        assignedCriterionRequirementToOrderElementModel.
                setValidCriterionRequirementDTO(requirement,false);
        reload();
    }

    public void validate(CriterionRequirementDTO requirement){
        assignedCriterionRequirementToOrderElementModel.
                setValidCriterionRequirementDTO(requirement,true);
        reload();
    }

    public void selectCriterionAndType(Listitem item,Bandbox bandbox,
        CriterionRequirementDTO criterionRequirementDTO){
        if(item != null){
            try{
                criterionRequirementDTO = updateRetrievedCriterionRequirement(criterionRequirementDTO);
                CriterionWithItsType criterionAndType =
                        (CriterionWithItsType)item.getValue();
                bandbox.close();
                validateCriterionWithItsType(bandbox,
                        criterionAndType,criterionRequirementDTO);
                bandbox.setValue(criterionAndType.getNameAndType());
                criterionRequirementDTO.setCriterionWithItsType(criterionAndType);
            }catch(WrongValueException e){
                bandbox.setValue("");
                criterionRequirementDTO.setCriterionWithItsType(null);
                throw e;
            }
        }else{
            bandbox.setValue("");
        }
    }

    private CriterionRequirementDTO updateRetrievedCriterionRequirement(
            CriterionRequirementDTO requirementDTO){
            return assignedCriterionRequirementToOrderElementModel.
                    updateRetrievedCriterionRequirement(requirementDTO);

    }

    private void validateCriterionWithItsType(Bandbox bandbox,
            CriterionWithItsType criterionAndType,
            CriterionRequirementDTO requirementDTO)throws WrongValueException{
            if(!assignedCriterionRequirementToOrderElementModel.
                    canAddCriterionRequirement(requirementDTO,criterionAndType)){
                throw new WrongValueException(bandbox,
                _("The criterion " + criterionAndType.getNameAndType() + " is not valid," +
                        " exist the same criterion into the order element or into its children."));
            }
    }

    private void reload() {
        Util.reloadBindings(listingRequirements);
    }

    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if(value instanceof CriterionRequirementDTO){
                validateCriterionRequirementDTO(invalidValue,
                        (CriterionRequirementDTO)value);
            }
        }
    }

    /**
     * Validates {@link CriterionSatisfactionDTO} data constraints
     *
     * @param invalidValue
     */
    private void validateCriterionRequirementDTO(InvalidValue invalidValue,
            CriterionRequirementDTO requirementDTO) {
        if(listingRequirements != null){
            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionSatisfactionDTO(listingRequirements.getRows(),
                    requirementDTO);
            if (row != null) {
                String propertyName = invalidValue.getPropertyName();

                if (CriterionRequirementDTO.CRITERION_WITH_ITS_TYPE.equals(propertyName)) {
                    Bandbox bandType = getBandType(row);
                    bandType.setValue(null);
                    throw new WrongValueException(bandType,
                            _("The criterion and its type cannot be null"));
                }
            }
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in
     * rows
     *
     * @param Rows
     * @param CriterionSatisfactionDTO
     * @return
     */
    private Row findRowOfCriterionSatisfactionDTO(Rows rows,
            CriterionRequirementDTO requirementDTO) {
        List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (requirementDTO.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

     /**
     * Locates {@link Bandbox} criterion requirement in {@link row}
     *
     * @param row
     * @return Bandbox
     */
    private Bandbox getBandType(Row row) {
        return (Bandbox)((Hbox) row.getChildren().get(0))
                .getChildren().get(0);
    }
}