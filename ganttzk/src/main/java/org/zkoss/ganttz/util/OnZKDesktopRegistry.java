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
        if (!isRegistered()) {
            throw new IllegalStateException("no " + klass.getSimpleName()
                    + " registered");
        }
        return klass.cast(get());
    }

}
