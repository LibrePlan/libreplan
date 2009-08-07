package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.workreports.entities.WorkReportLine;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IAsignedHoursToOrderElementModel{
    public List<WorkReportLine> getWorkReportLines();
    public int getAsignedDirectHours();
    public int getTotalAsignedHours();
    public int getAsignedDirectHoursChildren();
    public void initOrderElement(OrderElement orderElement);
    public int getEstimatedHours();
    public int getProgressWork();
}
