/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2011 Wireless Galicia, S.L.
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

package org.libreplan.web.common.converters;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A {@link IConverter} for {@link TaskElement} <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementConverter implements IConverter<TaskElement> {

        @Autowired
        private ITaskElementDAO taskElementDAO;

        @Override
        public String asString(TaskElement entity) {
            return entity.getId() + "";
        }

        @Override
        public Class<TaskElement> getType() {
            return TaskElement.class;
        }

        @Override
        public String asStringUngeneric(Object entity) {
            return asString((TaskElement) entity);
        }

        @Override
        @Transactional(readOnly = true)
        public TaskElement asObject(String stringRepresentation) {
            long id = Long.parseLong(stringRepresentation);
            try {
                return taskElementDAO.find(id);
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }