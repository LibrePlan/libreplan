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
package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class SchedulingState {

    public enum Type {
        SCHEDULING_POINT {
            @Override
            public boolean belongsToSchedulingPoint() {
                return true;
            }

            @Override
            public boolean isCompletelyScheduled() {
                return true;
            }

            @Override
            public boolean isPartiallyScheduled() {
                return false;
            }
        },
        SCHEDULED_SUBELEMENT {
            @Override
            public boolean belongsToSchedulingPoint() {
                return true;
            }

            @Override
            public boolean isCompletelyScheduled() {
                return true;
            }

            @Override
            public boolean isPartiallyScheduled() {
                return false;
            }
        },
        PARTIALY_SCHEDULED_SUPERELEMENT {
            @Override
            public boolean belongsToSchedulingPoint() {
                return false;
            }

            @Override
            public boolean isCompletelyScheduled() {
                return false;
            }

            @Override
            public boolean isPartiallyScheduled() {
                return true;
            }
        },
        COMPLETELY_SCHEDULED_SUPERELEMENT {
            @Override
            public boolean belongsToSchedulingPoint() {
                return false;
            }

            @Override
            public boolean isCompletelyScheduled() {
                return true;
            }

            @Override
            public boolean isPartiallyScheduled() {
                return false;
            }
        },
        NO_SCHEDULED {
            @Override
            public boolean belongsToSchedulingPoint() {
                return false;
            }

            @Override
            public boolean isCompletelyScheduled() {
                return false;
            }

            @Override
            public boolean isPartiallyScheduled() {
                return false;
            }
        };

        public abstract boolean belongsToSchedulingPoint();

        public abstract boolean isPartiallyScheduled();

        public abstract boolean isCompletelyScheduled();

        public boolean isSomewhatScheduled() {
            return isPartiallyScheduled() || isCompletelyScheduled();
        }
    }

    private Type type = Type.NO_SCHEDULED;
    private SchedulingState parent;

    private List<SchedulingState> children = new ArrayList<SchedulingState>();

    public SchedulingState() {
    }

    public SchedulingState(List<SchedulingState> children) {
        for (SchedulingState each : children) {
            if (!each.isRoot()) {
                throw new IllegalArgumentException(each
                        + " is already child of another "
                        + SchedulingState.class.getSimpleName());
            }
            add(each);
        }
    }

    public SchedulingState(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void add(SchedulingState child) {
        child.parent = this;
        children.add(child);
        setType(calculateTypeFromChildren());
    }

    public SchedulingState getParent() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean canBeScheduled() {
        return type == Type.NO_SCHEDULED;
    }

    public void schedule() {
        if (type.isSomewhatScheduled()) {
            throw new IllegalStateException("it's already somewhat scheduled");
        }
        setType(Type.SCHEDULING_POINT);
        for (SchedulingState schedulingState : getDescendants()) {
            schedulingState.setType(Type.SCHEDULED_SUBELEMENT);
        }
    }

    private void setType(Type type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        notifyParentOfTypeChange();
    }

    private void notifyParentOfTypeChange() {
        if (isRoot()) {
            return;
        }
        parent.typeChangedOnChild(this);
    }

    private void typeChangedOnChild(SchedulingState child) {
        if (getType().belongsToSchedulingPoint()) {
            return;
        }
        setType(calculateTypeFromChildren());
    }

    private Type calculateTypeFromChildren() {
        Validate.isTrue(!children.isEmpty());
        boolean allScheduled = true;
        boolean someScheduled = false;
        for (SchedulingState each : children) {
            someScheduled = someScheduled
                    || each.getType().isSomewhatScheduled();
            allScheduled = allScheduled
                    && each.getType().isCompletelyScheduled();
        }
        if (allScheduled) {
            return Type.COMPLETELY_SCHEDULED_SUPERELEMENT;
        } else if (someScheduled) {
            return Type.PARTIALY_SCHEDULED_SUPERELEMENT;
        } else {
            return Type.NO_SCHEDULED;
        }
    }

    private List<SchedulingState> getDescendants() {
        List<SchedulingState> result = new ArrayList<SchedulingState>();
        addDescendants(result);
        return result;
    }

    private void addDescendants(List<SchedulingState> result) {
        for (SchedulingState each : children) {
            result.add(each);
            each.addDescendants(result);
        }
    }

    public boolean isCompletelyScheduled() {
        return type.isCompletelyScheduled();
    }

    public boolean isPartiallyScheduled() {
        return type.isPartiallyScheduled();
    }

    public boolean isSomewhatScheduled() {
        return type.isSomewhatScheduled();
    }

    public void removeChild(SchedulingState schedulingState) {
        boolean removed = children.remove(schedulingState);
        if (removed) {
            schedulingState.parent = null;
        }
        setType(calculateTypeFromChildren());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(type).toString();
    }


}
