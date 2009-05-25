package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.NotEmpty;

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

    @NotEmpty
    private String type;

    private boolean active = true;

    public static Criterion ofType(String type) {
        Validate.notEmpty(type);
        Criterion result = new Criterion();
        result.type = type;
        return result;
    }

    public static Criterion withNameAndType(String name, String type) {
        return new Criterion(name, type);
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Criterion() {
    }

    private Criterion(String name, String type) {
        Validate.notEmpty(name);
        Validate.notEmpty(type);
        this.type = type;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean isSatisfiedBy(Resource resource) {
        return !resource.getActiveSatisfactionsFor(this).isEmpty();
    }

    public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
        return !resource.getActiveSatisfactionsForIn(this, start, end)
                .isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
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

}
