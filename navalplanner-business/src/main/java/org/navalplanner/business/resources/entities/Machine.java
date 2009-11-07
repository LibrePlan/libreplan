package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotEmpty;

public class Machine extends Resource {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    private Set<MachineWorkersConfigurationUnit> configurationUnits = new HashSet<MachineWorkersConfigurationUnit>();

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

    protected Machine() {

    }

    protected Machine(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static Machine create() {
        return (Machine) create(new Machine());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean satisfiesCriterions(Set<Criterion> criterions) {
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                new ArrayList<ICriterion>(criterions)).getResult();
        return compositedCriterion.isSatisfiedBy(this);
    }

}
