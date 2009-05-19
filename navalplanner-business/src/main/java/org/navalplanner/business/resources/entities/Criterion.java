package org.navalplanner.business.resources.entities;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.NotEmpty;

/**
 * A criterion stored in the database <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Criterion implements ICriterion, Serializable {

    @SuppressWarnings("unused")
    private long version;

    @NotEmpty
    private String name;

    @NotEmpty
    private String type;

    private boolean active = true;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Criterion() {
    }

    public Criterion(String name, String type) {
        Validate.notNull(name);
        Validate.notNull(type);
        this.type = type;
        this.name = name;
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

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Criterion) {
            Criterion other = (Criterion) obj;
            return new EqualsBuilder().append(name, other.name).append(type,
                    other.type).isEquals();
        }
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
