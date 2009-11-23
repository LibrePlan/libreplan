package org.navalplanner.web.materials;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.materials.daos.IMaterialCategoryDAO;
import org.navalplanner.business.materials.daos.IMaterialDAO;
import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.util.MutableTreeModel;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MaterialsModel implements IMaterialsModel {

    @Autowired
    IMaterialCategoryDAO categoryDAO;

    @Autowired
    IMaterialDAO materialDAO;

    MutableTreeModel<MaterialCategory> materialCategories = MutableTreeModel.create(MaterialCategory.class);

    List<Material> materials = new ArrayList<Material>();

    private void initializeMaterialCategories() {
        final List<MaterialCategory> categories = categoryDAO.getAllRootMaterialCategories();
        for (MaterialCategory materialCategory: categories) {
            materialCategories.addToRoot(materialCategory);
            addCategories(materialCategory, materialCategory.getSubcategories());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeMaterials() {
        materials = new ArrayList(materialDAO.getAll());
    }

    private void addCategories(MaterialCategory materialCategory, Set<MaterialCategory> categories) {
        for (MaterialCategory category: categories) {
            materialCategories.add(materialCategory, category);
            final Set<MaterialCategory> subcategories = category.getSubcategories();
            if (subcategories != null) {
                addCategories(category, subcategories);
            }
        }
    }

    @Transactional(readOnly=true)
    public MutableTreeModel<MaterialCategory> getMaterialCategories() {
        if (materialCategories.isEmpty()) {
            initializeMaterialCategories();
        }
        return materialCategories;
    }

    @Override
    @Transactional(readOnly=true)
    public List<Material> getMaterials() {
        if (materials.isEmpty()) {
            initializeMaterials();
        }
        return materials;
    }

    @Override
    public void addMaterialCategory(MaterialCategory parent, MaterialCategory child) throws ValidationException {
        Validate.notNull(child);

        final MaterialCategory materialCategory = findMaterialCategory(parent, child);
        if (materialCategory != null) {
            final InvalidValue invalidValue = new InvalidValue(_("{0} already exists", materialCategory.getName()),
                    MaterialCategory.class, "name", materialCategory.getName(), materialCategory);
            throw new ValidationException(invalidValue);
        }

        if (parent == null) {
            materialCategories.addToRoot(child);
        } else {
            materialCategories.add(parent, child);
        }
    }

    private MaterialCategory findMaterialCategory(final MaterialCategory parent, final MaterialCategory category) {
        for (int i = 0; i < materialCategories.getChildCount(parent); i++) {
            final MaterialCategory each = materialCategories.getChild(parent, i);
            if (equalsMaterialCategory(each, category)) {
                return each;
            }
        }
        return null;
    }

    private boolean equalsMaterialCategory(MaterialCategory obj1, MaterialCategory obj2) {
        return obj1.getName().equals(obj2.getName());
    }

    @Override
    public void removeMaterialCategory(MaterialCategory materialCategory) {
        materialCategories.remove(materialCategory);
    }

    @Override
    public void addMaterialToMaterialCategory(MaterialCategory materialCategory) {
        Material material = Material.create("");
        material.setCategory(materialCategory);
        materials.add(material);
    }

}
