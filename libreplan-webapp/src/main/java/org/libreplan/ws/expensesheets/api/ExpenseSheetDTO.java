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

package org.libreplan.ws.expensesheets.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link ExpenseSheet} entity.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "expense-sheet")
public class ExpenseSheetDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "expense-sheet";

    @XmlAttribute
    public String description;

    @XmlElementWrapper(name = "expense-sheet-line-list")
    @XmlElement(name = "expense-sheet-line")
    public List<ExpenseSheetLineDTO> lines = new ArrayList<ExpenseSheetLineDTO>();

    public ExpenseSheetDTO() {
    }

    public ExpenseSheetDTO(String code, String description,
            List<ExpenseSheetLineDTO> lines) {
        super(code);
        this.description = description;
        this.lines = lines;
    }

    public ExpenseSheetDTO(String description, List<ExpenseSheetLineDTO> lines) {
        this(generateCode(), description, lines);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
