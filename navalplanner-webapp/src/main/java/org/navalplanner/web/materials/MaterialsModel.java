package org.navalplanner.web.materials;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
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

    MutableTreeModel<MaterialCategory> materialCategories = MutableTreeModel
            .create(MaterialCategory.class);

    @Override
    @Transactional(readOnly=true)
    public MutableTreeModel<MaterialCategory> getMaterialCategories() {
        if (materialCategories.isEmpty()) {
            initializeMaterialCategories();
        }
        return materialCategories;
    }

    @Override
    @Transactional(readOnly=true)
    public void reloadMaterialCategories() {
        materialCategories = MutableTreeModel.create(MaterialCategory.class);
        initializeMaterialCategories();
    }

    private void initializeMaterialCategories() {
        final List<MaterialCategory> categories = categoryDAO.getAllRootMaterialCategories();
        for (MaterialCategory materialCategory: categories) {
            initializeMaterials(materialCategory.getMaterials());
            materialCategories.addToRoot(materialCategory);
            addCategories(materialCategory, materialCategory.getSubcategories());
        }
    }

    private void initializeMaterials(Set<Material> materials) {
        for (Material each: materials) {
            each.getDescription();
        }
    }

    private void addCategories(MaterialCategory materialCategory, Set<MaterialCategory> categories) {
        for (MaterialCategory category: categories) {
            initializeMaterials(category.getMaterials());
            materialCategories.add(materialCategory, category);
            final Set<MaterialCategory> subcategories = category.getSubcategories();
            if (subcategories != null) {
                addCategories(category, subcategories);
            }
        }
    }

    @Override
    @Transactional(readOnly=true)
    public List<Material> getMaterials(MaterialCategory materialCategory) {
        List<Material> result = new ArrayList<Material>();
        result.addAll(materialCategory.getMaterials());
        return result;
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
    @Transactional
    public void confirmRemoveMaterialCategory(MaterialCategory materialCategory) {
        try {
            final Long idMaterialCategory = materialCategory.getId();
            if (idMaterialCategory == null) {
                materialCategories.remove(materialCategory);
            } else {
                categoryDAO.remove(idMaterialCategory);
                reloadMaterialCategories();
            }
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void addMaterialToMaterialCategory(MaterialCategory materialCategory) {
        Material material = Material.create("");
        material.setCategory(materialCategory);
        materialCategory.addMaterial(material);
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        List<MaterialCategory> categories = new ArrayList<MaterialCategory>();
        asList(materialCategories.getRoot(), categories);
        for (MaterialCategory each: categories) {
            categoryDAO.save(each);
        }
    }

    private void asList(MaterialCategory root, List<MaterialCategory> result) {
        List<MaterialCategory> list = new ArrayList<MaterialCategory>();
        for (int i = 0; i < materialCategories.getChildCount(root); i++) {
            final MaterialCategory materialCategory = materialCategories.getChild(root, i);
            list.add(materialCategory);
            result.add(materialCategory);
        }

        for (MaterialCategory each: list) {
            asList(each, result);
        }
    }

    @Override
    public void removeMaterial(Material material) {
        material.getCategory().removeMaterial(material);
    }

}
