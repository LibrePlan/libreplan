package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.entities.Resource;

public class ResourceDaoHibernate extends GenericDaoHibernate<Resource, Long>
    implements IResourceDao {}
