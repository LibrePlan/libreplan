package org.navalplanner.web.workreports;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * Contract for {@link WorkRerportType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IWorkReportTypeModel {

    /**
     * Gets the current {@link WorkReportType}.
     *
     * @return A {@link WorkReportType}
     */
    WorkReportType getWorkReportType();

    /**
     * Gets the {@link List} of {@link WorkReportType}.
     *
     * @return A {@link List} of {@link WorkReportType}
     */
    List<WorkReportType> getWorkReportTypes();

    /**
     * Stores the current {@link WorkReportType}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void save() throws ValidationException;

    /**
     * Deletes the {@link WorkReportType} passed as parameter.
     *
     * @param workReportType
     *            The object to be removed
     */
    void remove(WorkReportType workReportType);

    /**
     * Makes some operations needed before create a new {@link WorkReportType}.
     */
    void prepareForCreate();

    /**
     * Makes some operations needed before edit a {@link WorkReportType}.
     *
     * @param workReportType
     *            The object to be edited
     */
    void initEdit(WorkReportType workReportType);

    /**
     * Makes some operations needed before remove a {@link WorkReportType}.
     *
     * @param workReportType
     *            The object to be removed
     */
    void prepareForRemove(WorkReportType workReportType);

    /**
     * Gets the {@link Set} of all {@link CriterionType}
     *
     * @return A {@link Set} of {@link CriterionType}
     */
    Set<CriterionType> getCriterionTypes();

    /**
     * Sets the {@link Set} of {@link CriterionType} for the current
     * {@link WorkReportType}.
     *
     * @param criterionTypes
     *            A {@link Set} of {@link CriterionType}
     */
    void setCriterionTypes(Set<CriterionType> criterionTypes);

    /**
     * Check if it's or not editing a {@link WorkReportType}
     *
     * @return true if it's editing a {@link WorkReportType}
     */
    boolean isEditing();

}
