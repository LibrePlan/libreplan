package org.navalplanner.business.common;

/**
 * Base class for all the application entities.
 *
 * It provides the basic behavior for id and version fields.
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

    protected void setId(Long id) {
        this.id = id;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    protected void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    protected boolean isNewObject() {
        return newObject;
    }

    /**
     * Once the has been really saved in DB (not a readonly transaction), it
     * could be necessary to unmark the object as newObject. This is the case if
     * you must use the same instance after the transaction. <br />
     */
    public void dontPoseAsTransientObjectAnymore() {
        setNewObject(false);
    }

}
