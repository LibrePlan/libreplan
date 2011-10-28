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

package org.libreplan.business.test.resources.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.daos.IMachineDAO;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.MachineWorkersConfigurationUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@MachineDAO}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@Transactional
public class MachineDAOTest {

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    IMachineDAO machineDAO;

    @Autowired
    IWorkerDAO workerDAO;

    private Machine createValidMachine() {
        Machine machine = Machine.create();
        machine.setCode("code");
        machine.setName("name");
        machine.setDescription("description");
        return machine;
    }

    @Test
    public void testInSpringContainer() {
        assertNotNull(machineDAO);
    }

    @Test
    public void testSaveMachine() {
        Machine machine = createValidMachine();
        machineDAO.save(machine);
        assertTrue(machine.getId() != null);
    }

    @Test
    public void testRemoveMachine() throws InstanceNotFoundException {
        Machine machine = createValidMachine();
        machineDAO.save(machine);
        machineDAO.remove(machine.getId());
        assertFalse(machineDAO.exists(machine.getId()));
    }

    @Test
    public void testListMachines() {
        int previous = machineDAO.list(Machine.class).size();
        Machine machine = createValidMachine();
        machineDAO.save(machine);
        List<Machine> list = machineDAO.list(Machine.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testSaveConfigurationUnits() throws InstanceNotFoundException {
        Machine machine = createValidMachine();
        MachineWorkersConfigurationUnit configurationUnit = MachineWorkersConfigurationUnit
                .create(machine, "Operation", new BigDecimal(1));
        Criterion criterion = CriterionDAOTest.createValidCriterion();
        configurationUnit.addRequiredCriterion(criterion);
        machine.addMachineWorkersConfigurationUnit(configurationUnit);
        machineDAO.save(machine);
        assertTrue(machine.getId() != null);
        assertTrue(machine.getConfigurationUnits().size() != 0);
        assertTrue(machine.getConfigurationUnits().iterator().next()
                .getRequiredCriterions().size() != 0);
    }

    @Test
    @NotTransactional
    public void testSaveTwoMachinesWithSameCodeForbidden() {
        final Machine machine = createValidMachine();
        saveMachineInTransaction(machine);
        try {
            saveMachineInTransaction(machine);
            fail("Expected ValidationException");
        } catch (ValidationException e) {}
    }

    private void saveMachineInTransaction(final Machine machine) {
        IOnTransaction<Void> createMachineTransaction =
            new IOnTransaction<Void>() {

            @Override public Void execute() {
                machineDAO.save(machine);
                return null;
            }
        };
        transactionService.runOnTransaction(createMachineTransaction);
    }
}
