package org.zkoss.ganttz.adapters;

import java.util.List;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IStructureNavigator<T> {

    public boolean isLeaf(T object);

    public List<T> getChildren(T object);

}
