/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.reports;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.JasperreportComponent;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class SchedulingProgressPerOrderController extends NavalplannerReportController {

    private static final String REPORT_NAME = "schedulingProgressPerOrderReport";

    private ISchedulingProgressPerOrderModel schedulingProgressPerOrderModel;

    private Listbox lbOrders;

    private Listbox lbAdvanceType;

    private Datebox referenceDate;

    private Datebox startingDate;

    private Datebox endingDate;

    private BandboxSearch bdOrders;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        lbAdvanceType.setSelectedIndex(0);
        schedulingProgressPerOrderModel.init();
    }

    public List<Order> getAllOrders() {
        return schedulingProgressPerOrderModel.getOrders();
    }

    public List<Order> getSelectedOrdersToFilter() {
        return (getSelectedOrders().isEmpty()) ? Collections
                .unmodifiableList(getAllOrders())
                : getSelectedOrders();
    }

    /**
     * Return selected orders, if none are selected return all orders in listbox
     * @return
     */
    public List<Order> getSelectedOrders() {
        return Collections.unmodifiableList(schedulingProgressPerOrderModel
                .getSelectedOrders());
    }

    public void onSelectOrder() {
        Order order = (Order) bdOrders.getSelectedElement();
        if (order == null) {
            throw new WrongValueException(bdOrders, _("please, select a project"));
        }
        boolean result = schedulingProgressPerOrderModel
                .addSelectedOrder(order);
        if (!result) {
            throw new WrongValueException(bdOrders,
                    _("This project has already been added."));
        } else {
            Util.reloadBindings(lbOrders);
        }
        bdOrders.clear();
    }

    public void onRemoveOrder(Order order) {
        schedulingProgressPerOrderModel.removeSelectedOrder(order);
        Util.reloadBindings(lbOrders);
    }

    protected String getReportName() {
        return REPORT_NAME;
    }

    protected JRDataSource getDataSource() {
        List<Order> orders = getSelectedOrdersToFilter();

        return schedulingProgressPerOrderModel
                .getSchedulingProgressPerOrderReport(orders, getAdvanceType(),
                        startingDate.getValue(), endingDate.getValue(),
                        new LocalDate(getReferenceDate()));
   }

    private Date getReferenceDate() {
        Date result = referenceDate.getValue();
        if (result == null) {
            referenceDate.setValue(new Date());
        }
        return referenceDate.getValue();
    }

    private Date getStartingDate() {
        return startingDate.getValue();
    }

    private Date getEndingDate() {
        return endingDate.getValue();
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("referenceDate", getReferenceDate());
        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());
        result.put("orderName", getSelectedOrderNames());
        result.put("advanceType", asString(getSelectedAdvanceType()));

        return result;
    }

    private AdvanceTypeDTO getSelectedAdvanceType() {
        final Listitem item = lbAdvanceType.getSelectedItem();
        return (AdvanceTypeDTO) item.getValue();
    }

    private String asString(AdvanceTypeDTO advanceTypeDTO) {
        return (advanceTypeDTO != null) ? advanceTypeDTO.getName() : _("SPREAD");
    }

    public AdvanceType getAdvanceType() {
        final AdvanceTypeDTO advanceTypeDTO = getSelectedAdvanceType();
        return (advanceTypeDTO != null) ? advanceTypeDTO.getAdvanceType() : null;
    }

    public String getSelectedOrderNames() {
        List<String> orderNames = new ArrayList<String>();

        final Set<Listitem> listItems = lbOrders.getSelectedItems();
        for (Listitem each: listItems) {
            final Order order = (Order) each.getValue();
            orderNames.add(order.getName());
        }
        return (!orderNames.isEmpty()) ? StringUtils.join(orderNames, ",") : _("All");
    }

    public List<AdvanceTypeDTO> getAdvanceTypeDTOs() {
        List<AdvanceTypeDTO> result = new ArrayList<AdvanceTypeDTO>();

        // Add value Spread
        AdvanceTypeDTO advanceTypeDTO = new AdvanceTypeDTO();
        advanceTypeDTO.setAdvanceType(null);
        advanceTypeDTO.setName(_("SPREAD"));
        result.add(advanceTypeDTO);

        final List<AdvanceType> advanceTypes = schedulingProgressPerOrderModel.getAdvanceTypes();
        for (AdvanceType each: advanceTypes) {
            result.add(new AdvanceTypeDTO(each));
        }

        return result;
    }

    public void checkCannotBeHigher(Datebox dbStarting, Datebox dbEnding) {
        dbStarting.clearErrorMessage(true);
        dbEnding.clearErrorMessage(true);

        final Date startingDate = (Date) dbStarting.getValue();
        final Date endingDate = (Date) dbEnding.getValue();

        if (endingDate != null && startingDate != null && startingDate.compareTo(endingDate) > 0) {
            throw new WrongValueException(dbStarting, _("Cannot be higher than Ending date"));
        }
    }

    public void showReport(JasperreportComponent jasperreport){
        checkCannotBeHigher(startingDate, endingDate);
        super.showReport(jasperreport);
    }

    public class AdvanceTypeDTO {

        private String name;

        private AdvanceType advanceType;

        public AdvanceTypeDTO() {

        }

        public AdvanceTypeDTO(AdvanceType advanceType) {
            this.name = advanceType.getUnitName().toUpperCase();
            this.advanceType = advanceType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AdvanceType getAdvanceType() {
            return advanceType;
        }

        public void setAdvanceType(AdvanceType advanceType) {
            this.advanceType = advanceType;
        }

    }

}
