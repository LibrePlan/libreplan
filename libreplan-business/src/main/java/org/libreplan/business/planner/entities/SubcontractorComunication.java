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

package org.libreplan.business.planner.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.externalcompanies.entities.ComunicationType;
import org.libreplan.business.qualityforms.entities.QualityFormItem;

/**
 *  Entity {@link SubcontractorComunication}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia>
 */
public class SubcontractorComunication extends BaseEntity {

    private SubcontractedTaskData subcontractedTaskData;

    private ComunicationType comunicationType;

    private Date comunicationDate;

    private Boolean reviewed = false;

    private List<SubcontractorComunicationValue> subcontratorComunicationValues = new ArrayList<SubcontractorComunicationValue>();

    // Default constructor, needed by Hibernate
    protected SubcontractorComunication() {

    }

    private SubcontractorComunication ( SubcontractedTaskData subcontractedTaskData, ComunicationType comunicationType, Date comunicationDate, Boolean reviewed){
        this.setSubcontractedTaskData(subcontractedTaskData);
        this.setComunicationType(comunicationType);
        this.setComunicationDate(comunicationDate);
        this.setReviewed(reviewed);
    }

    public static SubcontractorComunication create(
            SubcontractedTaskData subcontractedTaskData,
            ComunicationType comunicationType, Date comunicationDate,
            Boolean reviewed) {
        return create(new SubcontractorComunication(subcontractedTaskData,
                comunicationType, comunicationDate, reviewed));
    }

    public static SubcontractorComunication create() {
        return create(new SubcontractorComunication());
    }

    public void setSubcontractedTaskData(SubcontractedTaskData subcontractedTaskData) {
        this.subcontractedTaskData = subcontractedTaskData;
    }

    @NotNull(message="subcontrated task data not specified")
    public SubcontractedTaskData getSubcontractedTaskData() {
        return subcontractedTaskData;
    }

    public void setComunicationType(ComunicationType comunicationType) {
        this.comunicationType = comunicationType;
    }

    public ComunicationType getComunicationType() {
        return comunicationType;
    }

    public void setComunicationDate(Date comunicationDate) {
        this.comunicationDate = comunicationDate;
    }

    public Date getComunicationDate() {
        return comunicationDate;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setSubcontratorComunicationValues(
            List<SubcontractorComunicationValue> subcontratorComunicationValues) {
        this.subcontratorComunicationValues = subcontratorComunicationValues;
    }

    public List<SubcontractorComunicationValue> getSubcontratorComunicationValues() {
        return subcontratorComunicationValues;
    }

    public SubcontractorComunicationValue getLastSubcontratorComunicationValues(){
        if (subcontratorComunicationValues.isEmpty()){
            return null;
        }
        return subcontratorComunicationValues.get(subcontratorComunicationValues.size()-1);
    }

    public Date getLastSubcontratorComunicationValueDate(){
        SubcontractorComunicationValue value = getLastSubcontratorComunicationValues();
        return (value == null) ? null : value.getDate();
    }
}