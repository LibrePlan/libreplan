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

package org.navalplanner.business.materials.entities;

import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;


/**
 * Defines the default {@link MaterialCategory}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public enum PredefinedMaterialCategories {

    IMPORTED_MATERIALS_WITHOUT_CATEGORY("Imported materials without category");

    private final String name;

    private PredefinedMaterialCategories(String name) {
        this.name = name;
    }

    public MaterialCategory createMaterialCategory() {
        return MaterialCategory.create(name);
    }

    public String getName() {
        return name;
    }

    public MaterialCategory getMaterialCategory() {
        try {
            return Registry.getMaterialCategoryDAO().findUniqueByName(name);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}