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

package org.zkoss.ganttz.timetracker;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

public class OnColumnsRowRenderer<C, T> implements RowRenderer {

    public static <C, T> OnColumnsRowRenderer<C, T> create(
            ICellForDetailItemRenderer<C, T> cellRenderer, Collection<C> columns) {
        return create(inferGenericType(cellRenderer), cellRenderer, columns);
    }

    public static <C, T> OnColumnsRowRenderer<C, T> create(Class<T> type,
            ICellForDetailItemRenderer<C, T> cellRenderer, Collection<C> columns) {
        return new OnColumnsRowRenderer<C, T>(type, cellRenderer, columns);
    }

    private static <T> Class<T> inferGenericType(
            ICellForDetailItemRenderer<?, T> renderer) {
        ParameterizedType parametrizedType = findRenderererInterfaceType(renderer);
        Type[] actualTypeArguments = parametrizedType.getActualTypeArguments();
        final int genericTypePosition = 1;
        Type type = actualTypeArguments[genericTypePosition];
        if (!isActualType(type)) {
            informCannotBeInferred(renderer);
        }
        return (Class<T>) actualTypeArguments[genericTypePosition];
    }

    private static boolean isActualType(Type t) {
        return t instanceof Class;
    }

    private static ParameterizedType findRenderererInterfaceType(
            ICellForDetailItemRenderer<?, ?> renderer) {
        Type[] genericInterfaces = renderer.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (isTypeForInterface(type, ICellForDetailItemRenderer.class)) {
                if (type instanceof ParameterizedType) {
                    return (ParameterizedType) type;
                } else {
                    informCannotBeInferred(renderer);
                }
            }
        }
        throw new RuntimeException("shouldn't reach here. Uncovered case for "
                + renderer);
    }

    private static boolean isTypeForInterface(Type type,
            Class<?> interfaceBeingSearched) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            Type rawType = p.getRawType();
            return rawType.equals(interfaceBeingSearched);
        }
        return type.equals(interfaceBeingSearched);
    }

    private static void informCannotBeInferred(
            ICellForDetailItemRenderer<?, ?> renderer) {
        throw new IllegalArgumentException(
                "the generic type cannot be inferred "
                        + "if actual type parameters are not declared "
                        + "or implements the raw interface: "
                        + renderer.getClass().getName());
    }

    private final List<C> columns;
    private final ICellForDetailItemRenderer<C, T> cellRenderer;
    private Class<T> type;

    private OnColumnsRowRenderer(Class<T> type,
            ICellForDetailItemRenderer<C, T> cellRenderer, Collection<C> columns) {
        Validate.notNull(type);
        Validate.notNull(columns);
        Validate.notNull(cellRenderer);
        Validate.noNullElements(columns);
        this.cellRenderer = cellRenderer;
        this.columns = new ArrayList<C>(columns);
        this.type = type;
    }

    @Override
    public void render(Row row, Object data) {
        if (!type.isInstance(data)) {
            throw new IllegalArgumentException(data + " is not instance of "
                    + type);
        }
        for (C item : columns) {
            Component child = cellRenderer.cellFor(item, type.cast(data));
            if (child != null) {
                child.setParent(row);
            }
        }
    }

}
