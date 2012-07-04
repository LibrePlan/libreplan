/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.business.planner.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.qualityforms.entities.QualityFormItem;

/**
 *  Entity {@link SubcontractorCommunication}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia>
 */
public class SubcontractorCommunication extends BaseEntity {

    private SubcontractedTaskData subcontractedTaskData;

    private CommunicationType communicationType;

    private Date communicationDate;

    private Boolean reviewed = false;

    private List<SubcontractorCommunicationValue> subcontractorCommunicationValues = new ArrayList<SubcontractorCommunicationValue>();

    // Default constructor, needed by Hibernate
    protected SubcontractorCommunication() {

    }

    private SubcontractorCommunication ( SubcontractedTaskData subcontractedTaskData, CommunicationType communicationType, Date communicationDate, Boolean reviewed){
        this.setSubcontractedTaskData(subcontractedTaskData);
        this.setCommunicationType(communicationType);
        this.setCommunicationDate(communicationDate);
        this.setReviewed(reviewed);
    }

    public static SubcontractorCommunication create(
            SubcontractedTaskData subcontractedTaskData,
            CommunicationType communicationType, Date communicationDate,
            Boolean reviewed) {
        return create(new SubcontractorCommunication(subcontractedTaskData,
                communicationType, communicationDate, reviewed));
    }

    public static SubcontractorCommunication create() {
        return create(new SubcontractorCommunication());
    }

    public void setSubcontractedTaskData(SubcontractedTaskData subcontractedTaskData) {
        this.subcontractedTaskData = subcontractedTaskData;
    }

    @NotNull(message="subcontrated task data not specified")
    public SubcontractedTaskData getSubcontractedTaskData() {
        return subcontractedTaskData;
    }

    public void setCommunicationType(CommunicationType communicationType) {
        this.communicationType = communicationType;
    }

    public CommunicationType getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationDate(Date communicationDate) {
        this.communicationDate = communicationDate;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setSubcontractorCommunicationValues(
            List<SubcontractorCommunicationValue> subcontractorCommunicationValues) {
        this.subcontractorCommunicationValues = subcontractorCommunicationValues;
    }

    public List<SubcontractorCommunicationValue> getSubcontractorCommunicationValues() {
        return subcontractorCommunicationValues;
    }

    public SubcontractorCommunicationValue getLastSubcontractorCommunicationValues(){
        if (subcontractorCommunicationValues.isEmpty()){
            return null;
        }
        return subcontractorCommunicationValues.get(subcontractorCommunicationValues.size()-1);
    }

    public Date getLastSubcontractorCommunicationValueDate(){
        SubcontractorCommunicationValue value = getLastSubcontractorCommunicationValues();
        return (value == null) ? null : value.getDate();
    }
}
