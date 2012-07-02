/*
 * This file is part of LibrePlan
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

package org.libreplan.web.scenarios;

import static org.libreplan.web.I18nHelper._;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.ITemplateModel;
import org.libreplan.web.common.ITemplateModel.IOnFinished;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * Controller for CRUD actions over a {@link Scenario}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ScenarioCRUDController extends BaseCRUDController<Scenario> {

    private static final Log LOG = LogFactory
            .getLog(ScenarioCRUDController.class);

    @Autowired
    private IScenarioModel scenarioModel;

    @Autowired
    private ITemplateModel templateModel;

    @Autowired
    private IScenarioManager scenarioManager;

    private ScenariosTreeitemRenderer scenariosTreeitemRenderer = new ScenariosTreeitemRenderer();

    public Scenario getScenario() {
        return scenarioModel.getScenario();
    }

    public void cancel() {
        scenarioModel.cancel();
    }

    @Override
    public void save() {
        scenarioModel.confirmSave();
    }

    @Override
    protected void initCreate() {
        // Do nothing, direct scenario creation is not allowed it should be
        // derived
    }

    public void goToCreateDerivedForm(Scenario scenario) {
        state = CRUDControllerState.CREATE;
        scenarioModel.initCreateDerived(scenario);
        showEditWindow();
    }

    public ScenariosTreeModel getScenariosTreeModel() {
        return new ScenariosTreeModel(new ScenarioTreeRoot(
                scenarioModel.getScenarios()));
    }

    public ScenariosTreeitemRenderer getScenariosTreeitemRenderer() {
        return scenariosTreeitemRenderer;
    }

    public class ScenariosTreeitemRenderer implements TreeitemRenderer {

        @Override
        public void render(final Treeitem item, Object data) {
            SimpleTreeNode simpleTreeNode = (SimpleTreeNode) data;
            final Scenario scenario = (Scenario) simpleTreeNode.getData();
            item.setValue(data);

            Scenario currentScenario = scenarioManager.getCurrent();
            boolean isCurrentScenario = currentScenario.getId().equals(
                    scenario.getId());

            Treerow treerow = new Treerow();

            Treecell nameTreecell = new Treecell();
            Label nameLabel = new Label(scenario.getName());
            nameTreecell.appendChild(nameLabel);
            treerow.appendChild(nameTreecell);

            Treecell operationsTreecell = new Treecell();

            Button createDerivedButton = new Button();
            createDerivedButton.setTooltiptext(_("Create derived"));
            createDerivedButton.setSclass("icono");
            createDerivedButton.setImage("/common/img/ico_derived1.png");
            createDerivedButton.setHoverImage("/common/img/ico_derived.png");

            createDerivedButton.addEventListener(Events.ON_CLICK,
                    new EventListener() {

                @Override
                public void onEvent(Event event) {
                    goToCreateDerivedForm(scenario);
                }

            });
            operationsTreecell.appendChild(createDerivedButton);

            Button editButton = Util.createEditButton(new EventListener() {

                @Override
                public void onEvent(Event event) {
                    goToEditForm(scenario);
                }

            });
            operationsTreecell.appendChild(editButton);

            Button removeButton = Util.createRemoveButton(new EventListener() {

                @Override
                public void onEvent(Event event) {
                    confirmDelete(scenario);
                }

            });

            boolean isMainScenario = PredefinedScenarios.MASTER.getScenario()
                    .getId().equals(scenario.getId());
            List<Scenario> derivedScenarios = scenarioModel
                    .getDerivedScenarios(scenario);
            if (isCurrentScenario || isMainScenario
                    || !derivedScenarios.isEmpty()) {
                removeButton.setDisabled(true);
            }
            operationsTreecell.appendChild(removeButton);

            Button connectButton = new Button(_("Connect"));
            connectButton.addEventListener(Events.ON_CLICK,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) {
                            connectTo(scenario);
                        }

                        private void connectTo(Scenario scenario) {
                            templateModel.setScenario(SecurityUtils
                                    .getSessionUserLoginName(),
                                    scenario,
                                    new IOnFinished() {
                                        @Override
                                        public void onWithoutErrorFinish() {
                                            Executions
                                                    .sendRedirect("/scenarios/scenarios.zul");
                                        }

                                        @Override
                                        public void errorHappened(
                                                Exception exceptionHappened) {
                                            errorHappenedDoingReassignation(exceptionHappened);
                                        }
                                    });
                        }

                    });
            if (isCurrentScenario) {
                connectButton.setDisabled(true);
            }
            operationsTreecell.appendChild(connectButton);

            treerow.appendChild(operationsTreecell);

            item.appendChild(treerow);

            // Show the tree expanded at start
            item.setOpen(true);
        }

    }

    private void errorHappenedDoingReassignation(Exception exceptionHappened) {
        LOG.error("error happened doing reassignation", exceptionHappened);
        messagesForUser.showMessage(Level.ERROR, _(
                "error doing reassignment: {0}", exceptionHappened));
    }

    public Set<Order> getOrders() {
        Scenario scenario = scenarioModel.getScenario();
        if (scenario == null) {
            return Collections.emptySet();
        }
        return scenario.getOrders().keySet();
    }

    @Override
    protected String getEntityType() {
        return "Scenario";
    }

    @Override
    protected String getPluralEntityType() {
        return "Scenarios";
    }

    @Override
    protected void initEdit(Scenario scenario) {
        scenarioModel.initEdit(scenario);
    }

    @Override
    protected Scenario getEntityBeingEdited() {
        return scenarioModel.getScenario();
    }

    @Override
    protected void delete(Scenario scenario) throws InstanceNotFoundException {
        scenarioModel.remove(scenario);
    }

}
