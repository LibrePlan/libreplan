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

package org.libreplan.web.common.components.finders;

import java.util.List;

import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Implements all the methods needed to comply IBandboxFinder
 *
 * This is a finder for {@link Label}l in a {@link Bandbox}. Provides how many
 * columns for {@link Label} will be shown, how to render {@link Label} object ,
 * how to do the matching, what text to show when an element is selected, etc
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Repository
public class LabelBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private ILabelDAO labelDAO;

    private final String headers[] = { _("Type"), _("Name") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getAll() {
        List<Label> labels = labelDAO.getAll();
        initializeLabels(labels);
        return labels;
    }

    private void initializeLabels(List<Label> labels) {
        for (Label label : labels) {
            initializeLabel(label);
        }
    }

    private void initializeLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final Label label = (Label) obj;
        text = text.toLowerCase();
        return (label.getType().getName().toLowerCase().contains(text.toLowerCase()) || label
                .getName().toLowerCase().contains(text));
    }

    @Override
    public String objectToString(Object obj) {
        return ((Label) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return labelRenderer;
    }

    /**
     * Render for {@link Label}
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     */
    private final ListitemRenderer labelRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            Label label = (Label) data;

            item.setValue(data);

            final Listcell labelType = new Listcell();
            labelType.setLabel(label.getType().getName());
            labelType.setParent(item);

            final Listcell labelName = new Listcell();
            labelName.setLabel(label.getName());
            labelName.setParent(item);
        }
    };
}
