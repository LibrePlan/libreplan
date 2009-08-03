/**
 *
 */
package org.zkoss.ganttz;

/**
 * An object that listens to the removing of a dependency. <br />
 * Created at Apr 29, 2009
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface IDependencyRemovedListener {

    public void dependenceRemoved(DependencyComponent dependencyComponent);

}
