/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.advance.bootstrap;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.math.BigDecimal;

import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.Registry;

public enum PredefinedAdvancedTypes {

    CHILDREN(_("children"), new BigDecimal(100), new BigDecimal(0.01), true,
            false),
    PERCENTAGE(_("percentage"), new BigDecimal(100),
            new BigDecimal(0.01), true, false),
    UNITS(_("units"),
            new BigDecimal(Integer.MAX_VALUE), new BigDecimal(1), false, false),
    SUBCONTRACTOR(
            _("subcontractor"), new BigDecimal(100), new BigDecimal(0.01),
            true, false);

    private PredefinedAdvancedTypes(String name, BigDecimal defaultMaxValue,
            BigDecimal precision, boolean percentage, boolean qualityForm) {
        this.name = name;
        this.defaultMaxValue = defaultMaxValue.setScale(4,
                BigDecimal.ROUND_HALF_UP);
        this.unitPrecision = precision.setScale(4, BigDecimal.ROUND_HALF_UP);
        this.percentage = percentage;
        this.qualityForm = qualityForm;
    }

    private final String name;

    private final BigDecimal defaultMaxValue;

    private final BigDecimal unitPrecision;

    private final boolean percentage;

    private final boolean qualityForm;

    public AdvanceType createType() {
        return AdvanceType.create(name, defaultMaxValue, false, unitPrecision,
                true, percentage, qualityForm);
    }

    public String getTypeName() {
        return name;
    }

    public AdvanceType getType() {
        return Registry.getAdvanceTypeDao().findByName(name);
    }

}
