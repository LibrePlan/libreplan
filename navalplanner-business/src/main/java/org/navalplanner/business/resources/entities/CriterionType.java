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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.springframework.stereotype.Component;

/**
 * Base implementation of {@link ICriterionType} <br />

 * @author Diego Pino García <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Component
public class CriterionType extends BaseEntity implements
        ICriterionType<Criterion> {

    public static CriterionType create() {
        CriterionType criterionType = new CriterionType();
        criterionType.setNewObject(true);
        return criterionType;
    }

    public static CriterionType create(String name,String description) {
        CriterionType criterionType = new CriterionType(name,description);
        criterionType.setNewObject(true);
        return criterionType;
    }

    public static CriterionType create(String name,String description,
            boolean allowHierarchy,boolean allowSimultaneousCriterionsPerResource,
            boolean enabled,ResourceEnum resource) {
        CriterionType criterionType = new CriterionType(name,description, allowHierarchy,
                allowSimultaneousCriterionsPerResource,enabled,resource);
        criterionType.setNewObject(true);
        return criterionType;
    }

    @NotEmpty
    private String name;

    private String description;

    private Boolean allowHierarchy = true;

    private Boolean allowSimultaneousCriterionsPerResource = true;

    private Boolean enabled = true;

    private ResourceEnum resource = ResourceEnum.getDefault();

    @Valid
    private Set<Criterion> criterions = new HashSet<Criterion>();

    private int numCriterions;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CriterionType() {

    }

    private CriterionType(String name,String description) {
        this.name = name;
        this.description = description;
    }

    private CriterionType(String name,String description, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource, boolean enabled,
            ResourceEnum resource) {

        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.enabled = enabled;
        this.name = name;
        this.description = description;
        this.resource = resource;
    }

    public static CriterionType asCriterionType(ICriterionType criterionType) {
        return create(criterionType.getName(),criterionType.getDescription(),
                criterionType.allowHierarchy(), criterionType
        .isAllowSimultaneousCriterionsPerResource(),
                criterionType.isEnabled(),
                CriterionType.getResource(criterionType));
    }

    private static ResourceEnum getResource(ICriterionType criterionType) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            if (criterionType.criterionCanBeRelatedTo(resource.asClass())) {
                return resource;
            }
        }

        return ResourceEnum.getDefault();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy == null ? false : allowHierarchy;
    }

    public void setAllowHierarchy(boolean allowHierarchy) {
        this.allowHierarchy = allowHierarchy;
    }

    @Override
    public boolean isAllowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource == null ? false : allowSimultaneousCriterionsPerResource;
    }

    public void setAllowSimultaneousCriterionsPerResource(boolean allowSimultaneousCriterionsPerResource) {
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
    }

    public ResourceEnum resource() {
        return resource;
    }

    public void setResource(ResourceEnum resource) {
        this.resource = resource;
    }

    @Override
    public Criterion createCriterion(String name) {
        return Criterion.withNameAndType(name, this);
    }

    public static Criterion createCriterion(
            PredefinedCriterionTypes predefinedCriterionType, String name) {

        CriterionType criterionType = CriterionType
                .asCriterionType(predefinedCriterionType);

        return Criterion.withNameAndType(name, criterionType);
    }

    @Override
    public Criterion createCriterionWithoutNameYet() {
        return Criterion.ofType(this);
    }

    @Override
    public boolean contains(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion c = (Criterion) criterion;
            return this.equals(c.getType());
        } else {
            return false;
        }
    }

    @Override
    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass) {
        for (ResourceEnum resource : ResourceEnum.values()) {
            if (resource.isAssignableFrom(klass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Two criterion types are equals if they both got the same name
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof CriterionType == false)
            return false;

        if (this == o)
            return true;

        CriterionType criterionType = (CriterionType) o;

        return new EqualsBuilder().append(criterionType.getName(),
                this.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled == null ? false : enabled;
    }

    @Override
    public boolean isImmutable(){
        return !isEnabled();
    }


    public int getNumCriterions(){
        return criterions.size();
    }

// FIXME: Internationalization must be provided.
    @AssertTrue(message="los nombres de los criterios deben ser únicos "
        + "dentro de un tipo de criterio")
    public boolean checkConstraintNonRepeatedCriterionNames() {

        Set<String> criterionNames = new HashSet<String>();

        for (Criterion c : criterions) {
            if (criterionNames.contains(c.getName())) {
                return false;
            }
            criterionNames.add(c.getName());
        }

        return true;

    }

// FIXME: Surprisingly, @AssertTrue in this method causes the Maven build to
// fail due to out of memory (probably due to the configuration of Hibernate to
// automatically execute validations when saving entities). Provisionally,
// "validate" method has been provided as a hack.
// FIXME: Internationalization must be provided.
//    @AssertTrue(message="el nombre del tipo de criterion ya se está usando")
    public boolean checkConstraintUniqueCriterionTypeName() {

        ICriterionTypeDAO criterionTypeDAO = Registry.getCriterionTypeDAO();

        if (isNewObject()) {
            return !criterionTypeDAO.existsByName(this);
        } else {
            try {
                CriterionType c = criterionTypeDAO.findUniqueByName(name);
                return c.getId() == getId();
            } catch (InstanceNotFoundException e) {
                return true;
            }

        }

    }

// FIXME: Internationalization must be provided.
    @AssertTrue(message="el tipo de recurso no permite jerarquía de recursos")
    public boolean checkConstraintAllowHierarchy() {

        if (!allowHierarchy) {
            for (Criterion c : criterions) {
                if (c.getParent() != null) {
                    return false;
                }
            }
        }

        return true;

    }

// FIXME: Internationalization must be provided.
    @AssertTrue(message="el tipo de recurso no permite criterios habilitados")
    public boolean checkConstraintEnabled() {

        if (!enabled) {
            for (Criterion c : criterions) {
                if (c.isActive()) {
                    return false;
                }
            }
        }

        return true;

    }

 // FIXME: hack to overcome problem with @AssertTrue and
 // "checkConstraintUniqueCriterionTypeName" method.
     public InvalidValue[] validate() {

         ClassValidator<CriterionType> criterionTypeValidator =
             new ClassValidator<CriterionType>(CriterionType.class);

         InvalidValue[] invalidValues =
             criterionTypeValidator.getInvalidValues(this);

         if (!checkConstraintUniqueCriterionTypeName()) {
             invalidValues =
                 Arrays.copyOf(invalidValues, invalidValues.length+1);
             invalidValues[invalidValues.length-1] =
                 new InvalidValue("el nombre del tipo de criterion ya se " +
                     "está usando", CriterionType.class,
                     "checkConstraintUniqueCriterionTypeName",
                     null, this);
         }

         return invalidValues;

     }

}
