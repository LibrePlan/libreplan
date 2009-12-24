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

package org.navalplanner.business.common.entities;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.hibernate.criterion.Order;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.i18n.I18nHelper;

/**
 * Sequence for {@link Order} codes.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderSequence extends BaseEntity {

    public static final Integer MIN_NUMBER_OF_DIGITS = 5;
    public static final Integer MAX_NUMBER_OF_DIGITS = 9;

    public static final String CODE_SEPARATOR = "-";

    public static OrderSequence create(String prefix) {
        return create(new OrderSequence(prefix));
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public OrderSequence() {
    }

    public OrderSequence(String prefix) {
        this.prefix = prefix;
    }

    private String prefix;

    private Integer lastValue = 0;

    private Integer numberOfDigits = MIN_NUMBER_OF_DIGITS;

    private Boolean active = false;

    public void setPrefix(String prefix) throws IllegalArgumentException {
        if (isAlreadyInUse()) {
            throw new IllegalArgumentException(
                    I18nHelper
                            ._("You can not modifiy this order sequence, it is already in use"));
        }

        this.prefix = prefix;
    }

    @NotEmpty(message = "prefix not specified")
    public String getPrefix() {
        if (prefix != null) {
            prefix = prefix.trim();
        }
        return prefix;
    }

    @NotNull(message = "last value not specified")
    public Integer getLastValue() {
        return lastValue;
    }

    @AssertTrue(message = "prefix must not contain white spaces")
    public boolean checkConstraintCompanyCodeWithoutWhiteSpaces() {
        if ((prefix == null) || (prefix.isEmpty())) {
            return false;
        }

        return !prefix.contains(" ");
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isActive() {
        return active;
    }

    public void setNumberOfDigits(Integer numberOfDigits)
            throws IllegalArgumentException {
        if (isAlreadyInUse()) {
            throw new IllegalArgumentException(
                    I18nHelper
                            ._("You can not modifiy this order sequence, it is already in use"));
        }

        if ((numberOfDigits != null)
                && (numberOfDigits >= MIN_NUMBER_OF_DIGITS)
                && (numberOfDigits <= MAX_NUMBER_OF_DIGITS)) {
            this.numberOfDigits = numberOfDigits;
        } else {
            throw new IllegalArgumentException(I18nHelper._(
                    "number of digits must be between {0} and {1}",
                    MIN_NUMBER_OF_DIGITS, MAX_NUMBER_OF_DIGITS));
        }
    }

    @NotNull(message = "number of digits not specified")
    public Integer getNumberOfDigits() {
        return numberOfDigits;
    }

    @AssertTrue(message = "number of digits is out of range")
    public boolean checkConstraintNumberOfDigitsInRange() {
        if ((numberOfDigits != null)
                && (numberOfDigits >= MIN_NUMBER_OF_DIGITS)
                && (numberOfDigits <= MAX_NUMBER_OF_DIGITS)) {
            return true;
        }
        return false;
    }

    public static String formatValue(int numberOfDigits, int value) {
        String format = "";
        for (int i = 0; i < numberOfDigits; i++) {
            format += "0";
        }

        NumberFormat numberFormat = new DecimalFormat(format);
        return numberFormat.format(value);
    }

    public boolean isAlreadyInUse() {
        return lastValue > 0;
    }

    public String getCode() {
        return prefix + CODE_SEPARATOR + formatValue(numberOfDigits, lastValue);
    }

    public void incrementLastValue() {
        lastValue++;
    }

}