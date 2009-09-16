package org.navalplanner.business.labels.entities;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * Label entity
 *
 * @author Diego Pino Garcia<dpino@igalia.com
 *
 */
public class Label extends BaseEntity {

    @NotEmpty
    private String name;

    @NotNull
    private LabelType type;

    private Set<OrderElement> orderElements;

    // Default constructor, needed by Hibernate
    protected Label() {

    }

    public static Label create(String name) {
        return (Label) create(new Label(name));
    }

    protected Label(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    public Set<OrderElement> getOrderElements() {
        return Collections.unmodifiableSet(orderElements);
    }

    public void addOrderElement(OrderElement orderElement) {
        Validate.notNull(orderElement);
        orderElements.add(orderElement);
    }

    public void removeOrderElement(OrderElement orderElement) {
        orderElements.add(orderElement);
    }
}
