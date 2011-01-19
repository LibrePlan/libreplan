/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.test.ws.subcontract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.OrderElementDTO;
import org.navalplanner.ws.common.api.OrderLineDTO;
import org.navalplanner.ws.common.impl.DateConverter;
import org.navalplanner.ws.subcontract.api.ISubcontractService;
import org.navalplanner.ws.subcontract.api.SubcontractedTaskDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link ISubcontractService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class SubcontractServiceTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        configurationBootstrap.loadRequiredData();
        scenariosBootstrap.loadRequiredData();
    }

    @Autowired
    private ISubcontractService subcontractService;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private OrderLineDTO givenBasicOrderLineDTO(String orderLineCode) {
        OrderLineDTO orderLineDTO = new OrderLineDTO();
        orderLineDTO.name = "Test";
        orderLineDTO.code = orderLineCode;
        orderLineDTO.initDate = DateConverter
                .toXMLGregorianCalendar(new Date());

        return orderLineDTO;
    }

    private ExternalCompany getClientExternalCompanySaved(String name,
            String nif) {
        ExternalCompany externalCompany = ExternalCompany.create(name, nif);
        externalCompany.setClient(true);

        externalCompanyDAO.save(externalCompany);
        externalCompanyDAO.flush();
        sessionFactory.getCurrentSession().evict(externalCompany);

        externalCompany.dontPoseAsTransientObjectAnymore();

        return externalCompany;
    }

    @Test
    @Rollback(false)
    public void testNotRollback() {
        // Just to do not make rollback in order to have the default
        // configuration, needed for prepareForCreate in order to autogenerate
        // the order code
    }

    @Test
    public void invalidSubcontractedTaskDataWithoutExternalCompanyNif() {
        int previous = orderDAO.getOrders().size();

        String orderLineCode = "order-line-code";

        OrderElementDTO orderElementDTO = givenBasicOrderLineDTO(orderLineCode);

        SubcontractedTaskDataDTO subcontractedTaskDataDTO = new SubcontractedTaskDataDTO();
        subcontractedTaskDataDTO.orderElementDTO = orderElementDTO;

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = subcontractService
                .subcontract(subcontractedTaskDataDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void invalidSubcontractedTaskDataWithoutOrderElement() {
        int previous = orderDAO.getOrders().size();

        ExternalCompany externalCompany = getClientExternalCompanySaved(
                "Company", "company-nif");

        SubcontractedTaskDataDTO subcontractedTaskDataDTO = new SubcontractedTaskDataDTO();
        subcontractedTaskDataDTO.externalCompanyNif = externalCompany.getNif();

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = subcontractService
                .subcontract(subcontractedTaskDataDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        assertThat(orderDAO.getOrders().size(), equalTo(previous));
    }

    @Test
    public void validSubcontractedTaskData() {
        int previous = orderDAO.getOrders().size();

        String orderLineCode = "order-line-code";

        OrderElementDTO orderElementDTO = givenBasicOrderLineDTO(orderLineCode);
        ExternalCompany externalCompany = getClientExternalCompanySaved(
                "Company", "company-nif");

        SubcontractedTaskDataDTO subcontractedTaskDataDTO = new SubcontractedTaskDataDTO();
        subcontractedTaskDataDTO.orderElementDTO = orderElementDTO;
        subcontractedTaskDataDTO.externalCompanyNif = externalCompany.getNif();

        String orderName = "Work description";
        String orderCustomerReference = "client-reference-code";
        BigDecimal orderBudget = new BigDecimal(1000).setScale(2);
        subcontractedTaskDataDTO.workDescription = orderName;
        subcontractedTaskDataDTO.subcontractedCode = orderCustomerReference;
        subcontractedTaskDataDTO.subcontractPrice = orderBudget;

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = subcontractService
                .subcontract(subcontractedTaskDataDTO).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        assertThat(orderDAO.getOrders().size(), equalTo(previous + 1));

        Order order = orderDAO.getOrders().get(previous);
        assertNotNull(order.getCode());
        assertTrue(order.isCodeAutogenerated());
        assertNull(order.getExternalCode());
        assertThat(order.getState(),
                equalTo(OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER));
        assertThat(order.getWorkHours(), equalTo(0));
        assertThat(order.getCustomer().getId(),
                equalTo(externalCompany.getId()));
        assertThat(order.getName(), equalTo(orderName));
        assertThat(order.getCustomerReference(),
                equalTo(orderCustomerReference));
        assertThat(order.getTotalBudget(), equalTo(orderBudget));

        List<OrderElement> children = order.getChildren();
        assertThat(children.size(), equalTo(1));
        assertThat(children.get(0).getExternalCode(), equalTo(orderLineCode));
    }

}
