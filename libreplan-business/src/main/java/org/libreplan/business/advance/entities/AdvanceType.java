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

package org.libreplan.business.advance.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.math.BigDecimal;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.libreplan.business.advance.daos.IAdvanceTypeDAO;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.OrderElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 */

public class AdvanceType extends BaseEntity implements IHumanIdentifiable{

    public static AdvanceType create() {
        AdvanceType advanceType = new AdvanceType();
        advanceType.setNewObject(true);
        return advanceType;
    }

    public static AdvanceType create(String unitName,
            BigDecimal defaultMaxValue, boolean updatable,
            BigDecimal unitPrecision, boolean active, boolean percentage) {
        return create(unitName, defaultMaxValue, updatable, unitPrecision,
                active, percentage, false);
    }

    public static AdvanceType create(String unitName,
            BigDecimal defaultMaxValue, boolean updatable,
            BigDecimal unitPrecision, boolean active, boolean percentage,
            boolean qualityForm) {
        return create(new AdvanceType(unitName, defaultMaxValue, updatable,
                unitPrecision, active, percentage, qualityForm));
    }

    private String unitName;

    @NotNull
    private BigDecimal defaultMaxValue = new BigDecimal(100);

    @NotNull
    private boolean updatable = true;

    @NotNull
    private BigDecimal unitPrecision = new BigDecimal(0.1);

    @NotNull
    private boolean active = true;

    @NotNull
    private boolean percentage = false;

    private Boolean qualityForm = false;

    private IAdvanceTypeDAO avanceTypeDAO = Registry.getAdvanceTypeDao();

    /**
     * Constructor for hibernate. Do not use!
     */
    public AdvanceType() {

    }

    private AdvanceType(String unitName, BigDecimal defaultMaxValue,
            boolean updatable, BigDecimal unitPrecision, boolean active,
            boolean percentage, boolean qualityForm) {
        this.unitName = unitName;
        this.percentage = percentage;
        setDefaultMaxValue(defaultMaxValue);
        this.defaultMaxValue.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.updatable = updatable;
        this.unitPrecision = unitPrecision;
        this.unitPrecision.setScale(4, BigDecimal.ROUND_HALF_UP);
        this.active = active;
        this.qualityForm = qualityForm;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @NotEmpty(message = "unit name not specified")
    public String getUnitName() {
        return this.unitName;
    }

    public void setDefaultMaxValue(BigDecimal defaultMaxValue) {
        if (defaultMaxValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "The maximum value must be greater than 0");
        }
        if (percentage) {
            if (defaultMaxValue.compareTo(new BigDecimal(100)) > 0) {
                throw new IllegalArgumentException(
                        "The maximum value for percentage is 100");
            }
        }
        this.defaultMaxValue = defaultMaxValue;
        this.defaultMaxValue.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getDefaultMaxValue() {
        return this.defaultMaxValue;
    }

    public boolean isUpdatable() {
        return this.updatable;
    }

    public boolean isImmutable() {
        return !this.updatable;
    }

    public void setUnitPrecision(BigDecimal precision) {
        this.unitPrecision = precision;
        this.unitPrecision.setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getUnitPrecision() {
        return this.unitPrecision;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }

    public String getType() {
        if (isUpdatable()) {
            return _("User");
        }
        if (isQualityForm()) {
            return _("Quality form");
        }
        return _("Predefined");
    }

    public void doPropagateAdvaceToParent(OrderElement orderElement) {
    }

    public boolean isPrecisionValid(BigDecimal precision) {
        if ((this.defaultMaxValue == null) || (precision == null)) {
            return true;
        }
        return this.defaultMaxValue.compareTo(precision) >= 0;

    }

    public boolean isDefaultMaxValueValid(BigDecimal defaultMaxValue) {
        if ((this.unitPrecision == null) || (defaultMaxValue == null)) {
            return true;
        }
        return this.unitPrecision.compareTo(defaultMaxValue) <= 0;
    }

    public static boolean equivalentInDB(AdvanceType type, AdvanceType otherType) {
        if (type == null || type.getId() == null || otherType == null
                || otherType.getId() == null) {
            return false;
        }
        return type.getId().equals(otherType.getId());
    }

    public void setPercentage(boolean percentage) {
        if (percentage) {
            defaultMaxValue = new BigDecimal(100);
        }
        this.percentage = percentage;
    }

    public boolean getPercentage() {
        return percentage;
    }

    @NotNull(message = "quality form not specified")
    public Boolean isQualityForm() {
        return BooleanUtils.toBoolean(qualityForm);
    }

    public void setQualityForm(Boolean qualityForm) {
        this.qualityForm = BooleanUtils.toBoolean(qualityForm);
    }

    @AssertTrue(message = "advance type marked as quality form but is updatable")
    public boolean checkConstraintIfIsQualityFormIsNotUpdatable() {
        if (isQualityForm()) {
            if (isUpdatable()) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "default max value of percentage advance type must be 100")
    public boolean checkConstraintDefaultMaxValueMustBe100ForPercentage() {
        if (percentage) {
            if (defaultMaxValue.compareTo(new BigDecimal(100)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getHumanId() {
        return unitName;
    }

    @AssertTrue(message = "the advance type name has to be unique. It is already used")
    public boolean checkConstraintUniqueName() {
        if (StringUtils.isBlank(unitName)) {
            return true;
        }
        if (isNewObject()) {
            return !avanceTypeDAO.existsByNameInAnotherTransaction(unitName);
        } else {
            return checkNotExistsOrIsTheSame();
        }
    }

    private boolean checkNotExistsOrIsTheSame() {
        try {
            AdvanceType advanceType = avanceTypeDAO
                    .findUniqueByNameInAnotherTransaction(unitName);
            return advanceType.getId().equals(getId());
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    @AssertTrue(message = "the default max value must be greater than the precision value")
    public boolean checkDefaultMaxValueGreaterThanPrecision() {
        if (defaultMaxValue.compareTo(unitPrecision) == -1) {
            return false;
        }
        return true;
    }

}
