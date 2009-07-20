package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zkoss.ganttz.data.Dependency;
import org.zkoss.ganttz.data.DependencyType;

/**
 * Represents a dependency in the domain.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DomainDependency<T> {

    public static <T> List<Dependency> toDependencies(
            IDomainAndBeansMapper<T> mapper,
            Collection<DomainDependency<T>> dependencies) {
        List<Dependency> result = new ArrayList<Dependency>();
        for (DomainDependency<T> domainDependency : dependencies) {
            result.add(domainDependency.toDependency(mapper));
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
