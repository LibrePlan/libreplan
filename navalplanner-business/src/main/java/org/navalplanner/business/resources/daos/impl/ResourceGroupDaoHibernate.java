package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IResourceGroupDao;
import org.navalplanner.business.resources.entities.ResourceGroup;

/**
 * Hibernate DAO for the <code>ResourceGroup</code> entity.
 * 
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
public class ResourceGroupDaoHibernate 
    extends GenericDaoHibernate<ResourceGroup, Long>
    implements IResourceGroupDao {}
