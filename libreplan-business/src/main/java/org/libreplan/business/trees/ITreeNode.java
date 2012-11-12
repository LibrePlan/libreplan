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
package org.libreplan.business.trees;

import java.util.List;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface ITreeNode<T extends ITreeNode<T>> {

    ITreeParentNode<T> getParent();

    List<T> getChildren();

    ITreeParentNode<T> toContainer();

    T toLeaf();

    /**
     * Using <a href="http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ206"
     * >getThis trick</a>
     * @return
     */
    T getThis();

    boolean isLeaf();

    /**
     * Checks if a leaf is or not empty. <br />
     * An empty leaf is defined as:
     * <ul>
     * <li>A leaf with number of hours zero</li>
     * <li>A leaf without direct criteria assigned</li>
     * <li>A leaf without progress</li>
     * <li>A leaf without quality forms</li>
     * <li>A leaf without labels</li>
     * <li>A leaf without work report lines devoting time to it</li>
     * <li>A leaf without resource allocations (not assigned yet)</li>
     * </ul>
     */
    boolean isEmptyLeaf();

    boolean isUpdatedFromTimesheets();

}
