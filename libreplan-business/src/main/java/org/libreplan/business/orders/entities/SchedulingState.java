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
package org.libreplan.business.orders.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
            public boolean belongsToOrIsSchedulingPoint() {
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
            public boolean belongsToOrIsSchedulingPoint() {
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
            public boolean belongsToOrIsSchedulingPoint() {
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
            public boolean belongsToOrIsSchedulingPoint() {
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
            public boolean belongsToOrIsSchedulingPoint() {
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

        public abstract boolean belongsToOrIsSchedulingPoint();

        public abstract boolean isPartiallyScheduled();

        public abstract boolean isCompletelyScheduled();

        public final Type newTypeWhenDetachedFromParent() {
            return this == SCHEDULED_SUBELEMENT ? NO_SCHEDULED : this;
        }

        public boolean isSomewhatScheduled() {
            return isPartiallyScheduled() || isCompletelyScheduled();
        }
    }

    public interface ITypeChangedListener {
        public void typeChanged(Type newType);
    }

    public static SchedulingState createSchedulingState(Type initialType,
            List<SchedulingState> childrenStates,
            ITypeChangedListener typeListener) {
        SchedulingState result = new SchedulingState(initialType,
                childrenStates);
        Type newType = result.getType();
        if (newType != initialType) {
            typeListener.typeChanged(newType);
        }
        result.addTypeChangeListener(typeListener);
        return result;
    }

    private Type type = Type.NO_SCHEDULED;
    private SchedulingState parent;

    private Set<SchedulingState> children = new LinkedHashSet<SchedulingState>();

    private List<ITypeChangedListener> listeners = new ArrayList<ITypeChangedListener>();

    public SchedulingState() {
    }

    public SchedulingState(Type type, List<SchedulingState> children) {
        this(type);
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
        children.add(child);
        child.changingParentTo(this);
        setType(calculateTypeFromChildren());
    }

    private void changingParentTo(SchedulingState parent) {
        this.parent = parent;
        if (parent.getType().belongsToOrIsSchedulingPoint()) {
            setTypeWithoutNotifyingParent(Type.SCHEDULED_SUBELEMENT);
            for (SchedulingState each : getDescendants()) {
                each.setTypeWithoutNotifyingParent(Type.SCHEDULED_SUBELEMENT);
            }
        }
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
        if (!canBeScheduled()) {
            throw new IllegalStateException("It is already somewhat scheduled");
        }
        setType(Type.SCHEDULING_POINT);
        for (SchedulingState schedulingState : getDescendants()) {
            schedulingState.setType(Type.SCHEDULED_SUBELEMENT);
        }
    }

    public boolean canBeUnscheduled() {
        return getType() == Type.SCHEDULING_POINT;
    }

    public void unschedule() {
        if (!canBeUnscheduled()) {
            throw new IllegalStateException("It cannot be unscheduled");
        }
        setType(Type.NO_SCHEDULED);
        markDescendantsAsNoScheduled();
    }

    private void markDescendantsAsNoScheduled() {
        for (SchedulingState each : children) {
            each.ancestorUnscheduled();
            each.markDescendantsAsNoScheduled();
        }
    }

    private void ancestorUnscheduled() {
        Validate.isTrue(type == Type.SCHEDULED_SUBELEMENT);
        setTypeWithoutNotifyingParent(Type.NO_SCHEDULED);
    }

    private void setType(Type type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        notifyParentOfTypeChange();
        fireTypeChanged();
    }

    private void setTypeWithoutNotifyingParent(Type type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        fireTypeChanged();
    }

    private void notifyParentOfTypeChange() {
        if (isRoot()) {
            return;
        }
        parent.typeChangedOnChild(this);
    }

    private void typeChangedOnChild(SchedulingState child) {
        setType(calculateTypeFromChildren());
    }

    private Type calculateTypeFromChildren() {
        if (getType().belongsToOrIsSchedulingPoint()) {
            return getType();
        }
        if (children.isEmpty()) {
            return Type.NO_SCHEDULED;
        }
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

    public boolean isNoScheduled() {
        return type == Type.NO_SCHEDULED;
    }

    public boolean isSomewhatScheduled() {
        return type.isSomewhatScheduled();
    }

    public void removeChild(SchedulingState schedulingState) {
        boolean removed = children.remove(schedulingState);
        if (removed) {
            schedulingState.detachFromParent();
        }
        setType(calculateTypeFromChildren());
    }

    private void detachFromParent() {
        this.parent = null;
        setType(type.newTypeWhenDetachedFromParent());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(type).toString();
    }

    private void fireTypeChanged() {
        for (ITypeChangedListener listener : listeners) {
            listener.typeChanged(type);
        }
    }

    public void addTypeChangeListener(ITypeChangedListener listener) {
        Validate.notNull(listener);
        listeners.add(listener);
    }

    public void removeTypeChangeListener(ITypeChangedListener listener) {
        listeners.remove(listener);
    }

    public int getChildrenNumber() {
        return children.size();
    }

    public String getStateName() {
        if (isCompletelyScheduled()) {
            return _("Fully scheduled");
        } else if (isPartiallyScheduled()) {
            return _("Partially scheduled");
        } else {
            return _("Unscheduled");
        }
    }

    public String getStateAbbreviation() {
        if (isCompletelyScheduled()) {
            return _("F");
        } else if (isPartiallyScheduled()) {
            return _("P");
        } else {
            return _("U");
        }
    }

    public String getCssClass() {
        String cssclass = "not-scheduled";
        if (isCompletelyScheduled()) {
            cssclass = "completely-scheduled";
        } else if (isPartiallyScheduled()) {
            cssclass = "partially-scheduled";
        }
        return cssclass;
    }

}
