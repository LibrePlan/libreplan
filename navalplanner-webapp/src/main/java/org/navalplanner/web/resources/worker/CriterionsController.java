/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.navalplanner.web.resources.worker;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;

import org.zkoss.zul.Constraint;
import static org.navalplanner.web.I18nHelper._;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionsController extends GenericForwardComposer {

    private IAssignedCriterionsModel assignedCriterionsModel;
    private Combobox comboboxFilter;
    private Grid listingCriterions;

    CriterionsController() {
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedCriterionsController", this, true);
        comboboxFilter = (Combobox) comp.getFellow("comboboxfilter");
        listingCriterions = (Grid) comp.getFellow("listingCriterions");
    }

    public void prepareForEdit(Worker worker) {
        this.assignedCriterionsModel.prepareForEdit(worker);
    }

    public Set<CriterionSatisfactionDTO> getCriterionSatisfactionDTOs() {
        Comboitem comboitem = comboboxFilter.getSelectedItem();
        if((comboitem != null) && (comboitem.getLabel().equals("in force"))) {
                return assignedCriterionsModel.getFilterCriterionSatisfactions();
            }
        return assignedCriterionsModel.getAllCriterionSatisfactions();
    }

    public void addCriterionSatisfaction() {
        assignedCriterionsModel.addCriterionSatisfaction();
        reload();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedCriterionsModel.getCriterionWithItsType();
    }

    public void reload() {
        Util.reloadBindings(listingCriterions);
    }

    public void remove(CriterionSatisfactionDTO criterionSatisfactionDTO){
        assignedCriterionsModel.remove(criterionSatisfactionDTO);
        reload();
    }

    public void selectCriterionAndType(Listitem item,Bandbox bandbox,
        CriterionSatisfactionDTO criterionSatisfactionDTO){
        if(item != null){
            CriterionWithItsType criterionAndType = (CriterionWithItsType)item.getValue();
            bandbox.setValue(criterionAndType.getNameAndType());
            setCriterionWithItsType(criterionAndType,criterionSatisfactionDTO,bandbox);
        }else{
            bandbox.setValue("");
        }
        bandbox.close();
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionAndType,
            CriterionSatisfactionDTO satisfaction,Bandbox bandbox)  throws WrongValueException{
            this.assignedCriterionsModel.setCriterionWithItsType(satisfaction, criterionAndType);
            validateCriterionWithItsType(satisfaction,bandbox);
    }

    private void validateCriterionWithItsType(CriterionSatisfactionDTO satisfaction,
            Component comp) throws WrongValueException{
            if(satisfaction.getCriterionWithItsType() == null) return;
            if(satisfaction.getStartDate() == null) return;
            if(assignedCriterionsModel.checkSameCriterionAndSameInterval(satisfaction)){
                throw new WrongValueException(comp,
                                        _("Criterion is not valid, the criterion overlap other criterionSatisfaction whith same criterion"));
            }
            if(assignedCriterionsModel.checkNotAllowSimultaneousCriterionsPerResource(satisfaction)){
                throw new WrongValueException(comp,
                                        _("CriterionType is not valid, the criterionType overlap other criterionSatisfaction whith same criterionType"));
            }
    }

    public void changeDate(Component comp){
        CriterionSatisfactionDTO criterionSatisfactionDTO =
            (CriterionSatisfactionDTO)((Row) comp.getParent()).getValue();
        validateCriterionWithItsType(criterionSatisfactionDTO,comp);
        reload();
    }

    public Constraint validateStartDate(){
        return new Constraint() {
                        @Override
                        public void validate(Component comp, Object value)
                                throws WrongValueException {
                            CriterionSatisfactionDTO criterionSatisfactionDTO =
                                    (CriterionSatisfactionDTO)((Row) comp.getParent()).getValue();
                            if(!criterionSatisfactionDTO.isLessToEndDate((Date) value)){
                                throw new WrongValueException(comp,
                                        _("Start date is not valid, the new start date must be lower than the end date"));
                            }else if(!criterionSatisfactionDTO.isPreviousStartDate((Date) value)){
                                throw new WrongValueException(comp,
                                        _("End date is not valid, the new end date must be later the current end date"));
                            }
                        }
                    };
    }

    public Constraint validateEndDate(){
        return new Constraint() {
                        @Override
                        public void validate(Component comp, Object value)
                                throws WrongValueException {
                            CriterionSatisfactionDTO criterionSatisfactionDTO =
                                    (CriterionSatisfactionDTO)((Row) comp.getParent()).getValue();
                            if(!criterionSatisfactionDTO.isGreaterStartDate((Date) value)){
                                throw new WrongValueException(comp,
                                        _("End date is not valid, the new end date must be greater than the start date"));
                            }else if(!criterionSatisfactionDTO.isPostEndDate((Date) value)){
                                throw new WrongValueException(comp,
                                        _("End date is not valid, the new end date must be later the current end date"));
                            }
                            validateCriterionWithItsType(criterionSatisfactionDTO,comp);
                        }
                    };
    }

    public void save() throws ValidationException{
          assignedCriterionsModel.save();
    }
}
