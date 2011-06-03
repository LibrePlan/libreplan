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

package org.navalplanner.ws.costcategories.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.ws.common.impl.DateConverter;
import org.navalplanner.ws.costcategories.api.CostCategoryDTO;
import org.navalplanner.ws.costcategories.api.HourCostDTO;

/**
 * Converter from/to cost-category-related entities to/from DTOs.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CostCategoryConverter {

    private CostCategoryConverter() {
    }

    public final static CostCategoryDTO toDTO(CostCategory costCategory) {

        Set<HourCostDTO> hourCostDTOs = new HashSet<HourCostDTO>();

        for (HourCost h : costCategory.getHourCosts()) {
            hourCostDTOs.add(toDTO(h));
        }

        if (hourCostDTOs.isEmpty()) {
            hourCostDTOs = null;
        }

        return new CostCategoryDTO(costCategory.getCode(), costCategory
                .getName(), costCategory.getEnabled(), hourCostDTOs);

    }

    public final static HourCostDTO toDTO(HourCost hourCost) {

        XMLGregorianCalendar initDate = null;
        if (hourCost.getInitDate() != null) {
            initDate = DateConverter.toXMLGregorianCalendar(hourCost
                    .getInitDate());
        }

        XMLGregorianCalendar endDate = null;
        if (hourCost.getEndDate() != null) {
            endDate = DateConverter.toXMLGregorianCalendar(hourCost
                    .getEndDate());
        }

        String type = null;
        if (hourCost.getType() != null) {
            type = hourCost.getType().getCode();
        }

        return new HourCostDTO(hourCost.getCode(), hourCost.getPriceCost(),
                initDate, endDate, type);

    }

    public final static CostCategory toEntity(CostCategoryDTO costCategoryDTO) {

        CostCategory costCategory = CostCategory.createUnvalidated(StringUtils
                .trim(costCategoryDTO.code), StringUtils
                .trim(costCategoryDTO.name), costCategoryDTO.enabled);

        for (HourCostDTO hourCostDTO : costCategoryDTO.hourCostDTOs) {
            HourCost hourCost = toEntity(hourCostDTO);
            hourCost.setCategory(costCategory);
            costCategory.addHourCost(hourCost);
        }

        return costCategory;

    }

    private static HourCost toEntity(HourCostDTO hourCostDTO)
            throws ValidationException {

        // Mandatory properties
        LocalDate initDate = null;
        if(hourCostDTO.initDate != null){
            initDate = DateConverter.toLocalDate(hourCostDTO.initDate);
        }

        //Create new hour cost
        HourCost hourCost = HourCost.createUnvalidated(hourCostDTO.code,
                hourCostDTO.priceCost, initDate);

        // optional properties
        if (hourCostDTO.endDate != null) {
            hourCost.setEndDate(DateConverter.toLocalDate(hourCostDTO.endDate));
        }

        if (hourCostDTO.type != null) {
            try {
                TypeOfWorkHours typeOfWorkHours = Registry
                        .getTypeOfWorkHoursDAO().findUniqueByCode(
                                hourCostDTO.type);
                hourCost.setType(typeOfWorkHours);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        _("There is no type of work hours with this code"));
            }
        }
        return hourCost;
    }

    public final static void updateCostCategory(CostCategory costCategory,
            CostCategoryDTO costCategoryDTO) throws ValidationException {
        /*
         * 1: Update the existing hour cost or add new hour cost.
         */
        for (HourCostDTO hourCostDTO : costCategoryDTO.hourCostDTOs) {

            /* Step 1.1: requires each hour cost DTO to have a code. */
            if (StringUtils.isBlank(hourCostDTO.code)) {
                throw new ValidationException(_("missing code in a hour cost"));
            }

            try {
                HourCost hourCost = costCategory
                        .getHourCostByCode(hourCostDTO.code);
                updateHourCost(hourCost, hourCostDTO);
            } catch (InstanceNotFoundException e) {
                HourCost hourCost = toEntity(hourCostDTO);
                hourCost.setCategory(costCategory);
                costCategory.addHourCost(hourCost);
            }
        }

        /* 2: Update cost category basic properties. */
        costCategory.updateUnvalidated(StringUtils.trim(costCategoryDTO.name),
                costCategoryDTO.enabled);

    }

    public final static void updateHourCost(HourCost hourCost,
            HourCostDTO hourCostDTO) throws ValidationException {

        // Mandatory properties
        LocalDate initDate = null;
        if (hourCostDTO.initDate != null) {
            initDate = DateConverter.toLocalDate(hourCostDTO.initDate);
        }

        // Create new hour cost
        hourCost.updateUnvalidated(hourCostDTO.priceCost, initDate);

        // optional properties
        if (hourCostDTO.endDate != null) {
            hourCost.setEndDate(DateConverter.toLocalDate(hourCostDTO.endDate));
        }

        if (hourCostDTO.type != null) {
            try {
                TypeOfWorkHours typeOfWorkHours = Registry
                        .getTypeOfWorkHoursDAO().findUniqueByCode(
                                hourCostDTO.type);
                hourCost.setType(typeOfWorkHours);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        _("There is no type of work hours with this code"));
            }
        }
    }
}
