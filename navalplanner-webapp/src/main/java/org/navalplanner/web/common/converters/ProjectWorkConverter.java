package org.navalplanner.web.common.converters;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.services.IProjectWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A {@link Converter} for {@link ProjectWork} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ProjectWorkConverter implements Converter<ProjectWork> {

    @Autowired
    private IProjectWorkService projectWorkService;

    @Override
    public ProjectWork asObject(String stringRepresentation) {
        try {
            return projectWorkService
                    .find(Long.parseLong(stringRepresentation));
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(ProjectWork entity) {
        return entity.getId() + "";
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((ProjectWork) entity);
    }

    @Override
    public Class<ProjectWork> getType() {
        return ProjectWork.class;
    }

}
