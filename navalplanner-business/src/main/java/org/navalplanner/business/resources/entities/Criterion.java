package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.resources.services.CriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Criterion implements ICriterion {

    private Long id;

    @SuppressWarnings("unused")
    private long version;

    @NotEmpty
    private String name;

    @NotNull
    private CriterionType type;

    private boolean active = true;

    public static Criterion ofType(CriterionType type) {
        return new Criterion(type);
    }

    public static Criterion withNameAndType(String name, CriterionType type) {
        return new Criterion(name, type);
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Criterion() {
    }

    public Criterion(CriterionType type) {
        Validate.notNull(type);

        this.type = type;
    }

    public Criterion(String name, CriterionType type) {
        Validate.notEmpty(name);
        Validate.notNull(type);

        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
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
