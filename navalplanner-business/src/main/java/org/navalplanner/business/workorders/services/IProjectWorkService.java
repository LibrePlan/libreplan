package org.navalplanner.business.workorders.services;

import java.util.List;

import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workorders.entities.ProjectWork;

/**
 * Management of {@link ProjectWork} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IProjectWorkService {

    void save(ProjectWork projectWork) throws ValidationException;

    boolean exists(ProjectWork projectWork);

    List<ProjectWork> getProjectWorks();

    void remove(ProjectWork projectWork) throws InstanceNotFoundException;

    ProjectWork find(Long workerId) throws InstanceNotFoundException;

    public <T> T onTransaction(OnTransaction<T> onTransaction);

}
