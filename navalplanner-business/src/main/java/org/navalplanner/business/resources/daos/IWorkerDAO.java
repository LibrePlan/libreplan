package org.navalplanner.business.resources.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Worker</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public interface IWorkerDAO extends IGenericDAO<Worker, Long> {

    /**
     * Finds a {@link Worker} with the NIF param that should be unique.
     *
     * @param nif
     *            The NIF to search the {@link Worker}
     * @return The {@link Worker} with this NIF
     * @throws InstanceNotFoundException
     *             If there're more than one {@link Worker} with this NIF or
     *             there isn't any {@link Worker} with this NIF
     */
    Worker findUniqueByNif(String nif) throws InstanceNotFoundException;

    /**
     * Return list of workers
     *
     * @return
     */
    List<Worker> getWorkers();

    /**
     * Returns workers which name/NIF partially matches with name, and complies
     * all of the given criterions
     *
     * @param name
     *            search worker by name/NIF
     * @param criterions
     *            search worker that matches with criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    List<Worker> findByNameAndCriterions(String name, List<Criterion> criterions);
}
