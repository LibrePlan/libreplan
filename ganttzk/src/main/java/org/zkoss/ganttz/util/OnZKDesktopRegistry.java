package org.zkoss.ganttz.util;

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;

public class OnZKDesktopRegistry<T> {

    public static <T> OnZKDesktopRegistry<T> getLocatorFor(Class<T> klass) {
        return new OnZKDesktopRegistry<T>(klass);
    }

    private final Class<T> klass;

    private final String attributeName;

    public OnZKDesktopRegistry(Class<T> klass) {
        Validate.notNull(klass);
        this.klass = klass;
        this.attributeName = klass.getName() + "_locator";
    }

    public void store(T object) {
        getDesktop().setAttribute(attributeName, object);
    }

    private static Desktop getDesktop() {
        return Executions.getCurrent().getDesktop();
    }

    public boolean isRegistered() {
        Object result = get();
        return result != null;
    }

    private Object get() {
        return getDesktop().getAttribute(attributeName);
    }

    public T retrieve() throws IllegalStateException {
        if (!isRegistered())
            throw new IllegalStateException("no " + klass.getSimpleName()
                    + " registered");
        return klass.cast(get());
    }

}
