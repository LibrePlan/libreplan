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

package org.libreplan.business.resources.entities;

import org.apache.commons.lang.Validate;

/**
 * A {@link ICriterion} with his associated {@link ICriterionType} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionWithItsType {

    private final ICriterionType<?> type;

    private final Criterion criterion;

    private String nameHierarchy;

    public CriterionWithItsType(ICriterionType<?> type, Criterion criterion) {
        Validate.notNull(type);
        Validate.notNull(criterion);
        Validate.isTrue(type.contains(criterion),
                "the criterion must be belong to the type");
        this.type = type;
        this.criterion = criterion;
        this.nameHierarchy = getNamesHierarchy(criterion,"");
    }

    public ICriterionType<?> getType() {
        return type;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setNameHierarchy(String nameHierarchy) {
        this.nameHierarchy = nameHierarchy;
    }

    public String getNameHierarchy() {
        return nameHierarchy;
    }

    public String getNameAndType(){
        String etiqueta = type.getName();
        return etiqueta.concat(" :: "+getNameHierarchy());
    }

    private String getNamesHierarchy(Criterion criterion,String etiqueta){
        Criterion parent = criterion.getParent();
        if(parent != null){
            etiqueta = getNamesHierarchy(parent,etiqueta);
            etiqueta = etiqueta.concat(" -> ");
        }
        return etiqueta.concat(criterion.getName());
    }
}
