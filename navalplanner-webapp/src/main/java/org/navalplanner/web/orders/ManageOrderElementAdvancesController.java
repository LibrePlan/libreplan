package org.navalplanner.web.orders;


import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

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

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp.getFellow("listOrderElementAdvances"));
        comp.setVariable("manageOrderElementAdvancesController", this, true);
        window = (Window) comp;
        messagesForUser = new MessagesForUser(window.getFellow("listOrderElementAdvances"));
    }

    public List<AdvanceMeasurementDTO> getAdvanceMeasurementDTOs() {
        return manageOrderElementAdvancesModel.getAdvanceMeasurementDTOs();
    }

    public List<AdvanceAssigmentDTO> getAdvanceAssigmentDTOs() {
        return manageOrderElementAdvancesModel.getAdvanceAssigmentDTOs();
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
        }catch(DuplicateAdvanceAssigmentForOrderElementException e){
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
        Util.reloadBindings(window);
    }

    public void prepareEditAdvanceMeasurements(AdvanceAssigmentDTO advanceAssigmentDTO){
        manageOrderElementAdvancesModel.prepareEditAdvanceMeasurements(advanceAssigmentDTO);
        Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
        this.indexSelectedItem = listAdvances.getIndexOfItem(listAdvances.getSelectedItem());
        Util.reloadBindings(window);
    }

    public void goToCreateLineAdvanceAssigment() {
        manageOrderElementAdvancesModel.addNewLineAdvaceAssigment();
        Util.reloadBindings(window);
    }

    public void goToCreateLineAdvanceMeasurement() {
        manageOrderElementAdvancesModel.addNewLineAdvaceMeasurement();
        Util.reloadBindings(window);
    }

    public void goToRemoveLineAdvanceAssigment(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        Listitem listItem = listAdvances.getItemAtIndex(indexSelectedItem);
        if(listItem != null){

            AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO) listItem.getValue();
            manageOrderElementAdvancesModel.removeLineAdvanceAssigment(advanceAssigmentDTO);
            Util.reloadBindings(window);
        }
    }

    public void goToRemoveLineAdvanceMeasurement(){
        Listbox listAdvancesMeasurement = (Listbox)window.getFellow("editAdvancesMeasurement");
        Listitem selectedItem = listAdvancesMeasurement.getSelectedItem();
        if(selectedItem != null){
            AdvanceMeasurementDTO advanceMeasurementDTO = (AdvanceMeasurementDTO) selectedItem.getValue();
            if(advanceMeasurementDTO != null){
                manageOrderElementAdvancesModel.removeLineAdvanceMeasurement(advanceMeasurementDTO);
                Util.reloadBindings(window);
            }
        }
    }

    public List<AdvanceType> getActivesAdvanceType() {
        return manageOrderElementAdvancesModel.getActivesAdvanceTypes();
    }

    public boolean isReadOnlyAdvanceMeasurementDTOs(){
       return manageOrderElementAdvancesModel.isReadOnlyAdvanceMeasurementDTOs();
    }

    public AdvanceTypeListRenderer getAdvancesRenderer() {
        return advanceTypeListRenderer;
    }

    public void updatesValue(final Decimalbox item){
        this.setPercentage();
        this.setCurrentValue();
        this.cleanDate((Listitem)item.getParent().getParent());
        this.setCurrentDate();
    }

    public class AdvanceTypeListRenderer implements ListitemRenderer {
         @Override
         public void render(Listitem listItem, Object data) throws Exception {
            final AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO) data;
            listItem.setValue(advanceDTO);
            listItem.setDraggable("true");
            listItem.setDroppable("true");

            if((advanceDTO.getType().equals(AdvanceAssigment.Type.DIRECT))
                    && ((advanceDTO.getIsNewObject())||(advanceDTO.getIsNewDTO()))){
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
        }
    }

    private void appendComboboxAdvancType(final Listitem listItem){
        final AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO)listItem.getValue();
        final Combobox comboAdvanceTypes = new Combobox();
        final List<AdvanceType> listAdvanceType = manageOrderElementAdvancesModel
                .getActivesAdvanceTypes();
        for(AdvanceType advanceType : listAdvanceType){
                Comboitem comboItem = new Comboitem();
                comboItem.setValue(advanceType);
                comboItem.setLabel(advanceType.getUnitName());
                comboItem.setParent(comboAdvanceTypes);

                if((advanceDTO.getAdvanceType()!=null)&&
                        (advanceDTO.getAdvanceType().getId().equals(advanceType.getId())))
                    comboAdvanceTypes.setSelectedItem(comboItem);
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
                                advanceDTO.setAdvanceType(advanceType);
                                advanceDTO.setMaxValue(advanceType.getDefaultMaxValue());
                            }
                        }
                    });
        Listcell listCell = new Listcell();
        listCell.appendChild(comboAdvanceTypes);
        listItem.appendChild(listCell);
    }

    private void appendLabelAdvanceType(final Listitem listItem){
        final AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Label unitName = new Label(advanceDTO.getAdvanceType().getUnitName());
        Listcell listCell = new Listcell();
        listCell.appendChild(unitName);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxMaxValue(final Listitem listItem){
        final AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Decimalbox maxValue = new Decimalbox();
        maxValue.setScale(2);

        if(advanceAssigmentDTO.getType().equals(AdvanceAssigment.Type.CALCULATED))
            maxValue.setDisabled(true);

        Util.bind(maxValue,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return advanceAssigmentDTO.getMaxValue();
                        }
                    }, new Util.Setter<BigDecimal>() {

                @Override
            public void set(BigDecimal value) {
                            advanceAssigmentDTO.setMaxValue(value);
                        }
        });
        maxValue.addEventListener(Events.ON_CHANGE,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        setPercentage();
                    }
                });

        Listcell listCell = new Listcell();
        listCell.appendChild(maxValue);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxValue(final Listitem listItem){
        final AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Decimalbox value = new Decimalbox();
        value.setScale(2);
        value.setDisabled(true);

        final AdvanceMeasurementDTO advanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
        if(advanceMeasurementDTO != null){
            Util.bind(value, new Util.Getter<BigDecimal>() {
                    @Override
                    public BigDecimal get() {
                        return advanceMeasurementDTO.getValue();
                    }
                }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    advanceMeasurementDTO.setValue(value);
                }
            });
        }
        Listcell listCell = new Listcell();
        listCell.appendChild(value);
        listItem.appendChild(listCell);
    }

    private void appendLabelPercentage(final Listitem listItem){
        final AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Label percentage = new Label();

        final AdvanceMeasurementDTO advanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
        if(advanceMeasurementDTO != null){
                percentage.setValue(advanceMeasurementDTO.getPercentage());
        }

        Listcell listCell = new Listcell();
        listCell.appendChild(percentage);
        listItem.appendChild(listCell);
    }

    private void appendDateBoxDate(final Listitem listItem){
        final AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Datebox date = new Datebox();
        date.setDisabled(true);

        final AdvanceMeasurementDTO advanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
        if(advanceMeasurementDTO != null){

            Util.bind(date, new Util.Getter<Date>() {
                @Override
                public Date get() {
                    return advanceMeasurementDTO.getDate();
                }
                }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    advanceMeasurementDTO.setDate(value);
                }
            });
        }
        Listcell listCell = new Listcell();
        listCell.appendChild(date);
        listItem.appendChild(listCell);
    }

    private void appendRadioSpread(final Listitem listItem){
        final AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Radio reportGlobalAdvance = new Radio();
        reportGlobalAdvance.setChecked(advanceAssigmentDTO.getReportGlobalAdvance());

        if(advanceAssigmentDTO.getType().equals(AdvanceAssigment.Type.CALCULATED))
            reportGlobalAdvance.setDisabled(true);

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
        final AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO)listItem.getValue();
        Checkbox calculated = new Checkbox();
        boolean isCalculated = advanceDTO.getType().equals(AdvanceAssigment.Type.CALCULATED);
        calculated.setChecked(isCalculated);
        calculated.setDisabled(true);

        Listcell listCell = new Listcell();
        listCell.appendChild(calculated);
        listItem.appendChild(listCell);
    }

    private void setMaxValue(final Listitem item,Combobox comboAdvanceTypes) {
        Listcell listCell = (Listcell)item.getChildren().get(1);
        Decimalbox miBox = ((Decimalbox) listCell.getFirstChild());
        Comboitem selectedItem = comboAdvanceTypes.getSelectedItem();
        if(selectedItem != null){
            AdvanceType advanceType = ((AdvanceType) selectedItem.getValue());
            if(advanceType != null){
                AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO)item.getValue();
                advanceDTO.setMaxValue(advanceType.getDefaultMaxValue());
                miBox.setValue(advanceType.getDefaultMaxValue());
            }
        }
    }

    private void setPercentage(){
        if(this.indexSelectedItem >= 0){
            Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssigmentDTO advanceAssigmentDTO =
                (AdvanceAssigmentDTO) selectedItem.getValue();

            final AdvanceMeasurementDTO greatAdvanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
            if(greatAdvanceMeasurementDTO != null){
                Listcell percentage = (Listcell)selectedItem.getChildren().get(3);
                ((Label)percentage.getFirstChild()).setValue(greatAdvanceMeasurementDTO.getPercentage());
            }
        }
    }

    private void setCurrentValue(){
      if(this.indexSelectedItem >= 0){
            Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssigmentDTO advanceAssigmentDTO =
                (AdvanceAssigmentDTO) selectedItem.getValue();
            final AdvanceMeasurementDTO greatAdvanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
            if(greatAdvanceMeasurementDTO != null){
                Listcell value = (Listcell)selectedItem.getChildren().get(2);
                ((Decimalbox)value.getFirstChild()).setValue(greatAdvanceMeasurementDTO.getValue());
            }
        }

    }

    public void setCurrentDate(Listitem item){
        this.manageOrderElementAdvancesModel.modifyListAdvanceMeasurement(
                (AdvanceMeasurementDTO)item.getValue());
        Util.reloadBindings(window.getFellow("editAdvancesMeasurement"));

        setCurrentDate();
        this.setPercentage();
        this.setCurrentValue();
    }

    private void setCurrentDate(){
         if(this.indexSelectedItem >= 0){
            Listbox listAdvances = ((Listbox) window.getFellow("editAdvances"));
            Listitem selectedItem = listAdvances.getItemAtIndex(indexSelectedItem);
            AdvanceAssigmentDTO advanceAssigmentDTO =
                (AdvanceAssigmentDTO) selectedItem.getValue();
             final AdvanceMeasurementDTO greatAdvanceMeasurementDTO =
                this.manageOrderElementAdvancesModel.getFirstAdvanceMeasurement(advanceAssigmentDTO);
             if(greatAdvanceMeasurementDTO != null){
                Listcell date = (Listcell)selectedItem.getChildren().get(4);
                ((Datebox)date.getFirstChild()).setValue(greatAdvanceMeasurementDTO.getDate());
             }
        }
    }

    private void cleanDate(final Listitem item){
        final AdvanceMeasurementDTO advanceMeasurementDTO = (AdvanceMeasurementDTO) item.getValue();
        advanceMeasurementDTO.setDate(null);
        Listcell celdaDate = (Listcell)item.getChildren().get(2);
        ((Datebox)celdaDate.getFirstChild()).setValue(null);
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
                    ((AdvanceAssigmentDTO)listItem.getValue()).setReportGlobalAdvance(false);
                }
            }
        }
        Listcell celdaSpread = (Listcell) item.getChildren().get(5);
        ((Radio)celdaSpread.getFirstChild()).setChecked(true);
        ((AdvanceAssigmentDTO)item.getValue()).setReportGlobalAdvance(true);
    }


    public Constraint checkValidValue() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((BigDecimal) value) != null){
                    if(manageOrderElementAdvancesModel.greatThanMaxValue((BigDecimal)value)){
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, the current value must be less than max value"));
                    }
                    if (!(manageOrderElementAdvancesModel.isPrecisionValid((BigDecimal)value))) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, the Precision value must be exact "
                                +manageOrderElementAdvancesModel.getUnitPrecision()));
                    }
                }
            }
        };
        return newConstraint;
    }

    public Constraint checkValidDate() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((Date) value) != null) {
                    if(!manageOrderElementAdvancesModel.isGreatValidDate((Date)value)){
                        throw new WrongValueException(
                                comp,
                                _("The date is not valid, the date must be great than the current date of the other advances measurement"));
                    }
                }
            }
        };
        return newConstraint;
    }

    private boolean validateDataForm(){
        return ((validateListAdvanceAssigment())
                &&(validateListAdvanceMeasurement()));
    }

    private boolean validateListAdvanceAssigment(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceAssigmentDTO advanceDTO = (AdvanceAssigmentDTO) listItem.getValue();
                if(advanceDTO.getAdvanceType() == null)return false;
                if(advanceDTO.getMaxValue() == null)return false;
            }
        }
        return true;
    }

    private boolean validateListAdvanceMeasurement(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvancesMeasurement");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) listItem.getValue();
                if(advanceDTO.getValue() == null)return false;
                if(advanceDTO.getDate() == null)return false;
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
                AdvanceAssigmentDTO advanceAssigmentDTO = (AdvanceAssigmentDTO) listItem.getValue();
                if(advanceAssigmentDTO.getType().equals(AdvanceAssigment.Type.DIRECT)){
                    existItems = true;
                    if(advanceAssigmentDTO.getReportGlobalAdvance()){
                        return true;
                    }
                }
            }
        }
        if(!existItems) return true;
        return false;
    }
}