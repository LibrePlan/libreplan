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
package org.navalplanner.business.orders.entities;

import java.util.List;

import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.business.trees.TreeNodeOnList;

/**
 * Takes into account the scheduling state when modifying the tree. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TreeNodeOnListWithSchedulingState<T extends ITreeNode<T>>
        extends TreeNodeOnList<T> {

    protected TreeNodeOnListWithSchedulingState(List<T> children) {
        super(children);
    }

    protected abstract SchedulingState getSchedulingStateFrom(T node);

    protected void updateSchedulingStateGiven(T node) {
        removeFromPreviousSchedulingState(node);
        updateWithNewChild(getSchedulingStateFrom(node));
    }

    @Override
    protected void onChildAdded(T newChild) {
        updateSchedulingStateGiven(newChild);
    }

    @Override
    protected void onChildRemoved(T previousChild) {
        removeFromPreviousSchedulingState(previousChild);
        onChildRemovedAdditionalActions(previousChild);
    }

    /**
     * This method is intended to be overriden
     * @param previousChild
     */
    protected void onChildRemovedAdditionalActions(T previousChild) {
    }

    protected abstract void updateWithNewChild(SchedulingState newChildState);

    protected void removeFromPreviousSchedulingState(T node) {
        SchedulingState schedulingState = getSchedulingStateFrom(node);
        if (!schedulingState.isRoot()) {
            schedulingState.getParent().removeChild(schedulingState);
        }
    }

}
