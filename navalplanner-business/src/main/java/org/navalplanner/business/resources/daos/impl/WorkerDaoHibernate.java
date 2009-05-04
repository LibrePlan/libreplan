package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IWorkerDao;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Hibernate DAO for the <code>Worker</code> entity.
 * 
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 *
 */
public class WorkerDaoHibernate extends GenericDaoHibernate<Worker, Long>
    implements IWorkerDao {}
