package org.navalplanner.business.labels.entities;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;

/**
 * LabeType entity
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public class LabelType extends BaseEntity {

    @NotEmpty
    private String name;

    private Set<Label> labels;

    // Default constructor, needed by Hibernate
    private LabelType() {

    }

    public static LabelType create(String name) {
        return (LabelType) create(new LabelType(name));
    }

    protected LabelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void addLabel(Label label) {
        Validate.notNull(label);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
    }

}
