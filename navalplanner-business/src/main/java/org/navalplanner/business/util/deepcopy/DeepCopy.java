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
package org.navalplanner.business.util.deepcopy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.navalplanner.business.workingday.EffortDuration;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class DeepCopy {
    private static Set<Class<?>> inmmutableTypes = new HashSet<Class<?>>(Arrays
            .<Class<?>> asList(Boolean.class, String.class, BigDecimal.class,
                    Double.class, Float.class, Integer.class, Short.class,
                    Byte.class, Character.class, LocalDate.class,
                    DateTime.class, EffortDuration.class));

    public static boolean isImmutableType(Class<?> klass) {
        return klass.isPrimitive() || isEnum(klass)
                || inmmutableTypes.contains(klass);
    }

    private static boolean isEnum(Class<?> klass) {
        Class<?> currentClass = klass;
        do {
            if (currentClass.isEnum()) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);
        return false;
    }

    public interface ICustomCopy {
        public boolean canHandle(Object object);

        public Object instantiateCopy(Strategy strategy, Object originValue);

        public void copyDataToResult(DeepCopy deepCopy, Object origin,
                Strategy strategy, Object result);
    }

    private static class DateCopy implements ICustomCopy {

        @Override
        public boolean canHandle(Object object) {
            return object instanceof Date;
        }

        @Override
        public void copyDataToResult(DeepCopy deepCopy, Object origin,
                Strategy strategy, Object result) {
            // already completed
        }

        @Override
        public Object instantiateCopy(Strategy strategy, Object originValue) {
            Date date = (Date) originValue;
            return new Date(date.getTime());
        }

    }

    private static abstract class CollectionCopy implements ICustomCopy {

        @Override
        public Object instantiateCopy(Strategy strategy, Object originValue) {
            return getResultData(originValue);
        }

        protected abstract Collection<Object> getResultData(Object originValue);

        @Override
        public void copyDataToResult(DeepCopy deepCopy, Object origin,
                Strategy strategy, Object result) {
            copy(deepCopy, origin, strategy, (Collection<Object>) result);
        }

        private void copy(DeepCopy deepCopy, Object origin, Strategy strategy,
                Collection<Object> destination) {
            Strategy childrenStrategy = getChildrenStrategy(strategy);
            for (Object each : originDataAsIterable(origin)) {
                destination.add(deepCopy.copy(each, childrenStrategy));
            }
        }

        private Strategy getChildrenStrategy(Strategy strategy) {
            if (strategy == Strategy.SHARE_COLLECTION_ELEMENTS) {
                return Strategy.SHARE;
            }
            return strategy;
        }

        private Iterable<Object> originDataAsIterable(Object originValue) {
            return (Iterable<Object>) originValue;
        }
    }

    private static class SetCopy extends CollectionCopy {

        @Override
        public boolean canHandle(Object object) {
            return object instanceof Set;
        }

        @Override
        protected Collection<Object> getResultData(Object object) {
            return instantiate(object.getClass());
        }

        private Set<Object> instantiate(final Class<? extends Object> klass) {
            return new ImplementationInstantiation() {
                @Override
                protected Set<?> createDefault() {
                    if (SortedSet.class.isAssignableFrom(klass)) {
                        return new TreeSet<Object>();
                    }
                    return new HashSet<Object>();
                }
            }.instantiate(klass);
        }
    }

    private static class MapCopy implements ICustomCopy {
        @Override
        public boolean canHandle(Object object) {
            return object instanceof Map;
        }

        @Override
        public Object instantiateCopy(Strategy strategy, Object originValue) {
            return instantiate(originValue.getClass());
        }

        private Map<Object, Object> instantiate(Class<? extends Object> klass) {
            return new ImplementationInstantiation() {
                @Override
                protected Object createDefault() {
                    return new HashMap<Object, Object>();
                }
            }.instantiate(klass);
        }

        @Override
        public void copyDataToResult(DeepCopy deepCopy, Object origin,
                Strategy strategy, Object result) {
            doCopy(deepCopy, (Map<?, ?>) ((Map<?, ?>) origin), strategy, ((Map<Object, Object>) result));
        }

        private void doCopy(DeepCopy deepCopy, Map<?, ?> origin,
                Strategy strategy, Map<Object, Object> resultMap) {
            Strategy keyStrategy = getKeysStrategy(strategy);
            Strategy valueStrategy = getValuesStrategy(strategy);
            for (Entry<?, ?> entry : origin.entrySet()) {
                Object key = deepCopy.copy(entry.getKey(), keyStrategy);
                Object value = deepCopy.copy(entry.getValue(), valueStrategy);
                resultMap.put(key, value);
            }
        }

        private Strategy getKeysStrategy(Strategy strategy) {
            if (Strategy.ONLY_SHARE_KEYS == strategy
                    || Strategy.SHARE_COLLECTION_ELEMENTS == strategy) {
                return Strategy.SHARE;
            }
            return strategy;
        }

        private Strategy getValuesStrategy(Strategy strategy) {
            if (Strategy.ONLY_SHARE_VALUES == strategy
                    || Strategy.SHARE_COLLECTION_ELEMENTS == strategy) {
                return Strategy.SHARE;
            }
            return strategy;
        }
    }

    private static class ListCopy extends CollectionCopy {

        @Override
        public boolean canHandle(Object object) {
            return object instanceof List;
        }

        @Override
        protected Collection<Object> getResultData(Object originValue) {
            return instantiate(originValue.getClass());
        }

        private List<Object> instantiate(Class<? extends Object> klass) {
            return new ImplementationInstantiation() {
                @Override
                protected Object createDefault() {
                    return new ArrayList<Object>();
                }
            }.instantiate(klass);
        }

    }

    private static abstract class ImplementationInstantiation {

        private static final String[] VETOED_IMPLEMENTATIONS = {
                "PersistentSet", "PersistentList", "PersistentMap",
                "PersistentSortedSet" };

        ImplementationInstantiation() {
        }

        <T> T instantiate(Class<?> type) {
            if (!isVetoed(type)) {
                try {
                    Constructor<? extends Object> constructor = type
                            .getConstructor();
                    return (T) type.cast(constructor.newInstance());
                } catch (Exception e) {
                }
            }
            return (T) createDefault();
        }

        private static boolean isVetoed(Class<?> type) {
            final String simpleName = type.getSimpleName();
            for (String each : VETOED_IMPLEMENTATIONS) {
                if (each.equalsIgnoreCase(simpleName)) {
                    return true;
                }
            }
            return false;
        }

        protected abstract Object createDefault();
    }

    private static List<ICustomCopy> DEFAULT_CUSTOM_COPIERS = Arrays
            .<ICustomCopy> asList(new DateCopy(), new SetCopy(), new MapCopy(),
                    new ListCopy());

    private Map<ByIdentity, Object> alreadyCopiedObjects = new HashMap<ByIdentity, Object>();

    private class ByIdentity {
        private final Object wrapped;

        ByIdentity(Object wrapped) {
            Validate.notNull(wrapped);
            this.wrapped = wrapped;
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof ByIdentity) {
                ByIdentity other = (ByIdentity) obj;
                return this.wrapped == other.wrapped;
            }
            return false;
        }

    }

    private ByIdentity byIdentity(Object value) {
        return new ByIdentity(value);
    }

    public <T> T copy(T entity) {
        return copy(entity, null);
    }

    private <T> T copy(T couldBeProxyValue, Strategy strategy) {
        if (couldBeProxyValue == null) {
            return null;
        }
        T value = desproxify(couldBeProxyValue);
        if (alreadyCopiedObjects.containsKey(byIdentity(value))) {
            return (T) alreadyCopiedObjects.get(byIdentity(value));
        }
        if (Strategy.SHARE == strategy || isImmutable(value)) {
            return value;
        }
        ICustomCopy copier = findCopier(value);
        if (copier != null) {
            Object resultData = copier.instantiateCopy(strategy, value);
            alreadyCopiedObjects.put(byIdentity(value), resultData);
            copier.copyDataToResult(this, value, strategy, resultData);
            return (T) resultData;
        }
        T result = instantiateUsingDefaultConstructor(getTypedClassFrom(value));
        alreadyCopiedObjects.put(byIdentity(value), result);
        copyProperties(value, result);
        callAferCopyHooks(result);
        return result;
    }

    private <T> T desproxify(T value) {
        if (value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) value;
            return (T) proxy.getHibernateLazyInitializer()
                    .getImplementation();
        }
        return value;
    }

    private boolean isImmutable(Object value) {
        return isImmutableType(value.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getTypedClassFrom(T entity) {
        return (Class<T>) entity.getClass();
    }

    private <T> T instantiateUsingDefaultConstructor(Class<T> klass) {
        Constructor<T> constructor;
        try {
            constructor = klass.getConstructor();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "could not invoke default no-args constructor", e);
        }
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void copyProperties(Object source, Object target) {
        List<Field> fields = getAllFieldsFor(source);
        for (Field each : fields) {
            each.setAccessible(true);
            if (!isIgnored(each)) {
                Object sourceValue = readFieldValue(source, each);
                if (sourceValue != null) {
                    Strategy strategy = getStrategy(each, sourceValue);
                    try {
                        writeFieldValue(target, each, copy(sourceValue, strategy));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private List<Field> getAllFieldsFor(Object source) {
        List<Field> result = new ArrayList<Field>();
        Class<? extends Object> currentClass = source.getClass();
        while (currentClass != null) {
            result.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return result;
    }

    private boolean isIgnored(Field field) {
        return isStatic(field) || isMarkedWithIgnore(field);
    }

    private boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private boolean isMarkedWithIgnore(Field each) {
        OnCopy onCopy = each.getAnnotation(OnCopy.class);
        return onCopy != null && onCopy.value() == Strategy.IGNORE;
    }

    private void writeFieldValue(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object readFieldValue(Object source, Field field) {
        try {
            return field.get(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Strategy getStrategy(Field field, Object sourceValue) {
        OnCopy onCopy = field.getAnnotation(OnCopy.class);
        return onCopy != null ? onCopy.value() : null;
    }

    private ICustomCopy findCopier(Object sourceValue) {
        for (ICustomCopy each : DEFAULT_CUSTOM_COPIERS) {
            if (each.canHandle(sourceValue)) {
                return each;
            }
        }
        return null;
    }

    private void callAferCopyHooks(Object value) {
        assert value != null;
        for (Method each : getAfterCopyHooks(value.getClass())) {
            each.setAccessible(true);
            try {
                each.invoke(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Method> getAfterCopyHooks(Class<?> klass) {
        Class<?> current = klass;
        List<Method> result = new ArrayList<Method>();
        while (current != null) {
            result.addAll(getAfterCopyDeclaredAt(current));
            current = current.getSuperclass();
        }
        return result;
    }

    private List<Method> getAfterCopyDeclaredAt(Class<?> klass) {
        List<Method> result = new ArrayList<Method>();
        for (Method each : klass.getDeclaredMethods()) {
            if (isAfterCopyHook(each)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean isAfterCopyHook(Method each) {
        AfterCopy annotation = each.getAnnotation(AfterCopy.class);
        return annotation != null;
    }

    public <T> DeepCopy replace(T toBeReplaced, T substitution) {
        alreadyCopiedObjects.put(byIdentity(toBeReplaced), substitution);
        return this;
    }
}
