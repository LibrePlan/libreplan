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

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.components.finders.Finder.SimpleListModelExt;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;

/**
 * Interface for providing, displaying and matching elements for an
 * {@link Autocomplete} combobox
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IFinder {

    /**
     * Text displayed for each {@link Comboitem}
     *
     * @param value
     * @return
     */
    String _toString(Object value);

    /**
     * Get list of {@link BaseEntity} to fill {@link Autocomplete}
     *
     * Executed once only when {@link Autocomplete} is rendered for the first
     * time
     *
     * @return
     */
    List<? extends BaseEntity> getAll();

    /**
     * Returns customize {@link ComboitemRenderer}
     *
     * When creating your own Renderer, labels should always use
     * _toString(Object value)
     *
     * @return
     */
    ComboitemRenderer getItemRenderer();

    /**
     * Returns a {@link SimpleListModelExt}
     *
     * @return
     */
    SimpleListModelExt getModel();

    /**
     * Boolean function to evaluate whether an entry matches with input text
     *
     * @param entry
     * @param text
     * @return
     */
    boolean entryMatchesText(String entry, String text);

}
