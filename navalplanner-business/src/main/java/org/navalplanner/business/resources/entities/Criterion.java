package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Criterion extends BaseEntity implements ICriterion {

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

    private boolean active = true;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEquivalent(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion other = (Criterion) criterion;
            return new EqualsBuilder().append(getName(), other.getName())
                    .append(getType(), other.getType()).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
