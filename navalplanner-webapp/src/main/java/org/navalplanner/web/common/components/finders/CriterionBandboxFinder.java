/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link Criterion}.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class CriterionBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private ICriterionDAO criterionDAO;

    private final String headers[] = { _("Type"), _("Criterion Name") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Criterion> getAll() {
        List<Criterion> criterions = criterionDAO.findAll();
        forLoadCriterions(criterions);
        return criterions;
    }

    private void forLoadCriterions(List<Criterion> criterions) {
        for (Criterion criterion : criterions) {
            criterion.getName();
            criterion.getType().getName();
            if (criterion.getParent() != null) {
                criterion.getParent().getName();
            }
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        Criterion criterion = (Criterion) obj;
        text = text.trim().toLowerCase();
        return (criterion.getType().getName().toLowerCase().contains(text) || getNamesHierarchy(
                criterion, new String())
                .toLowerCase().contains(text));
    }

    @Override
    @Transactional(readOnly = true)
    public String objectToString(Object obj) {
        Criterion criterion = (Criterion) obj;
        return criterion.getType().getName() + " :: "
                + getNamesHierarchy(criterion, new String());
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return orderRenderer;
    }

    private final ListitemRenderer orderRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            Criterion criterion = (Criterion)data;
            item.setValue(criterion);

            Listcell criterionType = new Listcell();
            criterionType.setLabel(criterion.getType().getName());
            criterionType.setParent(item);

            Listcell criterionName = new Listcell();
            criterionName.setLabel(getNamesHierarchy(criterion,new String()));
            criterionName.setParent(item);
        }

    };

    private String getNamesHierarchy(Criterion criterion,String etiqueta){
        Criterion parent = criterion.getParent();
        if(parent != null){
            etiqueta = getNamesHierarchy(parent,etiqueta);
            etiqueta = etiqueta.concat(" -> ");
        }
        return etiqueta.concat(criterion.getName());
    }

}