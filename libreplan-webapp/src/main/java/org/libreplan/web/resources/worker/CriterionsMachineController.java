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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.libreplan.web.resources.worker;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.common.InvalidInputsChecker.isInvalid;
import static org.libreplan.web.common.InvalidInputsChecker.thereAreInvalidInputsOn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.resources.machine.IAssignedMachineCriterionsModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public class CriterionsMachineController extends GenericForwardComposer {

    private IAssignedMachineCriterionsModel assignedMachineCriterionsModel;

    private Grid listingCriterions;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Checkbox criterionFilterCheckbox;

    public CriterionsMachineController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedCriterionsController", this, true);
        messages = new MessagesForUser(messagesContainer);
    }

    public void prepareForEdit(Machine machine) {
        machine.getConfigurationUnits();
        assignedMachineCriterionsModel.prepareForEdit(machine);
        reload();
    }

    public void prepareForCreate(Machine machine) {
        assignedMachineCriterionsModel.prepareForCreate(machine);
    }

    public List<CriterionSatisfactionDTO> getCriterionSatisfactionDTOs() {
        List<CriterionSatisfactionDTO> list = new ArrayList<CriterionSatisfactionDTO>();
        if (criterionFilterCheckbox.isChecked()) {
            list.addAll(assignedMachineCriterionsModel
                    .getFilterCriterionSatisfactions());
        } else {
            list.addAll(assignedMachineCriterionsModel
                    .getAllCriterionSatisfactions());
        }
        return list;
    }

    public void addCriterionSatisfaction() {
        assignedMachineCriterionsModel.addCriterionSatisfaction();
        reload();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedMachineCriterionsModel.getCriterionWithItsType();
    }

    public List<CriterionWithItsType> getCriterionWorkersWithItsTypes() {
        // othermodel
        return assignedMachineCriterionsModel.getCriterionWorkersWithItsType();
    }

    public void reload() {
        Util.reloadBindings(listingCriterions);
        forceSortGridSatisfaction();
    }

    public void forceSortGridSatisfaction() {
        Column column = (Column) listingCriterions.getColumns().getFirstChild();
        ListModelExt model = (ListModelExt) listingCriterions.getModel();
        if ("ascending".equals(column.getSortDirection())) {
            model.sort(column.getSortAscending(), true);
        }
        if ("descending".equals(column.getSortDirection())) {
            model.sort(column.getSortDescending(), false);
        }
    }

    public void remove(CriterionSatisfactionDTO criterionSatisfactionDTO){
        assignedMachineCriterionsModel.remove(criterionSatisfactionDTO);
        reload();
    }

    public void selectCriterionAndType(Listitem item, Bandbox bandbox,
        CriterionSatisfactionDTO criterionSatisfactionDTO){
        if(item != null){
            CriterionWithItsType criterionAndType = (CriterionWithItsType)item.getValue();
            bandbox.setValue(criterionAndType.getNameAndType());
            setCriterionWithItsType(criterionAndType,criterionSatisfactionDTO,bandbox);
        } else {
            bandbox.setValue("");
        }
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionAndType,
            CriterionSatisfactionDTO satisfaction,Bandbox bandbox)  throws WrongValueException{
        this.assignedMachineCriterionsModel.setCriterionWithItsType(
                satisfaction, criterionAndType);
            validateCriterionWithItsType(satisfaction,bandbox);
    }

    private void validateCriterionWithItsType(CriterionSatisfactionDTO satisfaction,
            Component comp) throws WrongValueException{
            if(satisfaction.getCriterionWithItsType() == null) {
                return;
            }
            if(satisfaction.getStartDate() == null) {
                return;
            }
        if (assignedMachineCriterionsModel
                .checkSameCriterionAndSameInterval(satisfaction)) {
                throw new WrongValueException(comp,
                                        _("Criterion is not valid, the criterion overlap other criterionSatisfaction whith same criterion"));
            }
        if (assignedMachineCriterionsModel
                .checkNotAllowSimultaneousCriterionsPerResource(satisfaction)) {
                throw new WrongValueException(comp,
                                        _("CriterionType is not valid, the criterionType overlap other criterionSatisfaction whith same criterionType"));
            }
    }

    public void changeDate(Component comp) {
        CriterionSatisfactionDTO criterionSatisfactionDTO =
            (CriterionSatisfactionDTO)((Row) comp.getParent()).getValue();
        validateCriterionWithItsType(criterionSatisfactionDTO,comp);
        reload();
    }

    public Constraint validateEndDate(){
        return new Constraint() {
                        @Override
                        public void validate(Component comp, Object value)
                                throws WrongValueException {
                validateEndDate(comp, value);
                        }
                    };
    }

    private void validateEndDate(Component comp, Object value) {
        CriterionSatisfactionDTO criterionSatisfactionDTO = (CriterionSatisfactionDTO) ((Row) comp
                .getParent()).getValue();
        if (!criterionSatisfactionDTO.isGreaterStartDate((Date) value)) {
            throw new WrongValueException(
                    comp,
                    _("End date is not valid, the new end date must be greater than the start date"));
        } else if (!criterionSatisfactionDTO.isPostEndDate((Date) value)) {
            throw new WrongValueException(
                    comp,
                    _("End date is not valid, the new end date must be later the current end date"));
        }
    }

    public Constraint validateStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                validateStartDate(comp, value);
            }
        };
    }

    private void validateStartDate(Component comp, Object value) {
        CriterionSatisfactionDTO criterionSatisfactionDTO = (CriterionSatisfactionDTO) ((Row) comp
                .getParent()).getValue();
        if (value == null) {
            throw new WrongValueException(comp, _("Start date cannot be null"));
        }
        if (!criterionSatisfactionDTO.isLessToEndDate((Date) value)) {
            throw new WrongValueException(
                    comp,
                    _("Start date is not valid, the new start date must be lower than the end date"));
        } else if (!criterionSatisfactionDTO.isPreviousStartDate((Date) value)) {
            throw new WrongValueException(
                    comp,
                    _("Start date is not valid, the new start date must be previous the current start date"));
        }
    }

    public void save() throws ValidationException{
        assignedMachineCriterionsModel.save();
    }

    /**
     * Shows invalid values for {@link CriterionSatisfaction} entities
     *
     * @param e
     */
    public boolean validate() throws ValidationException {
        try {
            if (thereAreInvalidInputsOn(this.listingCriterions)) {
                showInvalidInputs();
                return false;
            }
            assignedMachineCriterionsModel.validate();
            reload();
        } catch (ValidationException e) {
            showInvalidValues(e);
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messages.showMessage(Level.ERROR, invalidValue
                        .getPropertyName()
                        + invalidValue.getMessage());
                return false;
            }
        } catch (IllegalStateException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Shows invalid inputs for {@link CriterionSatisfactionDTO} entities
     *
     * @param
     */
    private void showInvalidInputs() {
        if (listingCriterions != null) {
            Rows rows = listingCriterions.getRows();
            List<Row> listRows = rows.getChildren();
            for (Row row : listRows) {
                // Validate endDate Domain Restricctions.
                Datebox endDate = getEndDatebox(row);
                if (isInvalid(endDate)) {
                    validateEndDate(endDate, endDate.getValue());
                }
                // Validate startDate Domain Restricctions.
                Datebox startDate = getStartDatebox(row);
                if (isInvalid(startDate)) {
                    validateStartDate(startDate, startDate.getValue());
                }
                // Validate endDate Domain Restricctions.
                Bandbox bandCriterion = getBandType(row);
                if (isInvalid(bandCriterion)) {
                    CriterionSatisfactionDTO satisfactionDTO = (CriterionSatisfactionDTO) row
                            .getValue();
                    validateCriterionWithItsType(satisfactionDTO, bandCriterion);
                }
            }
        }
    }

    /**
     * Shows invalid values for {@link CriterionSatisfactionDTO} entities
     *
     * @param e
     */
    private void showInvalidValues(ValidationException e) {
        for (InvalidValue invalidValue : e.getInvalidValues()) {
            Object value = invalidValue.getBean();
            if (value instanceof CriterionSatisfactionDTO) {
                validateCriterionSatisfactionDTO(invalidValue,
                        (CriterionSatisfactionDTO) value);
            }
        }
    }

    /**
     * Validates {@link CriterionSatisfactionDTO} data constraints
     *
     * @param invalidValue
     */
    private void validateCriterionSatisfactionDTO(InvalidValue invalidValue,
            CriterionSatisfactionDTO satisfactionDTO) {
        if (listingCriterions != null) {

            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionSatisfactionDTO(listingCriterions
                    .getRows(), satisfactionDTO);

            if (row != null) {
                String propertyName = invalidValue.getPropertyName();

                if (CriterionSatisfactionDTO.START_DATE.equals(propertyName)) {
                    // Locate TextboxResource
                    Datebox startDate = getStartDatebox(row);
                    // Value is incorrect, clear
                    startDate.setValue(null);
                    throw new WrongValueException(startDate,
                            _("The start date cannot be null"));
                }
                if (CriterionSatisfactionDTO.CRITERION_WITH_ITS_TYPE
                        .equals(propertyName)) {
                    // Locate TextboxResource
                    Bandbox bandType = getBandType(row);
                    // Value is incorrect, clear
                    bandType.setValue(null);
                    throw new WrongValueException(bandType,
                            _("The criterion and its type cannot be null"));
                }
            }
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in rows
     *
     * @param Rows
     * @param CriterionSatisfactionDTO
     * @return
     */
    private Row findRowOfCriterionSatisfactionDTO(Rows rows,
            CriterionSatisfactionDTO satisfactionDTO) {
        List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (satisfactionDTO.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    /**
     * Locates {@link Datebox} criterion satisfaction in {@link row}
     *
     * @param row
     * @return
     */
    private Datebox getStartDatebox(Row row) {
        return (Datebox) (row.getChildren().get(1));
    }

    /**
     * Locates {@link Datebox} criterion satisfaction in {@link row}
     *
     * @param row
     * @return
     */
    private Datebox getEndDatebox(Row row) {
        return (Datebox) (row.getChildren().get(2));
    }

    /**
     * Locates {@link Bandbox} criterion satisfaction in {@link row}
     *
     * @param row
     * @return
     */
    private Bandbox getBandType(Row row) {
        return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren().get(0);
    }

    public void validateConstraints() {
        ConstraintChecker.isValid(self);
    }

    public void onChangingText(Event event) {
        Bandbox bd = (Bandbox) event.getTarget();
        final String inputText = ((InputEvent) event).getValue();
        Listbox listbox = (Listbox) bd.getFirstChild().getFirstChild();
        listbox.setModel(getSubModel(inputText));
        listbox.invalidate();
        bd.open();
    }

    public void onCtrlKey(Event event) {
        Bandbox bd = (Bandbox) event.getTarget();
        Listbox listbox = (Listbox) bd.getFirstChild().getFirstChild();
        List<Listitem> items = listbox.getItems();
        if (!items.isEmpty()) {
            listbox.setSelectedIndex(0);
            items.get(0).setFocus(true);
        }
    }

    private ListModel getSubModel(String text) {
        List<CriterionWithItsType> list = new ArrayList<CriterionWithItsType>();
        text = text.trim().toLowerCase();
        for (CriterionWithItsType criterion : this.getCriterionWithItsTypes()) {
            if ((criterion.getNameHierarchy().toLowerCase()
                    .contains(text) || criterion.getType().getName()
                    .toLowerCase().contains(text))) {
                list.add(criterion);
            }
        }
        return new SimpleListModel(list);
    }

    public void onOK(KeyEvent event) {
        Component listitem = event.getReference();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent()
                    .getParent();
            CriterionSatisfactionDTO criterionSatisfactionDTO = (CriterionSatisfactionDTO) ((Row) bandbox
                    .getParent().getParent()).getValue();

            selectCriterionAndType((Listitem) listitem, bandbox,
                    criterionSatisfactionDTO);

            bandbox.close();
        }
    }

    public void onClick(MouseEvent event) {
        Component listitem = event.getTarget();
        if (listitem instanceof Listitem) {
            Bandbox bandbox = (Bandbox) listitem.getParent().getParent()
                    .getParent();
            bandbox.close();
        }
    }

}
