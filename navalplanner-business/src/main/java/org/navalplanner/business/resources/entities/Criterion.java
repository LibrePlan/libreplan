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

package org.navalplanner.business.resources.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.resources.daos.ICriterionDAO;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class Criterion extends IntegrationEntity implements ICriterion {

    public static Criterion createUnvalidated(String code, String name,
        CriterionType type, Criterion parent, Boolean active) {

        Criterion criterion = create(new Criterion(), code);

        criterion.name = name;
        criterion.type = type;
        criterion.parent = parent;

        if (active != null) {
            criterion.active = active;
        }

        return criterion;

    }

    public static Set<Criterion> withAllDescendants(
            Collection<? extends Criterion> originalCriteria) {
        Set<Criterion> result = new HashSet<Criterion>();
        for (Criterion each : originalCriteria) {
            result.add(each);
            result.addAll(withAllDescendants(each.getChildren()));
        }
        return result;
    }

    public static final Comparator<Criterion> byName = new Comparator<Criterion>() {

        @Override
        public int compare(Criterion o1, Criterion o2) {
            if (o1.getName() == null) {
                return 1;
            }
            if (o2.getName() == null) {
                return -1;
            }
            return o1.getName().toLowerCase()
                    .compareTo(o2.getName().toLowerCase());
        }
    };

    public static final Comparator<Criterion> byType = new Comparator<Criterion>() {

        @Override
        public int compare(Criterion o1, Criterion o2) {
            if (o1.getType().getName() == null) {
                return 1;
            }
            if (o2.getType().getName() == null) {
                return -1;
            }
            return o1.getType().getName().toLowerCase()
                    .compareTo(o2.getType().getName().toLowerCase());
        }
    };

    public static final Comparator<Criterion> byInclusion = new Comparator<Criterion>() {

        @Override
        public int compare(Criterion o1, Criterion o2) {
            if (o1.isEquivalent(o2)) {
                return 0;
            }
            if (o1.includes(o2)) {
                return -1;
            } else {
                return 1;
            }
        }
    };

    public static List<Criterion> sortByName(
            Collection<? extends Criterion> criterions) {
        List<Criterion> result = new ArrayList<Criterion>(criterions);
        Collections.sort(result, byName);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Criterion> sortByTypeAndName(
            Collection<? extends Criterion> criterions) {
        List<Criterion> result = new ArrayList<Criterion>(criterions);
        Collections.sort(result,
                ComparatorUtils.chainedComparator(byType, byName));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Criterion> sortByInclusionTypeAndName(
            Collection<? extends Criterion> criterions) {
        List<Criterion> result = new ArrayList<Criterion>(criterions);
        Collections.sort(
                result,
                ComparatorUtils.chainedComparator(new Comparator[] {
                        byInclusion, byType, byName }));
        return result;
    }

    /**
     * Returns a string of criterion names separated by comma
     * @deprecated use {@link #getCaptionFor(ResourceEnum, Collection)} instead
     * @param criteria
     * @return
     */
    @Deprecated
    public static String getCaptionFor(Collection<? extends Criterion> criteria) {
        return getCaptionFor(ResourceEnum.WORKER, criteria);
    }

    public static String getCaptionFor(
            GenericResourceAllocation allocation) {
        return getCaptionFor(allocation.getResourceType(),
                allocation.getCriterions());
    }

    /**
     * Returns a string of criterion names separated by comma
     * @param resourceType
     * @param criteria
     * @return
     */
    public static String getCaptionFor(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria) {
        if (criteria.isEmpty()) {
            return allCaptionFor(resourceType);
        }
        List<String> result = new ArrayList<String>();
        for (Criterion each : criteria) {
            result.add(each.getCompleteName());
        }
        return StringUtils.join(result, ",");
    }

    private static String allCaptionFor(ResourceEnum resourceType) {
        switch (resourceType) {
        case WORKER:
            return allWorkersCaption();
        case MACHINE:
            return allMachinesCaption();
        default:
            throw new RuntimeException("cant handle " + resourceType);
        }
    }

    private static String allWorkersCaption() {
        return _("[generic all workers]");
    }

    private static String allMachinesCaption() {
        return _("[generic all machines]");
    }

    public void updateUnvalidated(String name, Boolean active) {

        if (!StringUtils.isBlank(name)) {
            this.name = name;
        }

        if (active != null) {
            this.active = active;
        }

    }

    public static Criterion create(CriterionType type) {
        return create(new Criterion(type),"");
    }

    public static Criterion create(String name, CriterionType type) {
        return create(new Criterion(name, type));
    }

    public static Criterion createPredefined(String name, CriterionType type) {
        Criterion result = create(name, type);
        result.predefinedCriterionInternalName = name;
        return result;
    }

    private String name;

    private String predefinedCriterionInternalName;

    private CriterionType type;

    private Criterion parent = null;

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
    public boolean isSatisfiedBy(Resource resource, LocalDate start, LocalDate end) {
        return !resource.query().from(this).enforcedInAll(
                Interval.range(start, end)).result().isEmpty();
    }

    @Override
    public boolean isSatisfiedBy(Resource resource, LocalDate atThisDate) {
        return !resource.query().from(this).enforcedInAll(
                Interval.point(atThisDate)).result().isEmpty();
    }

    @NotEmpty(message="criterion name not specified")
    public String getName() {
        return name;
    }

    public String getPredefinedCriterionInternalName() {
        return predefinedCriterionInternalName;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message="criterion type not specified")
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

    @Valid
    public Set<Criterion> getChildren() {
        return children;
    }

    @Valid
    public List<Criterion> getSortedChildren() {
        List<Criterion> children = new ArrayList<Criterion>(getChildren());
        Collections.sort(children, new Comparator<Criterion>() {
            @Override
            public int compare(Criterion o1, Criterion o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return children;
    }

    public void setChildren(Set<Criterion> children) {
        this.children = children;
    }

    public void moveTo(Criterion newParent) {

        if (parent == null) {

            if (newParent != null) {
                parent = newParent;
                parent.getChildren().add(this);
            }

        } else { // parent != null

            if (!parent.equals(newParent)) {
                parent.getChildren().remove(this);
                parent = newParent;
                if (parent != null) {
                    parent.getChildren().add(this);
                }
            }

        }

    }

    public boolean isEquivalent(Criterion other) {
        return new EqualsBuilder().append(getName(), other.getName())
                .append(getType(), other.getType()).isEquals();
    }

    public boolean isEquivalentOrIncludedIn(Criterion other) {
        if (isEquivalent(other)) {
            return true;
        }
        for (Criterion each : other.getChildren()) {
            if (isEquivalentOrIncludedIn(each)) {
                return true;
            }
        }
        return false;
    }

    public boolean includes(Criterion other) {
        if (isEquivalent(other)) {
            return true;
        }
        for (Criterion each : this.getChildren()) {
            if (each.includes(other)) {
                return true;
            }
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

    @Override
    protected ICriterionDAO getIntegrationEntityDAO() {
        return Registry.getCriterionDAO();
    }

    @Override
    public void setCodeAutogenerated(Boolean codeAutogenerated) {
        // do nothing
    }

    @Override
    public Boolean isCodeAutogenerated() {
        return getType() != null ? getType().isCodeAutogenerated() : false;
    }

    @Override
    public String toString() {
        return String.format("%s :: %s", type, name);
    }

}
