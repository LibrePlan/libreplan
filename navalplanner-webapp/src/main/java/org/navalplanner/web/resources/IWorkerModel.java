package org.navalplanner.web.resources;

import java.util.List;

import org.navalplanner.business.resources.entities.Worker;

/**
 * Interface for workerModel. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IWorkerModel {

    Worker createNewInstance();

    void save(Worker worker);

    List<Worker> getWorkers();

}