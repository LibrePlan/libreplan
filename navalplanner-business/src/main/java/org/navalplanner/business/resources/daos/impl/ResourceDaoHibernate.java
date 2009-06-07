package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Hibernate DAO for the <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
public class ResourceDaoHibernate extends GenericDaoHibernate<Resource, Long>
    implements IResourceDao {}
