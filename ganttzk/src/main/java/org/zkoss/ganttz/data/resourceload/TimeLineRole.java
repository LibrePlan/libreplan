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

package org.zkoss.ganttz.data.resourceload;

import org.zkoss.ganttz.i18n.I18nHelper;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class TimeLineRole<T> {

    private T entity;

    private TimeLineRoleEnum type;

    public TimeLineRole(T entity) {
        this.entity = entity;
        this.type = TimeLineRoleEnum.create(getClassName(entity));
    }

    public boolean isVisibleScheduled() {
        return type != null ? type.isVisibleScheduled() : false;
    }

    public T getEntity() {
        return entity;
    }

    private String getClassName(T entity) {
        return entity != null ? entity.getClass().getSimpleName() : null;
    }

    /**
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public enum TimeLineRoleEnum {

        NONE(_("None")), WORKER(_("Worker")), ORDER(_("Project")), TASK(_("Task")) {
            @Override
            public boolean isVisibleScheduled() {
                return true;
            }
        },
        CRITERION(_("Criterion"));

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private String name;

        private TimeLineRoleEnum(String name) {
            this.name = name;
        }

        public static TimeLineRoleEnum create(String name) {
            TimeLineRoleEnum requiredTimeLineRole = TimeLineRoleEnum.NONE;
            if (name != null) {
                for (TimeLineRoleEnum role : TimeLineRoleEnum.values()) {
                    if (name.contains(role.name)) {
                        requiredTimeLineRole = role;
                    }
                }
            }
            return requiredTimeLineRole;
        }

        public String toString() {
            return I18nHelper._(this.name);
        }

        public boolean isVisibleScheduled() {
            return false;
        }
    }
}
