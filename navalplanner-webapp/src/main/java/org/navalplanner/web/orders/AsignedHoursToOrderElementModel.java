package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to show the asigned hours of a selected order element
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AsignedHoursToOrderElementModel implements
        IAsignedHoursToOrderElementModel {

    @Autowired
    private final IWorkReportLineDAO workReportLineDAO;

    private int asignedDirectHours;

    private OrderElement orderElement;

    private List<WorkReportLine> listWRL;

    @Autowired
    public AsignedHoursToOrderElementModel(IWorkReportLineDAO workReportLineDAO) {
        Validate.notNull(workReportLineDAO);
        this.workReportLineDAO = workReportLineDAO;
        this.asignedDirectHours = 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportLine> getWorkReportLines() {
        if (orderElement == null) {
            return new ArrayList<WorkReportLine>();
        }
        this.asignedDirectHours = 0;
        this.listWRL = workReportLineDAO.findByOrderElement(orderElement);
        Iterator<WorkReportLine> iterador = listWRL.iterator();
        while (iterador.hasNext()) {
            WorkReportLine w = iterador.next();
            w.getResource().getDescription();
            w.getOrderElement().getWorkHours();
            w.getWorkReport().getDate();
            this.asignedDirectHours = this.asignedDirectHours + w.getNumHours();
        }
        return listWRL;
    }

    @Override
    public int getAsignedDirectHours() {
        if (orderElement == null) {
            return 0;
        }
        return this.asignedDirectHours;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalAsignedHours() {
        if (orderElement == null) {
            return 0;
        }
        return getAddAsignedHours(this.orderElement);
    }

    @Transactional(readOnly = true)
    private int getAddAsignedHours(OrderElement orderElement) {
        int addAsignedHoursChildren = 0;
        if (!orderElement.getChildren().isEmpty()) {
            List<OrderElement> children = orderElement.getChildren();
            Iterator<OrderElement> iterador = children.iterator();
            while (iterador.hasNext()) {
                OrderElement w = iterador.next();
                addAsignedHoursChildren = addAsignedHoursChildren
                        + getAddAsignedHours(w);
            }
        }
        List<WorkReportLine> listWRL = this.workReportLineDAO
                .findByOrderElement(orderElement);
        return (getAsignedDirectHours_(listWRL) + addAsignedHoursChildren);
    }

    @Transactional(readOnly = true)
    private int getAsignedDirectHours_(List<WorkReportLine> listWRL) {
        int asignedDirectHours = 0;
        Iterator<WorkReportLine> iterator = listWRL.iterator();
        while (iterator.hasNext()) {
            asignedDirectHours = asignedDirectHours
                    + iterator.next().getNumHours();
        }
        return asignedDirectHours;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAsignedDirectHoursChildren() {
        if (orderElement == null) {
            return 0;
        }
        if (orderElement.getChildren().isEmpty())
            return 0;
        int asignedDirectChildren = getTotalAsignedHours()
                - this.asignedDirectHours;
        return asignedDirectChildren;
    }

    @Override
    @Transactional(readOnly = true)
    public void initOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    @Override
    @Transactional(readOnly = true)
    public int getEstimatedHours() {
        if (orderElement == null) {
            return 0;
        }
        return orderElement.getWorkHours();
    }

    @Override
    @Transactional(readOnly = true)
    public int getProgressWork() {
        if (orderElement == null) {
            return 0;
        }
        double addAsignedHours = getTotalAsignedHours();
        double estimatedHours = getEstimatedHours();
        if (estimatedHours < 1)
            return 0;
        return (int) (((double) (addAsignedHours / estimatedHours)) * 100);
    }
}
