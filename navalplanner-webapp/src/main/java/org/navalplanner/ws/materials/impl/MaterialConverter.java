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

package org.navalplanner.ws.materials.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.ws.materials.api.MaterialCategoryDTO;
import org.navalplanner.ws.materials.api.MaterialCategoryListDTO;
import org.navalplanner.ws.materials.api.MaterialDTO;

/**
 * Converter from/to material-related entities to/from DTOs.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public final class MaterialConverter {

    private MaterialConverter() {
    }

    public final static MaterialCategoryDTO toDTO(
            MaterialCategory materialCategory) {

        // converter the materials
        List<MaterialDTO> materialDTOs = new ArrayList<MaterialDTO>();

        for (Material m : materialCategory.getMaterials()) {
            materialDTOs.add(toDTO(m));
        }

        if (materialDTOs.isEmpty()) {
            materialDTOs = null;
        }

        // converter the subcategories
        MaterialCategoryListDTO subcategories = createSubcategoriesList(materialCategory
                .getSubcategories());

        // get the parent code
        String parentCode = null;
        if (materialCategory.getParent() != null) {
            parentCode = materialCategory.getParent().getCode();
        }

        return new MaterialCategoryDTO(materialCategory.getCode(),
                materialCategory.getName(), parentCode, subcategories,
                materialDTOs);

    }

    private static MaterialCategoryListDTO createSubcategoriesList(
            Set<MaterialCategory> subcategories) {

        List<MaterialCategoryDTO> DTOs = new ArrayList<MaterialCategoryDTO>();

        for (MaterialCategory m : subcategories) {
            DTOs.add(toDTO(m));
        }

        if (DTOs.isEmpty()) {
            return null;
        }

        return new MaterialCategoryListDTO(DTOs);
    }

    public final static MaterialDTO toDTO(Material material) {

        String unitTypeCode = null;
        if (material.getUnitType() != null) {
            unitTypeCode = material.getUnitType().getCode();
        }

        return new MaterialDTO(material.getCode(), material.getDescription(),
                material.getDefaultUnitPrice(), unitTypeCode, material
                        .getDisabled());

    }

    public final static MaterialCategory toEntity(
            MaterialCategoryDTO materialCategoryDTO) {
        MaterialCategory materialCategory = toEntityCategory(materialCategoryDTO);

        // find the parent
        if (materialCategoryDTO.parent != null) {
            try {
                MaterialCategory parentCategory = Registry
                        .getMaterialCategoryDAO().findByCode(
                                materialCategoryDTO.parent);
                materialCategory.setParent(parentCategory);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        _("There is no material category with this code"));
            }
        }

        return materialCategory;
    }

    public final static MaterialCategory toEntityCategory(
            MaterialCategoryDTO materialCategoryDTO) {

        MaterialCategory materialCategory = MaterialCategory.createUnvalidated(
                StringUtils.trim(materialCategoryDTO.code), StringUtils
                        .trim(materialCategoryDTO.name));

        // Create and add the materials
        if (materialCategoryDTO.materials != null) {
            for (MaterialDTO materialDTO : materialCategoryDTO.materials) {
                materialCategory.addMaterial(toEntity(materialDTO));
            }
        }

        // Create and add the subcategories
        if (materialCategoryDTO.subcategories != null) {
            for (MaterialCategoryDTO subCategoryDTO : materialCategoryDTO.subcategories.materialCategoryDTOs) {
                materialCategory.addSubcategory(toEntitySubCategories(
                        subCategoryDTO, materialCategory));
            }
        }

        return materialCategory;

    }

    public final static MaterialCategory toEntitySubCategories(
            MaterialCategoryDTO materialCategoryDTO, MaterialCategory parent) {

        MaterialCategory materialCategory = toEntityCategory(materialCategoryDTO);

        // find the parent
        if (materialCategoryDTO.parent != null) {
            if (!materialCategoryDTO.parent.equalsIgnoreCase(parent.getCode())) {
                throw new ValidationException(_("inconsistent parent code."));
            }
        }

        return materialCategory;

    }

    private static Material toEntity(MaterialDTO materialDTO) {

        Material material = Material.createUnvalidated(StringUtils
                .trim(materialDTO.code), StringUtils
                .trim(materialDTO.description), materialDTO.defaultPrice,
                materialDTO.disabled);

        if (materialDTO.unitType != null) {
            try {
                UnitType unitType = Registry.getUnitTypeDAO()
                        .findByCodeAnotherTransaction(
                        materialDTO.unitType);
                material.setUnitType(unitType);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(_("unit type code not found"));
            }
        }

        return material;

    }

    public final static void updateMaterialCategory(
            MaterialCategory materialCategory,
            MaterialCategoryDTO materialCategoryDTO) throws ValidationException {

        /* check the parent code */
        if (((materialCategoryDTO.parent != null)
                && (materialCategory.getParent() != null)
                && (!materialCategory.getParent().getCode().equalsIgnoreCase(
                        materialCategoryDTO.parent)))
                || ((!(materialCategoryDTO.parent == null) && (materialCategory
                        .getParent() == null)))) {
            throw new ValidationException(_("inconsistent parent code."));
        }

        /*
         * 1: Update basic properties in existing material category and add new
         * materials.
         */
        if (materialCategoryDTO.materials != null) {
            for (MaterialDTO materialDTO : materialCategoryDTO.materials) {

                /* Step 1.1 requires each material DTO to have a code. */
                if (StringUtils.isBlank(materialDTO.code)) {
                    throw new ValidationException(
                            _("missing code in a material"));
                }

                try {
                    Material material = materialCategory
                        .getMaterialByCode(materialDTO.code);
                    updateMaterial(material, materialDTO);
                } catch (InstanceNotFoundException e) {
                    materialCategory.addMaterial(toEntity(materialDTO));
                }
            }
        }

        /*
         * 2: Update basic properties in existing subcategories and add new
         * subcategories.
         */
        if (materialCategoryDTO.subcategories != null) {
            for (MaterialCategoryDTO subcategoryDTO : materialCategoryDTO.subcategories.materialCategoryDTOs) {

                /* Step 2.1 requires each subcategory DTO to have a code. */
                if (StringUtils.isBlank(subcategoryDTO.code)) {
                    throw new ValidationException(
                        _("missing code in a subcategory"));
                }

                try {
                    MaterialCategory subcategory = materialCategory
                        .getSubcategoryByCode(subcategoryDTO.code);
                    updateMaterialCategory(subcategory, subcategoryDTO);
                } catch (InstanceNotFoundException e) {
                        materialCategory
                                .addSubcategory(toEntity(subcategoryDTO));
                    }
            }
        }

        /* 3: Update material category basic properties. */
        materialCategory.updateUnvalidated(StringUtils
                .trim(materialCategoryDTO.name));

    }

    public final static void updateMaterial(Material material,
            MaterialDTO materialDTO) throws ValidationException {
        if (materialDTO.unitType != null) {
            try {
                UnitType type = Registry.getUnitTypeDAO()
                        .findByCodeAnotherTransaction(
                        materialDTO.unitType);
                material.setUnitType(type);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(_("unit type code not found"));
            }
        }
        material.updateUnvalidated(StringUtils.trim(materialDTO.description),
                materialDTO.defaultPrice, materialDTO.disabled);
    }
}
