package org.navalplanner.business.common;

/**
 * TODO
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class BaseEntity {

    private Long id;

    private Long version;

    private boolean newObject = false;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        if (isNewObject()) {
            return null;
        }

        return version;
    }

    protected void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    protected boolean isNewObject() {
        return newObject;
    }

}
