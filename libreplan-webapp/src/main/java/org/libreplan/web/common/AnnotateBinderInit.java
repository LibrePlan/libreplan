/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.web.common;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zk.ui.util.InitiatorExt;

/**
 * Replacement for the removed {@code org.zkoss.zkplus.databind.AnnotateDataBinderInit}, referenced
 * from the same {@code <?init class="..."?>} processing instruction across the app's .zul files.
 * Creates bindings for each of the page's root components once they're composed, the same way
 * {@link Util#createBindingsFor(Component)} does when called explicitly from a controller.
 */
public class AnnotateBinderInit implements Initiator, InitiatorExt {

    @Override
    public void doInit(Page page, Map<String, Object> args) throws Exception {
    }

    @Override
    public void doAfterCompose(Page page, Component[] result) throws Exception {
        for (Component each : result) {
            Util.createBindingsFor(each);
        }
    }

    @Override
    public boolean doCatch(Throwable ex) throws Exception {
        return false;
    }

    @Override
    public void doFinally() throws Exception {
    }

}
