package org.navalplanner.business.planner.entities;

/**
 * This interface represents an object that can be do an allocation based on an
 * amount of {@link ResourcesPerDay}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IAllocatable {
    public void allocate(ResourcesPerDay resourcesPerDay);
}