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
package org.navalplanner.business.trees;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Implementation of {@link ITreeParentNode} that mutates a list <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TreeNodeOnList<T extends ITreeNode<T>> implements
        ITreeParentNode<T> {

    private final List<T> children;

    protected TreeNodeOnList(List<T> children) {
        this.children = children;
    }

    @Override
    public void add(T newChild) {
        checkNotContained(newChild);
        setParentIfRequired(newChild);
        children.add(newChild);
        onChildAdded(newChild);
    }

    private void checkNotContained(T newChild) {
        Validate.isTrue(!children.contains(newChild));
    }

    protected abstract void setParentIfRequired(T newChild);

    protected abstract void onChildAdded(T newChild);

    protected abstract void onChildRemoved(T previousChild);

    @Override
    public void remove(T previousChild) {
        children.remove(previousChild);
        onChildRemoved(previousChild);
    }

    @Override
    public void replace(T previousChild, T newChild) {
        setParentIfRequired(newChild);
        Collections.replaceAll(children, previousChild, newChild);
        onChildAdded(newChild);
        onChildRemoved(previousChild);
    }

    @Override
    public void down(T existentChild) {
        int position = children.indexOf(existentChild);
        if (position < children.size() - 1) {
            children.remove(position);
            children.add(position + 1, existentChild);
        }
    }

    @Override
    public void up(T existentChild) {
        int position = children.indexOf(existentChild);
        if (position > 0) {
            children.remove(position);
            children.add(position - 1, existentChild);
        }
    }

    @Override
    public void add(int position, T newChild) {
        checkNotContained(newChild);
        setParentIfRequired(newChild);
        children.add(position, newChild);
        onChildAdded(newChild);
    }

    public List<T> getChildren() {
        return Collections.unmodifiableList(children);
    }

}
