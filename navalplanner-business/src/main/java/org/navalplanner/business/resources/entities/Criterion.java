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

package org.navalplanner.business.resources.entities;


import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.requirements.entities.CriterionRequirement;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class Criterion extends BaseEntity implements ICriterion {

    public static Criterion createUnvalidated(String name, CriterionType type,
        Criterion parent, boolean active) {

        Criterion criterion = new Criterion();

        criterion.name = name;
        criterion.type = type;
        criterion.parent = parent;
        criterion.active = active;
        criterion.setNewObject(true);

        return criterion;

    }

    public static Criterion create(CriterionType type) {
        Criterion criterion = new Criterion(type);
        criterion.setNewObject(true);
        return criterion;
    }

    public static Criterion create(String name, CriterionType type) {
        Criterion criterion = new Criterion(name, type);
        criterion.setNewObject(true);
        return criterion;
    }

    @NotEmpty
    private String name;

    @NotNull
    private CriterionType type;

    private Criterion parent = null;

    @Valid
    private Set<Criterion> children =  new HashSet<Criterion>();

    private boolean active = true;

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();
    /*
     * Just for Hibernate mapping in order to have an unique constraint with
     * name and type properties.
     */
    private Long typeId;

    public static Criterion ofType(CriterionType type) {
        return create(type);
    }

    public static Criterion withNameAndType(String name, CriterionType type) {
        return create(name, type);
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Criterion() {
    }

    private Criterion(CriterionType type) {
        Validate.notNull(type);

        this.type = type;
    }

    private Criterion(String name, CriterionType type) {
        Validate.notEmpty(name);
        Validate.notNull(type);

        this.name = name;
        this.type = type;
    }

    @Override
    public boolean isSatisfiedBy(Resource resource) {
        return !resource.getCurrentSatisfactionsFor(this).isEmpty();
    }

    @Override
    public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
        return !resource.query().from(this).enforcedInAll(
                Interval.range(start, end)).result().isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CriterionType getType() {
        return type;
    }

    public void setType(CriterionType type) {
        this.type = type;
    }

    public String getCompleteName() {
        return type.getName() + " :: " + name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Criterion getParent() {
        return parent;
    }

    public void setParent(Criterion parent) {
        this.parent = parent;
    }

    public Set<Criterion> getChildren() {
        return children;
    }

    public void setChildren(Set<Criterion> children) {
        this.children = children;
    }

    public boolean isEquivalent(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion other = (Criterion) criterion;
            return new EqualsBuilder().append(getName(), other.getName())
                    .append(getType(), other.getType()).isEquals();
        }
        return false;
    }

    @AssertTrue(message="a disabled resource has enabled subresources")
    public boolean checkConstraintActive() {

        if (!active) {
            for (Criterion c : children) {
                if (c.isActive()) {
                    return false;
                }
            }
        }
        return true;

    }

    public Set<CriterionRequirement> getCriterionRequirements() {
        return Collections.unmodifiableSet(criterionRequirements);
    }

    public void setCriterionRequirements(
            Set<CriterionRequirement> criterionRequirements) {
        this.criterionRequirements = criterionRequirements;
    }

    public void removeCriterionRequirement(
            CriterionRequirement criterionRequirement) {
        this.criterionRequirements.remove(criterionRequirement);
    }

    public void addCriterionRequirement(
            CriterionRequirement criterionRequirement) {
        criterionRequirement.setCriterion(this);
        this.criterionRequirements.add(criterionRequirement);
    }
}
