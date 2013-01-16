/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.common.daos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.entities.AppProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link AppProperties}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AppPropertiesDAO extends GenericDAOHibernate<AppProperties, Long>
        implements IAppPropertiesDAO {

    @Override
    @Transactional(readOnly = true)
    public List<AppProperties> getAll() {
        return list(AppProperties.class);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, String> findByMajorId(String majorId) {
        Criteria c = getSession().createCriteria(AppProperties.class).add(
                Restrictions.eq("majorId", majorId));
        List<AppProperties> list = c.list();

        Map<String, String> map = new HashMap<String, String>();
        for (AppProperties appProperty : list) {
            map.put(appProperty.getPropertyName(),
                    appProperty.getPropertyValue());
        }
        return map;

    }

    @Override
    @Transactional(readOnly = true)
    public AppProperties findByMajorIdAndName(String majorId, String proprtyName) {
        return (AppProperties) getSession().createCriteria(AppProperties.class)
                .add(Restrictions.eq("majorId", majorId))
                .add(Restrictions.eq("propertyName", proprtyName))
                .uniqueResult();
    }

}
