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

package org.navalplanner.ws.unittypes.impl;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.ws.unittypes.api.UnitTypeDTO;

/**
 * Service for managing unit-types-related entities.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class UnitTypeConverter {

    private UnitTypeConverter() {
    }

    public final static UnitTypeDTO toDTO(UnitType unitType) {
        return new UnitTypeDTO(unitType.getCode(), unitType.getMeasure());
    }

    public final static UnitType toEntity(UnitTypeDTO unitTypeDTO) {
        return UnitType.create(unitTypeDTO.code, unitTypeDTO.measure);
    }

    public final static void updateUnitType(UnitType unitType,
            UnitTypeDTO unitTypeDTO) throws ValidationException {
        /* 1: Update unit type basic properties. */
        unitType.updateUnvalidated(StringUtils.trim(unitTypeDTO.measure));
    }

}