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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Implements all the methods needed to comply IBandboxFinder This is a finder
 * for {@link QualityForm}l in a {@link Bandbox}. Provides how many columns for
 * {@link QualityForm} will be shown, how to render {@link QualityForm} object ,
 * how to do the matching, what text to show when an element is selected, etc
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class QualityFormBandboxFinder extends BandboxFinder implements
        IBandboxFinder {

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    private final String headers[] = { _("Name"), _("Type") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityForm> getAll() {
        List<QualityForm> qualityForms = qualityFormDAO.getAll();
        initializeQualityForms(qualityForms);
        return qualityForms;
    }

    private void initializeQualityForms(List<QualityForm> qualityForms) {
        for (QualityForm qualityForm : qualityForms) {
            initializeQualityForm(qualityForm);
        }
    }

    private void initializeQualityForm(QualityForm qualityForm) {
        qualityForm.getName();
        qualityForm.getQualityFormType();
        for (QualityFormItem qualityFormItem : qualityForm
                .getQualityFormItems()) {
            qualityFormItem.getName();
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final QualityForm qualityForm = (QualityForm) obj;
        text = text.toLowerCase();
        return (qualityForm.getQualityFormType().name().toLowerCase()
                .contains(text.toLowerCase()) || qualityForm.getName()
                .toLowerCase().contains(
                text));
    }

    @Override
    public String objectToString(Object obj) {
        return ((QualityForm) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return qualityFormRenderer;
    }

    /**
     * Render for {@link QualityForm}
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    private final ListitemRenderer qualityFormRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            QualityForm qualityForm = (QualityForm) data;
            item.setValue(data);

            final Listcell labelName = new Listcell();
            labelName.setLabel(qualityForm.getName());
            labelName.setParent(item);

            final Listcell labelType = new Listcell();
            labelType.setLabel(qualityForm.getQualityFormType().name());
            labelType.setParent(item);

        }
    };
}
