/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IAppPropertiesDAO;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderSyncInfoDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.importers.tim.Duration;
import org.libreplan.importers.tim.Person;
import org.libreplan.importers.tim.Product;
import org.libreplan.importers.tim.RegistrationDate;
import org.libreplan.importers.tim.TimOptions;
import org.libreplan.importers.tim.TimeRegistration;
import org.libreplan.importers.tim.TimeRegistrationRequest;
import org.libreplan.importers.tim.TimeRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportTimesheetsToTim implements IExportTimesheetsToTim {

    private static final Log LOG = LogFactory
            .getLog(ExportTimesheetsToTim.class);

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    IOrderSyncInfoDAO orderSyncInfoDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    private List<Worker> workers;

    @Autowired
    private IAppPropertiesDAO appPropertiesDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public void exportTimesheets() {
        Map<String, String> prop = appPropertiesDAO.findByMajorId("Tim");
        List<Order> orders = orderDAO.getOrders();
        for (Order order : orders) {
            OrderSyncInfo orderSyncInfo = orderSyncInfoDAO
                    .findByOrderLastSynchronizedInfo(order);
            if (orderSyncInfo == null) {
                LOG.warn("Order '" + order.getName()
                        + "' is not yet synchronized");
            } else {
                boolean result = exportTimesheets(orderSyncInfo.getCode(),
                        orderSyncInfo.getOrder(), prop);
                LOG.info("Export successful: " + result);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exportTimesheets(String productCode, Order order) {
        if (productCode == null || productCode.isEmpty()) {
            throw new RuntimeException("Product code should not be empty");
        }
        if (order == null) {
            throw new RuntimeException("Order should not be empty");
        }

        Map<String, String> prop = appPropertiesDAO.findByMajorId("Tim");
        return exportTimesheets(productCode, order, prop);

    }

    /**
     * exports time sheets to Tim
     *
     * @param productCode
     *            the product code
     * @param order
     *            the order
     * @param appProperties
     *            the app properties
     *
     * @return true if export is succeeded, false otherwise
     */
    private boolean exportTimesheets(String productCode, Order order,
            Map<String, String> appProperties) {

        workers = workerDAO.findAll();
        LOG.info("workers.findAll(): " + workers.size());
        if (workers == null && workers.isEmpty()) {
            LOG.warn("No workers found!");
            return false;
        }

        String url = appProperties.get("Server");
        String userName = appProperties.get("Username");
        String password = appProperties.get("Password");
        int nrDaysTimesheetToTim = Integer.parseInt(appProperties
                .get("NrDaysTimesheetToTim"));

        Calendar dateNrOfDaysBack = Calendar.getInstance();
        dateNrOfDaysBack.add(Calendar.DAY_OF_MONTH, -nrDaysTimesheetToTim);

        List<WorkReportLine> workReportLines = order.getWorkReportLines(
                new Date(dateNrOfDaysBack.getTimeInMillis()), new Date(), true);
        if (workReportLines == null || workReportLines.isEmpty()) {
            LOG.warn("No work reportlines are found");
            return false;
        }

        List<TimeRegistration> timeRegistrations = new ArrayList<TimeRegistration>();

        for (WorkReportLine workReportLine : workReportLines) {
            TimeRegistration timeRegistration = createExportTimeRegistration(
                    productCode, workReportLine);
            if (timeRegistration != null) {
                timeRegistrations.add(timeRegistration);
            }
        }

        if (timeRegistrations.isEmpty()) {
            LOG.warn("Unable to crate timeregistration request");
            return false;
        }

        TimeRegistrationRequest timeRegistrationRequest = new TimeRegistrationRequest();
        timeRegistrationRequest.setTimeRegistrations(timeRegistrations);

        TimeRegistrationResponse timeRegistrationResponse = TimSoapClient
                .sendRequestReceiveResponse(url, userName, password,
                        timeRegistrationRequest, TimeRegistrationResponse.class);

        if (isRefsListEmpty(timeRegistrationResponse.getRefs())) {
            LOG.warn("Registration response empty refs");
            return false;
        }
        saveSyncInfoOnAnotherTransaction(productCode, order);
        return true;
    }

    /**
     * checks if list of refs is empty
     *
     * @param refs
     *            the list of refs
     * @return true if list is empty otherwise false
     */
    private boolean isRefsListEmpty(List<Integer> refs) {
        if (refs == null) {
            return true;
        }
        refs.removeAll(Collections.singleton(0));
        return refs.isEmpty();
    }

    /**
     * Saves synchronization info
     *
     * @param productCode
     *            the productcode
     * @param order
     *            the order
     */
    private void saveSyncInfoOnAnotherTransaction(final String productCode,
            final Order order) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        OrderSyncInfo orderSyncInfo = OrderSyncInfo
                                .create(order);
                        orderSyncInfo.setCode(productCode);
                        orderSyncInfoDAO.save(orderSyncInfo);
                        return null;
                    }
                });
    }

    /**
     * Creates export time registration
     *
     * @param productCode
     *            the product code
     * @param workReportLine
     *            the workreportLine
     * @return timeRegistration
     */
    private TimeRegistration createExportTimeRegistration(String productCode,
            WorkReportLine workReportLine) {
        Worker worker = getWorker(workReportLine.getResource().getCode());
        if (worker == null) {
            LOG.warn("Worker not found!");
            return null;
        }

        Person person = new Person();
        person.setName(worker.getName());
        // person.setNetworkName(worker.getNif());
        person.setOptions(TimOptions.UPDATE_OR_INSERT);

        Product product = new Product();
        product.setOptions(TimOptions.UPDATE_OR_INSERT);
        product.setCode(productCode);

        RegistrationDate date = new RegistrationDate();
        date.setOptions(TimOptions.UPDATE_OR_INSERT);
        date.setDate(workReportLine.getLocalDate());

        Duration duration = new Duration();
        duration.setOptions(TimOptions.DECIMAL);
        duration.setDuration(workReportLine.getEffort()
                .toHoursAsDecimalWithScale(2).doubleValue());

        TimeRegistration timeRegistration = new TimeRegistration();
        timeRegistration.setPerson(person);
        timeRegistration.setProduct(product);
        timeRegistration.setRegistrationDate(date);
        timeRegistration.setDuration(duration);
        return timeRegistration;
    }

    /**
     * get worker based on the specified <code>code</code>
     *
     * @param code
     * @return worker
     */
    private Worker getWorker(String code) {
        for (Worker worker : workers) {
            if (worker.getCode().equals(code)) {
                return worker;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSyncInfo getOrderLastSyncInfo(Order order) {
        return orderSyncInfoDAO.findByOrderLastSynchronizedInfo(order);
    }

}
