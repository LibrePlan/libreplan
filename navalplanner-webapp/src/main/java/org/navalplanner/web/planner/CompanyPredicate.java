package org.navalplanner.web.planner;

import java.util.Date;
import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.orders.IPredicate;
import org.navalplanner.web.orders.OrderPredicate;

/**
 * Checks if {@link Order}, the start date and finish date from associated
 * {@link Task} matches attributes
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CompanyPredicate extends OrderPredicate implements IPredicate {

    public CompanyPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, Boolean includeOrderElements) {
        super(filters, startDate, finishDate, includeOrderElements);
    }

    @Override
    protected boolean acceptFiltersDates(Order order) {
        TaskGroup associatedTaskElement = order.getAssociatedTaskElement();
        return (this.acceptStartDate(associatedTaskElement.getStartDate()) && (acceptFinishDate(associatedTaskElement
                .getEndDate())));
    }
}