package org.navalplanner.web.workorders;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workorders.entities.ITaskWorkContainer;
import org.navalplanner.business.workorders.entities.ProjectWork;

/**
 * Contract for {@link ProjectWorkModel}<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IProjectWorkModel {

    List<ProjectWork> getProjects();

    void prepareEditFor(ProjectWork project);

    void prepareForCreate();

    void save() throws ValidationException;

    ITaskWorkContainer getProject();

    void remove(ProjectWork projectWork);

    void prepareForRemove(ProjectWork project);

    TaskTreeModel getTasksTreeModel();

}
