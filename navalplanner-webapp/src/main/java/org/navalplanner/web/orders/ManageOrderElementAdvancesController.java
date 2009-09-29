package org.navalplanner.web.orders;


import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;
import org.zkoss.zul.XYModel;

/**
 * Controller for show the advances of the selected order element<br />
 * @author Susana Montes Pedreria <smontes@wirelessgalicia.com>
 */

public class ManageOrderElementAdvancesController extends
        GenericForwardComposer {

    private IMessagesForUser messagesForUser;

    private Window window;

    private int indexSelectedItem = -1;

    private IManageOrderElementAdvancesModel manageOrderElementAdvancesModel;

    private AdvanceTypeListRenderer advanceTypeListRenderer = new AdvanceTypeListRenderer();

    private AdvanceMeasurementRenderer advanceMeasurementRenderer = new AdvanceMeasurementRenderer();

    private Set<AdvanceAssignment> selectedAdvances = new HashSet<AdvanceAssignment>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp.getFellow("listOrderElementAdvances"));
        comp.setVariable("manageOrderElementAdvancesController", this, true);
        window = (Window) comp;
        messagesForUser = new MessagesForUser(window
                .getFellow("messagesContainerAdvances"));
    }

    public List<AdvanceMeasurement> getAdvanceMeasurements() {
        return manageOrderElementAdvancesModel.getAdvanceMeasurements();
    }

    public List<AdvanceAssignment> getAdvanceAssignments() {
        return manageOrderElementAdvancesModel.getAdvanceAssignments();
    }

    public void cancel() {
        window.setVisible(false);
        Util.reloadBindings(window.getParent());
    }

    public void accept() throws org.navalplanner.business.common.exceptions.InstanceNotFoundException, DuplicateValueTrueReportGlobalAdvanceException {
        if(!validateDataForm()){
               messagesForUser.showMessage(
            Level.INFO, _("Values are not valid, the values must not be null"));
            return;
        }
        if(!validateReportGlobalAdvance()){
               messagesForUser.showMessage(
            Level.INFO, _("The Spread values are not valid, must be exist at least one value of spread to true"));
            return;
        }
        try{
            this.manageOrderElementAdvancesModel.accept();
            window.setVisible(false);
            Util.reloadBindings(window.getParent());
        }catch(DuplicateAdvanceAssignmentForOrderElementException e){
            messagesForUser.showMessage( Level.INFO, _("It not must be include Advance with the same advance type."));
            return;
        }catch(DuplicateValueTrueReportGlobalAdvanceException e){
            messagesForUser.showMessage(
            Level.INFO, _("The Spread values are not valid, There are several spread values to true"));
            return;
        }

    }

    public void openWindow(IOrderElementModel orderElementModel) {
        this.manageOrderElementAdvancesModel.init(orderElementModel
                .getOrderElement());
        this.indexSelectedItem = -1;
        selectedAdvances.clear();
        Util.reloadBindings(window);
    }

    public void prepareEditAdvanceMeasurements(AdvanceAssignment advanceAssignment) {
        manageOrderElementAdvancesModel
                .prepareEditAdvanceMeasurements(advanceAssignment);
        Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
        this.indexSelectedItem = listAdvances.getIndexOfItem(listAdvances.getSelectedItem());
        Util.reloadBindings(window);
    }

    public void goToCreateLineAdvanceAssignment() {
        manageOrderElementAdvancesModel.addNewLineAdvaceAssignment();
        manageOrderElementAdvancesModel.prepareEditAdvanceMeasurements(null);
        this.indexSelectedItem = -1;
        Util.reloadBindings(window);
    }

    public void goToCreateLineAdvanceMeasurement() {
        manageOrderElementAdvancesModel.addNewLineAdvaceMeasurement();
        Util.reloadBindings(window);
    }

    public void goToRemoveLineAdvanceAssignment(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        Listitem listItem = listAdvances.getItemAtIndex(indexSelectedItem);
        if(listItem != null){

            AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                    .getValue();
            manageOrderElementAdvancesModel
                    .removeLineAdvanceAssignment(advanceAssignment);
            Util.reloadBindings(window);
        }
    }

    public void goToRemoveLineAdvanceMeasurement(){
        Listbox listAdvancesMeasurement = (Listbox)window.getFellow("editAdvancesMeasurement");
        Listitem selectedItem = listAdvancesMeasurement.getSelectedItem();
        if(selectedItem != null){
            AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) selectedItem
                    .getValue();
            if (advanceMeasurement != null) {
                manageOrderElementAdvancesModel
                        .removeLineAdvanceMeasurement(advanceMeasurement);
                Util.reloadBindings(window);
            }
        }
    }

    public String getInfoAdvance() {
        String infoAdvanceAssignment = manageOrderElementAdvancesModel
                .getInfoAdvanceAssignment();
        if (infoAdvanceAssignment.isEmpty()) {
            return _("Advance measurements");
        }

        return _("Advance measurements: ") + infoAdvanceAssignment;
    }

    public boolean isReadOnlyAdvanceMeasurements() {
       return manageOrderElementAdvancesModel.isReadOnlyAdvanceMeasurements();
    }

    public AdvanceTypeListRenderer getAdvancesRenderer() {
        return advanceTypeListRenderer;
    }

    public void updatesValue(final Decimalbox item){
        this.setPercentage();
        this.setCurrentValue();
    }

    public class AdvanceTypeListRenderer implements ListitemRenderer {
         @Override
         public void render(Listitem listItem, Object data) throws Exception {
            final AdvanceAssignment advance = (AdvanceAssignment) data;
            listItem.setValue(advance);
            listItem.setDraggable("true");
            listItem.setDroppable("true");

            if ((advance instanceof DirectAdvanceAssignment)
                    && ((DirectAdvanceAssignment) advance)
                            .getAdvanceMeasurements().isEmpty()) {
                appendComboboxAdvancType(listItem);
            } else {
                appendLabelAdvanceType(listItem);
            }
            appendDecimalBoxMaxValue(listItem);
            appendDecimalBoxValue(listItem);
            appendLabelPercentage(listItem);
            appendDateBoxDate(listItem);
            appendRadioSpread(listItem);
            appendCalculatedCheckbox(listItem);
            appendChartCheckbox(listItem);
            appendRemoveButton(listItem);
        }
    }

    private void appendComboboxAdvancType(final Listitem listItem){
        final DirectAdvanceAssignment advance = (DirectAdvanceAssignment) listItem
                .getValue();
        final Combobox comboAdvanceTypes = new Combobox();
        final List<AdvanceType> listAdvanceType = manageOrderElementAdvancesModel
                .getPossibleAdvanceTypes(advance);
        for(AdvanceType advanceType : listAdvanceType){
            if (!advanceType.getUnitName().equals(
                    PredefinedAdvancedTypes.CHILDREN.getTypeName())) {
                Comboitem comboItem = new Comboitem();
                comboItem.setValue(advanceType);
                comboItem.setLabel(advanceType.getUnitName());
                comboItem.setParent(comboAdvanceTypes);

                if ((advance.getAdvanceType() != null)
                    && (advance.getAdvanceType().getId().equals(advanceType
                            .getId())))
                    comboAdvanceTypes.setSelectedItem(comboItem);
            }
        }
        comboAdvanceTypes.addEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        setMaxValue(listItem,comboAdvanceTypes);
                        cleanFields();
                        setPercentage();
                    }
        });

        Util.bind(comboAdvanceTypes,
                    new Util.Getter<Comboitem>() {
                        @Override
                        public Comboitem get(){
                                return comboAdvanceTypes.getSelectedItem();
                        }
                    }, new Util.Setter<Comboitem>() {
                @Override
            public void set(Comboitem comboItem) {
                            if(((comboItem!=null))&&(comboItem.getValue() != null)&&
                                    (comboItem.getValue() instanceof AdvanceType)){
                                AdvanceType advanceType = (AdvanceType)comboItem.getValue();
                                advance.setAdvanceType(advanceType);
                    advance.setMaxValue(advanceType.getDefaultMaxValue());
                            }
                        }
                    });
        Listcell listCell = new Listcell();
        listCell.appendChild(comboAdvanceTypes);
        listItem.appendChild(listCell);
    }

    private void appendLabelAdvanceType(final Listitem listItem){
        final AdvanceAssignment advance = (AdvanceAssignment) listItem.getValue();
        Label unitName = new Label(advance.getAdvanceType().getUnitName());
        Listcell listCell = new Listcell();
        listCell.appendChild(unitName);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxMaxValue(final Listitem listItem){
        AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                .getValue();
        Decimalbox maxValue = new Decimalbox();
        maxValue.setScale(2);

        final DirectAdvanceAssignment directAdvanceAssignment;
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            maxValue.setDisabled(true);
            directAdvanceAssignment = manageOrderElementAdvancesModel
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
        } else {
            directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
        }

        Util.bind(maxValue, new Util.Getter<BigDecimal>() {
            @Override
            public BigDecimal get() {
                return directAdvanceAssignment.getMaxValue();
            }
        }, new Util.Setter<BigDecimal>() {

            @Override
            public void set(BigDecimal value) {
                directAdvanceAssignment.setMaxValue(value);
            }
        });
        maxValue.addEventListener(Events.ON_CHANGE,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        setPercentage();
                        Util.reloadBindings(window.getFellow("infoAdvance"));
                    }
                });

        Listcell listCell = new Listcell();
        listCell.appendChild(maxValue);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxValue(final Listitem listItem){
        final AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                .getValue();
        Decimalbox value = new Decimalbox();
        value.setScale(2);
        value.setDisabled(true);

        DirectAdvanceAssignment directAdvanceAssignment;
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            directAdvanceAssignment = manageOrderElementAdvancesModel
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
        } else {
            directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
        }

        final AdvanceMeasurement advanceMeasurement = this.manageOrderElementAdvancesModel
                .getLastAdvanceMeasurement(directAdvanceAssignment);
        if (advanceMeasurement != null) {
            Util.bind(value, new Util.Getter<BigDecimal>() {
                @Override
                public BigDecimal get() {
                    return advanceMeasurement.getValue();
                }
            });
        }
        Listcell listCell = new Listcell();
        listCell.appendChild(value);
        listItem.appendChild(listCell);
    }

    private void appendLabelPercentage(final Listitem listItem){
        final AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                .getValue();
        Label percentage = new Label();

        DirectAdvanceAssignment directAdvanceAssignment;
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            directAdvanceAssignment = manageOrderElementAdvancesModel
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
        } else {
            directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
        }
        final AdvanceMeasurement advanceMeasurement = this.manageOrderElementAdvancesModel
                .getLastAdvanceMeasurement(directAdvanceAssignment);
        if (advanceMeasurement != null) {
            percentage
                    .setValue(this.manageOrderElementAdvancesModel
                    .getPercentageAdvanceMeasurement(advanceMeasurement)
                    .toString()
                    + " %");
        }

        Listcell listCell = new Listcell();
        listCell.appendChild(percentage);
        listItem.appendChild(listCell);
    }

    private void appendDateBoxDate(final Listitem listItem){
        final AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                .getValue();
        Datebox date = new Datebox();
        date.setDisabled(true);

        DirectAdvanceAssignment directAdvanceAssignment;
        if (advanceAssignment instanceof IndirectAdvanceAssignment) {
            directAdvanceAssignment = manageOrderElementAdvancesModel
                    .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
        } else {
            directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
        }
        final AdvanceMeasurement advanceMeasurement = this.manageOrderElementAdvancesModel
                .getLastAdvanceMeasurement(directAdvanceAssignment);
        if (advanceMeasurement != null) {

            Util.bind(date, new Util.Getter<Date>() {
                @Override
                public Date get() {
                    if (advanceMeasurement.getDate() == null) {
                        return null;
                    }
                    return advanceMeasurement.getDate()
                            .toDateTimeAtStartOfDay().toDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    advanceMeasurement.setDate(new LocalDate(value));
                }
            });
        }
        Listcell listCell = new Listcell();
        listCell.appendChild(date);
        listItem.appendChild(listCell);
    }

    private void appendRadioSpread(final Listitem listItem){
        final AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                .getValue();
        Radio reportGlobalAdvance = new Radio();
        reportGlobalAdvance.setChecked(advanceAssignment
                .getReportGlobalAdvance());

        reportGlobalAdvance.addEventListener(Events.ON_CHECK,
            new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                setReportGlobalAdvance(listItem);
            }
        });
        Listcell listCell = new Listcell();
        listCell.appendChild(reportGlobalAdvance);
        listItem.appendChild(listCell);
    }

    private void appendCalculatedCheckbox(final Listitem listItem){
        final AdvanceAssignment advance = (AdvanceAssignment) listItem.getValue();
        Checkbox calculated = new Checkbox();
        boolean isCalculated = advance instanceof IndirectAdvanceAssignment;
        calculated.setChecked(isCalculated);
        calculated.setDisabled(true);

        Listcell listCell = new Listcell();
        listCell.appendChild(calculated);
        listItem.appendChild(listCell);
    }

    private void appendChartCheckbox(final Listitem listItem) {
        final AdvanceAssignment advance = (AdvanceAssignment) listItem
                .getValue();
        final Checkbox chartCheckbox = new Checkbox();
        chartCheckbox.setChecked(selectedAdvances.contains(advance));
        chartCheckbox.addEventListener(Events.ON_CHECK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (chartCheckbox.isChecked()) {
                    selectedAdvances.add(advance);
                } else {
                    selectedAdvances.remove(advance);
                }
                Util.reloadBindings(window);
            }
        });

        Listcell listCell = new Listcell();
        listCell.appendChild(chartCheckbox);
        listItem.appendChild(listCell);
    }

    private void appendRemoveButton(final Listitem listItem) {
        final AdvanceAssignment advance = (AdvanceAssignment) listItem
                .getValue();
        final Button removeButton = createRemoveButton();

        removeButton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                manageOrderElementAdvancesModel
                        .removeLineAdvanceAssignment(advance);
                Util.reloadBindings(window);
            }
        });

        Listcell listCell = new Listcell();
        listCell.appendChild(removeButton);
        listItem.appendChild(listCell);
    }

    private void setMaxValue(final Listitem item,Combobox comboAdvanceTypes) {
        Listcell listCell = (Listcell)item.getChildren().get(1);
        Decimalbox miBox = ((Decimalbox) listCell.getFirstChild());
        Comboitem selectedItem = comboAdvanceTypes.getSelectedItem();
        if(selectedItem != null){
            AdvanceType advanceType = ((AdvanceType) selectedItem.getValue());
            if(advanceType != null){
                DirectAdvanceAssignment advance = (DirectAdvanceAssignment) item
                        .getValue();
                advance.setMaxValue(advanceType.getDefaultMaxValue());
                miBox.setValue(advanceType.getDefaultMaxValue());
            }
        }
    }

    private void setPercentage(){
        Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
        if ((this.indexSelectedItem < listAdvances.getItemCount())
                && (this.indexSelectedItem >= 0)) {
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssignment advanceAssignment = (AdvanceAssignment) selectedItem
                    .getValue();

            DirectAdvanceAssignment directAdvanceAssignment;
            if (advanceAssignment instanceof IndirectAdvanceAssignment) {
                directAdvanceAssignment = manageOrderElementAdvancesModel
                        .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
            } else {
                directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
            }
            final AdvanceMeasurement greatAdvanceMeasurement = this.manageOrderElementAdvancesModel
                    .getLastAdvanceMeasurement(directAdvanceAssignment);
            if (greatAdvanceMeasurement != null) {
                Listcell percentage = (Listcell) selectedItem.getChildren()
                        .get(3);
                ((Label) percentage.getFirstChild())
                        .setValue(this.manageOrderElementAdvancesModel
                                .getPercentageAdvanceMeasurement(
                                        greatAdvanceMeasurement).toString()
                                + " %");
            }
        }
    }

    private void setCurrentValue(){
      if(this.indexSelectedItem >= 0){
            Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssignment advanceAssignment = (AdvanceAssignment) selectedItem
                    .getValue();

            DirectAdvanceAssignment directAdvanceAssignment;
            if (advanceAssignment instanceof IndirectAdvanceAssignment) {
                directAdvanceAssignment = manageOrderElementAdvancesModel
                        .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
            } else {
                directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
            }
            final AdvanceMeasurement greatAdvanceMeasurement = this.manageOrderElementAdvancesModel
                    .getLastAdvanceMeasurement(directAdvanceAssignment);
            if (greatAdvanceMeasurement != null) {
                Listcell value = (Listcell)selectedItem.getChildren().get(2);
                ((Decimalbox) value.getFirstChild())
                        .setValue(greatAdvanceMeasurement.getValue());
            }
        }

    }

    public void setCurrentDate(Listitem item){
        this.manageOrderElementAdvancesModel.sortListAdvanceMeasurement();
        Util.reloadBindings(window.getFellow("editAdvancesMeasurement"));

        this.setCurrentDate();
        this.setPercentage();
        this.setCurrentValue();
        Util.reloadBindings(window.getFellow("chart"));
    }

    private void setCurrentDate(){
         if(this.indexSelectedItem >= 0){
            Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssignment advanceAssignment = (AdvanceAssignment) selectedItem
                    .getValue();

            DirectAdvanceAssignment directAdvanceAssignment;
            if (advanceAssignment instanceof IndirectAdvanceAssignment) {
                directAdvanceAssignment = manageOrderElementAdvancesModel
                        .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advanceAssignment);
            } else {
                directAdvanceAssignment = (DirectAdvanceAssignment) advanceAssignment;
            }
             final AdvanceMeasurement greatAdvanceMeasurement =
                 this.manageOrderElementAdvancesModel
                    .getLastAdvanceMeasurement(directAdvanceAssignment);
             if(greatAdvanceMeasurement != null){
                 Listcell date = (Listcell) selectedItem.getChildren().get(4);
                 LocalDate newDate = greatAdvanceMeasurement.getDate();
                 if (newDate != null) {
                     ((Datebox) date.getFirstChild()).setValue(newDate
                            .toDateTimeAtStartOfDay().toDate());
                 } else {
                    ((Datebox) date.getFirstChild()).setValue(null);
                }
             }
        }
    }

    private void cleanFields(){
        this.manageOrderElementAdvancesModel.cleanAdvance();
        Util.reloadBindings(window);
    }

    private void setReportGlobalAdvance(final Listitem item){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                Listcell celdaSpread = (Listcell) listItem.getChildren().get(5);
                Radio radioSpread = ((Radio)celdaSpread.getFirstChild());
                if(!radioSpread.isDisabled()){
                    radioSpread.setChecked(false);
                    ((AdvanceAssignment) listItem.getValue())
                            .setReportGlobalAdvance(false);
                }
            }
        }
        Listcell celdaSpread = (Listcell) item.getChildren().get(5);
        ((Radio)celdaSpread.getFirstChild()).setChecked(true);
        ((AdvanceAssignment) item.getValue()).setReportGlobalAdvance(true);
    }


    private boolean validateDataForm(){
        return ((validateListAdvanceAssignment())
                &&(validateListAdvanceMeasurement()));
    }

    private boolean validateListAdvanceAssignment(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceAssignment advance = (AdvanceAssignment) listItem
                        .getValue();
                if (advance.getAdvanceType() == null)
                    return false;

                DirectAdvanceAssignment directAdvanceAssignment;
                if (advance instanceof IndirectAdvanceAssignment) {
                    directAdvanceAssignment = manageOrderElementAdvancesModel
                            .calculateFakeDirectAdvanceAssignment((IndirectAdvanceAssignment) advance);
                } else {
                    directAdvanceAssignment = (DirectAdvanceAssignment) advance;
                }
                if (directAdvanceAssignment.getMaxValue() == null)
                    return false;
            }
        }
        return true;
    }

    private boolean validateListAdvanceMeasurement(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvancesMeasurement");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceMeasurement advance = (AdvanceMeasurement) listItem
                        .getValue();
                if (advance.getValue() == null)
                    return false;
                if (advance.getDate() == null)
                    return false;
            }
        }
        return true;
    }

    private boolean validateReportGlobalAdvance(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        boolean existItems = false;
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceAssignment advanceAssignment = (AdvanceAssignment) listItem
                        .getValue();
                existItems = true;
                if (advanceAssignment.getReportGlobalAdvance()) {
                    return true;
                }
            }
        }
        if(!existItems) return true;
        return false;
    }

    public AdvanceMeasurementRenderer getAdvanceMeasurementRenderer() {
        return advanceMeasurementRenderer;
    }

    private class AdvanceMeasurementRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) data;

            item.setValue(advanceMeasurement);

            appendDecimalBoxValue(item);
            appendLabelPercentage(item);
            appendDateboxDate(item);
            appendRemoveButton(item);
        }

        private void appendDecimalBoxValue(final Listitem listitem) {
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listitem
                    .getValue();
            final Decimalbox value = new Decimalbox();

            Listcell listcell = new Listcell();
            listcell.appendChild(value);
            listitem.appendChild(listcell);

            value.setScale(2);
            value.setDisabled(isReadOnlyAdvanceMeasurements());
            value.addEventListener(Events.ON_CHANGE, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    updatesValue(value);
                }
            });
            value.setConstraint(checkValidValue());

            Util.bind(value, new Util.Getter<BigDecimal>() {

                @Override
                public BigDecimal get() {
                    return advanceMeasurement.getValue();
                }
            }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    advanceMeasurement.setValue(value);
                }
            });
        }

        private void appendLabelPercentage(final Listitem listitem) {
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listitem
                    .getValue();

            BigDecimal percentage = manageOrderElementAdvancesModel
                    .getPercentageAdvanceMeasurement(advanceMeasurement);
            Label percentageLabel = new Label(percentage.toString() + " %");

            Listcell listcell = new Listcell();
            listcell.appendChild(percentageLabel);
            listitem.appendChild(listcell);
        }

        private void appendDateboxDate(final Listitem listitem) {
            final AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listitem
                    .getValue();
            Datebox date = new Datebox();

            Listcell listcell = new Listcell();
            listcell.appendChild(date);
            listitem.appendChild(listcell);

            date.setDisabled(isReadOnlyAdvanceMeasurements());
            date.addEventListener(Events.ON_CHANGE, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    setCurrentDate(listitem);
                }
            });
            date.setConstraint(checkValidDate());

            Util.bind(date, new Util.Getter<Date>() {

                @Override
                public Date get() {
                    if (advanceMeasurement.getDate() == null) {
                        return null;
                    }
                    return advanceMeasurement.getDate()
                            .toDateTimeAtStartOfDay().toDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    advanceMeasurement.setDate(new LocalDate(value));
                }
            });
        }

        private Constraint checkValidValue() {
            Constraint newConstraint = new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (((BigDecimal) value) != null) {
                        if (manageOrderElementAdvancesModel
                                .greatThanMaxValue((BigDecimal) value)) {
                            throw new WrongValueException(
                                    comp,
                                    _("Value is not valid, the current value must be less than max value"));
                        }
                        if (!(manageOrderElementAdvancesModel
                                .isPrecisionValid((BigDecimal) value))) {
                            throw new WrongValueException(
                                    comp,
                                    _("Value is not valid, the Precision value must be exact "
                                            + manageOrderElementAdvancesModel
                                                    .getUnitPrecision()));
                        }
                    }
                }
            };
            return newConstraint;
        }

        private Constraint checkValidDate() {
            Constraint newConstraint = new Constraint() {
                @Override
                public void validate(Component comp, Object value)
                        throws WrongValueException {
                    if (((Date) value) != null) {
                        Listitem listitem = (Listitem) comp.getParent()
                                .getParent();
                        AdvanceMeasurement advanceMeasurement = (AdvanceMeasurement) listitem
                                .getValue();
                        if (!manageOrderElementAdvancesModel
                                .isDistinctValidDate((Date) value,
                                        advanceMeasurement)) {
                            throw new WrongValueException(
                                    comp,
                                    _("The date is not valid, the date must be unique for this  advance assignment"));
                        }
                    }
                }
            };
            return newConstraint;
        }

        private void appendRemoveButton(final Listitem listItem) {
            final AdvanceMeasurement advance = (AdvanceMeasurement) listItem
                    .getValue();
            final Button removeButton = createRemoveButton();

            removeButton.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    manageOrderElementAdvancesModel
                            .removeLineAdvanceMeasurement(advance);
                    Util.reloadBindings(window);
                }
            });

            Listcell listCell = new Listcell();
            listCell.appendChild(removeButton);
            listItem.appendChild(listCell);
        }

    }

    public XYModel getChartData() {
        return this.manageOrderElementAdvancesModel.getChartData(selectedAdvances);
    }

    private Button createRemoveButton() {
        Button removeButton = new Button();
        removeButton.setSclass("icono");
        removeButton.setImage("/common/img/ico_borrar1.png");
        removeButton.setHoverImage("/common/img/ico_borrar.png");
        removeButton.setTooltiptext(_("Delete"));

        return removeButton;
    }

}
