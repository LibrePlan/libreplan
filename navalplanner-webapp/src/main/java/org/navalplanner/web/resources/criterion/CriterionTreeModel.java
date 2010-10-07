/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.web.resources.criterion;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * Model for a the {@link Criterion} tree for a {@link CriterionType} <br />
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionTreeModel implements ICriterionTreeModel {

    private MutableTreeModel<CriterionDTO> tree;

    private final CriterionType criterionType;

    private final CriterionDTO criterionRootDTO;

    private static MutableTreeModel<CriterionDTO> createTreeFrom(CriterionDTO criterionRootDTO) {
        List<CriterionDTO> criterionDTOs = criterionRootDTO.getChildren();
        MutableTreeModel<CriterionDTO> treeModel = MutableTreeModel
                .create(CriterionDTO.class);
        CriterionDTO parent = treeModel.getRoot();
        treeModel.add(parent, criterionDTOs);
        addChildren(treeModel, criterionDTOs);
        return treeModel;
    }

    private static List<CriterionDTO> getCriterionDTOs(
            CriterionType criterionType,CriterionDTO criterionRootDTO){
        List<Criterion> criterions = Criterion
                .sortByName(getDirectCriterions(criterionType));
        return CriterionDTO.asListDTO(criterions, criterionRootDTO);
    }

    private static Set<Criterion> getDirectCriterions(CriterionType criterionType){
        Set<Criterion> criterions = new HashSet<Criterion>();
        for(Criterion criterion : criterionType.getCriterions()){
            if(criterion.getParent() == null){
                criterions.add(criterion);
            }
        }
        return criterions;
    }

    private static void addChildren(MutableTreeModel<CriterionDTO> treeModel,
            List<CriterionDTO> criterions) {
        for (CriterionDTO criterion : criterions) {
            treeModel.add(criterion, criterion.getChildren());
            addChildren(treeModel, criterion.getChildren());
        }
    }

    public CriterionType getCriterionType(){
        return criterionType;
    }

    public CriterionTreeModel(CriterionType criterionType) {
        this.criterionType = criterionType ;
        criterionRootDTO = new CriterionDTO();
        List<CriterionDTO> listDTOs = getCriterionDTOs(criterionType,criterionRootDTO);
        criterionRootDTO.setChildren(listDTOs);
        tree = this.createTreeFrom(criterionRootDTO);
    }

    public TreeModel asTree() {
        return tree;
    }

    public void addCriterion(String name) {
        CriterionDTO newCriterionDTO = createNewCriterion(name);
        newCriterionDTO.setActive(criterionType.isEnabled());
        addToTree(tree.getRoot(), newCriterionDTO,0);
        addCriterionAtCriterionType(newCriterionDTO,0);
    }

    public void addCriterionAt(CriterionDTO node,String name) {
        CriterionDTO newCriterion = createNewCriterion(name);
        newCriterion.setActive(criterionType.isEnabled());
        addToTree(node,newCriterion,0);
        addCriterionAtCriterion(node, newCriterion,0);
    }

    private CriterionDTO createNewCriterion(String name) {
        CriterionDTO newCriterion = new CriterionDTO();
        newCriterion.setName(_(name));
        Criterion criterion = Criterion.create(criterionType);
        newCriterion.setCriterion(criterion);
        return newCriterion;
    }

    private void addToTree(CriterionDTO parentNode, CriterionDTO elementToAdd,int position) {
        tree.add(parentNode,position,Arrays.asList(elementToAdd));
        addChildren(tree,Arrays.asList(elementToAdd));
    }

    private void addToTree(CriterionDTO parentNode,
            List<CriterionDTO> elementsToAdd) {
        tree.add(parentNode, elementsToAdd);
        addChildren(tree, elementsToAdd);
    }

    private void addCriterionAtCriterionType(CriterionDTO elementToAdd,int position) {
        elementToAdd.setParent(criterionRootDTO);
        criterionRootDTO.getChildren().add(position,elementToAdd);
    }

    private void addCriterionAtCriterion(CriterionDTO parent, CriterionDTO elementToAdd,int position) {
        elementToAdd.setParent(parent);
        parent.getChildren().add(position,elementToAdd);
    }

    @Override
    public void up(CriterionDTO node) {
        CriterionDTO parent = asCriterion(((CriterionDTO)tree.getParent(node)));
        int pos = parent.up(node);
        tree.up(node);
        if((pos == 0)&&(!parent.equals(criterionRootDTO))){
            upGranParent(node);
        }
    }

    private void upGranParent(CriterionDTO node){
        CriterionDTO parent = tree.getParent(node);
        CriterionDTO grandParent = tree.getParent(parent);
        int position = getChildren(grandParent).indexOf(parent);
        if(tree.isRoot(grandParent)){
            this.moveToRoot(node,position);
        }else{
            this.moveImpl(node, grandParent, position);
        }
    }

    @Override
    public void down(CriterionDTO node) {
        CriterionDTO parent = asCriterion(((CriterionDTO)tree.getParent(node)));
        int pos = parent.down(node);
        tree.down(node);
        if((pos == parent.getChildren().size() - 1)
                &&(!parent.equals(criterionRootDTO))){
            downGranParent(node);
        }
    }

    private void downGranParent(CriterionDTO node){
        CriterionDTO parent = tree.getParent(node);
        CriterionDTO grandParent = tree.getParent(parent);
        int position = getChildren(grandParent).indexOf(parent)+1;
        if(tree.isRoot(grandParent)){
            this.moveToRoot(node,position);
        }else{
            this.moveImpl(node, grandParent, position);
        }
    }

    @Override
    public void indent(CriterionDTO nodeToIndent) {
        CriterionDTO parentOfSelected = tree.getParent(nodeToIndent);
        int position = getChildren(parentOfSelected).indexOf(nodeToIndent);
        if (position == 0) {
            return;
        }
        CriterionDTO destination = (CriterionDTO) getChildren(parentOfSelected)
                .get(position - 1);
        moveImpl(nodeToIndent, destination, getChildren(destination).size());
    }

    @Override
    public void unindent(CriterionDTO nodeToUnindent) {
        CriterionDTO parent = tree.getParent(nodeToUnindent);
        if (tree.isRoot(parent)) {
            return;
        }
        CriterionDTO destination = tree.getParent(parent);
        moveImpl(nodeToUnindent, destination, getChildren(destination).indexOf(
                parent) + 1);
    }

    private List<CriterionDTO> getChildren(CriterionDTO node) {
        List<CriterionDTO> result = new ArrayList<CriterionDTO>();
        final int childCount = tree.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            result.add(tree.getChild(node, i));
        }
        return result;
    }

    @Override
    public void move(CriterionDTO toBeMoved, CriterionDTO destination,int position) {
        if(isGreatInHierarchy(toBeMoved,destination)){
            return;
        }
        if(criterionType.allowHierarchy()){
           moveImpl(toBeMoved, destination,position);
        }
    }

    @Override
    public void moveToRoot(CriterionDTO toBeMoved,int position) {
        moveImpl(toBeMoved, tree.getRoot(),position);
    }

    private void moveImpl(CriterionDTO toBeMoved, CriterionDTO destination,int position) {
        if (getChildren(destination).contains(toBeMoved)) {
            return;// it's already moved
        }
        removeNodeImpl(toBeMoved);
        moveCriterion(toBeMoved,destination,position);
        addToTree(destination, toBeMoved,position);
    }

    private void moveCriterion(CriterionDTO toBeMoved, CriterionDTO destination,int position){
        //Add at CriterionType or at a criterion
        if(tree.isRoot(destination)){
            toBeMoved.setParent(criterionRootDTO);
            criterionRootDTO.getChildren().add(position,toBeMoved);
        }else{
            toBeMoved.setParent(destination);
            destination.getChildren().add(position,toBeMoved);
        }
    }

    private CriterionDTO asCriterion(CriterionDTO node) {
        if (tree.isRoot(node)) {
            return criterionRootDTO;
        }
        return (CriterionDTO) node;
    }

    public void removeNode(CriterionDTO node) {
        removeNodeImpl(node);
    }

    private void removeNodeImpl(CriterionDTO criterionDto) {
        if (criterionDto == tree.getRoot()) {
            return;
        }
        CriterionDTO parentDto = criterionDto.getParent();
        if (parentDto == criterionRootDTO) {
            criterionRootDTO.getChildren().remove(criterionDto);
        } else {
            parentDto.getChildren().remove(criterionDto);
        }

        Criterion parent = criterionDto.getCriterion().getParent();
        if ((parent != null) && (!parent.getChildren().isEmpty())) {
            parent.getChildren().remove(criterionDto.getCriterion());
        }

        tree.remove(criterionDto);
    }

    public int[] getPath(Criterion criterion) {
        return tree.getPath(tree.getRoot(), criterion);
    }

    public void flattenTree(){
        List<CriterionDTO> criterions = copyCriterions(criterionRootDTO.getChildren());
        for(CriterionDTO criterion : criterions){
            flattenTree(criterion);
        }

    }

    private void flattenTree(CriterionDTO criterion){
        if(criterion.getChildren().size()>0){
            List<CriterionDTO> criterions = copyCriterions(criterion.getChildren());
            for(CriterionDTO criterionChild : criterions){
                flattenTree(criterionChild);
            }
        }
        moveToRoot(criterion,criterionRootDTO.getChildren().size());
    }

    private List<CriterionDTO> copyCriterions(List<CriterionDTO> criterions){
        List<CriterionDTO> newCriterions = new ArrayList<CriterionDTO>();
        for(CriterionDTO criterion : criterions){
            newCriterions.add(criterion);
        }
        return newCriterions;
    }

   public void thereIsOtherWithSameNameAndType(String name)
        throws ValidationException{
       thereIsOtherWithSameNameAndType(name,criterionRootDTO.getChildren());
   }

   private void thereIsOtherWithSameNameAndType(String name,List<CriterionDTO> criterions)
        throws ValidationException{
        for(CriterionDTO criterion : criterions){
            if(criterion.getName().equals(name)){
                InvalidValue[] invalidValues = {
                    new InvalidValue(_("Already exists other " +
                            "criterion with the same name"),
                            Criterion.class, "name",
                            criterion.getName(), criterion)};
                throw new ValidationException(invalidValues);
            }
            thereIsOtherWithSameNameAndType(name,criterion.getChildren());
        }
    }

    public void validateNameNotEmpty(String name)
        throws ValidationException{
        if(name.isEmpty()){
                InvalidValue[] invalidValues = {
                    new InvalidValue(_("The name of the criterion is empty."),
                            CriterionType.class, "name",
                            "",criterionType)};
                throw new ValidationException(invalidValues);
            }
    }

    public void updateEnabledCriterions(boolean isChecked){
        List<CriterionDTO> list = criterionRootDTO.getChildren();
        updateEnabledCriterions(isChecked,list);
        updateEnabledCriterionsTree(isChecked,getChildren(tree.getRoot()));
    }

    public void updateEnabledCriterions(boolean isChecked,CriterionDTO criterion){
        List<CriterionDTO> list = criterion.getChildren();
        updateEnabledCriterions(isChecked,list);
        updateEnabledCriterionsTree(isChecked,list);
    }

    private void updateEnabledCriterions(boolean isChecked,
            List<CriterionDTO> criterions){
        for(CriterionDTO criterion : criterions){
            criterion.setActive(isChecked);
            if(criterion.getChildren().size() > 0){
                updateEnabledCriterions(isChecked,criterion.getChildren());
            }
        }
    }

    private void updateEnabledCriterionsTree(boolean isChecked,
            List<CriterionDTO> criterions){
        for(CriterionDTO criterion: criterions){
            criterion.setActive(isChecked);
            tree.replace(criterion, criterion);
            addToTree(criterion, criterion.getChildren());
            updateEnabledCriterionsTree(isChecked,getChildren(criterion));
        }
    }

    @Override
    public void saveCriterions(CriterionType criterionType){
        updateCriterions(criterionRootDTO.getChildren());
    }

    public void updateCriterions(List<CriterionDTO> criterionDTOs){
        for(CriterionDTO criterionDTO : criterionDTOs){
            updateDataCriterion(criterionDTO);
            updateParent(criterionDTO);
            updateCriterions(criterionDTO.getChildren());
        }
    }

    private void updateParent(CriterionDTO criterionDTO){
        if(!criterionDTO.isNewObject()){
            updateOldParent(criterionDTO);
        }
        updateNewParent(criterionDTO);
    }

    private void updateOldParent(CriterionDTO criterionDTO){
        Criterion oldParent = criterionDTO.getCriterion().getParent();
        if(oldParent != null){
            oldParent.getChildren().remove(criterionDTO.getCriterion());
        }
    }

    private void updateNewParent(CriterionDTO criterionDTO){
        Criterion newParent = criterionDTO.getParent().getCriterion();
        criterionDTO.getCriterion().setParent(newParent);
        if(newParent == null){
            criterionType.getCriterions().add(criterionDTO.getCriterion());
        }else{
             newParent.getChildren().add(criterionDTO.getCriterion());
        }
    }

    private void updateDataCriterion(CriterionDTO criterionDTO){
        Criterion criterion = criterionDTO.getCriterion();
        criterion.setName(criterionDTO.getName());
        criterion.setActive(criterionDTO.isActive());
    }

    private boolean isGreatInHierarchy(CriterionDTO parent,CriterionDTO child){
        return find(child,getChildren(parent));
    }

    private boolean find(CriterionDTO child,List<CriterionDTO> children){
        if(children.indexOf(child) >= 0) {
            return true;
        }
        for(CriterionDTO criterionDTO : children){
            return find(child,getChildren(criterionDTO));
        }
        return false;
    }

    public void regenerateCodeForUnsavedCriteria() {
        regenerateCodeForUnsavedCriteria(criterionRootDTO.getChildren());
    }

    private void regenerateCodeForUnsavedCriteria(
            List<CriterionDTO> criterionDTOs) {
        for(CriterionDTO criterionDTO : criterionDTOs){
            if(criterionDTO.getCriterion().isNewObject()) {
                criterionDTO.getCriterion().setCodeAutogenerated();
            }
            regenerateCodeForUnsavedCriteria(criterionDTO.getChildren());
        }
    }

}
