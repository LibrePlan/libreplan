package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.resources.daos.IWorkerDAO;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 * Implements a {@link IFinder} class for providing {@link Worker}
 * elements
 *
 */
@Repository
public class WorkerFinder extends Finder implements IFinder {

    @Autowired
    private IWorkerDAO workerDAO;

    @Transactional(readOnly = true)
    public List<Worker> getAll() {
        return workerDAO.getWorkers();
    }

    @Override
    public String _toString(Object value) {
        return ((Worker) value).getName();
    }

}
