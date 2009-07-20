package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.data.ITaskFundamentalProperties;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AutoAdapter implements
        IAdapterToTaskFundamentalProperties<ITaskFundamentalProperties> {

    @Override
    public ITaskFundamentalProperties adapt(ITaskFundamentalProperties object) {
        return object;
    }

    @Override
    public List<DomainDependency<ITaskFundamentalProperties>> getDependenciesOriginating(
            ITaskFundamentalProperties object) {
        return new ArrayList<DomainDependency<ITaskFundamentalProperties>>();
    }

    @Override
    public void addDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        // do nothing
    }

    @Override
    public boolean canAddDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        return true;
    }

    @Override
    public void removeDependency(
            DomainDependency<ITaskFundamentalProperties> dependency) {
        //do nothing
    }

}
