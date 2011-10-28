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

package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.IDependency;

/**
 * Represents a dependency in the domain.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DomainDependency<T> implements IDependency<T> {

    private static final Log LOG = LogFactory.getLog(DomainDependency.class);

    public static <T> List<Dependency> toDependencies(
            IDomainAndBeansMapper<T> mapper,
            Collection<DomainDependency<T>> dependencies) {
        List<Dependency> result = new ArrayList<Dependency>();
        for (DomainDependency<T> domainDependency : dependencies) {
            try {
                result.add(domainDependency.toDependency(mapper));
            } catch (Exception e) {
                LOG.error("error creating dependency from domainDependency", e);
            }
        }
        return result;
    }

    public static <T> DomainDependency<T> createDependency(T source,
            T destination, DependencyType type) {
        return new DomainDependency<T>(source, destination, type);
    }

    private final T source;

    private final T destination;

    private final DependencyType type;

    private DomainDependency(T source, T destination, DependencyType type) {
        super();
        this.source = source;
        this.destination = destination;
        this.type = type;
    }

    public T getSource() {
        return source;
    }

    public T getDestination() {
        return destination;
    }

    public DependencyType getType() {
        return type;
    }

    public Dependency toDependency(IDomainAndBeansMapper<T> mapper) {
        return new Dependency(mapper.findAssociatedBean(source), mapper
                .findAssociatedBean(destination), type);
    }
}
