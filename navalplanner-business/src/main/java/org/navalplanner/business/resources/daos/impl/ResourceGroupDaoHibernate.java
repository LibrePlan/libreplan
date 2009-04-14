package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IResourceGroupDao;
import org.navalplanner.business.resources.entities.ResourceGroup;

public class ResourceGroupDaoHibernate 
    extends GenericDaoHibernate<ResourceGroup, Long>
    implements IResourceGroupDao {}
