package org.navalplanner.web.advance;

import java.math.BigDecimal;
import java.util.List;

import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workreports.entities.WorkReportType;

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
     */
    void prepareForEdit(AdvanceType advanceType);

    /**
     * Makes some operations needed before remove a {@link AdvanceType}.
     * @param AdvanceType
     *            The object to be removed
     */
    void prepareForRemove(AdvanceType advanceType);

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
     * Check if the precision value is less than default max value.
     * @return true if precision is less than default max value.
     */
    public boolean isPrecisionValid(BigDecimal precision);

    /**
     * Check if the default max value is greater than precision value.
     * @return true if default max value is greater than precision value
     */
    public boolean isDefaultMaxValueValid(BigDecimal defaultMaxValue);
}
