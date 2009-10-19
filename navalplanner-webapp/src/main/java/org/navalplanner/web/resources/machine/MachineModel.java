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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MachineModel implements IMachineModel {

    @Autowired
    IResourceDAO resourceDAO;

    @Autowired
    IMachineDAO machineDAO;

    @Autowired
    IBaseCalendarDAO baseCalendarDAO;

    private Machine machine;

    private ClassValidator<Machine> validator = new ClassValidator<Machine>(Machine.class);

    @Autowired
    @Qualifier("subclass")
    private IBaseCalendarModel baseCalendarModel;

    @Override
    public void initCreate() {
        machine = Machine.create();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Machine> getMachines() {
        return machineDAO.getAll();
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        if (machine.getCalendar() != null) {
            baseCalendarModel.checkInvalidValuesCalendar(machine.getCalendar());
        }
        InvalidValue[] invalidValues = validator.getInvalidValues(getMachine());
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
        resourceDAO.save(machine);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(Machine machine) {
        Validate.notNull(machine);
        this.machine = getFromDB(machine);
    }

    private Machine getFromDB(Machine machine) {
        return getFromDB(machine.getId());
    }

    private Machine getFromDB(Long id) {
        try {
            Machine machine = (Machine) resourceDAO.find(id);
            initializeCriterionsSatisfactions(machine
                    .getCriterionSatisfactions());
            return machine;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeCriterionsSatisfactions(
            Set<CriterionSatisfaction> criterionSatisfactions) {
        for (CriterionSatisfaction criterionSatisfaction : criterionSatisfactions) {
            initializeCriterionSatisfaction(criterionSatisfaction);
        }
    }

    private void initializeCriterionSatisfaction(
            CriterionSatisfaction criterionSatisfaction) {
        initializeCriterion(criterionSatisfaction.getCriterion());
    }

    private void initializeCriterion(Criterion criterion) {
        criterion.getName();
        criterion.getName();
        if (criterion.getParent() != null) {
            criterion.getParent().getName();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public void setCalendar(ResourceCalendar resourceCalendar) {
        if (machine != null) {
            machine.setCalendar(resourceCalendar);
        }
    }

    @Override
    public ResourceCalendar getCalendar() {
        return (machine != null) ? machine.getCalendar() : null;
    }

}
