/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class PreAndPostNotReentrantActionsWrapper {

    public interface IAction {
        public void doAction();
    }

    private final ThreadLocal<Boolean> inside = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public void doAction(IAction action) {
        Validate.notNull(action);
        if (inside.get()) {
            action.doAction();
        } else {
            executeWithPreAndPostActions(action);
        }
    }

    private void executeWithPreAndPostActions(IAction action) {
        preAction();
        inside.set(true);
        try {
            action.doAction();
        } finally {
            inside.set(false);
            postAction();
        }
    }

    protected abstract void postAction();

    protected abstract void preAction();

}
