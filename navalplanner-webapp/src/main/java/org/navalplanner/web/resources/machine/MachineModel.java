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

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Machine;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Machine machine;

    private ClassValidator<Machine> validator = new ClassValidator<Machine>(Machine.class);

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
        InvalidValue[] invalidValues = validator.getInvalidValues(getMachine());
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
        resourceDAO.save(machine);
    }

}
