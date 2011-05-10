/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.navalplanner.web.common.entrypoints;

import java.util.HashMap;
import java.util.Map;

import org.navalplanner.web.common.converters.IConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Registry of {@link EntryPointsHandler} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class URLHandlerRegistry implements IURLHandlerRegistry {

    @Autowired
    private IExecutorRetriever executorRetriever;

    @Autowired
    private IConverterFactory converterFactory;

    private Map<Class<?>, EntryPointsHandler<?>> cached = new HashMap<Class<?>, EntryPointsHandler<?>>();;

    @SuppressWarnings("unchecked")
    public <T> EntryPointsHandler<T> getRedirectorFor(Class<T> klassWithLinkableMetadata) {
        if (cached.containsKey(klassWithLinkableMetadata)) {
            return (EntryPointsHandler<T>) cached.get(klassWithLinkableMetadata);
        }
        EntryPointsHandler<T> result = new EntryPointsHandler<T>(converterFactory,
                executorRetriever, klassWithLinkableMetadata);
        cached.put(klassWithLinkableMetadata, result);
        return result;
    }
}
