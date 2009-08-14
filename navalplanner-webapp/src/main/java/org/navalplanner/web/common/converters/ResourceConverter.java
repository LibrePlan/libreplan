package org.navalplanner.web.common.converters;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A {@link IConverter} for {@link Resource} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceConverter implements IConverter<Resource> {

    @Autowired
    private IResourceDAO resourceDAO;

    @Override
    @Transactional(readOnly = true)
    public Resource asObject(String stringRepresentation) {
        long id = Long.parseLong(stringRepresentation);
        try {
            return resourceDAO.find(id);
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
