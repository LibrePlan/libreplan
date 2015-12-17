package org.libreplan.business.common.entities;

import org.libreplan.business.common.BaseEntity;

/**
 * Limits entity, represents a limits for any functionality.
 * This class is intended to work as a Hibernate component.
 * It represents the limit that can be modified only in database.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 17.12.2015.
 */
public class Limits extends BaseEntity{

    private String type;

    private Long value;


    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Long getValue() {
        return value;
    }
    public void setValue(Long value) {
        this.value = value;
    }
}
