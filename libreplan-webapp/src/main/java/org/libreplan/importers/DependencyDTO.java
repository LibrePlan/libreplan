/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.importers;

/**
 * Class that represents the dependencies between {@link OrderElementDTO}
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public class DependencyDTO {

    /**
     * Enumerate that represent the different types
     * of dependencies supported in LP
     */
    public enum TypeOfDependencyDTO {

        END_START, START_START, END_END, START_END;

    }

    /**
     * Object that is the origin of the dependency.
     * It can be a {@link OrderElementDTO} or a {@link MilestoneDTO}
     */
    public IHasTaskAssociated origin;

    /**
     * Object that is the destination of the dependency.
     * It can be a {@link OrderElementDTO} or a {@link MilestoneDTO}
     */
    public IHasTaskAssociated destination;

    /**
     * Type of the dependency.
     */
    public TypeOfDependencyDTO type;

}
