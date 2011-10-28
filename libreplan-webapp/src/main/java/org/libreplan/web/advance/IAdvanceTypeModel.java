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

package org.libreplan.web.advance;

import java.math.BigDecimal;
import java.util.List;

import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.workreports.entities.WorkReportType;

/**
 * Contract for {@link WorkRerportType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAdvanceTypeModel {

    /**
     * Gets the current {@link WorkReportType}.
     * @return A {@link AdvanceType}
     */
    AdvanceType getAdvanceType();

    /**
     * Gets the {@link List} of {@link AdvanceType}.
     * @return A {@link List} of {@link AdvanceType}
     */
    List<AdvanceType> getAdvanceTypes();

    /**
     * Stores the current {@link AdvanceType}.
     * @throws ValidationException
     *             If validation fails
     */
    void save() throws ValidationException;

    /**
     * Deletes the {@link AdvanceType} passed as parameter.
     * @param AdvanceType
     *            The object to be removed
     */
    void remove(AdvanceType advanceType);

    /**
     * Makes some operations needed before create a new {@link AdvanceType}.
     */
    void prepareForCreate();

    /**
     * Makes some operations needed before edit a {@link AdvanceType}.
     * @param AdvanceType
     *            The object to be edited
     * @throws IllegalArgumentException
     *             if {@link IAdvanceTypeModel#canBeModified(AdvanceType)} is
     *             false
     */
    void prepareForEdit(AdvanceType advanceType);

    /**
     * Makes some operations needed before remove a {@link AdvanceType}.
     * @param AdvanceType
     *            The object to be removed
     * @throws IllegalArgumentException
     *             if {@link IAdvanceTypeModel#canBeModified(AdvanceType)} is
     *             false
     */
    void prepareForRemove(AdvanceType advanceType)
            throws IllegalArgumentException;

    /**
     * Check if it's or not updatable a {@link AdvanceType}
     * @return the type according to the updatable value of the
     *         {@link AdvanceType}
     */

    /**
     * Check if the advance type names are distinct.
     * @return true if the names is not similar
     */
    public boolean distinctNames(String name);

    /**
     * Check if the advance type is updatable and the it can be removed or
     * modified.
     * @return false if the attribute updatable is false.
     */
    public boolean canBeModified(AdvanceType advanceType);

    /**
     * Check if the precision value is less than default max value.
     * @return true if precision is less than default max value.
     */
    public boolean isPrecisionValid(BigDecimal precision);

    /**
     * Check if the default max value is greater than precision value.
     * @return true if default max value is greater than precision value
     */
    public boolean isDefaultMaxValueValid(BigDecimal defaultMaxValue);

    void setDefaultMaxValue(BigDecimal defaultMaxValue);

    BigDecimal getDefaultMaxValue();

    void setPercentage(Boolean percentage);

    Boolean getPercentage();

    boolean isImmutable();

    boolean isImmutableOrAlreadyInUse(AdvanceType advanceType);
}
