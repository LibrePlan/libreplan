/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.orders;
import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.INewObject;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ResourceEnum;

/**
 * DTO represents the handled data in the form of assigning criterion requirement.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionRequirementWrapper  implements INewObject {

    public final String DIRECT = _("Direct");

    public static String getIndirectTypeLabel() {
        return _("Indirect");
    }

    private String type;

    private String criterionAndType;

    private Boolean newObject = false;

    private CriterionRequirement criterionRequirement;

    private Boolean valid = true;

    private Boolean newException = false;

    private CriterionWithItsType criterionWithItsType;

    private HoursGroupWrapper hoursGroupWrapper;

    public CriterionRequirementWrapper(String type) {
        this.newObject = true;
        this.type = type;
    }

    public CriterionRequirementWrapper(
            CriterionRequirement criterionRequirement,
            HoursGroupWrapper hoursGroupWrapper,
            boolean isNewObject) {
        this.criterionAndType = "";
        this.criterionRequirement = criterionRequirement;
        this.hoursGroupWrapper = hoursGroupWrapper;
        this.initType(criterionRequirement);
        this.initValid(criterionRequirement);
        this.setNewObject(isNewObject);

        if (!isNewObject) {
            Criterion criterion = criterionRequirement.getCriterion();
            CriterionType type = criterion.getType();
            setCriterionWithItsType(new CriterionWithItsType(type, criterion));
        }
    }

    public CriterionWithItsType getCriterionWithItsType() {
        return criterionWithItsType;
    }

    public void setCriterionWithItsType(CriterionWithItsType criterionWithItsType) {
        this.criterionWithItsType = criterionWithItsType;
        if (criterionRequirement != null) {
            if (criterionWithItsType != null) {
                criterionRequirement.setCriterion(criterionWithItsType
                    .getCriterion());
            } else {
                criterionRequirement.setCriterion(null);
            }
        }
    }

    public void setCriterionAndType(String criterionAndType) {
        this.criterionAndType = criterionAndType;
    }

    public String getCriterionAndType() {
        if (criterionWithItsType == null) {
            return criterionAndType;
        }
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
        this.initValid(criterionRequirement);
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

    private void initType(CriterionRequirement criterionRequirement) {
        if (criterionRequirement instanceof DirectCriterionRequirement) {
            type = DIRECT;
        } else if (criterionRequirement instanceof IndirectCriterionRequirement) {
            type = getIndirectTypeLabel();
        }
    }

    public String getTypeToHoursGroup() {
        if (isDirect()) {
            return type;
        }
        return "Exception " + type;
    }

    public boolean isDirect(){
        return (type.equals(DIRECT)) ? true : false;
    }

    public ResourceEnum getResourceTypeHoursGroup() {
        if (hoursGroupWrapper != null) {
            return hoursGroupWrapper.getResourceType();
        }
        return null;
    }

    public boolean isNewDirectAndItsHoursGroupIsWorker() {
        return ((isNewDirect()) && (getResourceTypeHoursGroup() != null) && (getResourceTypeHoursGroup()
                .equals(ResourceEnum.WORKER)));
    }

    public boolean isNewDirectAndItsHoursGroupIsMachine() {
        return ((isNewDirect()) && (getResourceTypeHoursGroup() != null) && (getResourceTypeHoursGroup()
                .equals(ResourceEnum.MACHINE)));
    }

    public boolean isIndirectValid(){
        return (!isDirect()) && (isValid());
    }

    public boolean isIndirectInvalid(){
        return (!isDirect()) && (isInvalid());
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
        if ((criterionRequirement != null)
                && (criterionRequirement instanceof IndirectCriterionRequirement)) {
            ((IndirectCriterionRequirement) criterionRequirement)
                    .setValid(valid);
        }
    }

    private void initValid(CriterionRequirement requirement) {
        this.valid = true;
        if(criterionRequirement instanceof IndirectCriterionRequirement){
            this.valid = ((IndirectCriterionRequirement)criterionRequirement).isValid();
        }
    }

    public boolean isValid() {
        if ((criterionRequirement != null)
                && (criterionRequirement instanceof IndirectCriterionRequirement)) {
            return ((IndirectCriterionRequirement) criterionRequirement)
                    .isValid();
        }
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

    public boolean isUpdatable(){
        return isNewObject();
    }

    public boolean isUnmodifiable() {
        return !isUpdatable();
    }

    public boolean isNewDirect() {
        return (isNewObject() && isDirect());
    }

    public void setNewException(boolean newException) {
        this.newException = newException;
    }

    public boolean isNewException() {
        return newException;
    }

    public boolean isOldDirectOrException() {
        return (!isNewDirect() && !isNewException());
    }
}