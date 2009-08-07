package org.navalplanner.business.resources.bootstrap;

import org.navalplanner.business.IDataBootstrap;

/**
 * Contract for {@link CriterionsBootstrap}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsBootstrap extends IDataBootstrap {

    public abstract void loadRequiredData();

}