/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.ws.expensesheets.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.expensesheet.entities.ExpenseSheetLine;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.expensesheets.api.ExpenseSheetDTO;
import org.libreplan.ws.expensesheets.api.ExpenseSheetLineDTO;
import org.libreplan.ws.expensesheets.api.ExpenseSheetListDTO;

/**
 * Converter from/to {@link ExpenseSheet} related entities to/from DTOs.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public final class ExpenseSheetConverter {

    private ExpenseSheetConverter() {
    }

    public final static ExpenseSheetListDTO toDTO(Collection<ExpenseSheet> expenseSheets) {
        List<ExpenseSheetDTO> expenseSheetDTOs = new ArrayList<ExpenseSheetDTO>();

        for (ExpenseSheet expenseSheet : expenseSheets) {
            expenseSheetDTOs.add(toDTO(expenseSheet));
        }

        return new ExpenseSheetListDTO(expenseSheetDTOs);
    }

    public final static ExpenseSheetDTO toDTO(ExpenseSheet expenseSheet) {
        List<ExpenseSheetLineDTO> lineDTOs = new ArrayList<ExpenseSheetLineDTO>();
        for (ExpenseSheetLine line : expenseSheet.getExpenseSheetLines()) {
            lineDTOs.add(toDTO(line));
        }
        return new ExpenseSheetDTO(expenseSheet.getCode(),
                expenseSheet.getDescription(), lineDTOs);
    }

    private static ExpenseSheetLineDTO toDTO(ExpenseSheetLine line) {
        if (line != null) {

            String code = line.getCode();
            if (StringUtils.isBlank(code)) {
                throw new ValidationException(
                        "missing code in the expense sheet line");
            }

            BigDecimal value = line.getValue();
            if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
                value = BigDecimal.ZERO;
            }
            String resourceCode = null;
            if (line.getResource() != null) {
                resourceCode = line.getResource().getCode();
            }

            String orderElementCode = null;
            if (line.getOrderElement() != null) {
                orderElementCode = line.getOrderElement().getCode();
            } else {
                throw new ValidationException(
                        "missing order element code in a expense sheet line");
            }

            XMLGregorianCalendar date = null;
            if (line.getDate() != null) {
                date = DateConverter.toXMLGregorianCalendar(line.getDate());
            } else {
                throw new ValidationException(
                        "missing date in a expense sheet line");
            }

            return new ExpenseSheetLineDTO(code, line.getConcept(), value,
                    resourceCode, orderElementCode, date);
        } else {
            throw new ValidationException(
                    "the expense sheet line is not initialized");
        }
    }

    public final static ExpenseSheet toEntity(ExpenseSheetDTO expenseSheetDTO) {
        ExpenseSheet expenseSheet = ExpenseSheet.create();
        expenseSheet.setCode(expenseSheetDTO.code);
        expenseSheet.setDescription(expenseSheetDTO.description);

        for (ExpenseSheetLineDTO lineDTO : expenseSheetDTO.lines) {
            expenseSheet.add(toEntity(lineDTO, expenseSheet));
        }

        return expenseSheet;
    }

    private static ExpenseSheetLine toEntity(ExpenseSheetLineDTO lineDTO, ExpenseSheet expenseSheet) {
        String code = lineDTO.code;
        if(StringUtils.isBlank(code)){
            throw new ValidationException("missing code expense sheet line");
        }

        BigDecimal value = lineDTO.value;
        String concept = lineDTO.concept;

        LocalDate date = null;
        if (lineDTO.date != null) {
            date = DateConverter.toLocalDate(lineDTO.date);
        }

        String orderElementCode = lineDTO.orderElement;
        OrderElement orderElement = null;
        try{
            orderElement = Registry.getOrderElementDAO().findByCode(
                    orderElementCode);
        }catch (InstanceNotFoundException e) {
            throw new ValidationException(
                    "There is no order element with this code");
        }

        ExpenseSheetLine line = ExpenseSheetLine.create(value, concept, date,
                orderElement);
        line.setExpenseSheet(expenseSheet);
        line.setCode(code);

        if(lineDTO.resource != null){
            String resourceCode = lineDTO.resource;
            try{
                Resource resource = Registry.getResourceDAO().findByCode(resourceCode);
                line.setResource(resource);
            }catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

        return line;
    }

    public final static void updateExpenseSheet(ExpenseSheet expenseSheet,
            ExpenseSheetDTO expenseSheetDTO) throws ValidationException {

        if (StringUtils.isBlank(expenseSheetDTO.code)) {
            throw new ValidationException("missing code in a expense sheet.");
        }

        if (!StringUtils.isBlank(expenseSheetDTO.description)) {
            expenseSheet.setDescription(expenseSheetDTO.description);
        }
        /*
         * 1: Update the existing expense sheet line or add new expense sheet
         * line.
         */
        for (ExpenseSheetLineDTO lineDTO : expenseSheetDTO.lines) {

            /* Step 1.1: requires each expense sheet line DTO to have a code. */
            if (StringUtils.isBlank(lineDTO.code)) {
                throw new ValidationException(
                        "missing code in a expense sheet line");
            }

            ExpenseSheetLine line = expenseSheet
                    .getExpenseSheetLineByCode(lineDTO.code);
            if (line != null) {
                updateExpenseSheetLine(line, lineDTO);
            } else {
                expenseSheet.add(toEntity(lineDTO, expenseSheet));
            }

        }
    }

    public final static void updateExpenseSheetLine(ExpenseSheetLine line,
            ExpenseSheetLineDTO lineDTO) throws ValidationException {

        /*
         * 1: Update the concept
         */
        if (lineDTO.concept != null) {
            line.setConcept(lineDTO.concept);
        }
        /*
         * 2: Update the value
         */
        if (lineDTO.value != null) {
            line.setValue(lineDTO.value);
        }

        /*
         * 3: Update the order element
         */
        String orderElementCode = lineDTO.orderElement;
        if (!StringUtils.isBlank(orderElementCode)) {
            try {
                OrderElement orderElement = Registry.getOrderElementDAO()
                        .findUniqueByCode(orderElementCode);
                line.setOrderElement(orderElement);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException("There is no task with this code");
            }
        }

        /* Step 3.1: Update the date. */
        if (lineDTO != null) {
            LocalDate date = DateConverter.toLocalDate(lineDTO.date);
            line.setDate(date);
        }

        /* Step 3.4: Update the resource. */
        if (lineDTO.resource != null) {
            try {
                Resource resource = Registry.getResourceDAO().findByCode(
                        lineDTO.resource);
                line.setResource(resource);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        "There is no resource with this code");
            }
        }

    }

}