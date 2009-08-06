package org.navalplanner.web.workreports;

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.business.workreports.entities.WorkReportType;

/**
 * Contract for {@link WorkRerportType}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IWorkReportModel {

    /**
     * Gets the current {@link WorkReport}.
     *
     * @return A {@link WorkReport}
     */
    WorkReport getWorkReport();

    /**
     * Stores the current {@link WorkReport}.
     *
     * @throws ValidationException
     *             If validation fails
     */
    void save() throws ValidationException;

    /**
     * Makes some operations needed before create a new {@link WorkReport}.
     */
    void prepareForCreate(WorkReportType workReportType);

    /**
     * Makes some operations needed before edit a {@link WorkReport}.
     *
     * @param workReport
     *            The object to be edited
     */
    void prepareEditFor(WorkReport workReport);

    /**
     * Finds an @{link OrdrElement} by code
     *
     * @param code
     * @return
     */
    OrderElement findOrderElement(String code) throws InstanceNotFoundException;

    /**
     * Find a @{link Worker} by nif
     *
     * @param nif
     * @return
     * @throws InstanceNotFoundException
     */
    Worker findWorker(String nif) throws InstanceNotFoundException;

    /**
     * Converts @{link Resource} to @{link Worker}
     *
     * @param resource
     * @return
     * @throws InstanceNotFoundException
     */
    Worker asWorker(Resource resource) throws InstanceNotFoundException;

    /**
     * Get all {@link WorkReport} elements
     *
     * @return
     */
    List<WorkReport> getWorkReports();

    /**
     * Returns true if WorkReport is being edited
     *
     * @return
     */
    boolean isEditing();

    /**
     * Returns distinguished code for {@link OrderElement}
     *
     * @param orderElement
     * @return
     */
    String getDistinguishedCode(OrderElement orderElement)
            throws InstanceNotFoundException;

    /**
     * Add new {@link WorkReportLine} to {@link WorkReport}
     *
     * @return
     */
    WorkReportLine addWorkReportLine();
}
