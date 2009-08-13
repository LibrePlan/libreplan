/**
 *
 */
package org.zkoss.ganttz;

/**
 * An object that listens to the removing of a dependency. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface IDependencyRemovedListener {

    public void dependenceRemoved(DependencyComponent dependencyComponent);

}
