package org.navalplanner.web.common;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A {@link Converter} for {@link Resource} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceConverter implements Converter<Resource> {

    @Autowired
    private ResourceService resourceService;

    @Override
    public Resource asObject(String stringRepresentation) {
        long id = Long.parseLong(stringRepresentation);
        try {
            return resourceService.findResource(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(Resource entity) {
        return entity.getId() + "";
    }

    @Override
    public Class<Resource> getType() {
        return Resource.class;
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString(getType().cast(entity));
    }

}
