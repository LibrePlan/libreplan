/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.orders;
import static org.navalplanner.web.I18nHelper._;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.INewObject;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;

/**
 * DTO represents the handled data in the form of assigning criterion requirement.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionRequirementDTO  implements INewObject {

    public static enum Type {
        DIRECT, INDIRECT;
    }

    public static enum FlagState {
        NORMAL, REMOVED, RETRIEVED;
    }

    private static final String DIRECT = _("Direct");

    private static final String INDIRECT = _("Indirect");

    public static final String CRITERION_WITH_ITS_TYPE = "criterionWithItsType";

    private String type;

    private String criterionAndType;

    private Boolean newObject = false;

    private CriterionRequirement criterionRequirement;

    private Boolean valid = true;

    private FlagState flagState = FlagState.NORMAL;

    @NotNull
    private CriterionWithItsType criterionWithItsType;

    public CriterionRequirementDTO(Type type){
        this.setNewObject(true);
        this.setType(type);
        this.setValid(true);
        this.criterionAndType = "";
    }

    public CriterionRequirementDTO(CriterionRequirement criterionRequirement) {
        this.criterionAndType = "";
        this.setCriterionRequirement(criterionRequirement);
        this.setType(criterionRequirement);
        this.setValid(criterionRequirement);

        Criterion criterion = criterionRequirement.getCriterion();
        CriterionType type = criterion.getType();
        setCriterionWithItsType(new CriterionWithItsType(type, criterion));
    }

    public CriterionWithItsType getCriterionWithItsType() {
        return criterionWithItsType;
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionWithItsType) {
        this.criterionWithItsType = criterionWithItsType;
    }

    public void setCriterionAndType(String criterionAndType) {
        this.criterionAndType = criterionAndType;
    }

    public String getCriterionAndType() {
        if(criterionWithItsType == null) return criterionAndType;
        return criterionWithItsType.getNameAndType();
    }

    public void setNewObject(Boolean isNewObject) {
        this.newObject = isNewObject;
    }

    public boolean isOldObject(){
        return !isNewObject();
    }

    @Override
    public boolean isNewObject() {
        return newObject == null ? false : newObject;
    }

    public void setCriterionRequirement(CriterionRequirement criterionRequirement) {
        this.criterionRequirement = criterionRequirement;
    }

    public CriterionRequirement getCriterionRequirement() {
        return criterionRequirement;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Type _getType() {
        if(type.equals(DIRECT)){
            return Type.DIRECT;
        }else{
            return Type.INDIRECT;
        }
    }

    private void setType(CriterionRequirement criterionRequirement){
        if(criterionRequirement instanceof DirectCriterionRequirement){
            type = DIRECT;
        }else if(criterionRequirement instanceof IndirectCriterionRequirement){
            type = INDIRECT;
        }
    }

    private void setType(Type type){
        if(type.equals(Type.DIRECT)){
            this.type = DIRECT;
        }else{
            this.type = INDIRECT;
        }
    }

    public boolean isDirect(){
        return (type.equals(DIRECT)) ? true : false;
    }

    public boolean isIndirectValid(){
        return (!isDirect()) && (isValid());
    }

    public boolean isIndirectInvalid(){
        return (!isDirect()) && (isInvalid());
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public void setValid(CriterionRequirement requirement) {
        this.valid = true;
        if(criterionRequirement instanceof IndirectCriterionRequirement){
            this.valid = ((IndirectCriterionRequirement)criterionRequirement).isIsValid();
        }
    }

    public boolean isValid() {
        return valid == null ? false : valid;
    }

    public boolean isInvalid(){
        return !isValid();
    }

    public String getLabelValidate(){
        if(isValid()){
            return _("Invalidate");
        }else{
            return _("Validate");
        }
    }

    public void setFlagState(FlagState flagState) {
        this.flagState = flagState;
    }

    public FlagState getFlagState() {
        return flagState;
    }

    public boolean isUpdatable(){
        return (isNewObject() || getFlagState().equals(FlagState.RETRIEVED));
    }

    public boolean isUnmodifiable(){
        return !isUpdatable();
    }
}