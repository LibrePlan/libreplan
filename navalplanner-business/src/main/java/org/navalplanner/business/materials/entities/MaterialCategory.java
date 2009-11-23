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

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;

/**
 * MaterialCategory entity
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 *
 */
public class MaterialCategory extends BaseEntity {

    @NotEmpty
    private String name;

    private MaterialCategory parent = null;

    private Set<MaterialCategory> subcategories = new HashSet<MaterialCategory>();

    private Set<Material> materials = new HashSet<Material>();

    // Default constructor, needed by Hibernate
    protected MaterialCategory() {

    }

    public static MaterialCategory create(String name) {
        return (MaterialCategory) create(new MaterialCategory(name));
    }

    protected MaterialCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MaterialCategory getParent() {
        return parent;
    }

    /* WARNING: do not use this method to set parent to null.
     * It must be done using removeParent().
     */
    public void setParent(MaterialCategory parent) {
        this.parent = parent;
        parent.addSubcategory(this);
    }

    public void removeParent() {
        MaterialCategory category = parent;
        parent = null;
        category.removeSubcategory(this);
    }

    public Set<MaterialCategory> getSubcategories() {
        return subcategories;
    }

    public void addSubcategory (MaterialCategory subcategory) {
        subcategories.add(subcategory);
        if(subcategory.getParent()==null)
            subcategory.setParent(this);
    }

    public void removeSubcategory (MaterialCategory subcategory) {
        subcategories.remove(subcategory);
        if(subcategory.getParent()!=null)
            subcategory.removeParent();
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public void addMaterial(Material material) {
        materials.add(material);
        if(material.getCategory()!=this)
            material.setCategory(this);
    }
    public void removeMaterial(Material material) {
        materials.remove(material);
    }
}
