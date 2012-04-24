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

import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.SimpleListModel;

/**
 * BandboxFinder implements basic methods for {@link IBandboxFinder} and
 * provides a default renderer, the rest of methods for {@link IBandboxFinder}
 * should be implement by a concrete class
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public abstract class BandboxFinder implements IBandboxFinder {

    private ListModel model;

    @Override
    @Transactional(readOnly = true)
    public ListModel getModel() {
        if (model == null) {
            model = new SimpleListModel(getAll());
        }
        return model;
    }

    @Override
    public void setModel(ListModel model) {
        this.model = model;
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return _defRend;
    }

    /**
     * Class for rendering combo items
     *
     * It's necessary to provide this renderer since by default a combobox sets
     * label as Objects.toString(data) which relies on the actual implementation
     * of Object.toString. By doing this, it's possible to decouple how an
     * object is shown from its Object.toString method.
     *
     * See Combobox.getDefaultItemRenderer()
     *
     * In general it won't be necessary to overwrite this Renderer. Use
     * _toString() to indicate how an object is shown in the list of matching
     * elements
     *
     * @author Diego Pino Garcia<dpino@igalia.com>
     *
     */
    private final ListitemRenderer _defRend = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            item.setLabel(objectToString(data));
            item.setValue(data);
        }
    };

    public void resetModel() {
        this.model = null;
    }
}
