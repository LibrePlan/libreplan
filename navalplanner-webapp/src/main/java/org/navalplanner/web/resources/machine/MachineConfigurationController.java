/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.web.resources.machine;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.MachineWorkerAssignment;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;


/**
 *
 * @author Lorenzo Tilve <ltilve@igalia.com>
 */
public class MachineConfigurationController extends GenericForwardComposer {

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    private IMachineModel machineModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Grid configurationUnitsGrid;

    private static final Log LOG = LogFactory
            .getLog(MachineConfigurationController.class);

    public MachineConfigurationController() {

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        messages = new MessagesForUser(messagesContainer);
    }

    public void addConfigurationUnit() {
        MachineWorkersConfigurationUnit unit = MachineWorkersConfigurationUnit
                .create(machineModel.getMachine(), "New configuration unit",
                        new BigDecimal(1));
        machineModel.getMachine().addMachineWorkersConfigurationUnit(unit);
        Util.reloadBindings(configurationUnitsGrid);
    }

    public void reload() {
        Util.reloadBindings(configurationUnitsGrid);
    }

    public IMachineModel getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(IMachineModel machineModel) {
        this.machineModel = machineModel;
    }

    public void initConfigurationController(IMachineModel machineModel) {
        this.machineModel = machineModel;
        Util.reloadBindings(configurationUnitsGrid);
    }

    public List<MachineWorkersConfigurationUnit> getConfigurationUnits() {
        return this.machineModel.getConfigurationUnitsOfMachine();
    }

    public List<MachineWorkerAssignment> getWorkerAssignments() {
        // Need to specify concrete unit
        MachineWorkersConfigurationUnit unit = (MachineWorkersConfigurationUnit) this.machineModel
                .getConfigurationUnitsOfMachine().iterator().next();
        return (List<MachineWorkerAssignment>) unit.getWorkerAssignments();
    }

    public List<Criterion> getRequiredCriterions() {
        // Need to specify concrete unit
        MachineWorkersConfigurationUnit unit = (MachineWorkersConfigurationUnit) this.machineModel
                .getConfigurationUnitsOfMachine().iterator().next();
        return (List<Criterion>) unit.getRequiredCriterions();
    }

    public void addWorkerAssignment(MachineWorkersConfigurationUnit unit,
            Component c) {
        machineModel.addWorkerAssigmentToConfigurationUnit(unit);
        Util.reloadBindings(c.getNextSibling());
    }

    public void addCriterionRequirement(MachineWorkersConfigurationUnit unit,
            Component c) {
        machineModel.addCriterionRequirementToConfigurationUnit(unit);
        Util.reloadBindings(c.getNextSibling());
    }

    public void deleteWorkerAssignment(MachineWorkerAssignment assignment) {
        MachineWorkersConfigurationUnit conf = assignment
                .getMachineWorkersConfigurationUnit();
        conf.removeMachineWorkersConfigurationUnit(assignment);

    }

}
