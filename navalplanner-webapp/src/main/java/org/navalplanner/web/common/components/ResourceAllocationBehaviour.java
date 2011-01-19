package org.navalplanner.web.common.components;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.web.common.components.NewAllocationSelector.AllocationType;

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

    NON_LIMITING(_("NON_LIMITING")) {

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
    LIMITING(_("LIMITING")) {

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
