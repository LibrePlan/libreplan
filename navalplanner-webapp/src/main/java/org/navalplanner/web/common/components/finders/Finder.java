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

import java.util.LinkedList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;

/**
 * Finder implements basic methods for {@link IFinder} leaving getAll() and
 * _toString() methods to concrete classes
 *
 * In addition it also provides default behaviour for entryMatchesText
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public abstract class Finder implements IFinder {

    @Override
    public ComboitemRenderer getItemRenderer() {
        return new ComboWorkerRenderer();
    }

    @Transactional(readOnly = true)
    public SimpleListModelExt getModel() {
        return new SimpleListModelExt(getAll());
    }

    /**
     * Returns true if entry contains text partly
     *
     * @param entry
     *            Element from model
     * @param text
     *            Input text
     */
    @Override
    public boolean entryMatchesText(String entry, String text) {
        return entry.toLowerCase().contains(text.toLowerCase());
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
    private class ComboWorkerRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem item, Object data) {
            item.setLabel(_toString(data));
            item.setValue(data);
        }

    }

    /**
     * Extends {@link SimpleListModelExt} to overwrite getSubModel() method
     *
     * The main goal of this class is to overwrite the getSubModel() method.
     * This method searches among all elements from model whether an input text
     * (value) matches or not. In case that element matches, it will be added to
     * list, and a new SimpleListModelExt if returned out of that list
     *
     * @author Diego Pino Garcia<dpino@igalia.com>
     *
     */
    public class SimpleListModelExt extends SimpleListModel {

        public SimpleListModelExt(List data) {
            super(data);
        }

        /**
         * Searches for value among all model entries
         *
         * entryMatchesText method is used to check if an entry matches or not.
         * Overwrite this method to provide your own behaviour
         *
         * @param value
         *            String to search
         * @param nRows
         *            Number of rows (maximum 10, see {@link SimpleListModel}
         */
        public ListModel getSubModel(Object value, int nRows) {
            final String idx = value == null ? "" : objectToString(value);
            if (nRows < 0) {
                nRows = 10;
            }
            final LinkedList data = new LinkedList();
            for (int i = 0; i < getSize(); i++) {
                if (idx.equals("")
                        || entryMatchesText(_toString(getElementAt(i)), idx)) {
                    data.add(getElementAt(i));
                    if (--nRows <= 0) {
                        break; // done
                    }
                }
            }
            return new SimpleListModelExt(data);
        }
    }

}
