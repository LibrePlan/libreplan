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

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewDataSortableGrid;
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
        if (showInvalidValues()) {
            return false;
        }
        assignedCriterionRequirementToOrderElementModel.confirm();
        return true;
    }

    public List<CriterionRequirementWrapper> criterionRequirementWrappers() {
         return assignedCriterionRequirementToOrderElementModel.
getCriterionRequirementWrappers();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedCriterionRequirementToOrderElementModel.getCriterionWithItsTypes();
    }

    public void addCriterionRequirementWrapper() {
        assignedCriterionRequirementToOrderElementModel
                .assignCriterionRequirementWrapper();
        reload();
    }

    public void remove(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
deleteCriterionRequirementWrapper(requirement);
        reload();
    }

    public void invalidate(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
setValidCriterionRequirementWrapper(requirement, false);
        reload();
    }

    public void validate(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
setValidCriterionRequirementWrapper(requirement, true);
        reload();
    }

    public void selectCriterionAndType(Listitem item,Bandbox bandbox,
            CriterionRequirementWrapper criterionRequirementWrapper) {
        if(item != null){
            CriterionWithItsType criterionAndType = (CriterionWithItsType) item
                    .getValue();
            bandbox.close();
            bandbox.setValue(criterionAndType.getNameAndType());
            if (!assignedCriterionRequirementToOrderElementModel
                    .canSetCriterionWithItsType(criterionRequirementWrapper,
                            criterionAndType)) {
                bandbox.setValue("");
                criterionRequirementWrapper.setCriterionWithItsType(null);
                throw new WrongValueException(
                        bandbox,
                        _("The criterion "
                                + criterionAndType.getNameAndType()
                                + " is not valid,"
                                + " exist the same criterion into the order element or into its children."));
            }
        }else{
            bandbox.setValue("");
        }
    }

    private void reload() {
        Util.reloadBindings(listingRequirements);
    }

    private boolean showInvalidValues() {
        CriterionRequirementWrapper invalidWrapper = this.assignedCriterionRequirementToOrderElementModel
                .validateWrappers();
        if (invalidWrapper != null) {
            showInvalidValues(invalidWrapper);
            return true;
        }
        return false;
    }

    /**
     * Validates {@link CriterionRequirementWrapper} data constraints
     * @param invalidValue
     */
    private void showInvalidValues(
            CriterionRequirementWrapper requirementWrapper) {
        if(listingRequirements != null){
            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionRequirementWrapper(listingRequirements
                    .getRows(),
 requirementWrapper);
            if (row != null) {
                    Bandbox bandType = getBandType(row);
                    bandType.setValue(null);
                    throw new WrongValueException(bandType,
                            _("The criterion and its type cannot be null"));
            }
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in rows
     * @param Rows
     * @param CriterionRequirementWrapper
     * @return
     */
    private Row findRowOfCriterionRequirementWrapper(Rows rows,
            CriterionRequirementWrapper requirementWrapper) {
        List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (requirementWrapper.equals(row.getValue())) {
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