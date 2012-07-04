/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.libreplan.web.common.components;

import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.resources.entities.ResourceType;
import org.libreplan.web.common.components.NewAllocationSelector.AllocationType;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Indicates what's the behaviour for each type of
 *         {@link ResourceAllocation}location. For instance,
 *         NonLimitingResourceAllocations allows multiple resource selection;
 *         searches for non limiting resources, etc
 *
 */
public enum ResourceAllocationBehaviour {

    NON_LIMITING(_("Normal")) {

        @Override
        public boolean allowMultipleSelection() {
            return true;
        }

        @Override
        public AllocationType[] allocationTypes() {
            AllocationType[] result = { AllocationType.GENERIC_WORKERS,
                    AllocationType.GENERIC_MACHINES, AllocationType.SPECIFIC };
            return result;
        }

        @Override
        public ResourceType getType() {
            return ResourceType.NON_LIMITING_RESOURCE;
        }

        @Override
        public String getFinder() {
            return "nonLimitingResourceAllocationMultipleFiltersFinder";
        }

    },
    LIMITING(_("Queue-based")) {

        @Override
        public boolean allowMultipleSelection() {
            return false;
        }

        @Override
        public AllocationType[] allocationTypes() {
            AllocationType[] result = { AllocationType.GENERIC_WORKERS,
                    AllocationType.GENERIC_MACHINES, AllocationType.SPECIFIC };
            return result;
        }

        @Override
        public ResourceType getType() {
            return ResourceType.LIMITING_RESOURCE;
        }

        @Override
        public String getFinder() {
            return "limitingResourceAllocationMultipleFiltersFinder";
        }

    };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String name;

    private ResourceAllocationBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract boolean allowMultipleSelection();

    public abstract AllocationType[] allocationTypes();

    public abstract ResourceType getType();

    public abstract String getFinder();

}
