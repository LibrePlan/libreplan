package org.navalplanner.web.workorders;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workorders.entities.ITaskWorkContainer;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.services.IProjectWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link ProjectWork}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProjectWorkModel implements IProjectWorkModel {

    private final IProjectWorkService projectService;

    private ProjectWork project;

    private ClassValidator<ProjectWork> projectValidator = new ClassValidator<ProjectWork>(
            ProjectWork.class);

    private TaskTreeModel tasksTreeModel;

    @Autowired
    public ProjectWorkModel(IProjectWorkService projectService) {
        Validate.notNull(projectService);
        this.projectService = projectService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectWork> getProjects() {
        return projectService.getProjectWorks();
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(ProjectWork project) {
        Validate.notNull(project);
        try {
            this.project = projectService.find(project.getId());
            this.tasksTreeModel = new TaskTreeModel(this.project);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForCreate() {
        this.project = new ProjectWork();
        this.tasksTreeModel = new TaskTreeModel(this.project);
        this.project.setInitDate(new Date());
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = projectValidator
                .getInvalidValues(project);
        if (invalidValues.length > 0)
            throw new ValidationException(invalidValues);
        this.projectService.save(project);
    }

    @Override
    public ITaskWorkContainer getProject() {
        return project;
    }

    @Override
    public void remove(ProjectWork projectWork) {
        try {
            this.projectService.remove(projectWork);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForRemove(ProjectWork project) {
        this.project = project;
    }

    @Override
    public TaskTreeModel getTasksTreeModel() {
        return tasksTreeModel;
    }

}
