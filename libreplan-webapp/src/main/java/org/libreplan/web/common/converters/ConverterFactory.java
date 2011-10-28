/*
 * This file is part of LibrePlan
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

package org.libreplan.web.common.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.libreplan.web.I18nHelper._;

/**
 * Default implementation for {@link IConverterFactory} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ConverterFactory implements IConverterFactory {

    private Map<Class<?>, IConverter<?>> convertersByType = new HashMap<Class<?>, IConverter<?>>();

    @Autowired
    public ConverterFactory(List<IConverter<?>> converters) {
        for (IConverter<?> converter : converters) {
            convertersByType.put(converter.getType(), converter);
        }
    }

    @Override
    public <T> IConverter<? super T> getConverterFor(Class<T> klass) {
        if (convertersByType.containsKey(klass)) {
            return (IConverter<? super T>) convertersByType.get(klass);
        }
        for (Class<?> registeredKlass : convertersByType.keySet()) {
            if (registeredKlass.isAssignableFrom(klass)) {
                IConverter<?> result = convertersByType.get(registeredKlass);
                convertersByType.put(klass, result);
                return (IConverter<? super T>) result;
            }
        }
        throw new RuntimeException(_("Not found converter for {0}",  klass));
    }
}
