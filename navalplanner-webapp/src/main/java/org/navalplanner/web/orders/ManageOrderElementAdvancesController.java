package org.navalplanner.web.orders;


import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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

/**
 * Controller for show the advances of the selected order element<br />
 * @author Susana Montes Pedreria <smontes@wirelessgalicia.com>
 */

public class ManageOrderElementAdvancesController extends
        GenericForwardComposer {

    private IMessagesForUser messagesForUser;

    private Window window;

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
        return manageOrderElementAdvancesModel.getAdvanceMeasurements();
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
    }

    public void goToCreateLine() {
        manageOrderElementAdvancesModel.addNewLine();
        Util.reloadBindings(window);
    }

    public void goToRemoveLine(){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        Listitem listItem = listAdvances.getSelectedItem();
        if(listItem != null){
            AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) listItem.getValue();
            manageOrderElementAdvancesModel.removeLine(advanceDTO);
            Util.reloadBindings(window);
        }
    }

    public List<AdvanceType> getActivesAdvanceType() {
        return manageOrderElementAdvancesModel.getActivesAdvanceTypes();
    }

    public AdvanceTypeListRenderer getAdvancesRenderer() {
        return advanceTypeListRenderer;
    }

    public class AdvanceTypeListRenderer implements ListitemRenderer {
         @Override
         public void render(Listitem listItem, Object data) throws Exception {
            final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) data;
            listItem.setValue(advanceDTO);
            listItem.setDraggable("true");
            listItem.setDroppable("true");

            if ((advanceDTO.getIsNewObject())||(advanceDTO.getIsNewDTO())){
                 appendComboboxAdvancType(listItem);
            } else {
                appendLabelAdvanceType(listItem);
            }
            appendDecimalBoxMaxValue(listItem);
            appendDecimalBoxValue(listItem);
            appendLabelPercentage(listItem);
            appendDateBoxDate(listItem);
            appendRadioSpread(listItem);
        }
    }

    private void appendComboboxAdvancType(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
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
                        cleanFields(listItem);
                        setPercentage(listItem);
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
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Label unitName = new Label(advanceDTO.getAdvanceType().getUnitName());
        Listcell listCell = new Listcell();
        listCell.appendChild(unitName);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxMaxValue(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Decimalbox maxValue = new Decimalbox();
        maxValue.setScale(2);
        Util.bind(maxValue,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return advanceDTO.getMaxValue();
                        }
                    }, new Util.Setter<BigDecimal>() {

                @Override
            public void set(BigDecimal value) {
                            advanceDTO.setMaxValue(value);
                        }
        });
        maxValue.addEventListener(Events.ON_CHANGE,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        setPercentage(listItem);
                    }
                });

        Listcell listCell = new Listcell();
        listCell.appendChild(maxValue);
        listItem.appendChild(listCell);
    }

    private void appendDecimalBoxValue(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Decimalbox value = new Decimalbox();
        value.setScale(2);
        Util.bind(value, new Util.Getter<BigDecimal>() {
                @Override
                public BigDecimal get() {
                    return advanceDTO.getValue();
                }
            }, new Util.Setter<BigDecimal>() {

                @Override
                public void set(BigDecimal value) {
                    advanceDTO.setValue(value);
                }
        });
        value.setConstraint(checkValidValue());
        value.addEventListener(Events.ON_CHANGE,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        setPercentage(listItem);
                        cleanDate(listItem);
                    }
                });

        Listcell listCell = new Listcell();
        listCell.appendChild(value);
        listItem.appendChild(listCell);
    }

    private void appendLabelPercentage(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Label percentage = new Label();
        percentage.setValue(advanceDTO.getPercentage() + " %");
        Listcell listCell = new Listcell();
        listCell.appendChild(percentage);
        listItem.appendChild(listCell);
    }

    private void appendDateBoxDate(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Datebox date = new Datebox();
        Util.bind(date, new Util.Getter<Date>() {
                @Override
                public Date get() {
                    return advanceDTO.getDate();
                }
            }, new Util.Setter<Date>() {

                @Override
                public void set(Date value) {
                    advanceDTO.setDate(value);
                }
        });
        date.setConstraint(checkValidDate());
        Listcell listCell = new Listcell();
        listCell.appendChild(date);
        listItem.appendChild(listCell);
    }

    private void appendRadioSpread(final Listitem listItem){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)listItem.getValue();
        Radio reportGlobalAdvance = new Radio();
        reportGlobalAdvance.setChecked(advanceDTO.getReportGlobalAdvance());

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

    private void setMaxValue(final Listitem item,Combobox comboAdvanceTypes) {
        Listcell listCell = (Listcell)item.getChildren().get(1);
        Decimalbox miBox = ((Decimalbox) listCell.getFirstChild());
        Comboitem selectedItem = comboAdvanceTypes.getSelectedItem();
        if(selectedItem != null){
            AdvanceType advanceType = ((AdvanceType) selectedItem.getValue());
            if(advanceType != null){
                AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO)item.getValue();
                advanceDTO.setMaxValue(advanceType.getDefaultMaxValue());
                miBox.setValue(advanceType.getDefaultMaxValue());
            }
        }
    }

    private void setPercentage(final Listitem item){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) item.getValue();
        Listcell celdaPercentage = (Listcell)item.getChildren().get(3);
        ((Label)celdaPercentage.getFirstChild()).setValue(advanceDTO.getPercentage()+" %");
    }

    private void cleanDate(final Listitem item){
        final AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) item.getValue();
        advanceDTO.setDate(null);
        Listcell celdaDate = (Listcell)item.getChildren().get(4);
        ((Datebox)celdaDate.getFirstChild()).setValue(null);
    }

    private void cleanFields(final Listitem item){
        AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) item.getValue();

        Listcell celdaValue = (Listcell) item.getChildren().get(2);
        ((Decimalbox)celdaValue.getFirstChild()).setValue(null);
        advanceDTO.setValue(null);

        Listcell celdaPercentage = (Listcell) item.getChildren().get(3);
        ((Label)celdaPercentage.getFirstChild()).setValue(" %");
        advanceDTO.setPercentage("");

        Listcell celdaDate = (Listcell) item.getChildren().get(4);
        ((Datebox)celdaDate.getFirstChild()).setValue(null);
        advanceDTO.setDate(null);

        Listcell celdaSpread = (Listcell) item.getChildren().get(5);
        ((Radio)celdaSpread.getFirstChild()).setChecked(false);
        advanceDTO.setReportGlobalAdvance(false);
    }

    private void setReportGlobalAdvance(final Listitem item){
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                Listcell celdaSpread = (Listcell) listItem.getChildren().get(5);
                ((Radio)celdaSpread.getFirstChild()).setChecked(false);
                ((AdvanceMeasurementDTO)listItem.getValue()).setReportGlobalAdvance(false);
            }
        }
        Listcell celdaSpread = (Listcell) item.getChildren().get(5);
        ((Radio)celdaSpread.getFirstChild()).setChecked(true);
        ((AdvanceMeasurementDTO)item.getValue()).setReportGlobalAdvance(true);
    }

    private Constraint checkValidValue() {
        Constraint newConstraint = new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (((BigDecimal) value) != null){
                    Listitem listItem = ((Listitem)comp.getParent().getParent());
                    AdvanceMeasurementDTO advanceDTO = ((AdvanceMeasurementDTO)listItem.getValue());
                    if(manageOrderElementAdvancesModel.greatThanMaxValue(advanceDTO,(BigDecimal)value)){
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, the current value must be less than max value"));
                    }
                    if (!(manageOrderElementAdvancesModel.isPrecisionValid(advanceDTO,(BigDecimal)value))) {
                        throw new WrongValueException(
                                comp,
                                _("Value is not valid, the Precision value must be exact "
                                +advanceDTO.getAdvanceType().getUnitPrecision()));
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
                    Listitem listItem = ((Listitem)comp.getParent().getParent());
                    AdvanceMeasurementDTO advanceDTO = ((AdvanceMeasurementDTO)listItem.getValue());
                    if(!manageOrderElementAdvancesModel.isGreatValidDate(advanceDTO,(Date)value)){
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
        Listbox listAdvances = (Listbox) window.getFellow("editAdvances");
        for(int i=0; i< listAdvances.getChildren().size(); i++){
            if(listAdvances.getChildren().get(i) instanceof Listitem){
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) listItem.getValue();
                if(advanceDTO.getAdvanceType() == null)return false;
                if(advanceDTO.getMaxValue() == null)return false;
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
                existItems = true;
                Listitem listItem = (Listitem) listAdvances.getChildren().get(i);
                AdvanceMeasurementDTO advanceDTO = (AdvanceMeasurementDTO) listItem.getValue();
                if(advanceDTO.getReportGlobalAdvance())
                    return true;
            }
        }
        if(!existItems) return true;
        return false;
    }
}