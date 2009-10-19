/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.navalplanner.web.resources.worker;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.resources.machine.IAssignedMachineCriterionsModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public class CriterionsMachineController extends GenericForwardComposer {

    private IAssignedMachineCriterionsModel assignedMachineCriterionsModel;

    private Combobox comboboxFilter;

    private Grid listingCriterions;

    private Listbox lbCriterions;

    public CriterionsMachineController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedCriterionsController", this, true);
        lbCriterions.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("### onChange");
                final Criterion criterion = (Criterion) event.getData();
                System.out.println("### criterion: " + criterion);
                final String resource = criterion.getType().getResource();
                if (!isValid(resource)) {
                    throw new WrongValueException(event.getTarget(),
                            _("resource not valid"));
                }
            }
        });

        lbCriterions.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                // System.out.println("### onSelect");
                // final Criterion criterion = (Criterion) event.getData();
                // Listitem item = lbCriterions.getSelectedItem();
                // System.out.println("### selected item: " + item);
//                System.out.println("### criterion: " + criterion);
//                final String resource = criterion.getType().getResource();
//                if (!isValid(resource)) {
//                    throw new WrongValueException(event.getTarget(),
//                            _("resource not valid"));
//                }
            }
        });
    }

    public void prepareForEdit(Machine machine) {
        this.assignedMachineCriterionsModel.prepareForEdit(machine);
    }

    public Set<CriterionSatisfactionDTO> getCriterionSatisfactionDTOs() {
        Comboitem comboitem = comboboxFilter.getSelectedItem();
        if((comboitem != null) && (comboitem.getLabel().equals("in force"))) {
            return assignedMachineCriterionsModel
                    .getFilterCriterionSatisfactions();
            }
        return assignedMachineCriterionsModel.getAllCriterionSatisfactions();
    }

    public void addCriterionSatisfaction() {
        assignedMachineCriterionsModel.addCriterionSatisfaction();
        reload();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedMachineCriterionsModel.getCriterionWithItsType();
    }

    public void reload() {
        Util.reloadBindings(listingCriterions);
    }

    public void remove(CriterionSatisfactionDTO criterionSatisfactionDTO){
        assignedMachineCriterionsModel.remove(criterionSatisfactionDTO);
        reload();
    }

    public void selectCriterionAndType(Listitem item, Bandbox bandbox,
        CriterionSatisfactionDTO criterionSatisfactionDTO){
        if(item != null){

            CriterionWithItsType criterionAndType = (CriterionWithItsType)item.getValue();
            CriterionType criterionType = (CriterionType) criterionAndType
                    .getType();
            final String resource = criterionType.getResource();
            // System.out.println("### criterionType: " + criterionType);
            // System.out.println("### resource: " + resource);
            if (!isValid(resource)) {
                throw new WrongValueException(item,
                        _("cannot apply criterion to machine"));
            }
            bandbox.setValue(criterionAndType.getNameAndType());
            setCriterionWithItsType(criterionAndType,criterionSatisfactionDTO,bandbox);
        } else {
            bandbox.setValue("");
        }
        bandbox.close();
    }

    private boolean isValid(String resource) {
        // return (ResourceEnum.RESOURCE.toString().equals(resource) ||
        // ResourceEnum.MACHINE
        // .toString().equals(resource));
        return isValid(ResourceEnum.valueOf(resource));
    }

    /**
     * Can only apply criterion types of resource type RESOURCE or MACHINE
     *
     * @param resource
     * @return
     */
    private boolean isValid(ResourceEnum resource) {
        return (ResourceEnum.RESOURCE.equals(resource) || ResourceEnum.MACHINE
                .equals(resource));
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionAndType,
            CriterionSatisfactionDTO satisfaction,Bandbox bandbox)  throws WrongValueException{
        this.assignedMachineCriterionsModel.setCriterionWithItsType(
                satisfaction, criterionAndType);
            validateCriterionWithItsType(satisfaction,bandbox);
    }

    private void validateCriterionWithItsType(CriterionSatisfactionDTO satisfaction,
            Component comp) throws WrongValueException{
            if(satisfaction.getCriterionWithItsType() == null) return;
            if(satisfaction.getStartDate() == null) return;
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
        assignedMachineCriterionsModel.save();
    }
}
