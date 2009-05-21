package org.navalplanner.business.resources.bootstrap;

import java.util.List;

import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.resources.entities.ICriterionType;

/**
 * Contract for {@link CriterionsBootstrap}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsBootstrap extends IDataBootstrap {

    public abstract List<ICriterionType<?>> getTypes();

    public abstract void loadRequiredData();

}