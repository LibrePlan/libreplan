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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CriterionDTO {

    private Criterion criterion = null;

    private String name;

    private CriterionDTO parent = null;

    private List<CriterionDTO> children =  new ArrayList<CriterionDTO>();

    private boolean active = true;

    private boolean newObject = true;

    public CriterionDTO(){
            this.name = "";
            this.newObject = true;
    }

    private CriterionDTO(Criterion criterion) {
        Validate.notNull(criterion);
        this.criterion = criterion;
        this.name = criterion.getName();
        this.active = criterion.isActive();
        this.newObject = false;
    }

    public static List<CriterionDTO> asListDTO(List<Criterion> setChildren,
            CriterionDTO parent) {
        List<CriterionDTO> listChildren = new ArrayList<CriterionDTO>();
        for(Criterion criterion : setChildren){
            CriterionDTO criterionDTO = new CriterionDTO(criterion);
            criterion.getChildren();
            List<Criterion> orderedChildren = Criterion.sortByName(criterion
                    .getChildren());
            criterionDTO.setChildren(asListDTO(orderedChildren, criterionDTO));
            criterionDTO.setParent(parent);
            listChildren.add(criterionDTO);
        }
        return listChildren;
    }

    public static Set<Criterion> asSet(List<CriterionDTO> criterionDTOs,Criterion parent,
            CriterionType criterionType){
        Set<Criterion> listChildren = new HashSet<Criterion>();
        for(CriterionDTO criterionDTO : criterionDTOs){
            Criterion criterion = Criterion.create(criterionDTO.getName(),
                    criterionType);
            criterion.setChildren(asSet(criterionDTO.getChildren(),criterion,criterionType));
            criterion.setParent(parent);
            listChildren.add(criterion);
        }
        return listChildren;
    }

    public boolean isNewObject() {
        return newObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CriterionDTO getParent() {
        return parent;
    }

    public void setParent(CriterionDTO parent) {
        this.parent = parent;
    }

    public List<CriterionDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CriterionDTO> children) {
        this.children = children;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public int up(CriterionDTO criterionDTO) {
        int position = children.indexOf(criterionDTO);
        if(position == 0){
            upParent(criterionDTO);
        }
        if (position > 0) {
            children.remove(position);
            children.add(position - 1, criterionDTO);
        }
        return position;
    }

    public int down(CriterionDTO criterionDTO) {
        int position = children.indexOf(criterionDTO);
         if(position == children.size()-1){
            downParent(criterionDTO);
        }
        if (position < children.size() - 1) {
            children.remove(position);
            children.add(position + 1, criterionDTO);
        }
        return position;
    }

    private void upParent(CriterionDTO criterionDTO){
        if(parent != null){
            int position = parent.getChildren().indexOf(this);
            parent.getChildren().add(position,criterionDTO);
        }
    }

    private void downParent(CriterionDTO criterionDTO){
        if(parent != null){
            int position = parent.getChildren().indexOf(this);
            parent.getChildren().add(position,criterionDTO);
        }
    }
}
