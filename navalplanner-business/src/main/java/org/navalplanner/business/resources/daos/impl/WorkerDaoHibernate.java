package org.navalplanner.business.resources.daos.impl;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.IWorkerDao;
import org.navalplanner.business.resources.entities.Worker;

public class WorkerDaoHibernate extends GenericDaoHibernate<Worker, Long>
    implements IWorkerDao {}
