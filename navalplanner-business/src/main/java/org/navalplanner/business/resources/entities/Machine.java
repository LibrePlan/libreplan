/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IMachineDAO;

/**
* Entity
*
* @author Javier Moran Rua <jmoran@igalia.com>
* @author Fernando Bellas Permuy <fbellas@udc.es>
*/
public class Machine extends Resource {

    private String code;

    private String name;

    private String description;

    private Set<MachineWorkersConfigurationUnit> configurationUnits = new HashSet<MachineWorkersConfigurationUnit>();

    @Valid
    public Set<MachineWorkersConfigurationUnit> getConfigurationUnits() {
        return Collections.unmodifiableSet(configurationUnits);
    }

    public void addMachineWorkersConfigurationUnit(
            MachineWorkersConfigurationUnit unit) {
        configurationUnits.add(unit);
    }

    public void removeMachineWorkersConfigurationUnit(
            MachineWorkersConfigurationUnit unit) {
        configurationUnits.remove(unit);
    }

    public static Machine createUnvalidated(String code, String name,
        String description) {

        Machine machine = create(new Machine());

        machine.code = code;
        machine.name = name;
        machine.description = description;

        return machine;

    }

    protected Machine() {

    }

    protected Machine(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static Machine create() {
        return create(new Machine());
    }

    @NotEmpty(message="machine code not specified")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @NotEmpty(message="machine name not specified")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return code + " :: " + name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean satisfiesCriterions(Set<Criterion> criterions) {
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                new ArrayList<ICriterion>(criterions)).getResult();
        return compositedCriterion.isSatisfiedBy(this);
    }

    @AssertTrue(message="machine code has to be unique. It is already used")
    public boolean checkConstraintUniqueCode() {

        /* Check if it makes sense to check the constraint .*/
        if (!isCodeSpecified()) {
            return true;
        }

        /* Check the constraint. */
        boolean result;
        if (isNewObject()) {
            result = !existsMachineWithTheCode();
        } else {
            result = isIfExistsTheExistentMachineThisOne();
        }
        return result;

    }

    private boolean isCodeSpecified() {
        return !StringUtils.isBlank(code);
    }

    private boolean existsMachineWithTheCode() {
        IMachineDAO machineDAO = Registry.getMachineDAO();
        return machineDAO.existsMachineWithCodeInAnotherTransaction(code);
    }

    private boolean isIfExistsTheExistentMachineThisOne() {
        IMachineDAO machineDAO = Registry.getMachineDAO();
        try {
            Machine machine =
                machineDAO.findUniqueByCodeInAnotherTransaction(code);
            return machine.getId().equals(getId());
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    @Override
    protected boolean isCriterionSatisfactionOfCorrectType(
       CriterionSatisfaction c) {

        return super.isCriterionSatisfactionOfCorrectType(c) ||
            c.getResourceType().equals(ResourceEnum.MACHINE);

    }

}
