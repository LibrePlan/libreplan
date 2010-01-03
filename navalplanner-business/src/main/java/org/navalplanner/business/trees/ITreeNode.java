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
package org.navalplanner.business.trees;


/**
 * Represents an entity that can work as a node at a tree<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITreeNode<T> {

    public void add(T newChild);

    public void remove(T existentChild);

    public void replace(T previousChild, T newChild);

    public void up(T existentChild);

    public void down(T existentChild);

    public void add(int position, T newChild);

}
