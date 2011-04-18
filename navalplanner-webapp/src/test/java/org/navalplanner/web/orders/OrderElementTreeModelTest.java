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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
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

    private Criterion criterion, criterion2;

    private DirectAdvanceAssignment directAdvanceAssignment,
            directAdvanceAssignment2;

    private MaterialAssignment materialAssignment;

    private Label label;

    private QualityForm qualityForm;

    private OrderElementTemplate template;

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
        criterion = criterionDAO.findByNameAndType("medicalLeave", "LEAVE")
                .get(0);
        DirectCriterionRequirement directCriterionRequirement = DirectCriterionRequirement
                .create(criterion);
        orderElement.addCriterionRequirement(directCriterionRequirement);
    }

    private void addAnotherCriterionRequirement(OrderElement orderElement) {
        criterion2 = criterionDAO.findByNameAndType(
                "hiredResourceWorkingRelationship", "WORK_RELATIONSHIP").get(0);
        DirectCriterionRequirement directCriterionRequirement = DirectCriterionRequirement
                .create(criterion2);
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

    private void addAnotherDirectAdvanceAssignment(OrderElement orderElement)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        directAdvanceAssignment2 = DirectAdvanceAssignment.create(false,
                HUNDRED);
        directAdvanceAssignment2.setAdvanceType(PredefinedAdvancedTypes.UNITS
                .getType());
        orderElement.addAdvanceAssignment(directAdvanceAssignment2);
    }

    private void addLabel(OrderElement orderElement) {
        label = Label.create("label");
        LabelType.create("label-type").addLabel(label);
        orderElement.addLabel(label);
    }

    private void addMaterialAssignment(OrderElement orderElement) {
        materialAssignment = MaterialAssignment.create(Material
                .createUnvalidated("material-code", "material-description",
                        HUNDRED, false));
        orderElement.addMaterialAssignment(materialAssignment);
    }

    private void addQualityForm(OrderElement element) {
        qualityForm = QualityForm.create("quality-form-name",
                "quality-form-description");
        element.addTaskQualityForm(qualityForm);
    }

    private void addTemplate(OrderElement element) {
        template = createNiceMock(OrderElementTemplate.class);
        expect(template.getName()).andReturn("order-element-template-name")
                .anyTimes();
        replay(template);
        element.initializeTemplate(template);
    }

    @Test
    public void checkAddElementWithCriteriaAndAdvancesOnParent()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        addCriterionRequirement(order);
        addDirectAdvanceAssignment(order);

        model.addElement("element", 100);

        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);
        assertTrue(element.getDirectAdvanceAssignments().isEmpty());

        assertThat(element.getCriterionRequirements().size(), equalTo(1));
        CriterionRequirement criterionRequirement = element
                .getCriterionRequirements().iterator().next();
        assertTrue(criterionRequirement instanceof IndirectCriterionRequirement);
        assertThat(criterionRequirement.getCriterion().getName(),
                equalTo(criterion.getName()));
    }

    @Test
    public void checkRemoveElementWithCriteriaAndAdvancesOnParent()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);

        addCriterionRequirement(order);
        addDirectAdvanceAssignment(order);

        OrderLine element = (OrderLine) order.getChildren().get(0);

        model.removeNode(element);
        assertTrue(order.getChildren().isEmpty());
        assertThat(order.getDirectAdvanceAssignments().size(), equalTo(1));
        assertFalse(order.getCriterionRequirements().isEmpty());
    }

    @Test
    public void checkRemoveElementWithCriteriaAndAdvancesOnChild()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) order.getChildren().get(0);

        addCriterionRequirement(element);
        addDirectAdvanceAssignment(element);

        assertNotNull(order
                .getIndirectAdvanceAssignment(directAdvanceAssignment
                        .getAdvanceType()));

        model.removeNode(element);
        assertTrue(order.getChildren().isEmpty());
        assertTrue(order.getDirectAdvanceAssignments().isEmpty());
        assertNull(order.getIndirectAdvanceAssignment(directAdvanceAssignment
                .getAdvanceType()));
        assertTrue(order.getCriterionRequirements().isEmpty());
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

    @Test
    public void checkAddElementOnOrderLineWithCriteriaAndAdvances()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        addCriterionRequirement(order);

        model.addElement("element", 100);
        OrderLine element = (OrderLine) model.getRoot().getChildren().get(0);

        addAnotherCriterionRequirement(element);
        addDirectAdvanceAssignment(element);

        model.addElementAt(element, "element2", 50);

        assertNotNull(order
                .getIndirectAdvanceAssignment(directAdvanceAssignment
                        .getAdvanceType()));
        assertThat(order.getCriterionRequirements().size(), equalTo(1));
        assertTrue(order.getCriterionRequirements().iterator().next() instanceof DirectCriterionRequirement);
        assertThat(order.getWorkHours(), equalTo(150));

        OrderLineGroup container = (OrderLineGroup) order.getChildren().get(0);
        assertThat(container.getCriterionRequirements().size(), equalTo(1));
        assertTrue(container.getCriterionRequirements().iterator().next() instanceof IndirectCriterionRequirement);
        assertTrue(container.getDirectAdvanceAssignments().isEmpty());
        assertNotNull(container
                .getIndirectAdvanceAssignment(directAdvanceAssignment
                        .getAdvanceType()));
        assertThat(container.getWorkHours(), equalTo(150));

        assertThat(container.getChildren().size(), equalTo(2));
        for (OrderElement each : container.getChildren()) {
            if (each.getName().equals("element")) {
                assertThat(each.getCriterionRequirements().size(), equalTo(2));
                assertThat(each.getDirectAdvanceAssignments().size(),
                        equalTo(1));
                assertNotNull(each
                        .getDirectAdvanceAssignmentByType(directAdvanceAssignment
                                .getAdvanceType()));
                assertThat(each.getWorkHours(), equalTo(100));
            } else if (each.getName().equals("element2")) {
                assertThat(each.getCriterionRequirements().size(), equalTo(1));
                assertTrue(each.getCriterionRequirements().iterator().next() instanceof IndirectCriterionRequirement);
                assertTrue(each.getDirectAdvanceAssignments().isEmpty());
                assertThat(each.getWorkHours(), equalTo(50));
            } else {
                fail("Unexpected OrderElment name: " + each.getName());
            }
        }
    }

    @Test
    public void checkAddElementOnOrderLineGroupWithCriteriaAndAdvances()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        addCriterionRequirement(order);

        model.addElement("element", 100);
        OrderLine element = (OrderLine) order.getChildren().get(0);

        model.addElementAt(element, "element2", 50);
        OrderLineGroup container = (OrderLineGroup) order.getChildren().get(0);
        container.setName("container");

        addAnotherCriterionRequirement(container);
        addDirectAdvanceAssignment(container);

        model.addElementAt(container, "element3", 150);

        assertThat(order.getWorkHours(), equalTo(300));

        assertThat(container.getChildren().size(), equalTo(3));
        for (OrderElement each : container.getChildren()) {
            if (each.getName().equals("element3")) {
                assertThat(each.getCriterionRequirements().size(), equalTo(2));
                for (CriterionRequirement criterionRequirement : each
                        .getCriterionRequirements()) {
                    assertTrue(criterionRequirement instanceof IndirectCriterionRequirement);
                }
                assertTrue(each.getDirectAdvanceAssignments().isEmpty());
                assertThat(each.getWorkHours(), equalTo(150));
            }
        }
    }

    @Test
    public void checkRemoveElementOnOnlyOrderLineWithCriteriaAndAdvances()
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        model.addElement("element", 100);
        OrderLine element = (OrderLine) order.getChildren().get(0);
        model.addElementAt(element, "element2", 50);

        OrderLineGroup container = (OrderLineGroup) order.getChildren().get(0);
        model.removeNode(container.getChildren().iterator().next());

        element = (OrderLine) container.getChildren().get(0);

        addCriterionRequirement(element);
        addDirectAdvanceAssignment(element);

        // * infoComponent (code, name, description)
        String name = "container";
        container.setName(name);
        String code = "code";
        container.setCode(code);
        String description = "description";
        container.setDescription(description);

        // * initDate
        Date date = new Date();
        container.setInitDate(date);
        // * deadline
        container.setDeadline(date);

        // * directAdvanceAssignments
        addAnotherDirectAdvanceAssignment(container);

        // * materialAssignments
        addMaterialAssignment(container);

        // * labels
        addLabel(container);

        // * taskQualityForms
        addQualityForm(container);

        // * criterionRequirements
        addAnotherCriterionRequirement(container);

        // * template
        addTemplate(container);

        // * externalCode
        String externalCode = "external-code";
        container.setExternalCode(externalCode);

        model.removeNode(element);

        assertTrue(order.getDirectAdvanceAssignments().isEmpty());
        assertNull(order.getIndirectAdvanceAssignment(directAdvanceAssignment
                .getAdvanceType()));
        assertNotNull(order
                .getIndirectAdvanceAssignment(directAdvanceAssignment2
                        .getAdvanceType()));
        assertTrue(order.getCriterionRequirements().isEmpty());
        assertThat(order.getWorkHours(), equalTo(0));

        element = (OrderLine) order.getChildren().get(0);
        assertThat(element.getWorkHours(), equalTo(0));

        // * infoComponent (code, name, description)
        assertThat(element.getName(), equalTo(name));
        assertThat(element.getCode(), equalTo(code));
        assertThat(element.getDescription(), equalTo(description));

        // * initDate
        assertThat(element.getInitDate(), equalTo(date));
        // * deadline
        assertThat(element.getDeadline(), equalTo(date));

        // * directAdvanceAssignments
        assertThat(element.getDirectAdvanceAssignments().size(), equalTo(1));
        assertNotNull(element
                .getDirectAdvanceAssignmentByType(directAdvanceAssignment2
                        .getAdvanceType()));
        assertThat(element.getDirectAdvanceAssignmentByType(
                directAdvanceAssignment2.getAdvanceType()).getOrderElement(),
                equalTo((OrderElement) element));

        // * materialAssignments
        assertThat(element.getMaterialAssignments().size(), equalTo(1));
        assertThat(element.getMaterialAssignments().iterator().next()
                .getMaterial(), equalTo(materialAssignment.getMaterial()));
        assertThat(element.getMaterialAssignments().iterator().next()
                .getOrderElement(), equalTo((OrderElement) element));

        // * labels
        assertThat(element.getLabels().size(), equalTo(1));
        assertThat(element.getLabels().iterator().next(), equalTo(label));
        assertThat(element.getLabels().iterator().next().getType(),
                equalTo(label.getType()));

        // * taskQualityForms
        assertThat(element.getQualityForms().size(), equalTo(1));
        assertThat(element.getQualityForms().iterator().next(),
                equalTo(qualityForm));
        assertThat(element.getTaskQualityForms().iterator().next()
                .getOrderElement(), equalTo((OrderElement) element));

        // * criterionRequirements
        assertThat(element.getCriterionRequirements().size(), equalTo(1));
        assertTrue(element.getCriterionRequirements().iterator().next()
                .getCriterion().isEquivalent(criterion2));
        assertThat(element.getCriterionRequirements().iterator().next()
                .getOrderElement(), equalTo((OrderElement) element));

        // * template
        assertNotNull(element.getTemplate());
        assertThat(element.getTemplate().getName(), equalTo(template.getName()));

        // * externalCode
        assertThat(element.getExternalCode(), equalTo(externalCode));
    }

}
