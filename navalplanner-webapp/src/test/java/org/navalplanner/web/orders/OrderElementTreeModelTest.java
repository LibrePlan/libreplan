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

package org.navalplanner.web.orders;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link OrderElementTreeModel}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class OrderElementTreeModelTest {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Resource
    private IDataBootstrap scenariosBootstrap;

    @Resource
    private IDataBootstrap criterionsBootstrap;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Autowired
    private ICriterionDAO criterionDAO;

    private Order order;

    private OrderElementTreeModel model;

    private Criterion criterion;

    private DirectAdvanceAssignment directAdvanceAssignment;

    @Before
    public void loadRequiredaData() {
        // Load data
        configurationBootstrap.loadRequiredData();
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
        scenariosBootstrap.loadRequiredData();
        criterionsBootstrap.loadRequiredData();

        givenOrder();
        givenModel();
    }

    private void givenOrder() {
        order = Order.create();
        order.setName("order");
        Scenario scenario = PredefinedScenarios.MASTER.getScenario();
        OrderVersion result = OrderVersion.createInitialVersion(scenario);
        order.setVersionForScenario(scenario, result);
        order.useSchedulingDataFor(scenario);
    }

    private void givenModel() {
        model = new OrderElementTreeModel(order);
    }

    private void addCriterionRequirement(OrderElement orderElement) {
        criterion = criterionDAO.findByNameAndType("medicalLeave",
                "LEAVE").get(0);
        DirectCriterionRequirement directCriterionRequirement = DirectCriterionRequirement
                .create(criterion);
        orderElement.addCriterionRequirement(directCriterionRequirement);
    }

    private void addDirectAdvanceAssignment(OrderElement orderElement)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        directAdvanceAssignment = DirectAdvanceAssignment.create(true, HUNDRED);
        directAdvanceAssignment
                .setAdvanceType(PredefinedAdvancedTypes.PERCENTAGE.getType());
        orderElement.addAdvanceAssignment(directAdvanceAssignment);
    }

    @Test
    public void checkAddElementWithCriteriaAndAdvancesInParent()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        addCriterionRequirement(order);
        addDirectAdvanceAssignment(order);

        model.addElement("element", 100);

        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);
        assertTrue(element.getDirectAdvanceAssignments().isEmpty());

        assertThat(element.getCriterionRequirements().size(), equalTo(1));
        CriterionRequirement criterionRequirement = element.getCriterionRequirements().iterator().next();
        assertTrue(criterionRequirement instanceof IndirectCriterionRequirement);
        assertThat(criterionRequirement.getCriterion().getName(),
                equalTo(criterion.getName()));
    }

    @Test
    public void checkAddCriterionOnChild()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);

        addCriterionRequirement(element);

        assertTrue(order.getCriterionRequirements().isEmpty());

        assertThat(element.getCriterionRequirements().size(), equalTo(1));
        CriterionRequirement criterionRequirement = element
                .getCriterionRequirements().iterator().next();
        assertTrue(criterionRequirement instanceof DirectCriterionRequirement);
        assertTrue(criterionRequirement.getCriterion().isEquivalent(criterion));
    }

    @Test
    public void checkAddCriterionOnParent()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);

        addCriterionRequirement(order);

        assertThat(order.getCriterionRequirements().size(), equalTo(1));
        CriterionRequirement criterionRequirement = order
                .getCriterionRequirements().iterator().next();
        assertTrue(criterionRequirement instanceof DirectCriterionRequirement);
        assertTrue(criterionRequirement.getCriterion().isEquivalent(criterion));

        assertThat(element.getCriterionRequirements().size(), equalTo(1));
        criterionRequirement = element.getCriterionRequirements().iterator()
                .next();
        assertTrue(criterionRequirement instanceof IndirectCriterionRequirement);
        assertTrue(criterionRequirement.getCriterion().isEquivalent(criterion));
    }

    @Test
    public void checkAddAssignmentOnChild()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);

        addDirectAdvanceAssignment(element);
        assertTrue(order.getDirectAdvanceAssignments().isEmpty());
        assertFalse(order.getIndirectAdvanceAssignments().isEmpty());
        assertNotNull(order
                .getIndirectAdvanceAssignment(directAdvanceAssignment
                        .getAdvanceType()));
        assertThat(element.getDirectAdvanceAssignments().size(), equalTo(1));
    }

    @Test
    public void checkAddAdvanceOnParent()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);

        addDirectAdvanceAssignment(order);

        assertThat(order.getDirectAdvanceAssignments().size(), equalTo(1));
        assertNotNull(order
                .getDirectAdvanceAssignmentByType(directAdvanceAssignment
                        .getAdvanceType()));
        assertTrue(element.getDirectAdvanceAssignments().isEmpty());
    }

}