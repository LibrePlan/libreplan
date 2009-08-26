package org.zkoss.ganttz.timetracker;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

public class OnDetailItemsRowRenderer<T> implements RowRenderer {

    public static <T> OnDetailItemsRowRenderer<T> create(
            ICellForDetailItemRenderer<T> cellRenderer,
            Collection<DetailItem> detailItems) {
        return create(inferGenericType(cellRenderer), cellRenderer, detailItems);
    }

    public static <T> OnDetailItemsRowRenderer<T> create(Class<T> type,
            ICellForDetailItemRenderer<T> cellRenderer,
            Collection<DetailItem> detailItems) {
        return new OnDetailItemsRowRenderer<T>(type, cellRenderer, detailItems);
    }

    private static <T> Class<T> inferGenericType(
            ICellForDetailItemRenderer<T> renderer) {
        ParameterizedType parametrizedType = findRenderererInterfaceType(renderer);
        Type[] actualTypeArguments = parametrizedType.getActualTypeArguments();
        Type type = actualTypeArguments[0];
        if (!isActualType(type)) {
            informCannotBeInferred(renderer);
        }
        return (Class<T>) actualTypeArguments[0];
    }

    private static boolean isActualType(Type t) {
        return t instanceof Class;
    }

    private static ParameterizedType findRenderererInterfaceType(
            ICellForDetailItemRenderer<?> renderer) {
        Type[] genericInterfaces = renderer.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (isTypeForInterface(type, ICellForDetailItemRenderer.class)) {
                if (type instanceof ParameterizedType) {
                    return (ParameterizedType) type;
                } else
                    informCannotBeInferred(renderer);
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
            ICellForDetailItemRenderer<?> renderer) {
        throw new IllegalArgumentException(
                "the generic type cannot be inferred "
                        + "if actual type parameters are not declared "
                        + "or implements the raw interface: "
                        + renderer.getClass().getName());
    }

    private final List<DetailItem> detailItems;
    private final ICellForDetailItemRenderer<T> cellRenderer;
    private Class<T> type;

    private OnDetailItemsRowRenderer(Class<T> type,
            ICellForDetailItemRenderer<T> cellRenderer,
            Collection<DetailItem> detailItems) {
        Validate.notNull(type);
        Validate.notNull(detailItems);
        Validate.notNull(cellRenderer);
        Validate.noNullElements(detailItems);
        this.cellRenderer = cellRenderer;
        this.detailItems = new ArrayList<DetailItem>(detailItems);
        this.type = type;
    }

    @Override
    public void render(Row row, Object data) {
        if (!type.isInstance(data))
            throw new IllegalArgumentException(data + " is not instance of "
                    + type);
        for (DetailItem item : detailItems) {
            Component child = cellRenderer.cellFor(item, type.cast(data));
            child.setParent(row);
        }
    }

}
