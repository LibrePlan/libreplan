package org.zkoss.ganttz.adapters;

import java.util.List;

import org.zkoss.ganttz.util.ITaskFundamentalProperties;

/**
 * Converts a domain object into a {@link ITaskFundamentalProperties}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IAdapterToTaskFundamentalProperties<T> {

    public ITaskFundamentalProperties adapt(T object);

    public List<DomainDependency<T>> getDependenciesOriginating(T object);

    public boolean canAddDependency(DomainDependency<T> dependency);

    public void addDependency(DomainDependency<T> dependency);

}
