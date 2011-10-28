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
package org.libreplan.web.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

public class ComponentsReplacer {

    private ComponentsReplacer() {
    }

    private static class ChildrenSnapshot implements IChildrenSnapshot {
        private final List<Component> children;
        private final Component parent;

        public ChildrenSnapshot(Component parent,
                List<Component> currentChildren) {
            this.parent = parent;
            this.children = currentChildren;
        }

        @Override
        public IChildrenSnapshot restore() {
            List<Component> removedChildren = removeChildren(parent);
            parent.getChildren().addAll(children);
            parent.getPage().invalidate();
            return new ChildrenSnapshot(parent, removedChildren);
        }
    }

    /**
     * @param parent
     * @param script
     * @param arguments
     * @return
     */
    public static IChildrenSnapshot replaceAllChildren(Component parent,
            String script,
            Map<String, Object> arguments) {
        List<Component> currentChildren = removeChildren(parent);
        ChildrenSnapshot result = new ChildrenSnapshot(parent, currentChildren);
        Executions.createComponents(script, parent, arguments);
        return result;
    }

    private static List<Component> removeChildren(Component parent) {
        List<Component> currentChildren = new ArrayList<Component>(parent
                .getChildren());
        for (Component c : currentChildren) {
            c.detach();
        }
        return currentChildren;
    }

}
